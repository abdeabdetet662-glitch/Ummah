package com.ummah.app;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        // FullscreenHelper.enable(this);  // DISABLED - crash

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundResource(R.drawable.bg_screen);
        root.setPadding(60, 60, 60, 60);

        final TextView flag = new TextView(this);
        flag.setText("🌍");
        flag.setTextSize(140);
        flag.setGravity(Gravity.CENTER);
        flag.setAlpha(0f);
        flag.setScaleX(0.3f);
        flag.setScaleY(0.3f);
        root.addView(flag);

        final TextView title = new TextView(this);
        title.setText("أُمّة");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(72);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setAlpha(0f);
        title.setTranslationY(50);
        title.setPadding(0, 20, 0, 0);
        root.addView(title);

        final View line = new View(this);
        LinearLayout.LayoutParams lineLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2);
        lineLp.setMargins(80, 30, 80, 30);
        line.setLayoutParams(lineLp);
        line.setBackgroundColor(Color.parseColor("#D4AF37"));
        line.setAlpha(0f);
        root.addView(line);

        final TextView sub = new TextView(this);
        sub.setText("أول دولة رقمية عربية");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(16);
        sub.setGravity(Gravity.CENTER);
        sub.setAlpha(0f);
        sub.setTranslationY(30);
        root.addView(sub);

        final TextView footer = new TextView(this);
        footer.setText("☆  حوكمة رقمية حقيقية  ☆");
        footer.setTextColor(Color.parseColor("#616161"));
        footer.setTextSize(11);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, 80, 0, 0);
        footer.setAlpha(0f);
        root.addView(footer);

        setContentView(root);

        ObjectAnimator flagScaleX = ObjectAnimator.ofFloat(flag, "scaleX", 0.3f, 1f);
        ObjectAnimator flagScaleY = ObjectAnimator.ofFloat(flag, "scaleY", 0.3f, 1f);
        ObjectAnimator flagAlpha = ObjectAnimator.ofFloat(flag, "alpha", 0f, 1f);
        flagScaleX.setDuration(800);
        flagScaleY.setDuration(800);
        flagAlpha.setDuration(800);

        ObjectAnimator titleAlpha = ObjectAnimator.ofFloat(title, "alpha", 0f, 1f);
        ObjectAnimator titleY = ObjectAnimator.ofFloat(title, "translationY", 50, 0);
        titleAlpha.setDuration(700);
        titleY.setDuration(700);

        ObjectAnimator lineAlpha = ObjectAnimator.ofFloat(line, "alpha", 0f, 1f);
        lineAlpha.setDuration(600);

        ObjectAnimator subAlpha = ObjectAnimator.ofFloat(sub, "alpha", 0f, 1f);
        ObjectAnimator subY = ObjectAnimator.ofFloat(sub, "translationY", 30, 0);
        subAlpha.setDuration(700);
        subY.setDuration(700);

        ObjectAnimator footerAlpha = ObjectAnimator.ofFloat(footer, "alpha", 0f, 1f);
        footerAlpha.setDuration(700);

        AnimatorSet set1 = new AnimatorSet();
        set1.playTogether(flagScaleX, flagScaleY, flagAlpha);
        set1.setInterpolator(new DecelerateInterpolator());

        AnimatorSet set2 = new AnimatorSet();
        set2.playTogether(titleAlpha, titleY, lineAlpha);
        set2.setStartDelay(400);

        AnimatorSet set3 = new AnimatorSet();
        set3.playTogether(subAlpha, subY);
        set3.setStartDelay(800);

        AnimatorSet set4 = new AnimatorSet();
        set4.playTogether(footerAlpha);
        set4.setStartDelay(1200);

        AnimatorSet all = new AnimatorSet();
        all.playTogether(set1, set2, set3, set4);
        all.setInterpolator(new AccelerateDecelerateInterpolator());
        all.start();

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 2500);
    }
}
