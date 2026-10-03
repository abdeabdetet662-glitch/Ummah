package com.ummah.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * NotificationHelper — إشعارات push في شريط الهاتف
 */
public class NotificationHelper {

    private static final String CHANNEL_DEFAULT = "ummah_default";
    private static final String CHANNEL_GIFT = "ummah_gift";
    private static final String CHANNEL_ADMIN = "ummah_admin";
    private static final String CHANNEL_NEWS = "ummah_news";

    /** إنشاء كل القنوات (Android 8+) */
    public static void createChannels(Context ctx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager nm = ctx.getSystemService(NotificationManager.class);
        if (nm == null) return;

        // 🎁 قناة الهدايا
        NotificationChannel gift = new NotificationChannel(
                CHANNEL_GIFT, "🎁 الهدايا والمكافآت", NotificationManager.IMPORTANCE_HIGH);
        gift.setDescription("إشعارات الهدايا والمكافآت والجوائز");
        gift.enableLights(true);
        gift.setLightColor(Color.parseColor("#D4AF37"));
        gift.enableVibration(true);
        gift.setShowBadge(true);
        nm.createNotificationChannel(gift);

        // 👑 قناة الإدارة
        NotificationChannel admin = new NotificationChannel(
                CHANNEL_ADMIN, "👑 رسائل الإدارة", NotificationManager.IMPORTANCE_HIGH);
        admin.setDescription("رسائل رسمية من إدارة أُمّة");
        admin.enableLights(true);
        admin.setLightColor(Color.parseColor("#10B981"));
        admin.enableVibration(true);
        admin.setShowBadge(true);
        nm.createNotificationChannel(admin);

        // 📰 قناة الأخبار
        NotificationChannel news = new NotificationChannel(
                CHANNEL_NEWS, "📰 الأخبار والأحداث", NotificationManager.IMPORTANCE_DEFAULT);
        news.setDescription("آخر أخبار وأحداث أُمّة");
        news.enableLights(true);
        news.setShowBadge(true);
        nm.createNotificationChannel(news);

        // 🔔 قناة عامة
        NotificationChannel def = new NotificationChannel(
                CHANNEL_DEFAULT, "🔔 إشعارات عامة", NotificationManager.IMPORTANCE_DEFAULT);
        def.setDescription("إشعارات عامة من أُمّة");
        nm.createNotificationChannel(def);
    }

    /** عرض إشعار في شريط الهاتف */
    public static void show(Context ctx, String title, String body, String type, int id) {
        show(ctx, title, body, type, id, null);
    }

    public static void show(Context ctx, String title, String body, String type, int id, String docId) {
        // اختيار القناة حسب النوع
        String channelId;
        int color;
        String emoji;

        switch (type == null ? "" : type) {
            case "gift":
                channelId = CHANNEL_GIFT;
                color = Color.parseColor("#D4AF37");
                emoji = "🎁";
                break;
            case "admin":
                channelId = CHANNEL_ADMIN;
                color = Color.parseColor("#10B981");
                emoji = "👑";
                break;
            case "news":
                channelId = CHANNEL_NEWS;
                color = Color.parseColor("#3B82F6");
                emoji = "📰";
                break;
            case "code":
                channelId = CHANNEL_GIFT;
                color = Color.parseColor("#F59E0B");
                emoji = "🎫";
                break;
            default:
                channelId = CHANNEL_DEFAULT;
                color = Color.parseColor("#D4AF37");
                emoji = "🔔";
        }

        // الـ Intent عند الضغط
        Intent intent = new Intent(ctx, NotificationCenterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (docId != null) {
            intent.putExtra("notif_id", docId);
            intent.putExtra("notif_type", type);
        }

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pi = PendingIntent.getActivity(
                ctx, id, intent, flags);

        // الصوت الافتراضي
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // البناء
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(emoji + "  " + (title == null ? "أُمّة" : title))
                .setContentText(body == null ? "" : body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body == null ? "" : body))
                .setColor(color)
                .setColorized(true)
                .setAutoCancel(true)
                .setSound(sound)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(pi)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true);

        // إضافة زر "فتح"
        Intent openIntent = new Intent(ctx, NotificationCenterActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent openPi = PendingIntent.getActivity(
                ctx, id + 1000, openIntent, flags);

        builder.addAction(R.drawable.ic_launcher_foreground, "افتح", openPi);

        // عرض الإشعار
        try {
            NotificationManagerCompat nm = NotificationManagerCompat.from(ctx);
            nm.notify(id, builder.build());
        } catch (SecurityException e) {
            // المستخدم ما عطاش الإذن (Android 13+)
            e.printStackTrace();
        }
    }

    /** إشعار اختباري */
    public static void test(Context ctx) {
        show(ctx, "اختبار", "هاد إشعار تجريبي من أُمّة 🎉", "default", 9999);
    }
}
