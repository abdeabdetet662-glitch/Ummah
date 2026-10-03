package com.ummah.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * شاشة إنشاء الهوية — 4 خطوات
 */
public class IdentityCreationActivity extends Activity {

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

    private int currentStep = 1;
    private static final int TOTAL_STEPS = 4;

    // البيانات
    private String selectedCountry = "";
    private String userName = "";
    private String seedPhrase = "";
    private String nationalId = "";

    // العناصر
    private LinearLayout contentBox;
    private TextView tvStep;
    private TextView tvProgress;
    private Button btnNext;
    private Button btnBack;
    private View progressFill;

    // عناصر الخطوات
    private LinearLayout countryListBox;
    private EditText etName;
    private TextView tvSeed;
    private CheckBox cbConfirm;
    private TextView tvNationalId;

    private static final int GOLD = Color.parseColor("#D4AF37");
    private static final int GRAY = Color.parseColor("#AAAAAA");
    private static final int WHITE = Color.WHITE;
    private static final int DARK = Color.parseColor("#1A1A1A");

    // 20 دولة عربية
    private static final String[][] COUNTRIES = {
            {"🇩🇿", "الجزائر"}, {"🇲🇦", "المغرب"}, {"🇹🇳", "تونس"}, {"🇱🇾", "ليبيا"},
            {"🇪🇬", "مصر"}, {"🇸🇦", "السعودية"}, {"🇦🇪", "الإمارات"}, {"🇶🇦", "قطر"},
            {"🇰🇼", "الكويت"}, {"🇧🇭", "البحرين"}, {"🇴🇲", "عمان"}, {"🇾🇪", "اليمن"},
            {"🇯🇴", "الأردن"}, {"🇱🇧", "لبنان"}, {"🇸🇾", "سوريا"}, {"🇮🇶", "العراق"},
            {"🇵🇸", "فلسطين"}, {"🇸🇩", "السودان"}, {"🇲🇷", "موريتانيا"}, {"🇸🇴", "الصومال"}
    };

