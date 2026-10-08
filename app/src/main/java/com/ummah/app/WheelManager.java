package com.ummah.app;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class WheelManager {

    private final FirebaseFirestore db;
    private final Random random = new Random();

    public WheelManager() {
        db = FirebaseFirestore.getInstance();
    }

    public interface ConfigListener {
        void onConfig(WheelConfig config);
        void onError(String msg);
    }

    public interface SegmentsListener {
        void onSegments(List<WheelSegment> segments);
        void onError(String msg);
    }

    public interface SpinListener {
        void onResult(WheelSegment segment, int jackpotAmount);
        void onError(String msg);
    }

    public interface OnDone {
        void onSuccess();
        void onError(String msg);
    }

    public static class WheelConfig {
        public int spinCost = 500;
        public boolean freeSpinEnabled = true;
        public long freeSpinCooldownMs = 24 * 60 * 60 * 1000L;
        public boolean jackpotEnabled = true;
        public long jackpotAmount = 5000;
        public long jackpotMin = 100000;
        public int jackpotPercent = 1;
        public boolean active = true;
        public String jackpotWinnerId = "";    // UID الفائز الوحيد
        public boolean jackpotClaimed = false; // هل استلم الجاكبوت؟
    }

    public ListenerRegistration listenConfig(final ConfigListener l) {
        return db.collection("wheel_config").document("main")
            .addSnapshotListener((doc, e) -> {
                if (e != null) { l.onError(e.getMessage()); return; }
                WheelConfig c = new WheelConfig();
                if (doc != null && doc.exists()) {
                    Long sc = doc.getLong("spinCost");
                    if (sc != null) c.spinCost = sc.intValue();
                    Boolean fs = doc.getBoolean("freeSpinEnabled");
                    if (fs != null) c.freeSpinEnabled = fs;
                    Long cd = doc.getLong("freeSpinCooldownMs");
                    if (cd != null) c.freeSpinCooldownMs = cd;
                    Boolean je = doc.getBoolean("jackpotEnabled");
                    if (je != null) c.jackpotEnabled = je;
                    Long ja = doc.getLong("jackpotAmount");
                    if (ja != null) c.jackpotAmount = ja;
                    String jw = doc.getString("jackpotWinnerId");
                    if (jw != null) c.jackpotWinnerId = jw;
                    Boolean jc = doc.getBoolean("jackpotClaimed");
                    if (jc != null) c.jackpotClaimed = jc;
                    Long jm = doc.getLong("jackpotMin");
                    if (jm != null) c.jackpotMin = jm;
                    Long jp = doc.getLong("jackpotPercent");
                    if (jp != null) c.jackpotPercent = jp.intValue();
                    Boolean ac = doc.getBoolean("active");
                    if (ac != null) c.active = ac;
                }
                l.onConfig(c);
            });
    }

    public ListenerRegistration listenSegments(final SegmentsListener l) {
        return db.collection("wheel_segments")
            .whereEqualTo("active", true)
            .addSnapshotListener((snap, e) -> {
                if (e != null) { l.onError(e.getMessage()); return; }
                if (snap == null) { l.onSegments(new ArrayList<>()); return; }
                List<WheelSegment> list = new ArrayList<>();
                for (QueryDocumentSnapshot d : snap) {
                    WheelSegment s = d.toObject(WheelSegment.class);
                    if (s != null) {
                        s.id = d.getId();
                        list.add(s);
                    }
                }
                list.sort((a, b) -> Integer.compare(a.order, b.order));
                l.onSegments(list);
            });
    }

    public void spin(final String nationalId, final boolean isFree,
                     final List<WheelSegment> segments, final WheelConfig config,
                     final SpinListener l) {
        if (segments == null || segments.isEmpty()) {
            l.onError("ما فيه قطاعات");
            return;
        }

        final WheelSegment winner = pickWinner(segments);
        if (winner == null) {
            l.onError("خطأ في الاختيار");
            return;
        }

        // ═══ معالجة الجاكبوت ═══
        final boolean isJackpotSegment = "jackpot".equals(winner.type);
        final boolean canWinJackpot = isJackpotSegment
                && config.jackpotEnabled
                && !config.jackpotClaimed
                && config.jackpotWinnerId != null
                && !config.jackpotWinnerId.isEmpty()
                && config.jackpotWinnerId.equals(nationalId);

        db.runTransaction(transaction -> {
            DocumentReference userRef = db.collection("citizens").document(nationalId);
            DocumentSnapshot user = transaction.get(userRef);
            if (!user.exists()) throw new RuntimeException("المستخدم غير موجود");

            Long balL = user.getLong("balance");
            int bal = balL != null ? balL.intValue() : 0;

            int cost = isFree ? 0 : config.spinCost;
            if (!isFree && bal < cost) {
                throw new RuntimeException("الرصيد غير كافٍ");
            }

            if (!isFree) {
                transaction.update(userRef, "balance", bal - cost);
            }

            if ("money".equals(winner.type) && winner.value > 0) {
                transaction.update(userRef, "balance",
                    FieldValue.increment(winner.value + cost));
            }

            Map<String, Object> spin = new HashMap<>();
            spin.put("nationalId", nationalId);
            spin.put("segmentId", winner.id);
            spin.put("segmentLabel", winner.label);
            spin.put("value", winner.value);
            spin.put("type", winner.type);
            spin.put("isFree", isFree);
            spin.put("timestamp", System.currentTimeMillis());

            transaction.set(db.collection("wheel_spins").document(), spin);

            if (config.jackpotEnabled && !isFree) {
                DocumentReference jackpotRef = db.collection("wheel_jackpot").document("main");
                DocumentSnapshot jackpotDoc = transaction.get(jackpotRef);
                long jAmount = 0;
                if (jackpotDoc.exists()) {
                    Long ja = jackpotDoc.getLong("amount");
                    jAmount = ja != null ? ja : 0;
                }
                long addToJackpot = (config.spinCost * config.jackpotPercent) / 100L;
                jAmount += addToJackpot;
                Map<String, Object> jm = new HashMap<>();
                jm.put("amount", jAmount);
                transaction.set(jackpotRef, jm);
            }

            return null;
        }).addOnSuccessListener(a -> {
            if (isJackpotSegment) {
                if (canWinJackpot) {
                    // ✅ أنت الفائز المُحدد — اربح الجاكبوت
                    int jackpotAmount = (int) config.jackpotAmount;
                    db.collection("citizens").document(nationalId)
                        .update("balance", FieldValue.increment(jackpotAmount));
                    db.collection("wheel_config").document("main")
                        .update("jackpotClaimed", true);
                    // نرسل الإشعار
                    db.collection("notifications").add(java.util.Map.of(
                        "userId", nationalId,
                        "title", "🎉 مبروك!",
                        "body", "ربحت الجاكبوت " + jackpotAmount + " Đ!",
                        "type", "jackpot",
                        "timestamp", System.currentTimeMillis()
                    ));
                    l.onResult(winner, jackpotAmount);
                } else {
                    // ❌ ليس الفائز — ياخذ 100 Đ بدل الجاكبوت
                    db.collection("citizens").document(nationalId)
                        .update("balance", FieldValue.increment(100));
                    WheelSegment alt = new WheelSegment(
                        "💚", "100 Đ", 100, "money", "#2E7D32", 20, 0);
                    l.onResult(alt, 0);
                }
            } else {
                l.onResult(winner, 0);
            }
        }).addOnFailureListener(e -> l.onError(e.getMessage()));
    }

    private WheelSegment pickWinner(List<WheelSegment> segments) {
        int total = 0;
        for (WheelSegment s : segments) {
            if (s.weight > 0) total += s.weight;
        }
        if (total == 0) return null;

        int pick = random.nextInt(total);
        int current = 0;
        for (WheelSegment s : segments) {
            if (s.weight <= 0) continue;
            current += s.weight;
            if (pick < current) return s;
        }
        return segments.get(0);
    }

    public interface FreeSpinListener {
        void onResult(boolean canFreeSpin, long nextFreeAt);
    }

    public void checkFreeSpin(String nationalId, long cooldownMs, final FreeSpinListener l) {
        db.collection("wheel_spins")
            .whereEqualTo("nationalId", nationalId)
            .whereEqualTo("isFree", true)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener(q -> {
                long now = System.currentTimeMillis();
                if (q.isEmpty()) {
                    l.onResult(true, now);
                    return;
                }
                Long lastTs = q.getDocuments().get(0).getLong("timestamp");
                long last = lastTs != null ? lastTs : 0;
                long nextAt = last + cooldownMs;
                l.onResult(now >= nextAt, nextAt);
            })
            .addOnFailureListener(e -> l.onResult(true, System.currentTimeMillis()));
    }

    public interface HistoryListener {
        void onList(List<Map<String, Object>> list);
    }

    public ListenerRegistration listenMySpins(String nationalId, final HistoryListener l) {
        return db.collection("wheel_spins")
            .whereEqualTo("nationalId", nationalId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener((snap, e) -> {
                if (snap == null) return;
                List<Map<String, Object>> list = new ArrayList<>();
                for (QueryDocumentSnapshot d : snap) {
                    Map<String, Object> m = d.getData();
                    m.put("_id", d.getId());
                    list.add(m);
                }
                l.onList(list);
            });
    }
}
