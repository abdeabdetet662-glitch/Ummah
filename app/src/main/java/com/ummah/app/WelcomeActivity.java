package com.ummah.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * شاشة الترحيب — 3 صفحات
 * تُعرض فقط للمستخدم الجديد
 */
public class WelcomeActivity extends Activity {

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

    private int currentPage = 0;
    private TextView tvIcon;
    private TextView tvTitle;
    private TextView tvDesc;
    private TextView[] dots;
    private FrameLayout contentBox;
    private Button btnNext;
    private Button btnSkip;

    // بيانات الصفحات الثلاث
    private static final String[] ICONS = {"🌍", "🆔", "👑"};
    private static final String[] TITLES = {
            "أهلاً بك في أُمّة",
            "احصل على هويتك الوطنية",
            "كن مواطناً حقيقياً"
    };
    private static final String[] DESCS = {
            "أول دولة رقمية عربية كاملة",
            "رقم وطني فريد + كلمات سرية آمنة",
            "محفظة، برلمان، انتخابات، ومدينة كاملة"
    };

    private static final int GOLD = Color.parseColor("#D4AF37");
    private static final int GRAY = Color.parseColor("#AAAAAA");
    private static final int WHITE = Color.WHITE;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        // ═══ Root ═══
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.bg_screen);

        // ═══ زر تخطي (فوق يمين) ═══
        FrameLayout topBox = new FrameLayout(this);
        LinearLayout.LayoutParams topLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        topBox.setLayoutParams(topLp);

        btnSkip = new Button(this);
        btnSkip.setText("تخطي");
        btnSkip.setTextColor(GRAY);
        btnSkip.setTextSize(14);
        btnSkip.setBackgroundColor(Color.TRANSPARENT);
        btnSkip.setPadding(40, 30, 40, 30);
        FrameLayout.LayoutParams skipLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        skipLp.gravity = Gravity.END | Gravity.TOP;
        btnSkip.setLayoutParams(skipLp);
        btnSkip.setOnClickListener(v -> goToIdentity());
        topBox.addView(btnSkip);
        root.addView(topBox);

        // ═══ محتوى الصفحة ═══
        LinearLayout centerBox = new LinearLayout(this);
        centerBox.setOrientation(LinearLayout.VERTICAL);
        centerBox.setGravity(Gravity.CENTER);
        centerBox.setPadding(60, 40, 60, 40);
        LinearLayout.LayoutParams centerLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        centerBox.setLayoutParams(centerLp);

        // الأيقونة
        tvIcon = new TextView(this);
        tvIcon.setTextSize(140);
        tvIcon.setGravity(Gravity.CENTER);
        tvIcon.setText(ICONS[0]);
        centerBox.addView(tvIcon);

        // العنوان
        tvTitle = new TextView(this);
        tvTitle.setTextSize(28);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setTextColor(GOLD);
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setPadding(0, 50, 0, 0);
        tvTitle.setText(TITLES[0]);
        centerBox.addView(tvTitle);

        // الوصف
        tvDesc = new TextView(this);
        tvDesc.setTextSize(16);
        tvDesc.setTextColor(WHITE);
        tvDesc.setGravity(Gravity.CENTER);
        tvDesc.setPadding(0, 24, 0, 0);
        tvDesc.setText(DESCS[0]);
        centerBox.addView(tvDesc);

        root.addView(centerBox);

        // ═══ النقاط ═══
        LinearLayout dotsBox = new LinearLayout(this);
        dotsBox.setOrientation(LinearLayout.HORIZONTAL);
        dotsBox.setGravity(Gravity.CENTER);
        dotsBox.setPadding(0, 20, 0, 20);

        dots = new TextView[3];
        for (int i = 0; i < 3; i++) {
            dots[i] = new TextView(this);
            dots[i].setText("●");
            dots[i].setTextSize(18);
            dots[i].setPadding(8, 0, 8, 0);
            dotsBox.addView(dots[i]);
        }
        root.addView(dotsBox);

        // ═══ الأزرار ═══
        LinearLayout btnBox = new LinearLayout(this);
        btnBox.setOrientation(LinearLayout.VERTICAL);
        btnBox.setPadding(30, 15, 30, 40);

        btnNext = new Button(this);
        btnNext.setText("التالي ←");
        btnNext.setTextSize(18);
        btnNext.setTypeface(null, Typeface.BOLD);
        btnNext.setTextColor(Color.BLACK);
        btnNext.setBackgroundResource(R.drawable.bg_btn_gold_hero);
        LinearLayout.LayoutParams nextLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 120);
        btnNext.setLayoutParams(nextLp);
        btnNext.setOnClickListener(v -> onNextClicked());
        btnBox.addView(btnNext);

        Button btnHaveAccount = new Button(this);
        btnHaveAccount.setText("لدي حساب — استعادة");
        btnHaveAccount.setTextSize(15);
        btnHaveAccount.setTextColor(GOLD);
        btnHaveAccount.setBackgroundColor(Color.TRANSPARENT);
        LinearLayout.LayoutParams acctLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 100);
        acctLp.topMargin = 20;
        btnHaveAccount.setLayoutParams(acctLp);
        btnHaveAccount.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, AccountRecoveryActivity.class));
            } catch (Exception e) {
                // الشاشة غير موجودة
            }
        });
        btnBox.addView(btnHaveAccount);

        root.addView(btnBox);

        setContentView(root);

        // تحديث النقاط
        updateDots();
    }

    private void onNextClicked() {
        if (currentPage < 2) {
            currentPage++;
            showPage(currentPage);
        } else {
            goToIdentity();
        }
    }

    private void showPage(int page) {
        tvIcon.setText(ICONS[page]);
        tvTitle.setText(TITLES[page]);
        tvDesc.setText(DESCS[page]);

        // Animation بسيط
        tvIcon.setAlpha(0f);
        tvTitle.setAlpha(0f);
        tvDesc.setAlpha(0f);
        tvIcon.animate().alpha(1f).setDuration(300).start();
        tvTitle.animate().alpha(1f).setDuration(400).start();
        tvDesc.animate().alpha(1f).setDuration(500).start();

        if (page == 2) {
            btnNext.setText("🚀 ابدأ الآن");
        } else {
            btnNext.setText("التالي ←");
        }

        updateDots();
    }

    private void updateDots() {
        for (int i = 0; i < 3; i++) {
            dots[i].setTextColor(i == currentPage ? GOLD : GRAY);
            dots[i].setTextSize(i == currentPage ? 22 : 16);
        }
    }

    private void goToIdentity() {
        // حفظ أن المستخدم شاهد الترحيب
        getSharedPreferences("ummah", MODE_PRIVATE).edit()
                .putBoolean("seen_welcome", true)
                .apply();

        startActivity(new Intent(this, IdentityCreationActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        if (currentPage > 0) {
            currentPage--;
            showPage(currentPage);
        } else {
            super.onBackPressed();
        }
    }
}
