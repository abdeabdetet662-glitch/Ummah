package com.ummah.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * NotificationCenterActivity — مركز الإشعارات
 * 
 * يعرض:
 * - إشعارات مخصصة من الإدارة
 * - إشعارات جماعية (target = "ALL")
 * - إشعارات فردية (target = nationalId)
 */
public class NotificationCenterActivity extends Activity {

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

    private IdentityManager im;
    private FirebaseFirestore db;
    private LinearLayout notifContainer;
    private TextView emptyView, statsView;
    private ListenerRegistration notifReg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        // ═══ تصفير عداد الإشعارات + إخفاء Badge ═══
        try {
            IdentityManager imReset = new IdentityManager(this);
            if (imReset.isCitizen() && imReset.getCitizen() != null) {
                String uidReset = imReset.getCitizen().nationalId;
                if (uidReset != null && !uidReset.isEmpty()) {
                    // 1. تصفير في Firestore
                    NotificationListener.clearNotifCount(uidReset);
                    
                    // 2. إخفاء Badge أيقونة التطبيق
                    BadgeHelper.clearAppBadge(this);
                    
                    android.util.Log.d("NotifCenter", "✅ تم تصفير العداد");
                }
            }
        } catch (Exception e) {
            android.util.Log.e("NotifCenter", "خطأ في التصفير", e);
        }
        im = new IdentityManager(this);
        db = FirebaseFirestore.getInstance();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 50, 30, 60);
        scroll.addView(root);

        // ═══ Header ═══
        TextView icon = new TextView(this);
        icon.setText("🔔");
        icon.setTextSize(60);
        icon.setGravity(Gravity.CENTER);
        root.addView(icon);

        TextView title = new TextView(this);
        title.setText(R.string.notif_title);
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(26);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 20, 0, 10);
        root.addView(title);

        statsView = new TextView(this);
        statsView.setText(R.string.notif_loading);
        statsView.setTextColor(Color.parseColor("#9E9E9E"));
        statsView.setTextSize(12);
        statsView.setGravity(Gravity.CENTER);
        statsView.setPadding(0, 0, 0, 30);
        root.addView(statsView);

        // ═══ Container ═══
        notifContainer = new LinearLayout(this);
        notifContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(notifContainer);

        // ═══ Empty view ═══
        emptyView = new TextView(this);
        emptyView.setText(R.string.notif_empty);
        emptyView.setTextColor(Color.parseColor("#9E9E9E"));
        emptyView.setTextSize(16);
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setPadding(0, 80, 0, 0);
        emptyView.setVisibility(View.GONE);
        root.addView(emptyView);

        setContentView(scroll);

        // ═══ تحميل الإشعارات ═══
        loadNotifications();
    }

    // ═══════════════════════════════════════════
    //  تحميل الإشعارات
    // ═══════════════════════════════════════════
    private void loadNotifications() {
        Citizen me = im.getCitizen();
        if (me == null) {
            statsView.setText(R.string.notif_not_registered);
            emptyView.setVisibility(View.VISIBLE);
            return;
        }

        final String myId = me.nationalId;

        notifReg = db.collection("notifications")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener((snap, e) -> {
                if (snap == null) return;

                List<DocumentSnapshot> myNotifs = new ArrayList<>();
                for (DocumentSnapshot d : snap.getDocuments()) {
                    String target = d.getString("target");
                    // نعرضو فقط: للكل أو لي أنا
                    if ("ALL".equals(target) || myId.equals(target)) {
                        myNotifs.add(d);
                    }
                }

                // ترتيب تنازلي
                Collections.reverse(myNotifs);

                renderNotifications(myNotifs);
            });
    }

    private void renderNotifications(List<DocumentSnapshot> list) {
        notifContainer.removeAllViews();

        if (list.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            statsView.setText(R.string.notif_no_notifs);
            return;
        }

        emptyView.setVisibility(View.GONE);
        statsView.setText(getString(R.string.notif_count, list.size()));

        for (DocumentSnapshot d : list) {
            addNotificationCard(d);
        }
    }

    private void addNotificationCard(DocumentSnapshot d) {
        String title = d.getString("title");
        String message = d.getString("message");
        String type = d.getString("type");
        String action = d.getString("action");
        String emoji = d.getString("emoji");
        Long ts = d.getLong("timestamp");

        if (title == null) title = "إشعار";
        if (message == null) message = "";
        if (emoji == null) emoji = emojiFromType(type);

        // ═══ بطاقة ═══
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card);
        card.setPadding(35, 30, 35, 30);

        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardLp.setMargins(0, 10, 0, 10);
        card.setLayoutParams(cardLp);
        card.setElevation(8f);

        // ═══ شريط ملون على الجانب حسب النوع ═══
        int accentColor = colorFromType(type);

        // ═══ الصف الأول: Emoji + Title + Type badge ═══
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvEmoji = new TextView(this);
        tvEmoji.setText(emoji);
        tvEmoji.setTextSize(28);
        tvEmoji.setPadding(0, 0, 15, 0);
        row1.addView(tvEmoji);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setTextSize(17);
        tvTitle.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        tvTitle.setLayoutParams(titleLp);
        row1.addView(tvTitle);

        // Badge النوع
        TextView tvBadge = new TextView(this);
        tvBadge.setText(typeLabel(type));
        tvBadge.setTextColor(accentColor);
        tvBadge.setTextSize(10);
        tvBadge.setTypeface(null, Typeface.BOLD);
        tvBadge.setPadding(12, 6, 12, 6);
        tvBadge.setBackgroundColor(Color.parseColor("#1A1A1A"));
        row1.addView(tvBadge);

        card.addView(row1);

        // ═══ الرسالة ═══
        if (!message.isEmpty()) {
            TextView tvMsg = new TextView(this);
            tvMsg.setText(message);
            tvMsg.setTextColor(Color.parseColor("#CCCCCC"));
            tvMsg.setTextSize(14);
            tvMsg.setPadding(0, 14, 0, 0);
            tvMsg.setLineSpacing(4, 1.2f);
            card.addView(tvMsg);
        }

        // ═══ التاريخ ═══
        if (ts != null) {
            TextView tvDate = new TextView(this);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
            tvDate.setText("🕐 " + sdf.format(new Date(ts)));
            tvDate.setTextColor(Color.parseColor("#666666"));
            tvDate.setTextSize(10);
            tvDate.setPadding(0, 12, 0, 0);
            card.addView(tvDate);
        }

        // ═══ زر الإجراء (إذا موجود) ═══
        if (action != null && !action.equals("none") && !action.isEmpty()) {
            final String finalAction = action;
            TextView tvAction = new TextView(this);
            tvAction.setText(actionLabel(action) + " ←");
            tvAction.setTextColor(Color.parseColor("#D4AF37"));
            tvAction.setTextSize(13);
            tvAction.setTypeface(null, Typeface.BOLD);
            tvAction.setPadding(0, 15, 0, 0);
            tvAction.setClickable(true);
            tvAction.setOnClickListener(v -> handleAction(finalAction));
            card.addView(tvAction);
        }

        notifContainer.addView(card);
    }

    // ═══════════════════════════════════════════
    //  Handle Action
    // ═══════════════════════════════════════════
    private void handleAction(String action) {
        try {
            Intent intent = null;
            switch (action) {
                case "wallet":
                    intent = new Intent(this, WalletActivity.class);
                    break;
                case "wheel":
                    intent = new Intent(this, WheelActivity.class);
                    break;
                case "city":
                    intent = new Intent(this, CityMapActivity.class);
                    break;
                case "market":
                    intent = new Intent(this, MarketActivity.class);
                    break;
                case "election":
                    intent = new Intent(this, ElectionActivity.class);
                    break;
            }
            if (intent != null) startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, R.string.main_page_error, Toast.LENGTH_SHORT).show();
        }
    }

    // ═══════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════
    private String emojiFromType(String type) {
        if (type == null) return "ℹ️";
        switch (type) {
            case "warning": return "⚠️";
            case "reward": return "🎁";
            case "promo": return "📢";
            case "event": return "🎉";
        }
        return "ℹ️";
    }

    private int colorFromType(String type) {
        if (type == null) return Color.parseColor("#2196F3");
        switch (type) {
            case "warning": return Color.parseColor("#FF9800");
            case "reward": return Color.parseColor("#4CAF50");
            case "promo": return Color.parseColor("#E91E63");
            case "event": return Color.parseColor("#9C27B0");
        }
        return Color.parseColor("#2196F3");
    }

    private String typeLabel(String type) {
        if (type == null) return "ℹ️ معلومة";
        switch (type) {
            case "warning": return "⚠️ تحذير";
            case "reward": return "🎁 مكافأة";
            case "promo": return "📢 عرض";
            case "event": return "🎉 حدث";
        }
        return "ℹ️ معلومة";
    }

    private String actionLabel(String action) {
        switch (action) {
            case "wallet": return "💰 افتح المحفظة";
            case "wheel": return "🎡 افتح عجلة الحظ";
            case "city": return "🏙️ افتح المدينة";
            case "market": return "🛒 افتح السوق";
            case "election": return "👑 افتح الانتخابات";
        }
        return "افتح";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notifReg != null) notifReg.remove();
    }
}
