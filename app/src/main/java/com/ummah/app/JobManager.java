package com.ummah.app;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobManager {

    private final FirebaseFirestore db;

    // ═══ الإعدادات ═══
    public static final int DAILY_LIMIT = 2;                       // 2 مرات فقط
    public static final long COOLDOWN_MS = 10L * 60 * 1000;        // 10 دقائق بين كل عمل
    public static final long LOCK_DURATION_MS = 24L * 60 * 60 * 1000; // 24 ساعة قفل

    public JobManager() {
        db = FirebaseFirestore.getInstance();
    }

    public interface JobsListener {
        void onJobs(List<Job> jobs);
        void onError(String msg);
    }

    public interface OnDone {
        void onSuccess(int earned);
        void onError(String msg);
    }

    public interface MyJobListener {
        void onJob(String jobId, String title, String emoji, int salary,
                   long lastWorkTime, int level, int xp,
                   int todayWorks, long lockedUntil);
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
                        l.onJob(null, null, null, 0, 0, 1, 0, 0, 0);
                        return;
                    }
                    String jobId = doc.getString("jobId");
                    String title = doc.getString("title");
                    String emoji = doc.getString("emoji");
                    Long salL = doc.getLong("salary");
                    Long lw = doc.getLong("lastWorkTime");
                    Long lvl = doc.getLong("level");
                    Long xp = doc.getLong("xp");
                    Long tw = doc.getLong("todayWorks");
                    Long lu = doc.getLong("lockedUntil");

                    l.onJob(jobId, title, emoji,
                            salL != null ? salL.intValue() : 0,
                            lw != null ? lw : 0,
                            lvl != null ? lvl.intValue() : 1,
                            xp != null ? xp.intValue() : 0,
                            tw != null ? tw.intValue() : 0,
                            lu != null ? lu : 0);
                });
    }

    // ═══ يختار وظيفة ═══
    public void chooseJob(String userId, Job job, final OnDone cb) {
        DocumentReference ref = db.collection("users_jobs").document(userId);

        db.runTransaction(transaction -> {
            DocumentSnapshot existing = transaction.get(ref);

            // 1️⃣ إذا عندو نفس الوظيفة → ما نديرو والو
            if (existing.exists()) {
                String currentJobId = existing.getString("jobId");
                if (job.id != null && job.id.equals(currentJobId)) {
                    throw new RuntimeException("أنت بالفعل في هذه الوظيفة");
                }
            }

            // 2️⃣ نتحققو من القفل العام
            long existingLock = 0L;
            int existingWorks = 0;
            if (existing.exists()) {
                Long lu = existing.getLong("lockedUntil");
                Long tw = existing.getLong("todayWorks");
                existingLock = lu != null ? lu : 0L;
                existingWorks = tw != null ? tw.intValue() : 0;
            }

            long now = System.currentTimeMillis();
            boolean isLocked = (existingLock > 0 && now < existingLock);

            // 3️⃣ وظيفة جديدة → ننشئو
            Map<String, Object> data = new HashMap<>();
            data.put("jobId", job.id);
            data.put("title", job.title);
            data.put("emoji", job.emoji);
            data.put("salary", job.salary);
            data.put("cooldownMin", job.cooldownMin);
            data.put("lastWorkTime", 0L);
            data.put("level", 1);
            data.put("xp", 0);
            data.put("chosenAt", now);

            // 4️⃣ إذا الحساب مقفل → نحافظ على القفل والعدّاد
            if (isLocked) {
                data.put("todayWorks", existingWorks);
                data.put("lockedUntil", existingLock);
            } else {
                // ما مقفلش → نبدأ من صفر
                data.put("todayWorks", 0);
                data.put("lockedUntil", 0L);
            }

            transaction.set(ref, data);
            return 0;
        })
        .addOnSuccessListener(r -> cb.onSuccess(0))
        .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ═══ يخدم → يربح ═══
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
            Long luL = jobDoc.getLong("lockedUntil");

            int salary = salaryL != null ? salaryL.intValue() : 0;
            long lastWork = lastWorkL != null ? lastWorkL : 0;
            int xp = xpL != null ? xpL.intValue() : 0;
            int level = lvlL != null ? lvlL.intValue() : 1;
            int todayWorks = twL != null ? twL.intValue() : 0;
            long lockedUntil = luL != null ? luL : 0;

            long now = System.currentTimeMillis();

            // 1️⃣ فحص القفل 24 ساعة
            if (lockedUntil > 0 && now < lockedUntil) {
                long remaining = lockedUntil - now;
                long hours = remaining / (60 * 60 * 1000);
                long minutes = (remaining % (60 * 60 * 1000)) / (60 * 1000);
                throw new RuntimeException("🔒 مقفل — يفتح بعد " + hours + "س " + minutes + "د");
            }

            // 2️⃣ إذا مر 24 ساعة → نصفّر
            if (lockedUntil > 0 && now >= lockedUntil) {
                todayWorks = 0;
                lockedUntil = 0;
            }

            // 3️⃣ فحص الحد اليومي
            if (todayWorks >= DAILY_LIMIT) {
                // نقفل 24 ساعة
                long newLock = now + LOCK_DURATION_MS;
                Map<String, Object> lockUpdate = new HashMap<>();
                lockUpdate.put("lockedUntil", newLock);
                transaction.update(jobRef, lockUpdate);
                throw new RuntimeException("🔒 وصلت الحد — مقفل 24 ساعة");
            }

            // 4️⃣ فحص الكولداون
            if (lastWork > 0 && now - lastWork < COOLDOWN_MS) {
                long remaining = COOLDOWN_MS - (now - lastWork);
                long sec = remaining / 1000;
                long min = sec / 60;
                long secRest = sec % 60;
                throw new RuntimeException("⏰ انتظر " + min + ":" + (secRest < 10 ? "0" : "") + secRest);
            }

            // 5️⃣ نزيد الرصيد
            DocumentReference userRef = db.collection("citizens").document(userId);
            transaction.update(userRef, "balance", FieldValue.increment(salary));

            // 6️⃣ نزيد XP
            xp += 10;
            int newLevel = level;
            if (xp >= level * 100) {
                newLevel = level + 1;
                xp = 0;
            }

            // 7️⃣ نحدّث
            int newWorks = todayWorks + 1;
            long newLocked = (newWorks >= DAILY_LIMIT) ? now + LOCK_DURATION_MS : 0;

            Map<String, Object> update = new HashMap<>();
            update.put("lastWorkTime", now);
            update.put("xp", xp);
            update.put("level", newLevel);
            update.put("todayWorks", newWorks);
            update.put("lockedUntil", newLocked);

            transaction.update(jobRef, update);
            return salary;
        }).addOnSuccessListener(r -> cb.onSuccess(r))
          .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void quitJob(String userId, final OnDone cb) {
        db.collection("users_jobs").document(userId).delete()
                .addOnSuccessListener(a -> cb.onSuccess(0))
                .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ═══ Helper: عدد الأعمال المتبقية ═══
    public static int worksLeft(int todayWorks, long lockedUntil) {
        long now = System.currentTimeMillis();
        // مقفل الآن
        if (lockedUntil > 0 && now < lockedUntil) return 0;
        // انتهى القفل → اصفّر
        if (lockedUntil > 0 && now >= lockedUntil) return DAILY_LIMIT;
        // عادي
        return Math.max(0, DAILY_LIMIT - todayWorks);
    }

    // ═══ Helper: هل يمكن يخدم؟ ═══
    public static String canWorkMessage(long lastWorkTime, int todayWorks, long lockedUntil) {
        long now = System.currentTimeMillis();

        // 1. مقفل الآن
        if (lockedUntil > 0 && now < lockedUntil) {
            long remaining = lockedUntil - now;
            long h = remaining / (60 * 60 * 1000);
            long m = (remaining % (60 * 60 * 1000)) / (60 * 1000);
            long s = (remaining % (60 * 1000)) / 1000;
            return "🔒 مقفل — يفتح بعد " + h + ":" 
                    + (m < 10 ? "0" : "") + m + ":" 
                    + (s < 10 ? "0" : "") + s;
        }

        // 2. انتهى القفل → اصفّر (بس نتحقق من الكولداون من جديد)
        if (lockedUntil > 0 && now >= lockedUntil) {
            // ما فيهش قفل — نقدر نخدم
        } else if (todayWorks >= DAILY_LIMIT) {
            return "🔒 وصلت الحد — مقفل 24 ساعة";
        }

        // 3. كولداون 10 دقائق
        if (lastWorkTime > 0 && now - lastWorkTime < COOLDOWN_MS) {
            long remaining = COOLDOWN_MS - (now - lastWorkTime);
            long sec = remaining / 1000;
            long min = sec / 60;
            long secRest = sec % 60;
            return "⏰ انتظر " + min + ":" + (secRest < 10 ? "0" : "") + secRest;
        }

        return null;
    }
}
