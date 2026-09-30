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

public class PresidentManager {

    private final FirebaseFirestore db;

    public PresidentManager() {
        db = FirebaseFirestore.getInstance();
    }

    public interface OnDone {
        void onSuccess();
        void onError(String msg);
    }

    public interface OnDoneAmount {
        void onSuccess(int amount);
        void onError(String msg);
    }

    public interface AnnouncementsListener {
        void onList(List<Map<String, Object>> list);
    }

    // ═══════════════════════════════════════
    //  1. إعلان رئاسي
    // ═══════════════════════════════════════
    public void publishAnnouncement(String presidentId, String presidentName,
                                     String title, String content, final OnDone cb) {
        Map<String, Object> data = new HashMap<>();
        data.put("presidentId", presidentId);
        data.put("presidentName", presidentName);
        data.put("title", title);
        data.put("content", content);
        data.put("timestamp", System.currentTimeMillis());
        data.put("type", "presidential_announcement");
        data.put("priority", "high");
        data.put("isPresidential", true);

        // ننشرو في presidential_announcements
        db.collection("presidential_announcements").add(data)
            .addOnSuccessListener(ref -> {
                // ننشرو كذلك في news باش يبان للجميع
                db.collection("news").add(data)
                    .addOnSuccessListener(ref2 -> cb.onSuccess())
                    .addOnFailureListener(e -> cb.onError(e.getMessage()));
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public ListenerRegistration listenAnnouncements(final AnnouncementsListener l) {
        return db.collection("presidential_announcements")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(30)
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

    // ═══════════════════════════════════════
    //  2. هدية رئاسية
    // ═══════════════════════════════════════
    public void sendPresidentialGift(final String presidentId, final String presidentName,
                                      final String toId, final int amount,
                                      final String message, final OnDone cb) {
        db.runTransaction(transaction -> {
            // نقراو الخزينة
            DocumentReference treasuryRef = db.collection("treasury").document("main");
            DocumentSnapshot treasury = transaction.get(treasuryRef);

            long balance = 0;
            if (treasury.exists()) {
                Long b = treasury.getLong("balance");
                balance = b != null ? b : 0;
            }

            if (balance < amount) throw new RuntimeException("الخزينة ما فيهاش فلوس كافية");

            // نقراو المستقبل
            DocumentReference toRef = db.collection("citizens").document(toId);
            DocumentSnapshot to = transaction.get(toRef);
            if (!to.exists()) throw new RuntimeException("المواطن غير موجود");

            // الكتابات
            transaction.update(treasuryRef, "balance", balance - amount);
            transaction.update(toRef, "balance", FieldValue.increment(amount));

            // نسجلو الهدية
            Map<String, Object> gift = new HashMap<>();
            gift.put("presidentId", presidentId);
            gift.put("presidentName", presidentName);
            gift.put("toId", toId);
            gift.put("amount", amount);
            gift.put("message", message);
            gift.put("timestamp", System.currentTimeMillis());
            gift.put("type", "presidential_gift");
            transaction.set(db.collection("presidential_gifts").document(), gift);

            return amount;
        }).addOnSuccessListener(a -> cb.onSuccess())
          .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ═══════════════════════════════════════
    //  3. تعيين وزير
    // ═══════════════════════════════════════
    public void appointMinister(String presidentId, String presidentName,
                                 String citizenId, String ministerRole, final OnDone cb) {
        Map<String, Object> data = new HashMap<>();
        data.put("isMinister", true);
        data.put("ministerRole", ministerRole);
        data.put("appointedBy", presidentName);
        data.put("appointedAt", System.currentTimeMillis());

        db.collection("citizens").document(citizenId).update(data)
            .addOnSuccessListener(a -> {
                // نسجلو
                Map<String, Object> log = new HashMap<>();
                log.put("presidentId", presidentId);
                log.put("presidentName", presidentName);
                log.put("citizenId", citizenId);
                log.put("role", ministerRole);
                log.put("timestamp", System.currentTimeMillis());
                db.collection("presidential_appointments").add(log);
                cb.onSuccess();
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void removeMinister(String presidentId, String citizenId, final OnDone cb) {
        Map<String, Object> data = new HashMap<>();
        data.put("isMinister", false);
        data.put("ministerRole", "");

        db.collection("citizens").document(citizenId).update(data)
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ═══════════════════════════════════════
    //  4. عفو رئاسي
    // ═══════════════════════════════════════
    public void pardonCitizen(String presidentId, String presidentName,
                               String citizenId, final OnDone cb) {
        Map<String, Object> data = new HashMap<>();
        data.put("blocked", false);
        data.put("muted", false);
        data.put("mutedUntil", 0);

        db.collection("citizens").document(citizenId).update(data)
            .addOnSuccessListener(a -> {
                Map<String, Object> log = new HashMap<>();
                log.put("presidentId", presidentId);
                log.put("presidentName", presidentName);
                log.put("citizenId", citizenId);
                log.put("timestamp", System.currentTimeMillis());
                db.collection("presidential_pardons").add(log);
                cb.onSuccess();
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public ListenerRegistration listenBlockedOrMuted(final PresidentListListener l) {
        return db.collection("citizens").addSnapshotListener((snap, e) -> {
            if (snap == null) return;
            List<PresidentCitizen> list = new ArrayList<>();
            for (QueryDocumentSnapshot d : snap) {
                Boolean b = d.getBoolean("blocked");
                Boolean m = d.getBoolean("muted");
                boolean blocked = b != null && b;
                boolean muted = m != null && m;
                if (blocked || muted) {
                    PresidentCitizen pc = new PresidentCitizen();
                    pc.nationalId = d.getId();
                    pc.name = d.getString("name");
                    pc.blocked = blocked;
                    pc.muted = muted;
                    Long mb = d.getLong("balance");
                    pc.balance = mb != null ? mb.intValue() : 0;
                    list.add(pc);
                }
            }
            l.onList(list);
        });
    }

    // ═══════════════════════════════════════
    //  5. خزينة
    // ═══════════════════════════════════════
    public void addToTreasury(long amount, final OnDone cb) {
        db.collection("treasury").document("main")
            .set(java.util.Collections.singletonMap("balance", FieldValue.increment(amount)),
                    com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void withdrawFromTreasury(long amount, final OnDone cb) {
        db.runTransaction(transaction -> {
            DocumentReference ref = db.collection("treasury").document("main");
            DocumentSnapshot doc = transaction.get(ref);
            long balance = 0;
            if (doc.exists()) {
                Long b = doc.getLong("balance");
                balance = b != null ? b : 0;
            }
            if (balance < amount) throw new RuntimeException("الرصيد غير كافي");
            transaction.update(ref, "balance", balance - amount);
            return null;
        }).addOnSuccessListener(a -> cb.onSuccess())
          .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public interface TreasuryListener {
        void onBalance(long balance);
    }

    public ListenerRegistration listenTreasury(final TreasuryListener l) {
        return db.collection("treasury").document("main")
            .addSnapshotListener((doc, e) -> {
                if (e != null || doc == null || !doc.exists()) { l.onBalance(0); return; }
                Long b = doc.getLong("balance");
                l.onBalance(b != null ? b : 0);
            });
    }

    // ═══════════════════════════════════════
    //  6. منح لقب
    // ═══════════════════════════════════════
    public void grantTitle(String presidentId, String presidentName,
                            String citizenId, String title, final OnDone cb) {
        Map<String, Object> titleData = new HashMap<>();
        titleData.put("title", title);
        titleData.put("grantedBy", presidentName);
        titleData.put("grantedAt", System.currentTimeMillis());

        db.collection("citizens").document(citizenId)
            .update("titles", FieldValue.arrayUnion(titleData))
            .addOnSuccessListener(a -> {
                Map<String, Object> log = new HashMap<>();
                log.put("presidentId", presidentId);
                log.put("citizenId", citizenId);
                log.put("title", title);
                log.put("timestamp", System.currentTimeMillis());
                db.collection("presidential_titles").add(log);
                cb.onSuccess();
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ═══════════════════════════════════════
    //  7. إصدار مرسوم
    // ═══════════════════════════════════════
    public void issueDecree(String presidentId, String presidentName,
                             String title, String content, final OnDone cb) {
        Map<String, Object> data = new HashMap<>();
        data.put("presidentId", presidentId);
        data.put("presidentName", presidentName);
        data.put("title", title);
        data.put("content", content);
        data.put("timestamp", System.currentTimeMillis());
        data.put("status", "active");
        data.put("type", "decree");

        db.collection("presidential_decrees").add(data)
            .addOnSuccessListener(ref -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public ListenerRegistration listenDecrees(final AnnouncementsListener l) {
        return db.collection("presidential_decrees")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
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

    // ═══════════════════════════════════════
    //  Helper: جلب المواطنين
    // ═══════════════════════════════════════
    public static class PresidentCitizen {
        public String nationalId, name;
        public int balance;
        public boolean blocked, muted, isMinister;
        public String ministerRole;
    }

    public interface PresidentListListener {
        void onList(List<PresidentCitizen> list);
    }

    public ListenerRegistration listenAllCitizensForPresident(final PresidentListListener l) {
        return db.collection("citizens").addSnapshotListener((snap, e) -> {
            if (snap == null) return;
            List<PresidentCitizen> list = new ArrayList<>();
            for (QueryDocumentSnapshot d : snap) {
                PresidentCitizen pc = new PresidentCitizen();
                pc.nationalId = d.getId();
                pc.name = d.getString("name");
                Long b = d.getLong("balance");
                pc.balance = b != null ? b.intValue() : 0;
                Boolean bl = d.getBoolean("blocked");
                pc.blocked = bl != null && bl;
                Boolean mu = d.getBoolean("muted");
                pc.muted = mu != null && mu;
                Boolean min = d.getBoolean("isMinister");
                pc.isMinister = min != null && min;
                pc.ministerRole = d.getString("ministerRole");
                list.add(pc);
            }
            l.onList(list);
        });
    }
}
