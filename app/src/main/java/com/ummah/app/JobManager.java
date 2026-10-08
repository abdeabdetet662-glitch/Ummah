package com.ummah.app;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class JobManager {

    private final FirebaseFirestore db;

    // ═══ الإعدادات ═══
    public static final int DAILY_LIMIT = 3;              // 3 مرات/يوم
    public static final long COOLDOWN_MS = 10L * 60 * 1000; // 10 دقائق

    public JobManager() {
        db = FirebaseFirestore.getInstance();
    }

    // ═══ Interfaces ═══
    public interface JobsListener {
        void onJobs(List<Job> jobs);
        void onError(String msg);
    }

    public interface OnDone {
        void onSuccess(int earned);
        void onError(String msg);
    }

    // ═══ نتيجة مفصلة ═══
    public interface MyJobListener {
        void onJob(String jobId, String title, int salary,
                   long lastWorkTime, int level, int xp,
                   int todayWorks, String todayDate);
        void onError(String msg);
    }

    // ═══ كل الوظائف ═══
    public ListenerRegistration listenAllJobs(final JobsListener l) {
        return db.collection("jobs").addSnapshotListener((snap, e) -> {
            if (e != null) { l.onError(e.getMessage()); return; }
            if (snap == null) { l.onJobs(new ArrayList<>()); return; }
            List<Job> jobs = new ArrayList<>();
            for (DocumentSnapshot d : snap.getDocuments()) {
                Job job = d.toObject(Job.class);
                if (job != null) {
                    job.id = d.getId();
                    jobs.add(job);
                }
            }
            l.onJobs(jobs);
        });
    }

    // ═══ يسمع لوظيفتي ═══
    public ListenerRegistration listenMyJob(String userId, final MyJobListener l) {
        return db.collection("users_jobs").document(userId)
                .addSnapshotListener((doc, e) -> {
                    if (e != null) { l.onError(e.getMessage()); return; }
                    if (doc == null || !doc.exists()) {
                        l.onJob(null, null, 0, 0, 1, 0, 0, today());
                        return;
                    }
                    String jobId = doc.getString("jobId");
                    String title = doc.getString("title");
                    Long salL = doc.getLong("salary");
                    Long lw = doc.getLong("lastWorkTime");
                    Long lvl = doc.getLong("level");
                    Long xp = doc.getLong("xp");
                    Long tw = doc.getLong("todayWorks");
                    String td = doc.getString("todayDate");

                    int salary = salL != null ? salL.intValue() : 0;
                    int todayWorks = tw != null ? tw.intValue() : 0;
                    String todayDate = td != null ? td : today();

                    // إذا التاريخ قديم — نصفّر
                    if (!todayDate.equals(today())) {
                        todayWorks = 0;
                    }

                    l.onJob(jobId, title, salary,
                            lw != null ? lw : 0,
                            lvl != null ? lvl.intValue() : 1,
                            xp != null ? xp.intValue() : 0,
                            todayWorks,
                            todayDate);
                });
    }

    // ═══ يختار وظيفة ═══
    public void chooseJob(String userId, Job job, final OnDone cb) {
        Map<String, Object> data = new HashMap<>();
        data.put("jobId", job.id);
        data.put("title", job.title);
        data.put("emoji", job.emoji);
        data.put("salary", job.salary);
        data.put("cooldownMin", job.cooldownMin);
        data.put("lastWorkTime", 0);
        data.put("level", 1);
        data.put("xp", 0);
        data.put("todayWorks", 0);
        data.put("todayDate", today());
        data.put("chosenAt", System.currentTimeMillis());

        db.collection("users_jobs").document(userId).set(data)
                .addOnSuccessListener(a -> cb.onSuccess(0))
                .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ═══ يخدم → يربح راتب ═══
    public void work(String userId, final OnDone cb) {
        db.runTransaction(transaction -> {
            DocumentReference jobRef = db.collection("users_jobs").document(userId);
            DocumentSnapshot jobDoc = transaction.get(jobRef);
            if (!jobDoc.exists()) throw new RuntimeException("لم تختر وظيفة بعد");

            Long salaryL = jobDoc.getLong("salary");
            Long lastWorkL = jobDoc.getLong("lastWorkTime");
            Long xpL = jobDoc.getLong("xp");
            Long lvlL = jobDoc.getLong("level");
            Long twL = jobDoc.getLong("todayWorks");
            String td = jobDoc.getString("todayDate");

            int salary = salaryL != null ? salaryL.intValue() : 0;
            long lastWork = lastWorkL != null ? lastWorkL : 0;
            int xp = xpL != null ? xpL.intValue() : 0;
            int level = lvlL != null ? lvlL.intValue() : 1;
            int todayWorks = twL != null ? twL.intValue() : 0;
            String todayDate = td != null ? td : today();

            // 1️⃣ إذا التاريخ تبدّل → نصفّر
            if (!todayDate.equals(today())) {
                todayWorks = 0;
            }

            // 2️⃣ نتأكد من الحد اليومي
            if (todayWorks >= DAILY_LIMIT) {
                throw new RuntimeException("وصلت الحد اليومي (" + DAILY_LIMIT + " أعمال). عد غداً 🌙");
            }

            // 3️⃣ نتأكد من الكولداون (10 دقائق)
            long now = System.currentTimeMillis();
            if (now - lastWork < COOLDOWN_MS) {
                long remaining = COOLDOWN_MS - (now - lastWork);
                long sec = remaining / 1000;
                long min = sec / 60;
                long secRest = sec % 60;
                throw new RuntimeException("انتظر " + min + ":" + (secRest < 10 ? "0" : "") + secRest + " ⏰");
            }

            // 4️⃣ نزيد الرصيد
            DocumentReference userRef = db.collection("citizens").document(userId);
            transaction.update(userRef, "balance", FieldValue.increment(salary));

            // 5️⃣ نزيد XP
            xp += 10;
            int newLevel = level;
            if (xp >= level * 100) {
                newLevel = level + 1;
                xp = 0;
            }

            // 6️⃣ نحدّث
            Map<String, Object> update = new HashMap<>();
            update.put("lastWorkTime", now);
            update.put("xp", xp);
            update.put("level", newLevel);
            update.put("todayWorks", todayWorks + 1);
            update.put("todayDate", today());

            transaction.update(jobRef, update);

            return salary;
        }).addOnSuccessListener(r -> cb.onSuccess(r))
          .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ═══ يمسح الوظيفة ═══
    public void quitJob(String userId, final OnDone cb) {
        db.collection("users_jobs").document(userId).delete()
                .addOnSuccessListener(a -> cb.onSuccess(0))
                .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ═══ التاريخ الحالي ═══
    private static String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    // ═══ Helper للـ Activity: هل يمكن يخدم دابا؟ ═══
    public static String canWorkMessage(long lastWorkTime, int todayWorks, String todayDate) {
        if (!todayDate.equals(today())) {
            todayWorks = 0;
        }
        if (todayWorks >= DAILY_LIMIT) {
            return "🌙 وصلت الحد اليومي (" + DAILY_LIMIT + "). عد غداً!";
        }
        long now = System.currentTimeMillis();
        if (now - lastWorkTime < COOLDOWN_MS) {
            long remaining = COOLDOWN_MS - (now - lastWorkTime);
            long sec = remaining / 1000;
            long min = sec / 60;
            long secRest = sec % 60;
            return "⏰ انتظر " + min + ":" + (secRest < 10 ? "0" : "") + secRest;
        }
        return null;
    }

    public static int worksLeft(int todayWorks, String todayDate) {
        if (!todayDate.equals(today())) {
            return DAILY_LIMIT;
        }
        return Math.max(0, DAILY_LIMIT - todayWorks);
    }
}
