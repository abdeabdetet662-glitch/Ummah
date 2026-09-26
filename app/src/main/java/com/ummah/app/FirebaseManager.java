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

    // ==================== المواطنون ====================

    public void registerCitizen(Citizen c, int balance, OnDone cb) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", c.name);
        data.put("nationalId", c.nationalId);
        data.put("joinDate", c.joinDate);
        data.put("balance", balance);
        data.put("country", c.country);
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

    public void addBalance(String nationalId, int amount, OnDone cb) {
        db.collection("citizens").document(nationalId)
            .update("balance", com.google.firebase.firestore.FieldValue.increment(amount))
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ==================== التحويلات ====================

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

    // ==================== الدستور ====================

    public void submitConstitutionVote(String nationalId, int articleNum, boolean yes, OnDone cb) {
        Map<String, Object> v = new HashMap<>();
        v.put("nationalId", nationalId);
        v.put("article", articleNum);
        v.put("yes", yes);
        v.put("timestamp", System.currentTimeMillis());

        db.collection("constitution_votes").document(nationalId + "_art" + articleNum).set(v)
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public interface ConstitutionVotesListener { void onVotes(int[] yes, int[] no); }

    public ListenerRegistration listenConstitutionVotes(ConstitutionVotesListener l) {
        return db.collection("constitution_votes").addSnapshotListener((snap, e) -> {
            if (snap == null) return;
            int[] yes = new int[11];
            int[] no = new int[11];
            for (com.google.firebase.firestore.QueryDocumentSnapshot d : snap) {
                Long art = d.getLong("article");
                Boolean y = d.getBoolean("yes");
                if (art == null || y == null) continue;
                int i = art.intValue();
                if (i < 1 || i > 10) continue;
                if (y) yes[i]++; else no[i]++;
            }
            l.onVotes(yes, no);
        });
    }

    // ==================== البرلمان ====================

    public interface ProposalsListener { void onProposals(java.util.List<Proposal> list); }

    public ListenerRegistration listenProposals(ProposalsListener l) {
        return db.collection("proposals")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener((snap, e) -> {
                if (snap == null) return;
                java.util.List<Proposal> list = new java.util.ArrayList<>();
                for (com.google.firebase.firestore.QueryDocumentSnapshot d : snap) {
                    Proposal p = new Proposal(
                        d.getId(),
                        d.getString("title") != null ? d.getString("title") : "",
                        d.getString("body") != null ? d.getString("body") : "",
                        d.getString("author") != null ? d.getString("author") : "مجهول"
                    );
                    Long y = d.getLong("yes");
                    Long n = d.getLong("no");
                    p.yes = y != null ? y.intValue() : 0;
                    p.no = n != null ? n.intValue() : 0;
                    list.add(p);
                }
                l.onProposals(list);
            });
    }

    public void submitProposal(Proposal p, OnDone cb) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", p.title);
        data.put("body", p.body);
        data.put("author", p.author);
        data.put("yes", 0);
        data.put("no", 0);
        data.put("timestamp", System.currentTimeMillis());

        db.collection("proposals").add(data)
            .addOnSuccessListener(doc -> { p.id = doc.getId(); cb.onSuccess(); })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void voteProposal(String proposalId, String nationalId, boolean yes, OnDone cb) {
        String voteDocId = proposalId + "_" + nationalId;

        db.collection("proposal_votes").document(voteDocId).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) { cb.onError("لقد صوّتت مسبقاً"); return; }
                Map<String, Object> v = new HashMap<>();
                v.put("proposalId", proposalId);
                v.put("nationalId", nationalId);
                v.put("yes", yes);
                v.put("timestamp", System.currentTimeMillis());

                db.collection("proposal_votes").document(voteDocId).set(v)
                    .addOnSuccessListener(a ->
                        db.collection("proposals").document(proposalId)
                            .update(yes ? "yes" : "no",
                                com.google.firebase.firestore.FieldValue.increment(1))
                            .addOnSuccessListener(x -> cb.onSuccess())
                            .addOnFailureListener(e -> cb.onError(e.getMessage())))
                    .addOnFailureListener(e -> cb.onError(e.getMessage()));
            });
    }

    // ==================== الأخبار ====================

    public interface NewsListener { void onNews(java.util.List<NewsItem> list); }

    public static class NewsItem {
        public String id;
        public String author;
        public String content;
        public long timestamp;
    }

    public void postNews(String author, String content, OnDone cb) {
        Map<String, Object> n = new HashMap<>();
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

    // ==================== دليل المواطنين ====================

    public interface CitizensListListener { void onList(java.util.List<CitizenItem> list); }

    public static class CitizenItem {
        public String nationalId;
        public String name;
        public String joinDate;
        public String country;
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
                    c.country = d.getString("country");
                    Long b = d.getLong("balance");
                    c.balance = b != null ? b.intValue() : 0;
                    list.add(c);
                }
                l.onList(list);
            });
    }

    public interface CitizenLookup {
        void onFound(CitizenItem c);
        void onNotFound();
    }

    public void searchCitizenByExactId(String nationalId, CitizenLookup cb) {
        db.collection("citizens").document(nationalId).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    CitizenItem c = new CitizenItem();
                    c.nationalId = doc.getId();
                    c.name = doc.getString("name");
                    c.joinDate = doc.getString("joinDate");
                    c.country = doc.getString("country");
                    Long b = doc.getLong("balance");
                    c.balance = b != null ? b.intValue() : 0;
                    cb.onFound(c);
                } else {
                    cb.onNotFound();
                }
            })
            .addOnFailureListener(e -> cb.onNotFound());
    }

    // ==================== الخزينة ====================

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
                    Map<String, Object> data = new HashMap<>();
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

    // ==================== الدردشة ====================

    public static class ChatMessage {
        public String id;
        public String author;
        public String nationalId;
        public String text;
        public long timestamp;
    }

    public interface ChatListener { void onMessages(java.util.List<ChatMessage> list); }

    public void sendGlobalMessage(String author, String nationalId, String text, OnDone cb) {
        Map<String, Object> m = new HashMap<>();
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
                    ChatMessage m = new ChatMessage();
                    m.id = d.getId();
                    m.author = d.getString("author");
                    m.nationalId = d.getString("nationalId");
                    m.text = d.getString("text");
                    Long t = d.getLong("timestamp");
                    m.timestamp = t != null ? t : 0;
                    list.add(m);
                }
                l.onMessages(list);
            });
    }

    private String chatId(String a, String b) {
        return a.compareTo(b) < 0 ? a + "_" + b : b + "_" + a;
    }

    public void sendPrivateMessage(String fromId, String fromName, String toId, String text, OnDone cb) {
        String cid = chatId(fromId, toId);
        Map<String, Object> m = new HashMap<>();
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

    // ============ الإحصائيات ============
    public interface StatsListener {
        void onStats(int citizens, int transfers, int totalTransferred, int news, int proposals);
    }

    public void loadStats(StatsListener l) {
        final int[] s = new int[5];
        db.collection("citizens").get().addOnSuccessListener(q -> {
            s[0] = q.size();
            db.collection("transfers").get().addOnSuccessListener(q2 -> {
                s[1] = q2.size();
                int total = 0;
                for (com.google.firebase.firestore.QueryDocumentSnapshot d : q2) {
                    Long a = d.getLong("amount");
                    if (a != null) total += a.intValue();
                }
                s[2] = total;
                db.collection("news").get().addOnSuccessListener(q3 -> {
                    s[3] = q3.size();
                    db.collection("proposals").get().addOnSuccessListener(q4 -> {
                        s[4] = q4.size();
                        l.onStats(s[0], s[1], s[2], s[3], s[4]);
                    });
                });
            });
        });
    }

    // ============ استعادة الحساب ============
    public void lookupBySeedHash(String seedHash, SeedLookup l) {
        db.collection("citizens").whereEqualTo("seedHash", seedHash).limit(1).get()
            .addOnSuccessListener(q -> {
                if (q.isEmpty()) { l.onNotFound(); return; }
                com.google.firebase.firestore.DocumentSnapshot d = q.getDocuments().get(0);
                CitizenItem c = new CitizenItem();
                c.nationalId = d.getId();
                c.name = d.getString("name");
                c.joinDate = d.getString("joinDate");
                c.country = d.getString("country");
                Long b = d.getLong("balance");
                c.balance = b != null ? b.intValue() : 0;
                l.onFound(c);
            })
            .addOnFailureListener(e -> l.onNotFound());
    }

    public interface SeedLookup {
        void onFound(CitizenItem c);
        void onNotFound();
    }

    // حفظ بصمة الكلمات السرية عند التسجيل
    public void saveSeedHash(String nationalId, String seedHash) {
        db.collection("citizens").document(nationalId).update("seedHash", seedHash);
    }

    // ==================== المحكمة ====================
    public static class CourtCase {
        public String id;
        public String plaintiffId;
        public String plaintiffName;
        public String defendantId;
        public String defendantName;
        public String claim;
        public String recommendation;
        public String status; // open, agreed, appealed, closed
        public long timestamp;
    }

    public interface CasesListener { void onCases(java.util.List<CourtCase> list); }

    public void fileCase(String plaintiffId, String plaintiffName,
                         String defendantId, String defendantName,
                         String claim, String recommendation, OnDone cb) {
        Map<String, Object> c = new HashMap<>();
        c.put("plaintiffId", plaintiffId);
        c.put("plaintiffName", plaintiffName);
        c.put("defendantId", defendantId);
        c.put("defendantName", defendantName);
        c.put("claim", claim);
        c.put("recommendation", recommendation);
        c.put("status", "open");
        c.put("timestamp", System.currentTimeMillis());

        db.collection("court_cases").add(c)
            .addOnSuccessListener(doc -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public ListenerRegistration listenCases(CasesListener l) {
        return db.collection("court_cases")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener((snap, e) -> {
                if (snap == null) return;
                java.util.List<CourtCase> list = new java.util.ArrayList<>();
                for (com.google.firebase.firestore.QueryDocumentSnapshot d : snap) {
                    CourtCase c = new CourtCase();
                    c.id = d.getId();
                    c.plaintiffId = d.getString("plaintiffId");
                    c.plaintiffName = d.getString("plaintiffName");
                    c.defendantId = d.getString("defendantId");
                    c.defendantName = d.getString("defendantName");
                    c.claim = d.getString("claim");
                    c.recommendation = d.getString("recommendation");
                    c.status = d.getString("status");
                    Long t = d.getLong("timestamp");
                    c.timestamp = t != null ? t : 0;
                    list.add(c);
                }
                l.onCases(list);
            });
    }

    public void updateCaseStatus(String caseId, String status, OnDone cb) {
        db.collection("court_cases").document(caseId)
            .update("status", status)
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ==================== الهدايا ====================



    public void sendGift(String fromId, String fromName, String toId,
                         String emoji, String giftName, String meaning, int price, OnDone cb) {
        db.collection("citizens").document(fromId).get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) { cb.onError("المرسل غير موجود"); return; }
                Long bal = doc.getLong("balance");
                int cur = bal != null ? bal.intValue() : 0;
                if (cur < price) { cb.onError("رصيدك غير كافٍ"); return; }

                Map<String, Object> gift = new HashMap<>();
                gift.put("fromId", fromId);
                gift.put("fromName", fromName);
                gift.put("toId", toId);
                gift.put("emoji", emoji);
                gift.put("giftName", giftName);
                gift.put("meaning", meaning);
                gift.put("price", price);
                gift.put("timestamp", System.currentTimeMillis());

                db.collection("gifts").add(gift)
                    .addOnSuccessListener(x -> {
                        db.collection("citizens").document(fromId)
                            .update("balance", com.google.firebase.firestore.FieldValue.increment(-price));
                        cb.onSuccess();
                    })
                    .addOnFailureListener(e -> cb.onError(e.getMessage()));
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public static class GiftEntry {
        public String fromId, fromName, toId, emoji, giftName, meaning;
        public int price;
        public long timestamp;
    }

    public interface GiftEntriesListener { void onGifts(java.util.List<GiftEntry> list); }

    public ListenerRegistration listenReceivedGifts(String nationalId, GiftEntriesListener l) {
        return db.collection("gifts")
            .whereEqualTo("toId", nationalId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener((snap, e) -> {
                if (snap == null) return;
                java.util.List<GiftEntry> list = new java.util.ArrayList<>();
                for (com.google.firebase.firestore.QueryDocumentSnapshot d : snap) {
                    GiftEntry g = new GiftEntry();
                    g.fromId = d.getString("fromId");
                    g.fromName = d.getString("fromName");
                    g.toId = d.getString("toId");
                    g.emoji = d.getString("emoji");
                    g.giftName = d.getString("giftName");
                    g.meaning = d.getString("meaning");
                    Long p = d.getLong("price");
                    g.price = p != null ? p.intValue() : 0;
                    Long t = d.getLong("timestamp");
                    g.timestamp = t != null ? t : 0;
                    list.add(g);
                }
                l.onGifts(list);
            });
    }

    public ListenerRegistration listenSentGifts(String nationalId, GiftEntriesListener l) {
        return db.collection("gifts")
            .whereEqualTo("fromId", nationalId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener((snap, e) -> {
                if (snap == null) return;
                java.util.List<GiftEntry> list = new java.util.ArrayList<>();
                for (com.google.firebase.firestore.QueryDocumentSnapshot d : snap) {
                    GiftEntry g = new GiftEntry();
                    g.fromId = d.getString("fromId");
                    g.fromName = d.getString("fromName");
                    g.toId = d.getString("toId");
                    g.emoji = d.getString("emoji");
                    g.giftName = d.getString("giftName");
                    g.meaning = d.getString("meaning");
                    Long p = d.getLong("price");
                    g.price = p != null ? p.intValue() : 0;
                    Long t = d.getLong("timestamp");
                    g.timestamp = t != null ? t : 0;
                    list.add(g);
                }
                l.onGifts(list);
            });
    }
}
