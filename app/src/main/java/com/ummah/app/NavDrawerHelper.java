package com.ummah.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * NavDrawerHelper — قائمة جانبية (Drawer)
 * 
 * الاستعمال في MainActivity:
 *   NavDrawerHelper.show(this, currentCitizen, currentTab, (tab) -> handleTabClick(tab));
 */
public class NavDrawerHelper {

    // أنواع العناصر
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_TAB = 1;
    public static final int TYPE_SETTINGS = 2;
    public static final int TYPE_LOGOUT = 3;
    public static final int TYPE_SECTION = 4;

    public interface OnDrawerClick {
        void onItem(int itemId, String title);
    }

    // IDs للعناصر
    public static final int ITEM_HOME = 100;
    public static final int ITEM_CITY = 101;
    public static final int ITEM_PARLIAMENT = 102;
    public static final int ITEM_ELECTIONS = 103;
    public static final int ITEM_MARKET = 104;
    public static final int ITEM_JOBS = 105;
    public static final int ITEM_GIFTS = 106;
    public static final int ITEM_LEADERBOARD = 107;
    public static final int ITEM_NEWS = 108;
    public static final int ITEM_STATS = 109;
    public static final int ITEM_PROFILE = 200;
    public static final int ITEM_SETTINGS = 201;
    public static final int ITEM_LOGOUT = 202;
    public static final int ITEM_ABOUT = 203;
    public static final int ITEM_WEBSITE = 204;
    public static final int ITEM_REDEEM = 205;
    public static final int ITEM_NOTIFICATIONS = 206;

    /**
     * عرض القائمة الجانبية
     */
    public static void show(final Activity activity,
                            final Citizen citizen,
                            final OnDrawerClick listener) {
        show(activity, citizen, 0, listener);
    }

    public static void show(final Activity activity,
                            final Citizen citizen,
                            final int balance,
                            final OnDrawerClick listener) {
        try {
            // ═══ Root Container ═══
            final android.widget.FrameLayout overlay = new android.widget.FrameLayout(activity);
            overlay.setBackgroundColor(Color.parseColor("#CC000000"));
            overlay.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT));
            overlay.setClickable(true);

            // ═══ Drawer Panel ═══
            LinearLayout drawer = new LinearLayout(activity);
            drawer.setOrientation(LinearLayout.VERTICAL);
            drawer.setBackgroundResource(R.drawable.bg_screen);
            drawer.setClickable(true);

            int drawerWidth = (int) (activity.getResources().getDisplayMetrics().widthPixels * 0.82);
            android.widget.FrameLayout.LayoutParams drawerLp = new android.widget.FrameLayout.LayoutParams(
                    drawerWidth,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT);
            drawerLp.gravity = Gravity.END;  // يفتح من اليمين (RTL)
            drawer.setLayoutParams(drawerLp);
            drawer.setElevation(20f);

            // ═══ ScrollView للمحتوى ═══
            android.widget.ScrollView scroll = new android.widget.ScrollView(activity);
            scroll.setFillViewport(true);
            LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
            scroll.setLayoutParams(scrollLp);

            LinearLayout content = new LinearLayout(activity);
            content.setOrientation(LinearLayout.VERTICAL);

            // ═══════════════════════════════════════════
            //  Header (بطاقة المواطن)
            // ═══════════════════════════════════════════
            addHeader(content, activity, citizen, balance);

            // ═══════════════════════════════════════════
            //  القسم الأول: التنقل
            // ═══════════════════════════════════════════
            addSection(content, activity, "🧭  التنقل");
            addItem(content, activity, "🏠", "الرئيسية", ITEM_HOME, listener);
            addItem(content, activity, "🏙️", "مدينة أُمّة", ITEM_CITY, listener);
            addItem(content, activity, "🏛️", "البرلمان", ITEM_PARLIAMENT, listener);
            addItem(content, activity, "👑", "الانتخابات", ITEM_ELECTIONS, listener);

            // ═══════════════════════════════════════════
            //  القسم الثاني: الاقتصاد
            // ═══════════════════════════════════════════
            addSection(content, activity, "💰  الاقتصاد");
            addItem(content, activity, "🛒", "السوق العام", ITEM_MARKET, listener);
            addItem(content, activity, "💼", "الوظائف", ITEM_JOBS, listener);
            addItem(content, activity, "🎁", "الهدايا", ITEM_GIFTS, listener);

            // ═══════════════════════════════════════════
            //  القسم الثالث: المجتمع
            // ═══════════════════════════════════════════
            addSection(content, activity, "👥  المجتمع");
            addItem(content, activity, "🏆", "المتصدرون", ITEM_LEADERBOARD, listener);
            addItem(content, activity, "📰", "الأخبار", ITEM_NEWS, listener);
            addItem(content, activity, "📊", "الإحصائيات", ITEM_STATS, listener);

