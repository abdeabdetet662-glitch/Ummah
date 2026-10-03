package com.ummah.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ElectionCelebrationActivity extends Activity {

    private CelebrationView confetti;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        String name = getIntent().getStringExtra("president_name");
        String slogan = getIntent().getStringExtra("president_slogan");
        int votes = getIntent().getIntExtra("votes", 0);
        if (name == null) name = "الرئيس";
        if (slogan == null || slogan.isEmpty()) slogan = getString(R.string.ecel_together);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor("#0A0A0A"));
        root.setOnClickListener(v -> finish());

        confetti = new CelebrationView(this);
        root.addView(confetti, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout center = new LinearLayout(this);
        center.setOrientation(LinearLayout.VERTICAL);
        center.setGravity(Gravity.CENTER);
        center.setPadding(60, 60, 60, 60);
        root.addView(center, new FrameLayout.LayoutParams(-1, -1));

        TextView crown = new TextView(this);
        crown.setText("👑");
        crown.setTextSize(130);
        crown.setGravity(Gravity.CENTER);
        crown.setAlpha(0f);
        crown.setScaleX(0f);
        crown.setScaleY(0f);
        center.addView(crown);

        TextView line1 = mkText(getString(R.string.ecel_announce_election), 20, "#9E9E9E", false, 30);
        center.addView(line1);
        TextView presName = mkText(name, 46, "#D4AF37", true, 20);
        center.addView(presName);
        TextView line3 = mkText(getString(R.string.ecel_as_president), 24, "#FFFFFF", false, 0);
        center.addView(line3);
        TextView slog = mkText("« " + slogan + " »", 18, "#FFD700", false, 40);
        center.addView(slog);
        TextView votesV = mkText("بأصوات " + votes + getString(R.string.ecel_citizen), 16, "#4CAF50", false, 20);
        center.addView(votesV);
        TextView close = mkText("(اضغط في أي مكان للإغلاق)", 11, "#616161", false, 60);
        close.setAlpha(0f);
        center.addView(close);

        setContentView(root);

        animateIn(crown, 200, 900, 1.2f);
        animateIn(line1, 1000, 500, 1f);
        animateIn(presName, 1400, 900, 1.1f);
        animateIn(line3, 2100, 500, 1f);
        animateIn(slog, 2500, 500, 1f);
        animateIn(votesV, 2900, 500, 1f);
        close.postDelayed(() -> close.animate().alpha(1f).setDuration(400).start(), 3500);
        vibrate(400);
    }

    private TextView mkText(String text, int size, String color, boolean bold, int topPad) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(size);
        tv.setTextColor(Color.parseColor(color));
        if (bold) tv.setTypeface(null, Typeface.BOLD);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, topPad, 0, 0);
        tv.setAlpha(0f);
        return tv;
    }

    private void animateIn(final TextView tv, long delay, long duration, final float scale) {
        tv.postDelayed(() -> {
            tv.animate().alpha(1f).scaleX(scale).scaleY(scale)
                .setDuration(duration).setInterpolator(new OvershootInterpolator()).start();
            vibrate(70);
        }, delay);
    }

    private void vibrate(long ms) {
        try {
            android.os.Vibrator v = (android.os.Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (v != null) v.vibrate(ms);
        } catch (Exception e) {}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (confetti != null) confetti.stop();
    }

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

}
