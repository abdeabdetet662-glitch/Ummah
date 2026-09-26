package com.ummah.admin;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminManager {

    private static AdminManager instance;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public static final String ADMIN_PASSWORD = "ummah2026"; // غيّرها

    private AdminManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static AdminManager get() {
        if (instance == null) instance = new AdminManager();
        return instance;
    }

    public interface OnDone { void onSuccess(); void onError(String msg); }

    public void signIn(final OnDone cb) {
        if (auth.getCurrentUser() != null) { cb.onSuccess(); return; }
        auth.signInAnonymously()
            .addOnSuccessListener(r -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ============ المواطنون ============
    public static class Citizen {
        public String nationalId, name, joinDate, country;
        public int balance;
    }

    public interface CitizensListener { void onList(List<Citizen> list); }

    public ListenerRegistration listenAllCitizens(final CitizensListener l) {
        return db.collection("citizens").addSnapshotListener((snap, e) -> {
            if (snap == null) return;
            List<Citizen> list = new ArrayList<>();
            for (QueryDocumentSnapshot d : snap) {
                Citizen c = new Citizen();
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

    public void setBalance(String nationalId, int newBalance, OnDone cb) {
        db.collection("citizens").document(nationalId)
            .update("balance", newBalance)
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void addBalance(String nationalId, int amount, OnDone cb) {
        db.collection("citizens").document(nationalId)
            .update("balance", FieldValue.increment(amount))
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void banCitizen(String nationalId, boolean banned, OnDone cb) {
        db.collection("citizens").document(nationalId)
            .update("banned", banned)
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ============ الهدايا ============
    public void sendGift(String fromName, String toId, String emoji, String giftName, String meaning, int price, OnDone cb) {
        Map<String, Object> gift = new HashMap<>();
        gift.put("fromId", "ADMIN");
        gift.put("fromName", "الإدارة - " + fromName);
        gift.put("toId", toId);
        gift.put("emoji", emoji);
        gift.put("giftName", giftName);
        gift.put("meaning", meaning);
        gift.put("price", price);
        gift.put("timestamp", System.currentTimeMillis());

        db.collection("gifts").add(gift)
            .addOnSuccessListener(x -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ============ الإذاعة ============
    public void broadcast(String message, OnDone cb) {
        Map<String, Object> n = new HashMap<>();
        n.put("author", "📢 الإدارة");
        n.put("content", message);
        n.put("timestamp", System.currentTimeMillis());
        n.put("isBroadcast", true);

        db.collection("news").add(n)
            .addOnSuccessListener(d -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ============ الإحصائيات ============
    public interface StatsListener {
        void onStats(int citizens, int gifts, int transfers, int news, int proposals, int complaints, long treasury, int totalVotes);
    }

    public void loadStats(final StatsListener l) {
        final int[] counts = new int[6];
        final long[] treasuryVal = new long[1];

        db.collection("citizens").get().addOnSuccessListener(q -> {
            counts[0] = q.size();
            db.collection("gifts").get().addOnSuccessListener(q2 -> {
                counts[1] = q2.size();
                db.collection("transfers").get().addOnSuccessListener(q3 -> {
                    counts[2] = q3.size();
                    db.collection("news").get().addOnSuccessListener(q4 -> {
                        counts[3] = q4.size();
                        db.collection("proposals").get().addOnSuccessListener(q5 -> {
                            counts[4] = q5.size();
                            db.collection("court_cases").get().addOnSuccessListener(q6 -> {
                                counts[5] = q6.size();
                                db.collection("treasury").document("main").get()
                                    .addOnSuccessListener(doc -> {
                                        Long tb = doc.getLong("balance");
                                        treasuryVal[0] = tb != null ? tb : 0;
                                        db.collection("constitution_votes").get()
                                            .addOnSuccessListener(q7 -> {
                                                l.onStats(counts[0], counts[1], counts[2],
                                                        counts[3], counts[4], counts[5],
                                                        treasuryVal[0], q7.size());
                                            });
                                    });
                            });
                        });
                    });
                });
            });
        });
    }

    // ============ الشكاوى (المحكمة) ============
    public static class Complaint {
        public String id, plaintiffName, defendantName, claim, recommendation, status;
        public long timestamp;
    }

    public interface ComplaintsListener { void onList(List<Complaint> list); }

    public ListenerRegistration listenComplaints(final ComplaintsListener l) {
        return db.collection("court_cases").addSnapshotListener((snap, e) -> {
            if (snap == null) return;
            List<Complaint> list = new ArrayList<>();
            for (QueryDocumentSnapshot d : snap) {
                Complaint c = new Complaint();
                c.id = d.getId();
                c.plaintiffName = d.getString("plaintiffName");
                c.defendantName = d.getString("defendantName");
                c.claim = d.getString("claim");
                c.recommendation = d.getString("recommendation");
                c.status = d.getString("status");
                Long t = d.getLong("timestamp");
                c.timestamp = t != null ? t : 0;
                list.add(c);
            }
            java.util.Collections.sort(list, (a, b) -> Long.compare(b.timestamp, a.timestamp));
            l.onList(list);
        });
    }

    public void updateComplaintStatus(String caseId, String status, OnDone cb) {
        db.collection("court_cases").document(caseId)
            .update("status", status)
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void deleteComplaint(String caseId, OnDone cb) {
        db.collection("court_cases").document(caseId)
            .delete()
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ============ التصويت على الدستور ============
    public interface VotesListener { void onList(List<Map<String, Object>> list); }

    public ListenerRegistration listenConstitutionVotes(final VotesListener l) {
        return db.collection("constitution_votes").addSnapshotListener((snap, e) -> {
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

    public void deleteConstitutionVote(String docId, OnDone cb) {
        db.collection("constitution_votes").document(docId).delete()
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void addConstitutionVote(String nationalId, int article, boolean yes, OnDone cb) {
        Map<String, Object> v = new HashMap<>();
        v.put("nationalId", nationalId);
        v.put("article", article);
        v.put("yes", yes);
        v.put("timestamp", System.currentTimeMillis());
        v.put("adminAdded", true);

        db.collection("constitution_votes").document("admin_" + System.currentTimeMillis() + "_" + article).set(v)
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ============ الاقتراحات ============
    public interface ProposalsListener { void onList(List<Map<String, Object>> list); }

    public ListenerRegistration listenProposals(final ProposalsListener l) {
        return db.collection("proposals").addSnapshotListener((snap, e) -> {
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

    public void deleteProposal(String id, OnDone cb) {
        db.collection("proposals").document(id).delete()
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void setProposalVotes(String id, int yes, int no, OnDone cb) {
        Map<String, Object> u = new HashMap<>();
        u.put("yes", yes);
        u.put("no", no);
        db.collection("proposals").document(id).update(u)
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ============ الدردشة ============
    public interface ChatsListener { void onList(List<Map<String, Object>> list); }

    public ListenerRegistration listenGlobalChat(final ChatsListener l) {
        return db.collection("global_chat")
            .limit(200)
            .addSnapshotListener((snap, e) -> {
                if (snap == null) return;
                List<Map<String, Object>> list = new ArrayList<>();
                for (QueryDocumentSnapshot d : snap) {
                    Map<String, Object> m = d.getData();
                    m.put("_id", d.getId());
                    list.add(m);
                }
                java.util.Collections.sort(list, (a, b) -> {
                    Long ta = (Long) a.get("timestamp");
                    Long tb = (Long) b.get("timestamp");
                    if (ta == null || tb == null) return 0;
                    return Long.compare(tb, ta);
                });
                l.onList(list);
            });
    }

    public void deleteChatMessage(String id, OnDone cb) {
        db.collection("global_chat").document(id).delete()
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ============ الخزينة ============
    public void setTreasury(long newBalance, OnDone cb) {
        Map<String, Object> data = new HashMap<>();
        data.put("balance", newBalance);
        db.collection("treasury").document("main").set(data)
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void addTreasury(long amount, OnDone cb) {
        db.collection("treasury").document("main")
            .update("balance", FieldValue.increment(amount))
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ============ الانتخابات ============
    public interface CandidatesListener { void onList(List<Map<String, Object>> list); }

    public ListenerRegistration listenCandidates(final CandidatesListener l) {
        return db.collection("candidates").addSnapshotListener((snap, e) -> {
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

    public void setCandidateVotes(String id, int votes, OnDone cb) {
        db.collection("candidates").document(id).update("votes", votes)
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void deleteCandidate(String id, OnDone cb) {
        db.collection("candidates").document(id).delete()
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void resetElection(OnDone cb) {
        db.collection("votes").get().addOnSuccessListener(q -> {
            for (QueryDocumentSnapshot d : q) d.getReference().delete();
            db.collection("candidates").get().addOnSuccessListener(q2 -> {
                for (QueryDocumentSnapshot d : q2) {
                    d.getReference().update("votes", 0);
                }
                cb.onSuccess();
            });
        });
    }
}
