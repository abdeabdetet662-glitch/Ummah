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

import com.google.firebase.firestore.ListenerRegistration;

public class PresidentDashboardActivity extends Activity {

    private IdentityManager im;
    private PresidentManager pm;
    private LinearLayout statsContainer;
    private ListenerRegistration treasuryReg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        im = new IdentityManager(this);
        pm = new PresidentManager();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(30, 50, 30, 60);
        scroll.addView(root);

        // ═══ Header ═══
        TextView crown = new TextView(this);
        crown.setText("👑");
        crown.setTextSize(72);
        crown.setGravity(Gravity.CENTER);
        root.addView(crown);

        TextView title = UiHelper.goldTitle(this, "لوحة الرئيس", 28);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("أدوات القيادة العليا");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 24);
        root.addView(sub);

        // ═══ Treasury ═══
        statsContainer = new LinearLayout(this);
        statsContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(statsContainer);

        // ═══ Sections ═══
        addSection(root, "📢  الإعلام والاتصال");
        addCard(root, "📢", "إعلان رئاسي", "رسالة رسمية لكل المواطنين",
                PresidentAnnounceActivity.class, "#C62828");

        addSection(root, "🎁  السلطات الرئاسية");
        addCard(root, "🎁", "هدية رئاسية", "وزّع فلوس من الخزينة",
                PresidentGiftActivity.class, "#2E7D32");
        addCard(root, "⚖️", "عفو رئاسي", "ارفع الحظر والكتم",
                PresidentPardonActivity.class, "#5D4037");

        addSection(root, "🏛️  الحكومة");
        addCard(root, "👥", "تعيين وزراء", "اختر مواطنين للمناصب",
                PresidentMinistersActivity.class, "#4A148C");
        addCard(root, "🏦", "التحكم في الخزينة", "أضف أو اسحب أموال",
                PresidentTreasuryActivity.class, "#1A237E");

        addSection(root, "🎖️  الألقاب والمراسيم");
        addCard(root, "🏅", "منح الألقاب", "امنح مواطن لقب شرفي",
                PresidentTitlesActivity.class, "#E65100");
        addCard(root, "📜", "إصدار مرسوم", "قرار رسمي للدولة",
                PresidentDecreesActivity.class, "#00695C");

        addSection(root, "📊  السجلات");
        addCard(root, "📋", "الإعلانات السابقة", "كل الإعلانات الرئاسية",
                PresidentAnnouncementsListActivity.class, "#0D47A1");

        setContentView(scroll);
        startTreasuryListener();
    }

    private void startTreasuryListener() {
        if (treasuryReg != null) treasuryReg.remove();
        treasuryReg = pm.listenTreasury(balance -> runOnUiThread(() -> {
            statsContainer.removeAllViews();
            addStatCard("🏦", "رصيد الخزينة", balance + " Đ", "#FFD700");
        }));
    }

    private void addStatCard(String emoji, String label, String value, String color) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackgroundResource(R.drawable.bg_president_card);
        card.setPadding(28, 24, 28, 24);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 8, 0, 20);
        card.setLayoutParams(lp);

        TextView e = new TextView(this);
        e.setText(emoji);
        e.setTextSize(36);
        e.setPadding(0, 0, 20, 0);
        card.addView(e);

        TextView l = new TextView(this);
        l.setText(label);
        l.setTextColor(Color.WHITE);
        l.setTextSize(14);
        l.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        card.addView(l);

        TextView v = new TextView(this);
        v.setText(value);
        v.setTextColor(Color.parseColor(color));
        v.setTextSize(22);
        v.setTypeface(null, Typeface.BOLD);
        card.addView(v);

        statsContainer.addView(card);
    }

    private void addSection(LinearLayout root, String text) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        container.setPadding(0, 32, 0, 12);

        View lineL = new View(this);
        LinearLayout.LayoutParams lLp = new LinearLayout.LayoutParams(0, 2, 1f);
        lineL.setLayoutParams(lLp);
        lineL.setBackgroundColor(Color.parseColor("#2A3D32"));
        container.addView(lineL);

        TextView t = new TextView(this);
        t.setText("  " + text + "  ");
        t.setTextColor(Color.parseColor("#D4AF37"));
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

    private void addCard(LinearLayout root, String emoji, String title, String subtitle,
                          final Class<?> cls, String color) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackgroundResource(R.drawable.bg_feature_card);
        card.setPadding(24, 24, 24, 24);
        card.setClickable(true);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 8, 0, 8);
        card.setLayoutParams(lp);

        TextView icon = new TextView(this);
        icon.setText(emoji);
        icon.setTextSize(28);
        icon.setGravity(Gravity.CENTER);
        icon.setBackgroundResource(R.drawable.bg_icon_circle);
        LinearLayout.LayoutParams icLp = new LinearLayout.LayoutParams(80, 80);
        icLp.setMargins(0, 0, 20, 0);
        icon.setLayoutParams(icLp);
        card.addView(icon);

        LinearLayout txt = new LinearLayout(this);
        txt.setOrientation(LinearLayout.VERTICAL);
        txt.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView t1 = new TextView(this);
        t1.setText(title);
        t1.setTextColor(Color.WHITE);
        t1.setTextSize(16);
        t1.setTypeface(null, Typeface.BOLD);
        txt.addView(t1);

        TextView t2 = new TextView(this);
        t2.setText(subtitle);
        t2.setTextColor(Color.parseColor("#9E9E9E"));
        t2.setTextSize(11);
        t2.setPadding(0, 4, 0, 0);
        txt.addView(t2);

        card.addView(txt);

        TextView arrow = new TextView(this);
        arrow.setText("›");
        arrow.setTextColor(Color.parseColor("#D4AF37"));
        arrow.setTextSize(28);
        card.addView(arrow);

        card.setOnClickListener(v -> {
            AnimHelper.pressEffect(v);
            AnimHelper.lightHaptic(this);
            startActivity(new Intent(this, cls));
        });

        root.addView(card);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (treasuryReg != null) treasuryReg.remove();
    }
}