            // ═══════════════════════════════════════════
            //  القسم الرابع: حسابي
            // ═══════════════════════════════════════════
            addSection(content, activity, "🎁  المكافآت");
            addItem(content, activity, "🎫", "استبدال كود", ITEM_REDEEM, listener);
            addItem(content, activity, "🔔", "الإشعارات", ITEM_NOTIFICATIONS, listener);

            addSection(content, activity, "👤  حسابي");
            addItem(content, activity, "👤", "الملف الشخصي", ITEM_PROFILE, listener);
            addItem(content, activity, "⚙️", "الإعدادات", ITEM_SETTINGS, listener);
            addItem(content, activity, "📖", "عن أُمّة", ITEM_ABOUT, listener);
            addItem(content, activity, "🌐", "الموقع الرسمي", ITEM_WEBSITE, listener);

            // ═══════════════════════════════════════════
            //  تسجيل الخروج
            // ═══════════════════════════════════════════
            addLogout(content, activity, ITEM_LOGOUT, listener);

            scroll.addView(content);
            drawer.addView(scroll);

            // ═══ Footer ═══
            LinearLayout footer = new LinearLayout(activity);
            footer.setOrientation(LinearLayout.VERTICAL);
            footer.setGravity(Gravity.CENTER);
            footer.setPadding(20, 20, 20, 40);

            TextView version = new TextView(activity);
            version.setText("أُمّة v6.0");
            version.setTextColor(Color.parseColor("#666666"));
            version.setTextSize(11);
            version.setGravity(Gravity.CENTER);
            footer.addView(version);

            TextView copyright = new TextView(activity);
            copyright.setText("© 2026 أُمّة — الجزائر 🇩🇿");
            copyright.setTextColor(Color.parseColor("#444444"));
            copyright.setTextSize(10);
            copyright.setGravity(Gravity.CENTER);
            copyright.setPadding(0, 6, 0, 0);
            footer.addView(copyright);

            drawer.addView(footer);

            // ═══ إضافة للـ Overlay ═══
            overlay.addView(drawer);

            // ═══ Animation: Slide from right ═══
            drawer.setTranslationX(drawerWidth);
            drawer.animate()
                    .translationX(0)
                    .setDuration(250)
                    .start();

            // ═══ ضغط على الخلفية = إغلاق ═══
            overlay.setOnClickListener(v -> closeDrawer(overlay, drawer, drawerWidth));

