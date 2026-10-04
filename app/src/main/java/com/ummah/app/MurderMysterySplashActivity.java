package com.ummah.app;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * MurderMysterySplashActivity — شاشة افتتاحية سينمائية
 *
 * المشهد:
 *  1. ضباب داكن
 *  2. عدسة مكبرة تطلع من الأسفل
 *  3. صوت نبض
 *  4. عنوان "جريمة أُمّة"
 *  5. كتابة آلة كاتبة
 *  6. انتقال للـ Lobby
 */
public class MurderMysterySplashActivity extends Activity {

    private LinearLayout root;
    private TextView typewriterView;
    private Handler handler = new Handler();

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        // ═══ الخلفية Noir ═══
        FrameLayout container = new FrameLayout(this);
        container.setBackgroundResource(R.drawable.bg_mystery_noir);

        // ═══ طبقة الضباب ═══
        View fog1 = createFogLayer();
        container.addView(fog1);

        // ═══ المحتوى المركزي ═══
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams rootLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        root.setLayoutParams(rootLp);

        // ═══ العدسة المكبرة 🔍 ═══
        TextView magnifier = new TextView(this);
        magnifier.setText("🔍");
        magnifier.setTextSize(120);
        magnifier.setGravity(Gravity.CENTER);
        magnifier.setAlpha(0f);
        root.addView(magnifier);

        // أنيميشن ظهور العدسة
        magnifier.animate()
                .alpha(1f)
                .scaleX(1.2f)
                .scaleY(1.2f)
                .rotation(-15f)
                .setDuration(1200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    // نبض العدسة
                    ObjectAnimator pulse = ObjectAnimator.ofFloat(magnifier, "scaleX", 1.2f, 1.35f);
                    pulse.setRepeatCount(ValueAnimator.INFINITE);
                    pulse.setRepeatMode(ValueAnimator.REVERSE);
                    pulse.setDuration(900);
                    pulse.start();

                    ObjectAnimator pulseY = ObjectAnimator.ofFloat(magnifier, "scaleY", 1.2f, 1.35f);
                    pulseY.setRepeatCount(ValueAnimator.INFINITE);
                    pulseY.setRepeatMode(ValueAnimator.REVERSE);
                    pulseY.setDuration(900);
                    pulseY.start();
                })
                .start();

        // ═══ العنوان الرئيسي ═══
        TextView title = new TextView(this);
        title.setText("جريمة أُمّة");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(42);
        title.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        title.setGravity(Gravity.CENTER);
        title.setAlpha(0f);
        title.setLetterSpacing(0.15f);
        title.setPadding(0, 32, 0, 8);
        root.addView(title);

        // ظهور العنوان
        title.animate()
                .alpha(1f)
                .setDuration(1500)
                .setStartDelay(800)
                .start();

        // ═══ خط فاصل ذهبي ═══
        View divider = new View(this);
        LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(0, 2);
        divLp.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200,
                getResources().getDisplayMetrics());
        divider.setLayoutParams(divLp);
        divider.setBackgroundColor(Color.parseColor("#D4AF37"));
        divider.setAlpha(0f);
        root.addView(divider);

        divider.animate()
                .alpha(1f)
                .setDuration(800)
                .setStartDelay(1500)
                .start();

        // ═══ الكتابة الآلية ═══
        typewriterView = new TextView(this);
        typewriterView.setText("");
        typewriterView.setTextColor(Color.parseColor("#9E9E9E"));
        typewriterView.setTextSize(14);
        typewriterView.setTypeface(Typeface.MONOSPACE);
        typewriterView.setGravity(Gravity.CENTER);
        typewriterView.setPadding(60, 24, 60, 0);
        typewriterView.setAlpha(0f);
        root.addView(typewriterView);

        typewriterView.animate()
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(2000)
                .withEndAction(this::startTypewriter)
                .start();

        // ═══ السطر السفلي ═══
        TextView footer = new TextView(this);
        footer.setText("· كل أسبوع · جريمة جديدة ·");
        footer.setTextColor(Color.parseColor("#666666"));
        footer.setTextSize(12);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, 40, 0, 0);
        footer.setAlpha(0f);
        root.addView(footer);

        footer.animate()
                .alpha(1f)
                .setDuration(1000)
                .setStartDelay(3500)
                .start();

        container.addView(root);
        setContentView(container);

        // ═══ الانتقال بعد 6 ثواني ═══
        handler.postDelayed(() -> {
            startActivity(new Intent(this, MurderMysteryLobbyActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 6000);
    }

    /** تأثير الكتابة الآلية */
    private void startTypewriter() {
        String text = "\"الجريمة الكاملة... هي التي لم تُكتشف بعد.\"";
        final int[] i = {0};

        Runnable type = new Runnable() {
            @Override
            public void run() {
                if (i[0] <= text.length()) {
                    typewriterView.setText(text.substring(0, i[0]));
                    i[0]++;
                    handler.postDelayed(this, 80);
                }
            }
        };
        handler.post(type);
    }

    /** طبقة الضباب */
    private View createFogLayer() {
        View fog = new View(this);
        fog.setBackgroundColor(Color.parseColor("#20000000"));
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        fog.setLayoutParams(lp);

        // أنيميشن تحريك الضباب
        TranslateAnimation drift = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, -0.15f,
                Animation.RELATIVE_TO_SELF, 0.15f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f);
        drift.setDuration(8000);
        drift.setRepeatCount(Animation.INFINITE);
        drift.setRepeatMode(Animation.REVERSE);
        fog.startAnimation(drift);

        return fog;
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
