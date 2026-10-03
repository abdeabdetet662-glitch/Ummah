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
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashSet;
import java.util.Set;

/**
 * NotificationListener — يستمع لـ notifications/{doc} في Firestore
 * ويعرضها كإشعارات push في شريط الهاتف
 *
 * البنية الفعلية:
 *   target:    [الرقم الوطني للمستخدم]
 *   title:     "عنوان"
 *   message:   "النص"
 *   type:      "reward" / "info" / "gift" / "code" / "admin"
 *   timestamp: (int64 ms)
 *   from:      "Admin"
 *   emoji:     "🎁"
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

    public void start(final String uid) {
        if (uid == null || uid.isEmpty()) {
            Log.w(TAG, "⚠️ UID فارغ");
            return;
        }

        Log.d(TAG, "🎧 بدينا الاستماع لـ target=" + uid);

        // الكويري: target == uid (بدون composite index)
        reg = FirebaseFirestore.getInstance()
                .collection("notifications")
                .whereEqualTo("target", uid)
                .limit(30)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "❌ خطأ: " + e.getMessage(), e);
                            return;
                        }
                        if (snapshots == null) {
                            Log.w(TAG, "⚠️ snapshots = null");
                            return;
                        }

                        Log.d(TAG, "📬 وصلنا snapshot: " + snapshots.size() + " إشعارات");

                        boolean isFirst = firstLoad;
                        firstLoad = false;

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() != DocumentChange.Type.ADDED) continue;

                            String id = dc.getDocument().getId();

                            // تجاهل الإشعارات القديمة عند أول تحميل
                            if (seenIds.contains(id)) continue;
                            seenIds.add(id);
                            saveSeen();

                            // ═══ استخراج البيانات ═══
                            String title = dc.getDocument().getString("title");
                            String message = dc.getDocument().getString("message");
                            String type = dc.getDocument().getString("type");
                            String from = dc.getDocument().getString("from");
                            String emoji = dc.getDocument().getString("emoji");

                            // إذا أول تحميل، ما نعرضش إشعارات قديمة
                            if (isFirst) {
                                Log.d(TAG, "⏭️ تجاهل إشعار قديم: " + id);
                                continue;
                            }

                            // تنظيف
                            if (title == null || title.isEmpty()) title = "أُمّة";
                            if (message == null) message = "";
                            if (type == null) type = "info";
                            if (emoji == null || emoji.isEmpty()) {
                                switch (type) {
                                    case "gift":   emoji = "🎁"; break;
                                    case "reward": emoji = "🎁"; break;
                                    case "code":   emoji = "🎫"; break;
                                    case "admin":  emoji = "👑"; break;
                                    case "news":   emoji = "📰"; break;
                                    default:       emoji = "🔔";
                                }
                            }

                            // إضافة الـ emoji للعنوان
                            String fullTitle = emoji + "  " + title;

                            // عرض الإشعار
                            int notifId = Math.abs(id.hashCode() % 10000);
                            NotificationHelper.show(ctx, fullTitle, message, type, notifId, id);

                            Log.d(TAG, "🔔 عُرض إشعار: " + fullTitle + " | " + message);
                        }
                    }
                });
    }

    private void saveSeen() {
        // نحفظو فقط آخر 200
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
