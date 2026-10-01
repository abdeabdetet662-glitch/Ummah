package com.ummah.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/**
 * SettingsActivity — شاشة إعدادات المستخدم
 * 
 * 17 إعداد في 5 أقسام:
 * - الحساب (3)
 * - المظهر (2)
 * - الإشعارات (3)
 * - الأمان (2)
 * - أخرى (7)
 */
public class SettingsActivity extends Activity {

    private SharedPreferences prefs;
    private IdentityManager im;
    private WalletManager wm;

    private static final int GOLD = Color.parseColor("#D4AF37");
    private static final int WHITE = Color.WHITE;
    private static final int GRAY = Color.parseColor("#9E9E9E");
    private static final int GREEN = Color.parseColor("#4CAF50");
    private static final int RED = Color.parseColor("#F44336");

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        prefs = getSharedPreferences("ummah_settings", MODE_PRIVATE);
        im = new IdentityManager(this);
        wm = new WalletManager(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 50, 30, 60);
        scroll.addView(root);

        // ═══ Header ═══
        TextView icon = new TextView(this);
        icon.setText("⚙️");
        icon.setTextSize(60);
        icon.setGravity(Gravity.CENTER);
        root.addView(icon);

        TextView title = new TextView(this);
        title.setText("الإعدادات");
        title.setTextColor(GOLD);
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 20, 0, 8);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("تحكم في تجربتك داخل أُمّة");
        subtitle.setTextColor(GRAY);
        subtitle.setTextSize(13);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, 30);
        root.addView(subtitle);

        // ═══════════════════════════════════════════
        //  القسم 1: الحساب
        // ═══════════════════════════════════════════
        addSection(root, "👤  الحساب");

        addClickItem(root, "👤", "الملف الشخصي", "اسمك، رقمك الوطني",
                v -> openActivity(ProfileActivity.class));

        addClickItem(root, "🆔", "رقمي الوطني", 
                im.isCitizen() ? im.getCitizen().nationalId : "غير مسجل",
                v -> showNationalId());

        addClickItem(root, "🔑", "الكلمات السرية", "احفظهم في مكان آمن",
                v -> showSeedWarning());

        // ═══════════════════════════════════════════
        //  القسم 2: المظهر
        // ═══════════════════════════════════════════
        addSection(root, "🎨  المظهر");

        addClickItem(root, "🌍", "اللغة", getCurrentLanguage(),
                v -> showLanguageDialog());

        addSwitchItem(root, "🌙", "الوضع الليلي", "قريباً", "dark_mode", false);

        // ═══════════════════════════════════════════
        //  القسم 3: الإشعارات
        // ═══════════════════════════════════════════
        addSection(root, "🔔  الإشعارات");

        addSwitchItem(root, "🔔", "الإشعارات العامة", "أخبار وإعلانات", 
                "notif_general", true);

        addSwitchItem(root, "💬", "إشعارات الدردشة", "رسائل جديدة",
                "notif_chat", true);

        addSwitchItem(root, "💰", "إشعارات التحويلات", "عند استقبال Đ",
                "notif_transfer", true);

        // ═══════════════════════════════════════════
        //  القسم 4: الأمان
        // ═══════════════════════════════════════════
        addSection(root, "🔐  الأمان");

        addSwitchItem(root, "🔐", "قفل التطبيق", "يطلب PIN عند الفتح",
                "lock_pin", false);

        addSwitchItem(root, "📱", "بصمة الإصبع", "دخول سريع",
                "lock_fingerprint", false);

        // ═══════════════════════════════════════════
        //  القسم 5: أخرى
        // ═══════════════════════════════════════════
        addSection(root, "📌  أخرى");

        addClickItem(root, "📤", "شارك التطبيق", "ادعُ أصدقاءك",
                v -> shareApp());

        addClickItem(root, "⭐", "قيّم التطبيق", "5 نجوم تعني لنا الكثير",
                v -> rateApp());

        addClickItem(root, "🌐", "الموقع الرسمي", "ummah.app",
                v -> openUrl("https://abdeabdetet662-glitch.github.io/ummah-website/"));

        addClickItem(root, "📖", "عن أُمّة", "الإصدار 6.0",
                v -> showAbout());

        addClickItem(root, "📜", "سياسة الخصوصية", "",
                v -> openUrl("https://abdeabdetet662-glitch.github.io/ummah-website/privacy.html"));

        addClickItem(root, "⚖️", "الشروط والأحكام", "",
                v -> openUrl("https://abdeabdetet662-glitch.github.io/ummah-website/terms.html"));

        addClickItem(root, "📧", "تواصل معنا", "abdeabderahman62@gmail.com",
                v -> sendEmail());

        // ═══════════════════════════════════════════
        //  القسم 6: خطر
        // ═══════════════════════════════════════════
        addSection(root, "⚠️  خطر");

        addDangerItem(root, "🗑️", "حذف الحساب", "لا يمكن التراجع",
                v -> confirmDeleteAccount());

        // ═══ Footer ═══
        TextView footer = new TextView(this);
        footer.setText("أُمّة v6.0 — صُنع بـ ❤️ في الجزائر 🇩🇿");
        footer.setTextColor(Color.parseColor("#444444"));
        footer.setTextSize(10);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, 40, 0, 0);
        root.addView(footer);

        setContentView(scroll);
    }

    // ═══════════════════════════════════════════
    //  Helper: قسم (Section)
    // ═══════════════════════════════════════════
    private void addSection(LinearLayout root, String title) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        container.setPadding(0, 30, 0, 12);

        View lineL = new View(this);
        LinearLayout.LayoutParams lLp = new LinearLayout.LayoutParams(0, 2, 1f);
        lineL.setLayoutParams(lLp);
        lineL.setBackgroundColor(Color.parseColor("#2A3D32"));
        container.addView(lineL);

        TextView t = new TextView(this);
        t.setText("  " + title + "  ");
        t.setTextColor(GOLD);
        t.setTextSize(15);
        t.setTypeface(null, Typeface.BOLD);
        container.addView(t);

        View lineR = new View(this);
        LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(0, 2, 1f);
        lineR.setLayoutParams(rLp);
        lineR.setBackgroundColor(Color.parseColor("#2A3D32"));
        container.addView(lineR);

        root.addView(container);
    }

    // ═══════════════════════════════════════════
    //  Helper: عنصر قابل للنقر
    // ═══════════════════════════════════════════
    private void addClickItem(LinearLayout root, String emoji, String title,
                                String subtitle, View.OnClickListener onClick) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundResource(R.drawable.bg_card);
        row.setPadding(30, 30, 30, 30);
        row.setClickable(true);
        row.setFocusable(true);

        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        rowLp.setMargins(0, 8, 0, 8);
        row.setLayoutParams(rowLp);
        row.setElevation(4f);

        // Emoji
        TextView tvIcon = new TextView(this);
        tvIcon.setText(emoji);
        tvIcon.setTextSize(24);
        tvIcon.setPadding(0, 0, 20, 0);
        row.addView(tvIcon);

        // Text
        LinearLayout textBox = new LinearLayout(this);
        textBox.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        textBox.setLayoutParams(textLp);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextColor(WHITE);
        tvTitle.setTextSize(16);
        tvTitle.setTypeface(null, Typeface.BOLD);
        textBox.addView(tvTitle);

        if (subtitle != null && !subtitle.isEmpty()) {
            TextView tvSub = new TextView(this);
            tvSub.setText(subtitle);
            tvSub.setTextColor(GRAY);
            tvSub.setTextSize(12);
            tvSub.setPadding(0, 4, 0, 0);
            textBox.addView(tvSub);
        }

        row.addView(textBox);

        // Arrow
        TextView arrow = new TextView(this);
        arrow.setText("‹");
        arrow.setTextColor(Color.parseColor("#666666"));
        arrow.setTextSize(24);
        row.addView(arrow);

        if (onClick != null) row.setOnClickListener(onClick);

        root.addView(row);
    }

    // ═══════════════════════════════════════════
    //  Helper: Switch Item
    // ═══════════════════════════════════════════
    private void addSwitchItem(LinearLayout root, String emoji, String title,
                                String subtitle, final String key, boolean defaultVal) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundResource(R.drawable.bg_card);
        row.setPadding(30, 30, 30, 30);

        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        rowLp.setMargins(0, 8, 0, 8);
        row.setLayoutParams(rowLp);
        row.setElevation(4f);

        TextView tvIcon = new TextView(this);
        tvIcon.setText(emoji);
        tvIcon.setTextSize(24);
        tvIcon.setPadding(0, 0, 20, 0);
        row.addView(tvIcon);

        LinearLayout textBox = new LinearLayout(this);
        textBox.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        textBox.setLayoutParams(textLp);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextColor(WHITE);
        tvTitle.setTextSize(16);
        tvTitle.setTypeface(null, Typeface.BOLD);
        textBox.addView(tvTitle);

        if (subtitle != null && !subtitle.isEmpty()) {
            TextView tvSub = new TextView(this);
            tvSub.setText(subtitle);
            tvSub.setTextColor(GRAY);
            tvSub.setTextSize(12);
            tvSub.setPadding(0, 4, 0, 0);
            textBox.addView(tvSub);
        }

        row.addView(textBox);

        Switch sw = new Switch(this);
        sw.setChecked(prefs.getBoolean(key, defaultVal));
        sw.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            prefs.edit().putBoolean(key, isChecked).apply();
            String msg = isChecked ? "✅ " + title + " مُفعل" : "⏸️ " + title + " متوقف";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
        row.addView(sw);

        root.addView(row);
    }

    // ═══════════════════════════════════════════
    //  Helper: Danger Item
    // ═══════════════════════════════════════════
    private void addDangerItem(LinearLayout root, String emoji, String title,
                                 String subtitle, View.OnClickListener onClick) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundResource(R.drawable.bg_card);
        row.setPadding(30, 30, 30, 30);
        row.setClickable(true);
        row.setFocusable(true);

        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        rowLp.setMargins(0, 8, 0, 8);
        row.setLayoutParams(rowLp);
        row.setElevation(4f);

        TextView tvIcon = new TextView(this);
        tvIcon.setText(emoji);
        tvIcon.setTextSize(24);
        tvIcon.setPadding(0, 0, 20, 0);
        row.addView(tvIcon);

        LinearLayout textBox = new LinearLayout(this);
        textBox.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        textBox.setLayoutParams(textLp);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextColor(RED);
        tvTitle.setTextSize(16);
        tvTitle.setTypeface(null, Typeface.BOLD);
        textBox.addView(tvTitle);

        if (subtitle != null && !subtitle.isEmpty()) {
            TextView tvSub = new TextView(this);
            tvSub.setText(subtitle);
            tvSub.setTextColor(GRAY);
            tvSub.setTextSize(12);
            tvSub.setPadding(0, 4, 0, 0);
            textBox.addView(tvSub);
        }

        row.addView(textBox);

        TextView arrow = new TextView(this);
        arrow.setText("‹");
        arrow.setTextColor(RED);
        arrow.setTextSize(24);
        row.addView(arrow);

        if (onClick != null) row.setOnClickListener(onClick);

        root.addView(row);
    }

    // ═══════════════════════════════════════════
    //  Dialogs & Actions
    // ═══════════════════════════════════════════

    private String getCurrentLanguage() {
        return prefs.getString("language", "العربية");
    }

    private void showLanguageDialog() {
        String[] languages = {"🇩🇿 العربية", "🇫🇷 Français", "🇬🇧 English", "🇷🇺 Русский"};
        new AlertDialog.Builder(this)
                .setTitle("🌍 اختر اللغة")
                .setItems(languages, (d, which) -> {
                    String chosen = languages[which];
                    prefs.edit().putString("language", chosen).apply();
                    Toast.makeText(this, "اللغة: " + chosen, Toast.LENGTH_SHORT).show();
                    Toast.makeText(this, "ℹ️ يحتاج إعادة تشغيل التطبيق", Toast.LENGTH_LONG).show();
                })
                .show();
    }

    private void showNationalId() {
        if (!im.isCitizen()) {
            Toast.makeText(this, "❌ ما راكش مسجل", Toast.LENGTH_SHORT).show();
            return;
        }
        Citizen c = im.getCitizen();
        new AlertDialog.Builder(this)
                .setTitle("🆔 رقمك الوطني")
                .setMessage(c.nationalId + "\n\nتاريخ الانضمام: " + c.joinDate)
                .setPositiveButton("📋 نسخ", (d, w) -> {
                    android.content.ClipboardManager cm = (android.content.ClipboardManager)
                            getSystemService(CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(android.content.ClipData.newPlainText("id", c.nationalId));
                    Toast.makeText(this, "✓ تم النسخ", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("إغلاق", null)
                .show();
    }

    private void showSeedWarning() {
        if (!im.isCitizen()) {
            Toast.makeText(this, "❌ ما راكش مسجل", Toast.LENGTH_SHORT).show();
            return;
        }
        Citizen c = im.getCitizen();
        new AlertDialog.Builder(this)
                .setTitle("🔑 كلماتك السرية")
                .setMessage("⚠️ احفظ هذه الكلمات في مكان آمن!\n\n" + c.seedPhrase + "\n\n" +
                        "هذي هي هويتك الوحيدة.")
                .setPositiveButton("📋 نسخ", (d, w) -> {
                    android.content.ClipboardManager cm = (android.content.ClipboardManager)
                            getSystemService(CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(android.content.ClipData.newPlainText("seed", c.seedPhrase));
                    Toast.makeText(this, "✓ تم النسخ", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("إغلاق", null)
                .show();
    }

    private void shareApp() {
        String text = "🌍 جرب أُمّة — أول دولة رقمية عربية!\n\n" +
                "https://abdeabdetet662-glitch.github.io/ummah-website/";
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(i, "شارك أُمّة"));
    }

    private void rateApp() {
        Toast.makeText(this, "⭐ شكراً! التطبيق على Google Play قريباً", Toast.LENGTH_LONG).show();
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, "تعذر فتح الرابط", Toast.LENGTH_SHORT).show();
        }
    }

    private void openActivity(Class<?> cls) {
        try {
            startActivity(new Intent(this, cls));
        } catch (Exception e) {
            Toast.makeText(this, "الشاشة غير متاحة حالياً", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmail() {
        Intent i = new Intent(Intent.ACTION_SENDTO);
        i.setData(Uri.parse("mailto:abdeabderahman62@gmail.com"));
        i.putExtra(Intent.EXTRA_SUBJECT, "استفسار حول أُمّة");
        try {
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "تعذر فتح البريد", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAbout() {
        new AlertDialog.Builder(this)
                .setTitle("📖 عن أُمّة")
                .setMessage("🌍 أُمّة v6.0\n\n" +
                        "أول دولة رقمية عربية كاملة.\n\n" +
                        "صُنع بـ ❤️ من الجزائر 🇩🇿\n\n" +
                        "المطور: abdeabdetet662-glitch\n" +
                        "GitHub: github.com/abdeabdetet662-glitch")
                .setPositiveButton("🌐 الموقع", (d, w) -> 
                        openUrl("https://abdeabdetet662-glitch.github.io/ummah-website/"))
                .setNegativeButton("إغلاق", null)
                .show();
    }

    private void confirmDeleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ حذف الحساب")
                .setMessage("هذا الإجراء لا يمكن التراجع عنه!\n\n" +
                        "رايح تفقد:\n" +
                        "• هويتك الوطنية\n" +
                        "• رصيدك\n" +
                        "• ممتلكاتك\n" +
                        "• كل شيء!")
                .setPositiveButton("🗑️ حذف نهائياً", (d, w) -> {
                    new AlertDialog.Builder(this)
                            .setTitle("⚠️ تأكيد أخير")
                            .setMessage("واش راك متأكد 100%؟")
                            .setPositiveButton("نعم، احذف", (d2, w2) -> {
                                prefs.edit().clear().apply();
                                getSharedPreferences("ummah_prefs", MODE_PRIVATE).edit().clear().apply();
                                getSharedPreferences("ummah", MODE_PRIVATE).edit().clear().apply();
                                Toast.makeText(this, "🗑️ تم حذف حسابك", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(this, WelcomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .setNegativeButton("إلغاء", null)
                            .show();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }
}
