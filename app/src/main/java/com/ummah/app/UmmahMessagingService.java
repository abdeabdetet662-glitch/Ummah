package com.ummah.app;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class UmmahMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage msg) {
        String title = "أُمّة";
        String body = "";
        if (msg.getNotification() != null) {
            if (msg.getNotification().getTitle() != null)
                title = msg.getNotification().getTitle();
            if (msg.getNotification().getBody() != null)
                body = msg.getNotification().getBody();
        }
        NotificationHelper.showBadgeNotification(
                this, 1001, title, body, 1);
    }

    @Override
    public void onNewToken(String token) {
        // نحفظ التوكن في Firestore (اختياري للمستقبل)
    }
}
