package com.ummah.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashSet;
import java.util.Set;

/**
 * NotificationListener — يستمع لإشعارات Firestore جديدة
 * ويعرضها كإشعارات push في شريط الهاتف
 */
public class NotificationListener {

    private static final String TAG = "UmmahNotif";
    private static final String PREFS = "notif_seen";
    private static final String KEY_SEEN = "seen_ids";

    private final Context ctx;
    private ListenerRegistration reg;
    private final Set<String> seenIds;
    private boolean firstLoad = true;

    public NotificationListener(Context ctx) {
        this.ctx = ctx.getApplicationContext();
        SharedPreferences prefs = this.ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        this.seenIds = new HashSet<>(prefs.getStringSet(KEY_SEEN, new HashSet<>()));
    }

    public void start(String uid) {
        if (uid == null || uid.isEmpty()) {
            Log.w(TAG, "⚠️ UID فارغ — ما نبداوش الاستماع");
            return;
        }

        Log.d(TAG, "🎧 بدينا الاستماع لإشعارات: " + uid);

        reg = FirebaseFirestore.getInstance()
                .collection("notifications")
                .whereEqualTo("userId", uid)
                .whereEqualTo("read", false)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(20)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "❌ خطأ في الاستماع", e);
                            return;
                        }
                        if (snapshots == null) return;

                        boolean isFirst = firstLoad;
                        firstLoad = false;

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() != DocumentChange.Type.ADDED) continue;

                            String id = dc.getDocument().getId();

                            // تجاهل الإشعارات القديمة عند أول تحميل
                            if (isFirst && seenIds.contains(id)) continue;
                            if (seenIds.contains(id)) continue;

                            seenIds.add(id);
                            saveSeen();

                            // استخراج البيانات
                            String title = dc.getDocument().getString("title");
                            String body = dc.getDocument().getString("body");
                            String type = dc.getDocument().getString("type");
                            if (type == null) type = "default";

                            // عرض الإشعار
                            int notifId = Math.abs(id.hashCode() % 10000);
                            NotificationHelper.show(ctx, title, body, type, notifId, id);

                            Log.d(TAG, "🔔 إشعار جديد: " + title);
                        }
                    }
                });
    }

    private void saveSeen() {
        // نحفظ فقط آخر 200
        if (seenIds.size() > 200) {
            Set<String> trimmed = new HashSet<>();
            int i = 0;
            for (String s : seenIds) {
                if (i++ > 100) break;
                trimmed.add(s);
            }
            seenIds.clear();
            seenIds.addAll(trimmed);
        }
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit().putStringSet(KEY_SEEN, seenIds).apply();
    }

    public void stop() {
        if (reg != null) {
            reg.remove();
            reg = null;
            Log.d(TAG, "🛑 وقفنا الاستماع");
        }
    }
}
