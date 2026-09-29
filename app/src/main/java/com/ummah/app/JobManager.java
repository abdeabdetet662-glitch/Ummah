package com.ummah.app;

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
        void onJob(String jobId, long lastWorkTime, int level, int xp);
        void onError(String msg);
    }

    // كل الوظائف
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

    // يسمع لوظيفتي
    public ListenerRegistration listenMyJob(String userId, final MyJobListener l) {
        return db.collection("users_jobs").document(userId)
                .addSnapshotListener((doc, e) -> {
                    if (e != null) { l.onError(e.getMessage()); return; }
                    if (doc == null || !doc.exists()) {
                        l.onJob(null, 0, 1, 0);
                        return;
                    }
                    String jobId = doc.getString("jobId");
                    Long lw = doc.getLong("lastWorkTime");
                    Long lvl = doc.getLong("level");
                    Long xp = doc.getLong("xp");
                    l.onJob(jobId,
                            lw != null ? lw : 0,
                            lvl != null ? lvl.intValue() : 1,
                            xp != null ? xp.intValue() : 0);
                });
    }

    // يختار وظيفة
    public void chooseJob(String userId, Job job, final OnDone cb) {
        Map<String, Object> data = new HashMap<>();
        data.put("jobId", job.id);
        data.put("title", job.title);
        data.put("salary", job.salary);
        data.put("lastWorkTime", 0);
        data.put("level", 1);
        data.put("xp", 0);
        data.put("chosenAt", System.currentTimeMillis());

        db.collection("users_jobs").document(userId).set(data)
                .addOnSuccessListener(a -> cb.onSuccess(0))
                .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // يخدم → يربح راتب
    public void work(String userId, final OnDone cb) {
        db.runTransaction(transaction -> {
            com.google.firebase.firestore.DocumentReference jobRef =
                    db.collection("users_jobs").document(userId);
            DocumentSnapshot jobDoc = transaction.get(jobRef);
            if (!jobDoc.exists()) throw new RuntimeException("لم تختر وظيفة بعد");

            Long salaryL = jobDoc.getLong("salary");
            Long lastWorkL = jobDoc.getLong("lastWorkTime");
            Long xpL = jobDoc.getLong("xp");
            Long lvlL = jobDoc.getLong("level");

            int salary = salaryL != null ? salaryL.intValue() : 0;
            long lastWork = lastWorkL != null ? lastWorkL : 0;
            int xp = xpL != null ? xpL.intValue() : 0;
            int level = lvlL != null ? lvlL.intValue() : 1;

            // cooldown 30 دقيقة
            long now = System.currentTimeMillis();
            long cooldownMs = 30L * 60 * 1000;
            if (now - lastWork < cooldownMs) {
                long remaining = cooldownMs - (now - lastWork);
                long minutes = remaining / 60000;
                throw new RuntimeException("انتظر " + minutes + " دقيقة");
            }

            // نزيد الرصيد
            com.google.firebase.firestore.DocumentReference userRef =
                    db.collection("citizens").document(userId);
            transaction.update(userRef, "balance", FieldValue.increment(salary));

            // نزيد XP
            xp += 10;
            int newLevel = level;
            if (xp >= level * 100) {
                newLevel = level + 1;
                xp = 0;
            }

            Map<String, Object> update = new HashMap<>();
            update.put("lastWorkTime", now);
            update.put("xp", xp);
            update.put("level", newLevel);

            transaction.update(jobRef, update);

            return salary;
        }).addOnSuccessListener(r -> cb.onSuccess(r))
          .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // يمسح الوظيفة
    public void quitJob(String userId, final OnDone cb) {
        db.collection("users_jobs").document(userId).delete()
                .addOnSuccessListener(a -> cb.onSuccess(0))
                .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }
}
