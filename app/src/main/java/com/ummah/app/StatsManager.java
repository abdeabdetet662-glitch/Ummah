package com.ummah.app;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * StatsManager — إدارة إحصائيات المستخدم
 *
 * Firestore:
 *   users_stats/{uid}
 *     ├── totalVisits: number
 *     ├── firstVisit: timestamp
 *     ├── lastVisit: timestamp
 *     ├── totalBalanceEarned: number
 *     └── daysActive: number
 *
 *   users_stats/{uid}/daily/{YYYY-MM-DD}
 *     └── visits: number
 *
 *   users_stats/{uid}/activities/{activityId}
 *     ├── type: "visit" | "transfer" | "gift" | "purchase"
 *     ├── description: string
 *     ├── amount: number
 *     └── timestamp: number
 */
public class StatsManager {

    private static final String TAG = "Stats";
    private static final String COL = "users_stats";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface StatsCallback {
        void onResult(UserStats stats);
        void onError(String e);
    }

    public interface ActivitiesCallback {
        void onResult(List<ActivityItem> items);
    }

    public interface ChartCallback {
        void onResult(List<ChartPoint> points);
    }

    // ═══ Models ═══
    public static class UserStats {
        public long totalVisits = 0;
        public long daysActive = 0;
        public long totalBalanceEarned = 0;
        public long firstVisit = 0;
        public long lastVisit = 0;
    }

    public static class ActivityItem {
        public String id;
        public String type;
        public String description;
        public long amount;
        public long timestamp;
    }

    public static class ChartPoint {
        public String date;
        public long balance;
    }

    // ═══ 1. تسجيل الزيارة ═══
    public void recordVisit(String uid, long currentBalance) {
        if (uid == null || uid.isEmpty()) return;

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(new Date());
        long now = System.currentTimeMillis();

        // ═══ نحدث users_stats ═══
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalVisits", FieldValue.increment(1));
        stats.put("lastVisit", now);

        db.collection(COL).document(uid)
            .get()
            .addOnSuccessListener(doc -> {
                Map<String, Object> upd = new HashMap<>(stats);

                if (!doc.exists()) {
                    upd.put("firstVisit", now);
                    upd.put("daysActive", 1);
                    upd.put("totalBalanceEarned", 0);
                }

                db.collection(COL).document(uid)
                    .set(upd, com.google.firebase.firestore.SetOptions.merge());

                // ═══ نحدّثو daily ═══
                db.collection(COL).document(uid).collection("daily")
                    .document(today)
                    .set(java.util.Collections.singletonMap("visits", FieldValue.increment(1)),
                         com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener(v -> {
                        // نحدّثو daysActive (نحسبو عدد الـ daily docs)
                        db.collection(COL).document(uid).collection("daily").get()
                            .addOnSuccessListener(snap -> {
                                db.collection(COL).document(uid)
                                    .update("daysActive", snap.size());
                            });
                    });

                // ═══ نسجّلو الرصيد الحالي ═══
                db.collection(COL).document(uid).collection("balance_history")
                    .document(today)
                    .set(java.util.Collections.singletonMap("balance", currentBalance),
                         com.google.firebase.firestore.SetOptions.merge());

                // ═══ نسجّلو نشاط ═══
                logActivity(uid, "visit", "دخول للتطبيق", 0);

                Log.d(TAG, "✅ Visit recorded");
            });
    }

    // ═══ 2. تسجيل نشاط ═══
    public void logActivity(String uid, String type, String description, long amount) {
        if (uid == null) return;

        Map<String, Object> item = new HashMap<>();
        item.put("type", type);
        item.put("description", description);
        item.put("amount", amount);
        item.put("timestamp", System.currentTimeMillis());

        db.collection(COL).document(uid).collection("activities")
            .add(item);
    }

    // ═══ 3. جلب الإحصائيات ═══
    public void fetchStats(String uid, StatsCallback cb) {
        if (uid == null) { cb.onError("uid فارغ"); return; }

        db.collection(COL).document(uid).get()
            .addOnSuccessListener(doc -> {
                UserStats s = new UserStats();
                if (doc.exists()) {
                    Long tv = doc.getLong("totalVisits");
                    Long da = doc.getLong("daysActive");
                    Long tbe = doc.getLong("totalBalanceEarned");
                    Long fv = doc.getLong("firstVisit");
                    Long lv = doc.getLong("lastVisit");

                    s.totalVisits = tv != null ? tv : 0;
                    s.daysActive = da != null ? da : 0;
                    s.totalBalanceEarned = tbe != null ? tbe : 0;
                    s.firstVisit = fv != null ? fv : 0;
                    s.lastVisit = lv != null ? lv : 0;
                }
                cb.onResult(s);
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ═══ 4. جلب آخر النشاطات ═══
    public void fetchActivities(String uid, int limit, ActivitiesCallback cb) {
        if (uid == null) return;

        db.collection(COL).document(uid).collection("activities")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .addOnSuccessListener(snap -> {
                List<ActivityItem> list = new ArrayList<>();
                for (DocumentSnapshot d : snap.getDocuments()) {
                    ActivityItem a = new ActivityItem();
                    a.id = d.getId();
                    a.type = d.getString("type");
                    a.description = d.getString("description");
                    Long amt = d.getLong("amount");
                    a.amount = amt != null ? amt : 0;
                    Long ts = d.getLong("timestamp");
                    a.timestamp = ts != null ? ts : 0;
                    list.add(a);
                }
                cb.onResult(list);
            });
    }

    // ═══ 5. جلب بيانات الرسم البياني (7 أيام) ═══
    public void fetchBalanceHistory(String uid, ChartCallback cb) {
        if (uid == null) return;

        // نجيبو آخر 7 أيام
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        long weekAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000;
        String weekAgoStr = sdf.format(new Date(weekAgo));

        db.collection(COL).document(uid).collection("balance_history")
            .orderBy(com.google.firebase.firestore.FieldPath.documentId())
            .whereGreaterThanOrEqualTo(
                com.google.firebase.firestore.FieldPath.documentId(), weekAgoStr)
            .get()
            .addOnSuccessListener(snap -> {
                List<ChartPoint> points = new ArrayList<>();
                for (DocumentSnapshot d : snap.getDocuments()) {
                    ChartPoint p = new ChartPoint();
                    p.date = d.getId();
                    Long b = d.getLong("balance");
                    p.balance = b != null ? b : 0;
                    points.add(p);
                }
                cb.onResult(points);
            })
            .addOnFailureListener(e -> cb.onResult(new ArrayList<>()));
    }
}
