package com.ummah.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration;

public class LifeStatsActivity extends Activity {

    private IdentityManager im;
    private LifeStatsManager lm;
    private ListenerRegistration reg;

    private TextView hungerVal, energyVal, happinessVal, healthVal;
    private TextView balanceView;
    private ProgressBar hungerBar, energyBar, happinessBar, healthBar;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        im = new IdentityManager(this);
        lm = new LifeStatsManager();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 50, 30, 60);
        scroll.addView(root);

        TextView title = UiHelper.goldTitle(this, "❤️  حياتي", 28);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("حافظ على صحتك وسعادتك");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 24);
        root.addView(sub);

        // بطاقة الرصيد
        LinearLayout balCard = UiHelper.card(this);
        balCard.setGravity(Gravity.CENTER);

        TextView balLabel = new TextView(this);
        balLabel.setText("💰  رصيدك");
        balLabel.setTextColor(Color.parseColor("#9E9E9E"));
        balLabel.setTextSize(12);
        balLabel.setGravity(Gravity.CENTER);
        balCard.addView(balLabel);

        balanceView = new TextView(this);
        balanceView.setText("...");
        balanceView.setTextColor(Color.parseColor("#D4AF37"));
        balanceView.setTextSize(36);
        balanceView.setTypeface(null, Typeface.BOLD);
        balanceView.setGravity(Gravity.CENTER);
        balCard.addView(balanceView);

        root.addView(balCard);

        // شريط الجوع
        hungerBar = addStatCard(root, "🍔", "الجوع", "#E65100");
        hungerVal = (TextView) ((LinearLayout) ((LinearLayout) hungerBar.getParent()).getParent()).getChildAt(0);
        // نبنيو الأزرار
        addActionButton(root, "🍔  تناول وجبة (-50 Đ)", "#E65100", 50, "hunger", 30);

        // شريط الطاقة
        energyBar = addStatCard(root, "⚡", "الطاقة", "#1976D2");
        addActionButton(root, "🛏  ارتاح قليلاً (-30 Đ)", "#1976D2", 30, "energy", 25);

        // شريط السعادة
        happinessBar = addStatCard(root, "😊", "السعادة", "#F9A825");
        addActionButton(root, "🎬  اذهب للترفيه (-40 Đ)", "#F9A825", 40, "happiness", 20);

        // شريط الصحة
        healthBar = addStatCard(root, "❤️", "الصحة", "#C62828");
        addActionButton(root, "💊  تناول دواء (-60 Đ)", "#C62828", 60, "health", 30);

        setContentView(scroll);

        startListeners();
    }

    private ProgressBar addStatCard(LinearLayout root, String emoji, String label, String color) {
        LinearLayout card = UiHelper.card(this);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 0, 0, 12);

        TextView e = new TextView(this);
        e.setText(emoji);
        e.setTextSize(30);
        e.setPadding(0, 0, 20, 0);
        row.addView(e);

        TextView t = new TextView(this);
        t.setText(label);
        t.setTextColor(Color.WHITE);
        t.setTextSize(18);
        t.setTypeface(null, Typeface.BOLD);
        t.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(t);

        TextView val = new TextView(this);
        val.setText("100");
        val.setTextColor(Color.parseColor(color));
        val.setTextSize(20);
        val.setTypeface(null, Typeface.BOLD);
        row.addView(val);

        card.addView(row);

        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(100);
        bar.setProgress(100);
        LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 30);
        bar.setLayoutParams(barLp);
        card.addView(bar);

        root.addView(card);

        // نخزنو الـ TextView في الـ tag
        bar.setTag(val);

        return bar;
    }

    private void addActionButton(LinearLayout root, String text, String color, int cost,
                                   String field, int amount) {
        Button btn = UiHelper.actionButton(this, text, color);
        btn.setOnClickListener(v -> doAction(field, cost, amount));
        root.addView(btn);
    }

    private void doAction(String field, int cost, int amount) {
        final Citizen c = im.getCitizen();
        if (c == null) return;

        lm.spendForAction(c.nationalId, cost, new LifeStatsManager.OnDone() {
            @Override public void onSuccess() {
                if ("hunger".equals(field)) {
                    lm.eat(c.nationalId, amount, done());
                } else if ("energy".equals(field)) {
                    lm.sleep(c.nationalId, amount, done());
                } else if ("happiness".equals(field)) {
                    lm.entertain(c.nationalId, amount, done());
                } else if ("health".equals(field)) {
                    lm.heal(c.nationalId, amount, done());
                }
            }
            @Override public void onError(String msg) {
                Toast.makeText(LifeStatsActivity.this, "❌ " + msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private LifeStatsManager.OnDone done() {
        return new LifeStatsManager.OnDone() {
            @Override public void onSuccess() {
                Toast.makeText(LifeStatsActivity.this, "✅ تم!", Toast.LENGTH_SHORT).show();
            }
            @Override public void onError(String msg) {
                Toast.makeText(LifeStatsActivity.this, "❌ " + msg, Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void startListeners() {
        Citizen c = im.getCitizen();
        if (c == null) { finish(); return; }

        // الرصيد
        FirebaseManager.get().listenBalance(c.nationalId, new FirebaseManager.BalanceListener() {
            @Override public void onBalance(int balance) {
                runOnUiThread(() -> balanceView.setText(balance + " Đ"));
            }
            @Override public void onError(String m) {}
        });

        // الإحصائيات
        if (reg != null) reg.remove();
        reg = lm.listenStats(c.nationalId, new LifeStatsManager.StatsListener() {
            @Override public void onStats(LifeStatsManager.Stats s) {
                runOnUiThread(() -> {
                    hungerBar.setProgress(s.hunger);
                    energyBar.setProgress(s.energy);
                    happinessBar.setProgress(s.happiness);
                    healthBar.setProgress(s.health);

                    ((TextView) hungerBar.getTag()).setText(String.valueOf(s.hunger));
                    ((TextView) energyBar.getTag()).setText(String.valueOf(s.energy));
                    ((TextView) happinessBar.getTag()).setText(String.valueOf(s.happiness));
                    ((TextView) healthBar.getTag()).setText(String.valueOf(s.health));
                });
            }
            @Override public void onError(String msg) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) reg.remove();
    }
}
