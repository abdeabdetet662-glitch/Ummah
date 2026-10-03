package com.ummah.app;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

/**
 * UmmahMessagingService — استقبال FCM Push من Firebase
 */
public class UmmahMessagingService extends FirebaseMessagingService {

    private static final String TAG = "UmmahFCM";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        Log.d(TAG, "📨 استقبلنا رسالة من: " + message.getFrom());

        String title = null;
        String body = null;
        String type = "default";

        // من data payload
        Map<String, String> data = message.getData();
        if (data != null) {
            title = data.get("title");
            body = data.get("body");
            String t = data.get("type");
            if (t != null) type = t;
        }

        // من notification payload
        if (message.getNotification() != null) {
            if (title == null) title = message.getNotification().getTitle();
            if (body == null) body = message.getNotification().getBody();
        }

        // عرض الإشعار
        if (title != null || body != null) {
            int id = new Random().nextInt(9999);
            NotificationHelper.show(
                    getApplicationContext(),
                    title != null ? title : "أُمّة",
                    body != null ? body : "",
                    type,
                    id
            );
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "🔑 Token جديد: " + token);

        // حفظ التوكن في Firestore
        try {
            IdentityManager im = new IdentityManager(getApplicationContext());
            if (im.isCitizen() && im.getCitizen() != null) {
                String uid = im.getCitizen().nationalId;
                if (uid != null) {
                    com.google.firebase.firestore.FirebaseFirestore
                            .getInstance()
                            .collection("fcm_tokens")
                            .document(uid)
                            .set(java.util.Collections.singletonMap("token", token))
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "✅ Token محفوظ"))
                            .addOnFailureListener(e -> Log.e(TAG, "❌ فشل حفظ Token", e));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "خطأ في حفظ Token", e);
        }
    }
}
