package com.ummah.app;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;

public class LifeStatsManager {

    private final FirebaseFirestore db;

    public LifeStatsManager() {
        db = FirebaseFirestore.getInstance();
    }

    public static class Stats {
        public int hunger = 100;    // 100 = شبعان، 0 = جوعان
        public int energy = 100;    // 100 = مرتاح
        public int happiness = 100; // 100 = سعيد
        public int health = 100;    // 100 = صحيح
        public long lastUpdate = 0;
    }

    public interface StatsListener {
        void onStats(Stats stats);
        void onError(String msg);
    }

    public interface OnDone {
        void onSuccess();
        void onError(String msg);
    }

    // قراءة الإحصائيات (مع تحديث النقصان حسب الوقت)
    public ListenerRegistration listenStats(String userId, final StatsListener l) {
        return db.collection("users_stats").document(userId)
                .addSnapshotListener((doc, e) -> {
                    if (e != null) { l.onError(e.getMessage()); return; }
                    Stats s = new Stats();
                    if (doc != null && doc.exists()) {
                        Long h = doc.getLong("hunger");
                        Long en = doc.getLong("energy");
                        Long hap = doc.getLong("happiness");
                        Long he = doc.getLong("health");
                        Long lu = doc.getLong("lastUpdate");
                        s.hunger = h != null ? h.intValue() : 100;
                        s.energy = en != null ? en.intValue() : 100;
                        s.happiness = hap != null ? hap.intValue() : 100;
                        s.health = he != null ? he.intValue() : 100;
                        s.lastUpdate = lu != null ? lu : System.currentTimeMillis();
                    } else {
                        s.lastUpdate = System.currentTimeMillis();
                    }
                    l.onStats(s);
                });
    }

    // نأكل → نزيد hunger
    public void eat(String userId, int amount, final OnDone cb) {
        updateStat(userId, "hunger", amount, cb);
    }

    // نرتاح → نزيد energy
    public void sleep(String userId, int amount, final OnDone cb) {
        updateStat(userId, "energy", amount, cb);
    }

    // نرفه → نزيد happiness
    public void entertain(String userId, int amount, final OnDone cb) {
        updateStat(userId, "happiness", amount, cb);
    }

    // نتدوا → نزيد health
    public void heal(String userId, int amount, final OnDone cb) {
        updateStat(userId, "health", amount, cb);
    }

    // نخصمو من الرصيد
    public void spendForAction(String userId, int cost, final OnDone cb) {
        db.runTransaction(transaction -> {
            com.google.firebase.firestore.DocumentReference userRef =
                    db.collection("citizens").document(userId);
            com.google.firebase.firestore.DocumentSnapshot user = transaction.get(userRef);
            if (!user.exists()) throw new RuntimeException("المستخدم غير موجود");

            Long balL = user.getLong("balance");
            int bal = balL != null ? balL.intValue() : 0;
            if (bal < cost) throw new RuntimeException("الرصيد غير كافٍ");

            transaction.update(userRef, "balance", bal - cost);
            return null;
        }).addOnSuccessListener(a -> cb.onSuccess())
          .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    private void updateStat(String userId, String field, int amount, final OnDone cb) {
        Map<String, Object> data = new HashMap<>();
        data.put(field, FieldValue.increment(amount));
        data.put("lastUpdate", System.currentTimeMillis());

        db.collection("users_stats").document(userId)
                .set(data, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(a -> cb.onSuccess())
                .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // تحديث تلقائي للنقصان (يستدعى كل ساعة)
    public void decayStats(String userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("hunger", FieldValue.increment(-2));
        data.put("energy", FieldValue.increment(-1));
        data.put("happiness", FieldValue.increment(-1));
        data.put("lastUpdate", System.currentTimeMillis());

        db.collection("users_stats").document(userId)
                .set(data, com.google.firebase.firestore.SetOptions.merge());
    }
}
