package com.ummah.app;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * MurderMysteryManager — إدارة لعبة جريمة أُمّة
 */
public class MurderMysteryManager {

    private static final String TAG = "MurderMystery";
    private static final String COLLECTION = "murder_mysteries";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ═══ Callbacks ═══
    public interface MysteryCallback {
        void onResult(MurderMystery mystery);
        void onError(String error);
    }

    public interface PlayersCallback {
        void onResult(List<MMPlayer> players);
        void onError(String error);
    }

    public interface CluesCallback {
        void onResult(List<MMClue> clues);
        void onError(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String error);
    }

    // ═══ جلب الجلسة الحالية ═══
    public void getCurrentMystery(MysteryCallback cb) {
        db.collection(COLLECTION)
            .whereIn("status", java.util.Arrays.asList("registration", "playing", "voting"))
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener(snap -> {
                if (snap.isEmpty()) {
                    cb.onResult(null);
                } else {
                    MurderMystery m = snap.getDocuments().get(0)
                        .toObject(MurderMystery.class);
                    if (m != null) m.id = snap.getDocuments().get(0).getId();
                    cb.onResult(m);
                }
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ═══ التسجيل في اللعبة ═══
    public void register(String gameId, Citizen citizen, SimpleCallback cb) {
        if (citizen == null) {
            cb.onError("مواطن غير موجود");
            return;
        }

        db.collection(COLLECTION).document(gameId).get()
            .addOnSuccessListener(gameDoc -> {
                if (!gameDoc.exists()) {
                    cb.onError("الجلسة ماكانتش");
                    return;
                }

                MurderMystery game = gameDoc.toObject(MurderMystery.class);
                if (game == null) { cb.onError("خطأ"); return; }

                if (!"registration".equals(game.status)) {
                    cb.onError("التسجيل مغلق");
                    return;
                }

                int fee = game.entryFee;
                if (citizen.balance < fee) {
                    cb.onError("ما عندكش " + fee + " Đ");
                    return;
                }

                // خصم الرسوم
                WalletManager wm = new WalletManager();
                wm.transferToTreasury(citizen.nationalId, fee, "اشتراك جريمة", null);

                // إضافة اللاعب
                Map<String, Object> player = new HashMap<>();
                player.put("userId", citizen.nationalId);
                player.put("userName", citizen.name);
                player.put("role", "investigator"); // مؤقتاً
                player.put("character", "مواطن");
                player.put("characterDesc", "في انتظار القصة...");
                player.put("avatarEmoji", "👤");
                player.put("joinedAt", System.currentTimeMillis());
                player.put("status", "active");
                player.put("suspicion", 0);

                db.collection(COLLECTION).document(gameId)
                    .collection("players").document(citizen.nationalId)
                    .set(player)
                    .addOnSuccessListener(v -> {
                        // زيادة العداد
                        db.collection(COLLECTION).document(gameId)
                            .update("currentPlayers",
                                com.google.firebase.firestore.FieldValue.increment(1))
                            .addOnSuccessListener(x -> cb.onSuccess())
                            .addOnFailureListener(e -> cb.onError(e.getMessage()));
                    })
                    .addOnFailureListener(e -> cb.onError(e.getMessage()));
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ═══ جلب اللاعبين ═══
    public ListenerRegistration listenPlayers(String gameId, PlayersCallback cb) {
        return db.collection(COLLECTION).document(gameId)
            .collection("players")
            .addSnapshotListener((snap, e) -> {
                if (e != null) { cb.onError(e.getMessage()); return; }
                if (snap == null) return;

                List<MMPlayer> list = new ArrayList<>();
                for (DocumentSnapshot doc : snap.getDocuments()) {
                    MMPlayer p = doc.toObject(MMPlayer.class);
                    if (p != null) list.add(p);
                }
                cb.onResult(list);
            });
    }

    // ═══ جلب الأدلة ═══
    public ListenerRegistration listenClues(String gameId, CluesCallback cb) {
        return db.collection(COLLECTION).document(gameId)
            .collection("clues")
            .orderBy("order", Query.Direction.ASCENDING)
            .addSnapshotListener((snap, e) -> {
                if (e != null) { cb.onError(e.getMessage()); return; }
                if (snap == null) return;

                List<MMClue> list = new ArrayList<>();
                for (DocumentSnapshot doc : snap.getDocuments()) {
                    MMClue c = doc.toObject(MMClue.class);
                    if (c != null) {
                        c.id = doc.getId();
                        // نعرضو فقط الأدلة العامة أو الخاصة بنا
                        list.add(c);
                    }
                }
                cb.onResult(list);
            });
    }

    // ═══ اختيار القاتل ═══
    public void chooseKiller(String gameId, SimpleCallback cb) {
        db.collection(COLLECTION).document(gameId).get()
            .addOnSuccessListener(gameDoc -> {
                db.collection(COLLECTION).document(gameId)
                    .collection("players").get()
                    .addOnSuccessListener(playersSnap -> {
                        List<String> ids = new ArrayList<>();
                        for (DocumentSnapshot d : playersSnap.getDocuments()) {
                            ids.add(d.getId());
                        }

                        if (ids.isEmpty()) {
                            cb.onError("ما فيه لاعبين");
                            return;
                        }

                        // اختيار عشوائي
                        Collections.shuffle(ids);
                        String killerId = ids.get(0);

                        // تعيين القاتل
                        db.collection(COLLECTION).document(gameId)
                            .collection("players").document(killerId)
                            .update("role", "killer");

                        // تحديث الحالة
                        Map<String, Object> upd = new HashMap<>();
                        upd.put("killerId", killerId);
                        upd.put("status", "playing");
                        upd.put("startTime", System.currentTimeMillis());
                        upd.put("endTime", System.currentTimeMillis() + 24 * 60 * 60 * 1000L);

                        db.collection(COLLECTION).document(gameId).update(upd);

                        // إضافة الدليل الأول
                        MMClue clue1 = new MMClue();
                        clue1.title = "الجثة";
                        clue1.description = "تم العثور على الملك ميتاً في مكتبه";
                        clue1.icon = "💀";
                        clue1.revealedAt = System.currentTimeMillis();
                        clue1.revealsFor = "all";
                        clue1.isKeyClue = true;
                        clue1.order = 1;

                        db.collection(COLLECTION).document(gameId)
                            .collection("clues").add(clue1);

                        Log.d(TAG, "🎭 القاتل: " + killerId);
                        cb.onSuccess();
                    });
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ═══ التصويت ═══
    public void vote(String gameId, String fromUser, String toUser, SimpleCallback cb) {
        Map<String, Object> vote = new HashMap<>();
        vote.put("fromUser", fromUser);
        vote.put("toUser", toUser);
        vote.put("votedAt", System.currentTimeMillis());

        db.collection(COLLECTION).document(gameId)
            .collection("votes")
            .document(fromUser)  // واحد لكل مستخدم
            .set(vote)
            .addOnSuccessListener(v -> {
                // نحدّث اللاعب
                db.collection(COLLECTION).document(gameId)
                    .collection("players").document(fromUser)
                    .update("votedFor", toUser)
                    .addOnSuccessListener(x -> cb.onSuccess())
                    .addOnFailureListener(e -> cb.onError(e.getMessage()));
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ═══ إعلان النتيجة ═══
    public void announceResult(String gameId, SimpleCallback cb) {
        db.collection(COLLECTION).document(gameId).get()
            .addOnSuccessListener(gameDoc -> {
                MurderMystery game = gameDoc.toObject(MurderMystery.class);
                if (game == null || game.killerId == null) {
                    cb.onError("خطأ");
                    return;
                }

                db.collection(COLLECTION).document(gameId)
                    .collection("votes").get()
                    .addOnSuccessListener(votesSnap -> {
                        // نحسبو التصويتات
                        Map<String, Integer> counts = new HashMap<>();
                        for (DocumentSnapshot v : votesSnap.getDocuments()) {
                            String to = v.getString("toUser");
                            if (to != null) {
                                counts.put(to, counts.getOrDefault(to, 0) + 1);
                            }
                        }

                        // الأكثر تصويتاً
                        String mostVoted = null;
                        int max = 0;
                        for (Map.Entry<String, Integer> e : counts.entrySet()) {
                            if (e.getValue() > max) {
                                max = e.getValue();
                                mostVoted = e.getKey();
                            }
                        }

                        boolean caught = mostVoted != null && mostVoted.equals(game.killerId);
                        String winner = caught ? mostVoted : game.killerId;

                        Map<String, Object> upd = new HashMap<>();
                        upd.put("status", "ended");
                        upd.put("winnerId", winner);
                        upd.put("solved", caught);
                        upd.put("endTime", System.currentTimeMillis());

                        db.collection(COLLECTION).document(gameId).update(upd)
                            .addOnSuccessListener(x -> {
                                // نمنحو الجائزة
                                if (caught) {
                                    // الفائز = أكثر محقق صوّت صح
                                    distributePrize(gameId, winner, game.prizePool, cb);
                                } else {
                                    // القاتل يفوز
                                    distributePrize(gameId, winner, game.prizePool, cb);
                                }
                            });
                    });
            });
    }

    private void distributePrize(String gameId, String winnerId, int amount, SimpleCallback cb) {
        WalletManager wm = new WalletManager();
        wm.addBalance(winnerId, amount, "فوز في جريمة أُمّة", null);
        cb.onSuccess();
    }
}