    // 48 كلمة عربية
    private static final String[] WORD_POOL = {
            "كتاب", "شمس", "بحر", "جبل", "قمر", "نجم", "نهر", "شجرة",
            "طير", "وردة", "باب", "نور", "قلم", "بيت", "ماء", "نار",
            "هوا", "تراب", "ذهب", "فضة", "عين", "يد", "قلب", "روح",
            "أمل", "حب", "سلام", "حرية", "عدل", "حق", "صبر", "قوة",
            "حلم", "فجر", "غروب", "مطر", "ثلج", "ريح", "سحاب", "برق",
            "رعد", "صوت", "لون", "طعم", "رحلة", "طريق", "أرض", "وطن"
    };

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.bg_screen);

        // ═══ Top: Progress ═══
        LinearLayout topBox = new LinearLayout(this);
        topBox.setOrientation(LinearLayout.VERTICAL);
        topBox.setPadding(40, 60, 40, 20);

        tvProgress = new TextView(this);
        tvProgress.setTextSize(14);
        tvProgress.setTextColor(GRAY);
        tvProgress.setText(getString(R.string.id_step_of, 1, 4));
        topBox.addView(tvProgress);

        // Progress bar بسيط
        LinearLayout progressBg = new LinearLayout(this);
        progressBg.setOrientation(LinearLayout.HORIZONTAL);
        progressBg.setBackgroundColor(Color.parseColor("#333333"));
        LinearLayout.LayoutParams bgLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 12);
        bgLp.topMargin = 12;
        progressBg.setLayoutParams(bgLp);

        progressFill = new View(this);
        progressFill.setBackgroundColor(GOLD);
        LinearLayout.LayoutParams fillLp = new LinearLayout.LayoutParams(0, 12, 0.25f);
        progressFill.setLayoutParams(fillLp);
        progressBg.addView(progressFill);

        topBox.addView(progressBg);
        root.addView(topBox);

        // ═══ Title ═══
        tvStep = new TextView(this);
        tvStep.setTextSize(24);
        tvStep.setTypeface(null, Typeface.BOLD);
        tvStep.setTextColor(GOLD);
        tvStep.setGravity(Gravity.CENTER);
        tvStep.setPadding(40, 20, 40, 20);
        tvStep.setText(R.string.id_step1);
        root.addView(tvStep);

        // ═══ Content (يتغير) ═══
        ScrollView scroll = new ScrollView(this);
        LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        scroll.setLayoutParams(scrollLp);

        contentBox = new LinearLayout(this);
        contentBox.setOrientation(LinearLayout.VERTICAL);
        contentBox.setPadding(40, 20, 40, 20);
        scroll.addView(contentBox);

        root.addView(scroll);

        // ═══ Bottom Buttons ═══
        LinearLayout btnBox = new LinearLayout(this);
        btnBox.setOrientation(LinearLayout.HORIZONTAL);
        btnBox.setPadding(30, 15, 30, 30);

        btnBack = new Button(this);
        btnBack.setText(R.string.id_back);
        btnBack.setTextSize(15);
        btnBack.setTextColor(GOLD);
        btnBack.setBackgroundColor(Color.TRANSPARENT);
        LinearLayout.LayoutParams backLp = new LinearLayout.LayoutParams(0, 110, 1f);
        btnBack.setLayoutParams(backLp);
        btnBack.setVisibility(View.GONE);
        btnBack.setOnClickListener(v -> onBack());
        btnBox.addView(btnBack);

        btnNext = new Button(this);
        btnNext.setText(R.string.welcome_next);
        btnNext.setTextSize(17);
        btnNext.setTypeface(null, Typeface.BOLD);
        btnNext.setTextColor(Color.BLACK);
        btnNext.setBackgroundResource(R.drawable.bg_btn_gold_hero);
        LinearLayout.LayoutParams nextLp = new LinearLayout.LayoutParams(0, 110, 2f);
        btnNext.setLayoutParams(nextLp);
        btnNext.setOnClickListener(v -> onNext());
        btnBox.addView(btnNext);

        root.addView(btnBox);

        setContentView(root);

        showStep(1);
    }

    // ══════════════════════════════════════════
    //              التنقل بين الخطوات
    // ══════════════════════════════════════════

    private void showStep(int step) {
        currentStep = step;
        contentBox.removeAllViews();

        tvProgress.setText(getString(R.string.id_step_of, step, TOTAL_STEPS));

        // تحديث شريط التقدم
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) progressFill.getLayoutParams();
        lp.weight = (float) step / TOTAL_STEPS;
        progressFill.setLayoutParams(lp);

        btnBack.setVisibility(step > 1 && step < 4 ? View.VISIBLE : View.GONE);

        switch (step) {
            case 1:
                tvStep.setText(R.string.id_step1);
                btnNext.setText(R.string.welcome_next);
                buildCountryStep();
                break;
            case 2:
                tvStep.setText(R.string.id_step2);
                btnNext.setText(R.string.welcome_next);
                buildNameStep();
                break;
            case 3:
                tvStep.setText(R.string.id_step3);
                btnNext.setText(R.string.id_confirm);
                buildSeedStep();
                break;
            case 4:
                tvStep.setText(R.string.id_step4);
                btnNext.setText(R.string.id_start_journey);
                buildCelebrationStep();
                break;
        }
    }

    // ══════════════════════════════════════════
    //              الخطوة 1: البلد
    // ══════════════════════════════════════════

    private void buildCountryStep() {
        TextView hint = new TextView(this);
        hint.setText(R.string.id_country_hint);
        hint.setTextColor(WHITE);
        hint.setTextSize(16);
        hint.setPadding(0, 0, 0, 24);
        contentBox.addView(hint);

        countryListBox = new LinearLayout(this);
        countryListBox.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < COUNTRIES.length; i++) {
            final int idx = i;
            final String flag = COUNTRIES[i][0];
            final String name = COUNTRIES[i][1];

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackgroundResource(R.drawable.bg_card);
            row.setPadding(30, 30, 30, 30);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rowLp.bottomMargin = 12;
            row.setLayoutParams(rowLp);
            row.setClickable(true);
            row.setFocusable(true);

            TextView tvFlag = new TextView(this);
            tvFlag.setText(flag);
            tvFlag.setTextSize(32);
            tvFlag.setPadding(0, 0, 20, 0);
            row.addView(tvFlag);

            TextView tvName = new TextView(this);
            tvName.setText(name);
            tvName.setTextColor(WHITE);
            tvName.setTextSize(18);
            tvName.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(tvName);

            TextView tvCheck = new TextView(this);
            tvCheck.setText("○");
            tvCheck.setTextColor(GRAY);
            tvCheck.setTextSize(24);
            row.addView(tvCheck);

            row.setOnClickListener(v -> {
                selectedCountry = name;
                // تحديث العرض
                for (int j = 0; j < countryListBox.getChildCount(); j++) {
                    LinearLayout r = (LinearLayout) countryListBox.getChildAt(j);
                    TextView check = (TextView) r.getChildAt(2);
                    if (j == idx) {
                        check.setText("●");
                        check.setTextColor(GOLD);
                        r.setBackgroundColor(Color.parseColor("#3A2F00"));
                    } else {
                        check.setText("○");
                        check.setTextColor(GRAY);
                        r.setBackgroundResource(R.drawable.bg_card);
                    }
                }
            });

            countryListBox.addView(row);
        }

        contentBox.addView(countryListBox);
    }

    // ══════════════════════════════════════════
    //              الخطوة 2: الاسم
    // ══════════════════════════════════════════

    private void buildNameStep() {
        TextView hint = new TextView(this);
        hint.setText("شلون نسميوك في أُمّة؟");
        hint.setTextColor(WHITE);
        hint.setTextSize(16);
        hint.setPadding(0, 0, 0, 24);
        contentBox.addView(hint);

        etName = new EditText(this);
        etName.setHint("اسمك أو كنيتك");
        etName.setTextColor(WHITE);
        etName.setHintTextColor(GRAY);
        etName.setTextSize(18);
        etName.setBackgroundResource(R.drawable.bg_input);
        etName.setPadding(30, 30, 30, 30);
        etName.setSingleLine(true);
        LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 140);
        etName.setLayoutParams(nameLp);
        contentBox.addView(etName);

        TextView tip = new TextView(this);
        tip.setText(R.string.id_name_tip);
        tip.setTextColor(GRAY);
        tip.setTextSize(13);
        tip.setPadding(0, 20, 0, 0);
        contentBox.addView(tip);
    }

    // ══════════════════════════════════════════
    //              الخطوة 3: الكلمات السرية
    // ══════════════════════════════════════════

    private void buildSeedStep() {
        // توليد الكلمات
        seedPhrase = generateSeed();

        // تحذير
        TextView warning = new TextView(this);
        warning.setText(R.string.id_seed_warning);
        warning.setTextColor(GOLD);
        warning.setTextSize(14);
        warning.setTypeface(null, Typeface.BOLD);
        warning.setBackgroundResource(R.drawable.bg_president_card);
        warning.setPadding(30, 30, 30, 30);
        contentBox.addView(warning);

        // شبكة الكلمات
        tvSeed = new TextView(this);
        tvSeed.setTextColor(WHITE);
        tvSeed.setTextSize(17);
        tvSeed.setBackgroundResource(R.drawable.bg_input);
        tvSeed.setPadding(30, 30, 30, 30);
        tvSeed.setLineSpacing(12, 1.2f);

        String[] words = seedPhrase.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            sb.append(String.format("%2d. %s", i + 1, words[i]));
            if ((i + 1) % 2 == 0 || i == words.length - 1) {
                sb.append("\n");
            } else {
                sb.append("        ");
            }
        }
        tvSeed.setText(sb.toString());

        LinearLayout.LayoutParams seedLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        seedLp.topMargin = 24;
        tvSeed.setLayoutParams(seedLp);
        contentBox.addView(tvSeed);

        // زر نسخ
        Button btnCopy = new Button(this);
        btnCopy.setText(R.string.id_copy_seed);
        btnCopy.setTextColor(WHITE);
        btnCopy.setTextSize(15);
        btnCopy.setBackgroundResource(R.drawable.bg_btn_outline);
        LinearLayout.LayoutParams copyLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 120);
        copyLp.topMargin = 20;
        btnCopy.setLayoutParams(copyLp);
        btnCopy.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("seed", seedPhrase));
            Toast.makeText(this, R.string.toast_copied_check, Toast.LENGTH_SHORT).show();
        });
        contentBox.addView(btnCopy);

        // تأكيد
        cbConfirm = new CheckBox(this);
        cbConfirm.setText(R.string.id_confirm_saved);
        cbConfirm.setTextColor(WHITE);
        cbConfirm.setTextSize(15);
        LinearLayout.LayoutParams cbLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cbLp.topMargin = 30;
        cbConfirm.setLayoutParams(cbLp);
        contentBox.addView(cbConfirm);
    }

    // ══════════════════════════════════════════
    //              الخطوة 4: الاحتفال
    // ══════════════════════════════════════════

    private void buildCelebrationStep() {
        // الأيقونة
        TextView celebrate = new TextView(this);
        celebrate.setText("🎉🎊🎉");
        celebrate.setTextSize(72);
        celebrate.setGravity(Gravity.CENTER);
        celebrate.setPadding(0, 40, 0, 20);
        contentBox.addView(celebrate);

        // العنوان
        TextView congrats = new TextView(this);
        congrats.setText(getString(R.string.id_congrats, userName));
        congrats.setTextColor(GOLD);
        congrats.setTextSize(26);
        congrats.setTypeface(null, Typeface.BOLD);
        congrats.setGravity(Gravity.CENTER);
        contentBox.addView(congrats);

        TextView subtitle = new TextView(this);
        subtitle.setText("راك مواطن أُمّة رسمياً");
        subtitle.setTextColor(WHITE);
        subtitle.setTextSize(17);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 12, 0, 40);
        contentBox.addView(subtitle);

        // بطاقة الرقم الوطني
        LinearLayout idBox = new LinearLayout(this);
        idBox.setOrientation(LinearLayout.VERTICAL);
        idBox.setBackgroundResource(R.drawable.bg_card_gold);
        idBox.setPadding(40, 40, 40, 40);
        idBox.setGravity(Gravity.CENTER);

        TextView idLabel = new TextView(this);
        idLabel.setText(R.string.id_national_id);
        idLabel.setTextColor(GOLD);
        idLabel.setTextSize(14);
        idLabel.setGravity(Gravity.CENTER);
        idBox.addView(idLabel);

        tvNationalId = new TextView(this);
        nationalId = generateNationalId(seedPhrase);
        tvNationalId.setText(nationalId);
        tvNationalId.setTextColor(WHITE);
        tvNationalId.setTextSize(20);
        tvNationalId.setTypeface(null, Typeface.BOLD);
        tvNationalId.setGravity(Gravity.CENTER);
        tvNationalId.setPadding(0, 16, 0, 0);
        idBox.addView(tvNationalId);

        LinearLayout.LayoutParams idLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        idLp.topMargin = 20;
        idBox.setLayoutParams(idLp);
        contentBox.addView(idBox);

        // مكافأة
        TextView reward = new TextView(this);
        reward.setText(R.string.id_welcome_reward);
        reward.setTextColor(Color.parseColor("#00FF88"));
        reward.setTextSize(17);
        reward.setTypeface(null, Typeface.BOLD);
        reward.setGravity(Gravity.CENTER);
        reward.setPadding(0, 40, 0, 20);
        contentBox.addView(reward);
    }

    // ══════════════════════════════════════════
    //              الأزرار
    // ══════════════════════════════════════════

    private void onNext() {
        switch (currentStep) {
            case 1:
                if (selectedCountry == null || selectedCountry.isEmpty()) {
                    Toast.makeText(this, R.string.id_error_country, Toast.LENGTH_SHORT).show();
                    return;
                }
                showStep(2);
                break;

            case 2:
                userName = etName.getText().toString().trim();
                if (userName.isEmpty()) {
                    Toast.makeText(this, R.string.id_error_name_empty, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (userName.length() < 2) {
                    Toast.makeText(this, R.string.id_error_name_short, Toast.LENGTH_SHORT).show();
                    return;
                }
                showStep(3);
                break;

            case 3:
                if (cbConfirm == null || !cbConfirm.isChecked()) {
                    Toast.makeText(this, R.string.id_error_confirm, Toast.LENGTH_SHORT).show();
                    return;
                }
                showStep(4);
                break;

            case 4:
                finishAndStart();
                break;
        }
    }

    private void onBack() {
        if (currentStep > 1) {
            showStep(currentStep - 1);
        }
    }

    // ══════════════════════════════════════════
    //              الحفظ والتوليد
    // ══════════════════════════════════════════

    private void finishAndStart() {
        // ═══ 1. نسجل المواطن في IdentityManager (باش MainActivity تلقاه) ═══
        try {
            IdentityManager im = new IdentityManager(this);
            String joinDate = new java.text.SimpleDateFormat("yyyy-MM-dd",
                    java.util.Locale.US).format(new java.util.Date());
            im.restoreCitizen(nationalId, userName, joinDate, seedPhrase, selectedCountry);
        } catch (Exception e) {
            // فشل؟ نحفظ يدوياً في نفس SharedPreferences
            try {
                getSharedPreferences("ummah_prefs", MODE_PRIVATE).edit()
                        .putString("national_id", nationalId)
                        .putString("citizen_name", userName)
                        .putString("join_date", new java.text.SimpleDateFormat("yyyy-MM-dd",
                                java.util.Locale.US).format(new java.util.Date()))
                        .putString("seed_phrase", seedPhrase)
                        .putString("country", selectedCountry)
                        .apply();
            } catch (Exception e2) {
                // صمت
            }
        }

        // ═══ 2. حفظ إضافي في SharedPreferences العامة ═══
        getSharedPreferences("ummah", MODE_PRIVATE).edit()
                .putString("pending_country", selectedCountry)
                .putString("pending_name", userName)
                .putString("pending_seed", seedPhrase)
                .putString("pending_national_id", nationalId)
                .putBoolean("pending_identity", true)
                .putBoolean("seen_welcome", true)
                .apply();

        // رسالة تأكيد
        new AlertDialog.Builder(this)
                .setTitle(R.string.congrats)
                .setMessage("راك مواطن أُمّة رسمياً!\n\n💰 +5 دج في محفظتك\n\nيلا نبداو رحلتك")
                .setPositiveButton("🚀 ابدأ", (d, w) -> {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private String generateSeed() {
        List<String> shuffled = new ArrayList<>();
        Collections.addAll(shuffled, WORD_POOL);
        Collections.shuffle(shuffled);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            if (i > 0) sb.append(" ");
            sb.append(shuffled.get(i));
        }
        return sb.toString();
    }

    private String generateNationalId(String seed) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(seed.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02X", b));
            String h = hex.toString();
            return "UMM-" + h.substring(0, 4) + "-" + h.substring(4, 8) + "-" + h.substring(8, 12);
        } catch (Exception e) {
            return "UMM-2026-0000-0001";
        }
    }

    @Override
    public void onBackPressed() {
        if (currentStep > 1) {
            onBack();
        } else {
            super.onBackPressed();
        }
    }
}