            // ═══ إضافة للـ Activity ═══
            android.view.ViewGroup root = activity.findViewById(android.R.id.content);
            root.addView(overlay);

        } catch (Exception e) {
            Toast.makeText(activity, "خطأ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * إغلاق القائمة
     */
    private static void closeDrawer(final android.widget.FrameLayout overlay,
                                     LinearLayout drawer, int width) {
        drawer.animate()
                .translationX(width)
                .setDuration(200)
                .withEndAction(() -> {
                    try {
                        android.view.ViewGroup root = (android.view.ViewGroup) overlay.getParent();
                        if (root != null) root.removeView(overlay);
                    } catch (Exception ignored) {}
                })
                .start();
    }

    // ═══════════════════════════════════════════
    //  Header
    // ═══════════════════════════════════════════
    private static void addHeader(LinearLayout parent, Activity activity, Citizen citizen, int balance) {
        LinearLayout header = new LinearLayout(activity);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setBackgroundResource(R.drawable.bg_hero_card);
        header.setPadding(30, 50, 30, 30);

        // الصف الأول: أيقونة + اسم
        LinearLayout row1 = new LinearLayout(activity);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setGravity(Gravity.CENTER_VERTICAL);

        TextView avatar = new TextView(activity);
        avatar.setText("👤");
        avatar.setTextSize(50);
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackgroundResource(R.drawable.bg_avatar_circle);
        int avatarSize = (int) (60 * activity.getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams avLp = new LinearLayout.LayoutParams(avatarSize, avatarSize);
        avatar.setLayoutParams(avLp);
        row1.addView(avatar);

        // معلومات المستخدم
        LinearLayout info = new LinearLayout(activity);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(20, 0, 0, 0);
        LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        info.setLayoutParams(infoLp);

        TextView name = new TextView(activity);
        String userName = (citizen != null && citizen.name != null && !citizen.name.isEmpty())
                ? citizen.name : "مواطن أُمّة";
        name.setText(userName);
        name.setTextColor(Color.parseColor("#D4AF37"));
        name.setTextSize(18);
        name.setTypeface(null, Typeface.BOLD);
        info.addView(name);

        TextView id = new TextView(activity);
        String uid = (citizen != null && citizen.nationalId != null)
                ? citizen.nationalId : "UMM-XXXX-XXXX-XXXX";
        id.setText(uid);
        id.setTextColor(Color.parseColor("#9E9E9E"));
        id.setTextSize(11);
        id.setPadding(0, 4, 0, 0);
        info.addView(id);

        row1.addView(info);
        header.addView(row1);

        // الرصيد
        LinearLayout balanceRow = new LinearLayout(activity);
        balanceRow.setOrientation(LinearLayout.HORIZONTAL);
        balanceRow.setGravity(Gravity.CENTER_VERTICAL);
        balanceRow.setPadding(0, 20, 0, 0);

        TextView balIcon = new TextView(activity);
        balIcon.setText("💰");
        balIcon.setTextSize(20);
        balIcon.setPadding(0, 0, 8, 0);
        balanceRow.addView(balIcon);

        TextView balText = new TextView(activity);
        balText.setText(balance + " Đ");
        balText.setTextColor(Color.parseColor("#4CAF50"));
        balText.setTextSize(16);
        balText.setTypeface(null, Typeface.BOLD);
        balanceRow.addView(balText);

        header.addView(balanceRow);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        header.setLayoutParams(lp);

        parent.addView(header);
    }

    // ═══════════════════════════════════════════
    //  Section Title
    // ═══════════════════════════════════════════
    private static void addSection(LinearLayout parent, Activity activity, String title) {
        LinearLayout container = new LinearLayout(activity);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        container.setPadding(30, 30, 30, 10);

        View lineL = new View(activity);
        LinearLayout.LayoutParams lLp = new LinearLayout.LayoutParams(0, 2, 1f);
        lineL.setLayoutParams(lLp);
        lineL.setBackgroundColor(Color.parseColor("#2A3D32"));
        container.addView(lineL);

        TextView t = new TextView(activity);
        t.setText("  " + title + "  ");
        t.setTextColor(Color.parseColor("#D4AF37"));
        t.setTextSize(13);
        t.setTypeface(null, Typeface.BOLD);
        container.addView(t);

        View lineR = new View(activity);
        LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(0, 2, 1f);
        lineR.setLayoutParams(rLp);
        lineR.setBackgroundColor(Color.parseColor("#2A3D32"));
        container.addView(lineR);

        parent.addView(container);
    }

    // ═══════════════════════════════════════════
    //  Item Row
    // ═══════════════════════════════════════════
    private static void addItem(LinearLayout parent, final Activity activity,
                                 String emoji, String title, final int itemId,
                                 final OnDrawerClick listener) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(40, 30, 40, 30);
        row.setClickable(true);
        row.setFocusable(true);

        // Ripple effect
        android.util.TypedValue outValue = new android.util.TypedValue();
        activity.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        row.setBackgroundResource(outValue.resourceId);

        // Emoji
        TextView icon = new TextView(activity);
        icon.setText(emoji);
        icon.setTextSize(22);
        icon.setPadding(0, 0, 20, 0);
        row.addView(icon);

        // Title
        TextView tvTitle = new TextView(activity);
        tvTitle.setText(title);
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setTextSize(16);
        tvTitle.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(tvTitle);

        // Arrow (سهم صغير)
        TextView arrow = new TextView(activity);
        arrow.setText("‹");
        arrow.setTextColor(Color.parseColor("#666666"));
        arrow.setTextSize(20);
        row.addView(arrow);

        row.setOnClickListener(v -> {
            if (listener != null) listener.onItem(itemId, title);
        });

        parent.addView(row);
    }

    // ═══════════════════════════════════════════
    //  Logout Row (مميز)
    // ═══════════════════════════════════════════
    private static void addLogout(LinearLayout parent, final Activity activity,
                                   final int itemId, final OnDrawerClick listener) {
        // فاصل
        View spacer = new View(activity);
        LinearLayout.LayoutParams spLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2);
        spLp.setMargins(40, 30, 40, 30);
        spacer.setLayoutParams(spLp);
        spacer.setBackgroundColor(Color.parseColor("#2A3D32"));
        parent.addView(spacer);

        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(40, 30, 40, 30);
        row.setClickable(true);

        android.util.TypedValue outValue = new android.util.TypedValue();
        activity.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        row.setBackgroundResource(outValue.resourceId);

        TextView icon = new TextView(activity);
        icon.setText("🚪");
        icon.setTextSize(22);
        icon.setPadding(0, 0, 20, 0);
        row.addView(icon);

        TextView title = new TextView(activity);
        title.setText("تسجيل الخروج");
        title.setTextColor(Color.parseColor("#F44336"));
        title.setTextSize(16);
        title.setTypeface(null, Typeface.BOLD);
        row.addView(title);

        row.setOnClickListener(v -> {
            if (listener != null) listener.onItem(itemId, "تسجيل الخروج");
        });

        parent.addView(row);
    }
}
