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

    // ═══ تطبيق اللغة ═══
    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

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
        title.setText(getString(R.string.setting_title));
        title.setTextColor(GOLD);
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 20, 0, 8);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText(getString(R.string.setting_subtitle));
        subtitle.setTextColor(GRAY);
        subtitle.setTextSize(13);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, 30);
        root.addView(subtitle);

        // ═══════════════════════════════════════════
        //  القسم 1: الحساب
        // ═══════════════════════════════════════════
        addSection(root, getString(R.string.setting_section_account));

        addClickItem(root, "👤", getString(R.string.setting_profile), getString(R.string.setting_profile_sub),
                v -> openActivity(ProfileActivity.class));

        addClickItem(root, "🆔", getString(R.string.setting_national_id), 
                im.isCitizen() ? im.getCitizen().nationalId : getString(R.string.setting_not_registered),
                v -> showNationalId());

        addClickItem(root, "🔑", getString(R.string.setting_secret_words), getString(R.string.setting_secret_words_sub),
                v -> showSeedWarning());

        // ═══════════════════════════════════════════
        //  القسم 2: المظهر
        // ═══════════════════════════════════════════
        addSection(root, getString(R.string.setting_section_appearance));

        addClickItem(root, "🌍", getString(R.string.setting_language), getCurrentLanguage(),
                v -> showLanguageDialog());

        addSwitchItem(root, "🌙", getString(R.string.setting_dark_mode), getString(R.string.setting_soon), "dark_mode", false);

        // ═══════════════════════════════════════════
        //  القسم 3: الإشعارات
        // ═══════════════════════════════════════════
        addSection(root, getString(R.string.setting_section_notifications));

        addSwitchItem(root, "🔔", getString(R.string.setting_notif_general), getString(R.string.setting_notif_general_sub), 
                "notif_general", true);

        addSwitchItem(root, "💬", getString(R.string.setting_notif_chat), getString(R.string.setting_notif_chat_sub),
                "notif_chat", true);

        addSwitchItem(root, "💰", getString(R.string.setting_notif_transfer), getString(R.string.setting_notif_transfer_sub),
                "notif_transfer", true);

        // ═══════════════════════════════════════════
        //  القسم 4: الأمان
        // ═══════════════════════════════════════════
        addSection(root, getString(R.string.setting_section_security));

        addSwitchItem(root, "🔐", getString(R.string.setting_lock_app), getString(R.string.setting_lock_app_sub),
                "lock_pin", false);

        addSwitchItem(root, "📱", getString(R.string.setting_fingerprint), getString(R.string.setting_fingerprint_sub),
                "lock_fingerprint", false);

        // ═══════════════════════════════════════════
        //  القسم 5: أخرى
        // ═══════════════════════════════════════════
        addSection(root, getString(R.string.setting_section_other));

        addClickItem(root, "📤", getString(R.string.setting_share_app), getString(R.string.setting_share_app_sub),
                v -> shareApp());

        addClickItem(root, "⭐", getString(R.string.setting_rate_app), getString(R.string.setting_rate_app_sub),
                v -> rateApp());

        addClickItem(root, "🌐", getString(R.string.setting_website), "ummah.app",
                v -> openUrl("https://abdeabdetet662-glitch.github.io/ummah-website/"));

        addClickItem(root, "📖", getString(R.string.setting_about), getString(R.string.setting_version),
                v -> showAbout());

        addClickItem(root, "📜", getString(R.string.setting_privacy), "",
                v -> openUrl("https://abdeabdetet662-glitch.github.io/ummah-website/privacy.html"));

        addClickItem(root, "⚖️", getString(R.string.setting_terms), "",
                v -> openUrl("https://abdeabdetet662-glitch.github.io/ummah-website/terms.html"));

        addClickItem(root, "📧", getString(R.string.setting_contact), "abdeabderahman62@gmail.com",
                v -> sendEmail());

        // ═══════════════════════════════════════════
        //  القسم 6: خطر
        // ═══════════════════════════════════════════
        addSection(root, getString(R.string.setting_section_danger));

        addDangerItem(root, "🗑️", getString(R.string.setting_delete_account), getString(R.string.setting_delete_account_sub),
                v -> confirmDeleteAccount());

        // ═══ Footer ═══
        TextView footer = new TextView(this);
        footer.setText(getString(R.string.setting_footer));
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
            String msg = isChecked 
                    ? "✅ " + title + " " + getString(R.string.setting_toggle_on)
                    : "⏸️ " + title + " " + getString(R.string.setting_toggle_off);
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
        String code = LocaleHelper.getLocale(this);
        if (code != null && !code.isEmpty()) {
            return LocaleHelper.getLanguageName(code);
        }
        return prefs.getString("language", "🇩🇿 العربية");
    }

    private void showLanguageDialog() {
        String[] languages = {"🇩🇿 العربية", "🇫🇷 Français", "🇬🇧 English", "🇷🇺 Русский"};
        String[] codes = {"ar", "fr", "en", "ru"};

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.setting_choose_language))
                .setItems(languages, (d, which) -> {
                    String code = codes[which];
                    String name = languages[which];

                    // نحفظو في التفضيلات
                    prefs.edit().putString("language", name).apply();

                    // نحفظو في LocaleHelper
                    LocaleHelper.setLocale(this, code);

                    // نعيد تشغيل الـ Activity
                    Toast.makeText(this, "🌍 " + name, Toast.LENGTH_SHORT).show();

                    // نحولو للـ MainActivity
                    try {
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        recreate();
                    }
                })
                .show();
    }

    private void showNationalId() {
        if (!im.isCitizen()) {
            Toast.makeText(this, getString(R.string.setting_error_not_registered), Toast.LENGTH_SHORT).show();
            return;
        }
        Citizen c = im.getCitizen();
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.setting_my_id))
                .setMessage(c.nationalId + "\n\n" + getString(R.string.setting_join_date) + ": " + c.joinDate)
                .setPositiveButton(getString(R.string.setting_copy), (d, w) -> {
                    android.content.ClipboardManager cm = (android.content.ClipboardManager)
                            getSystemService(CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(android.content.ClipData.newPlainText("id", c.nationalId));
                    Toast.makeText(this, getString(R.string.setting_copied), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.setting_close), null)
                .show();
    }

    private void showSeedWarning() {
        if (!im.isCitizen()) {
            Toast.makeText(this, getString(R.string.setting_error_not_registered), Toast.LENGTH_SHORT).show();
            return;
        }
        Citizen c = im.getCitizen();
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.setting_secret_title))
                .setMessage(getString(R.string.setting_secret_warning) + "\n\n" + c.seedPhrase + "\n\n" +
                        getString(R.string.setting_secret_note))
                .setPositiveButton(getString(R.string.setting_copy), (d, w) -> {
                    android.content.ClipboardManager cm = (android.content.ClipboardManager)
                            getSystemService(CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(android.content.ClipData.newPlainText("seed", c.seedPhrase));
                    Toast.makeText(this, getString(R.string.setting_copied), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.setting_close), null)
                .show();
    }

    private void shareApp() {
        String text = getString(R.string.setting_share_text) + "\n\n" +
                "https://abdeabdetet662-glitch.github.io/ummah-website/";
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(i, getString(R.string.setting_share_title)));
    }

    private void rateApp() {
        Toast.makeText(this, getString(R.string.setting_rate_thanks), Toast.LENGTH_LONG).show();
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.setting_error_open_link), Toast.LENGTH_SHORT).show();
        }
    }

    private void openActivity(Class<?> cls) {
        try {
            startActivity(new Intent(this, cls));
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.setting_error_screen), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmail() {
        Intent i = new Intent(Intent.ACTION_SENDTO);
        i.setData(Uri.parse("mailto:abdeabderahman62@gmail.com"));
        i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.setting_email_subject));
        try {
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.setting_error_mail), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAbout() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.setting_about_title))
                .setMessage(getString(R.string.setting_about_v) + "\n\n" +
                        getString(R.string.setting_about_desc) + "\n\n" +
                        getString(R.string.setting_about_made) + "\n\n" +
                        getString(R.string.setting_about_dev) + ": abdeabdetet662-glitch\n" +
                        "GitHub: github.com/abdeabdetet662-glitch")
                .setPositiveButton(getString(R.string.setting_about_website), (d, w) -> 
                        openUrl("https://abdeabdetet662-glitch.github.io/ummah-website/"))
                .setNegativeButton(getString(R.string.setting_close), null)
                .show();
    }

    private void confirmDeleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.setting_delete_title))
                .setMessage(getString(R.string.setting_delete_warning) + "\n\n" +
                        getString(R.string.setting_delete_lose) + "\n" +
                        getString(R.string.setting_delete_item1) + "\n" +
                        getString(R.string.setting_delete_item2) + "\n" +
                        getString(R.string.setting_delete_item3) + "\n" +
                        getString(R.string.setting_delete_item4))
                .setPositiveButton(getString(R.string.setting_delete_confirm), (d, w) -> {
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.setting_delete_final))
                            .setMessage(getString(R.string.setting_delete_sure))
                            .setPositiveButton(getString(R.string.setting_delete_yes), (d2, w2) -> {
                                prefs.edit().clear().apply();
                                getSharedPreferences("ummah_prefs", MODE_PRIVATE).edit().clear().apply();
                                getSharedPreferences("ummah", MODE_PRIVATE).edit().clear().apply();
                                Toast.makeText(this, getString(R.string.setting_delete_done), Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(this, WelcomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .setNegativeButton(getString(R.string.setting_cancel), null)
                            .show();
                })
                .setNegativeButton(getString(R.string.setting_cancel), null)
                .show();
    }
}
