package com.ummah.app;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.Map;

public class FirebaseManager {

    private static FirebaseManager instance;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public interface OnDone {
        void onSuccess();
        void onError(String message);
    }

    private FirebaseManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static FirebaseManager get() {
        if (instance == null) instance = new FirebaseManager();
        return instance;
    }

    public String getUid() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
    }

    public void signIn(OnDone cb) {
        if (auth.getCurrentUser() != null) { cb.onSuccess(); return; }
        auth.signInAnonymously()
            .addOnSuccessListener(r -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void registerCitizen(Citizen c, int balance, OnDone cb) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", c.name);
        data.put("nationalId", c.nationalId);
        data.put("joinDate", c.joinDate);
        data.put("balance", balance);
        data.put("uid", getUid());
        data.put("createdAt", System.currentTimeMillis());

        db.collection("citizens").document(c.nationalId).set(data)
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public interface CountListener { void onCount(int count); }

    public ListenerRegistration listenCitizensCount(CountListener l) {
        return db.collection("citizens").addSnapshotListener((snap, e) -> {
            if (snap != null) l.onCount(snap.size());
        });
    }

    public interface BalanceListener {
        void onBalance(int balance);
        void onError(String message);
    }

    public ListenerRegistration listenBalance(String nationalId, BalanceListener l) {
        return db.collection("citizens").document(nationalId)
            .addSnapshotListener((doc, e) -> {
                if (e != null) { l.onError(e.getMessage()); return; }
                if (doc == null || !doc.exists()) { l.onError("غير موجود"); return; }
                Long b = doc.getLong("balance");
                l.onBalance(b != null ? b.intValue() : 0);
            });
    }

    public void transfer(String senderId, String receiverId, int amount, String note, OnDone cb) {
        if (senderId.equals(receiverId)) { cb.onError("لا يمكنك الإرسال لنفسك"); return; }
        if (amount <= 0) { cb.onError("المبلغ غير صالح"); return; }

        final DocumentReference senderRef = db.collection("citizens").document(senderId);
        final DocumentReference receiverRef = db.collection("citizens").document(receiverId);

        db.runTransaction((Transaction.Function<Void>) tx -> {
            com.google.firebase.firestore.DocumentSnapshot recSnap = tx.get(receiverRef);
            if (!recSnap.exists()) throw new RuntimeException("RECEIVER_NOT_FOUND");

            com.google.firebase.firestore.DocumentSnapshot senSnap = tx.get(senderRef);
            if (!senSnap.exists()) throw new RuntimeException("SENDER_NOT_FOUND");
            Long senBalL = senSnap.getLong("balance");
            int senBal = senBalL != null ? senBalL.intValue() : 0;
            if (senBal < amount) throw new RuntimeException("INSUFFICIENT");

            tx.update(senderRef, "balance", senBal - amount);
            Long recBalL = recSnap.getLong("balance");
            int recBal = recBalL != null ? recBalL.intValue() : 0;
            tx.update(receiverRef, "balance", recBal + amount);

            Map<String, Object> t = new HashMap<>();
            t.put("from", senderId);
            t.put("to", receiverId);
            t.put("amount", amount);
            t.put("note", note == null ? "" : note);
            t.put("timestamp", System.currentTimeMillis());
            tx.set(db.collection("transfers").document(), t);

            return null;
        }).addOnSuccessListener(a -> cb.onSuccess())
          .addOnFailureListener(e -> {
              String msg = e.getMessage();
              if (msg != null) {
                  if (msg.contains("RECEIVER_NOT_FOUND")) msg = "المستقبل غير موجود";
                  else if (msg.contains("SENDER_NOT_FOUND")) msg = "المرسل غير موجود";
                  else if (msg.contains("INSUFFICIENT")) msg = "رصيدك غير كافٍ";
              } else msg = "خطأ غير معروف";
              cb.onError(msg);
          });
    }

    public interface LookupListener {
        void onFound(String name);
        void onNotFound();
    }

    public void lookupCitizen(String nationalId, LookupListener l) {
        db.collection("citizens").document(nationalId).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String name = doc.getString("name");
                    l.onFound(name != null ? name : "مجهول");
                } else {
                    l.onNotFound();
                }
            })
            .addOnFailureListener(e -> l.onNotFound());
    }

    public interface TransferListener {
        void onTransfers(java.util.List<TransferItem> list);
    }

    public static class TransferItem {
        public String from, to, note;
        public int amount;
        public long timestamp;
    }

    public ListenerRegistration listenMyTransfers(String nationalId, TransferListener l) {
        return db.collection("transfers")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener((snap, e) -> {
                if (snap == null) return;
                java.util.List<TransferItem> list = new java.util.ArrayList<>();
                for (com.google.firebase.firestore.QueryDocumentSnapshot d : snap) {
                    String from = d.getString("from");
                    String to = d.getString("to");
                    if (from == null || to == null) continue;
                    if (!from.equals(nationalId) && !to.equals(nationalId)) continue;
                    TransferItem t = new TransferItem();
                    t.from = from;
                    t.to = to;
                    t.note = d.getString("note");
                    Long a = d.getLong("amount");
                    t.amount = a != null ? a.intValue() : 0;
                    Long ts = d.getLong("timestamp");
                    t.timestamp = ts != null ? ts : 0;
                    list.add(t);
                }
                l.onTransfers(list);
            });
    }

    public void addBalance(String nationalId, int amount, OnDone cb) {
        db.collection("citizens").document(nationalId)
            .update("balance", com.google.firebase.firestore.FieldValue.increment(amount))
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ============ الأخبار ============
    public interface NewsListener { void onNews(java.util.List<NewsItem> list); }

    public static class NewsItem {
        public String id;
        public String author;
        public String content;
        public long timestamp;
    }

    public void postNews(String author, String content, OnDone cb) {
        java.util.Map<String, Object> n = new HashMap<>();
        n.put("author", author);
        n.put("content", content);
        n.put("timestamp", System.currentTimeMillis());

        db.collection("news").add(n)
            .addOnSuccessListener(doc -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public ListenerRegistration listenNews(NewsListener l) {
        return db.collection("news")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener((snap, e) -> {
                if (snap == null) return;
                java.util.List<NewsItem> list = new java.util.ArrayList<>();
                for (com.google.firebase.firestore.QueryDocumentSnapshot d : snap) {
                    NewsItem n = new NewsItem();
                    n.id = d.getId();
                    n.author = d.getString("author");
                    n.content = d.getString("content");
                    Long t = d.getLong("timestamp");
                    n.timestamp = t != null ? t : 0;
                    list.add(n);
                }
                l.onNews(list);
            });
    }

    // ============ دليل المواطنين ============
    public interface CitizensListListener { void onList(java.util.List<CitizenItem> list); }

    public static class CitizenItem {
        public String nationalId;
        public String name;
        public String joinDate;
        public int balance;
    }

    public ListenerRegistration listenTopCitizens(int limit, CitizensListListener l) {
        return db.collection("citizens")
            .orderBy("balance", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener((snap, e) -> {
                if (snap == null) return;
                java.util.List<CitizenItem> list = new java.util.ArrayList<>();
                for (com.google.firebase.firestore.QueryDocumentSnapshot d : snap) {
                    CitizenItem c = new CitizenItem();
                    c.nationalId = d.getId();
                    c.name = d.getString("name");
                    c.joinDate = d.getString("joinDate");
                    Long b = d.getLong("balance");
                    c.balance = b != null ? b.intValue() : 0;
                    list.add(c);
                }
                l.onList(list);
            });
    }

    public void searchCitizenByExactId(String nationalId, CitizenLookup cb) {
        db.collection("citizens").document(nationalId).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    CitizenItem c = new CitizenItem();
                    c.nationalId = doc.getId();
                    c.name = doc.getString("name");
                    c.joinDate = doc.getString("joinDate");
                    Long b = doc.getLong("balance");
                    c.balance = b != null ? b.intValue() : 0;
                    cb.onFound(c);
                } else {
                    cb.onNotFound();
                }
            })
            .addOnFailureListener(e -> cb.onNotFound());
    }

    public interface CitizenLookup {
        void onFound(CitizenItem c);
        void onNotFound();
    }

    // ============ الخزينة العامة ============
    public ListenerRegistration listenTreasury(BalanceListener l) {
        return db.collection("treasury").document("main")
            .addSnapshotListener((doc, e) -> {
                if (e != null) { l.onError(e.getMessage()); return; }
                if (doc == null || !doc.exists()) { l.onBalance(0); return; }
                Long b = doc.getLong("balance");
                l.onBalance(b != null ? b.intValue() : 0);
            });
    }

    public void contributeToTreasury(int amount, OnDone cb) {
        db.collection("treasury").document("main").get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) {
                    java.util.Map<String, Object> data = new HashMap<>();
                    data.put("balance", amount);
                    data.put("createdAt", System.currentTimeMillis());
                    db.collection("treasury").document("main").set(data)
                        .addOnSuccessListener(a -> cb.onSuccess())
                        .addOnFailureListener(e -> cb.onError(e.getMessage()));
                } else {
                    db.collection("treasury").document("main")
                        .update("balance", com.google.firebase.firestore.FieldValue.increment(amount))
                        .addOnSuccessListener(a -> cb.onSuccess())
                        .addOnFailureListener(e -> cb.onError(e.getMessage()));
                }
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ============ الدردشة ============
    public static class ChatMessage {
        public String id;
        public String author;
        public String nationalId;
        public String text;
        public long timestamp;
    }

    public interface ChatListener { void onMessages(java.util.List<ChatMessage> list); }

    // دردشة عامة
    public void sendGlobalMessage(String author, String nationalId, String text, OnDone cb) {
        java.util.Map<String, Object> m = new HashMap<>();
        m.put("author", author);
        m.put("nationalId", nationalId);
        m.put("text", text);
        m.put("timestamp", System.currentTimeMillis());
        db.collection("global_chat").add(m)
            .addOnSuccessListener(d -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public ListenerRegistration listenGlobalChat(ChatListener l) {
        return db.collection("global_chat")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .limit(200)
            .addSnapshotListener((snap, e) -> {
                if (snap == null) return;
                java.util.List<ChatMessage> list = new java.util.ArrayList<>();
                for (com.google.firebase.firestore.QueryDocumentSnapshot d : snap) {
                    list.add(toMsg(d));
                }
                l.onMessages(list);
            });
    }

    // دردشة خاصة
    private String chatId(String a, String b) {
        return a.compareTo(b) < 0 ? a + "_" + b : b + "_" + a;
    }

    public void sendPrivateMessage(String fromId, String fromName, String toId, String text, OnDone cb) {
        String cid = chatId(fromId, toId);
        java.util.Map<String, Object> m = new HashMap<>();
        m.put("fromId", fromId);
        m.put("fromName", fromName);
        m.put("toId", toId);
        m.put("text", text);
        m.put("timestamp", System.currentTimeMillis());
        db.collection("chats").document(cid).collection("messages").add(m)
            .addOnSuccessListener(d -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public ListenerRegistration listenPrivateChat(String myId, String otherId, ChatListener l) {
        String cid = chatId(myId, otherId);
        return db.collection("chats").document(cid).collection("messages")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .limit(200)
            .addSnapshotListener((snap, e) -> {
                if (snap == null) return;
                java.util.List<ChatMessage> list = new java.util.ArrayList<>();
                for (com.google.firebase.firestore.QueryDocumentSnapshot d : snap) {
                    ChatMessage m = new ChatMessage();
                    m.id = d.getId();
                    m.author = d.getString("fromName");
                    m.nationalId = d.getString("fromId");
                    m.text = d.getString("text");
                    Long t = d.getLong("timestamp");
                    m.timestamp = t != null ? t : 0;
                    list.add(m);
                }
                l.onMessages(list);
            });
    }

    private ChatMessage toMsg(com.google.firebase.firestore.QueryDocumentSnapshot d) {
        ChatMessage m = new ChatMessage();
        m.id = d.getId();
        m.author = d.getString("author");
        m.nationalId = d.getString("nationalId");
        m.text = d.getString("text");
        Long t = d.getLong("timestamp");
        m.timestamp = t != null ? t : 0;
        return m;
    }
}
