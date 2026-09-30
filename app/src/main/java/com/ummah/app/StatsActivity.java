package com.ummah.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class StatsActivity extends Activity {
    private FirebaseManager fm;
    private TextView citizensView, transfersView, totalView, newsView, proposalsView;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        // Feature Check
        if (!FeatureFlags.checkOrToast(this, "stats")) {
            finish();
            return;
        }

        fm = FirebaseManager.get();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0A"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 60, 40, 60);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("📊 إحصائيات أُمّة");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("دولتك في الوقت الحقيقي");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 8, 0, 40);
        root.addView(sub);

        citizensView = addStatCard(root, "👥", "المواطنون", "الحساب...");
        transfersView = addStatCard(root, "💸", "التحويلات", "الحساب...");
        totalView = addStatCard(root, "💰", "إجمالي المُحوّل", "الحساب...");
        newsView = addStatCard(root, "📰", "الأخبار", "الحساب...");
        proposalsView = addStatCard(root, "🗳️", "الاقتراحات", "الحساب...");

        setContentView(scroll);
        load();
    }

    private TextView addStatCard(LinearLayout parent, String emoji, String label, String value) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackgroundColor(Color.parseColor("#141414"));
        card.setPadding(28, 24, 28, 24);
        card.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 14);
        card.setLayoutParams(lp);

        TextView e = new TextView(this);
        e.setText(emoji);
        e.setTextSize(30);
        e.setPadding(0, 0, 20, 0);
        card.addView(e);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView l = new TextView(this);
        l.setText(label);
        l.setTextColor(Color.parseColor("#9E9E9E"));
        l.setTextSize(12);
        info.addView(l);

        TextView v = new TextView(this);
        v.setText(value);
        v.setTextColor(Color.parseColor("#D4AF37"));
        v.setTextSize(22);
        v.setTypeface(null, Typeface.BOLD);
        info.addView(v);

        card.addView(info);
        parent.addView(card);
        return v;
    }

    private void load() {
        fm.loadStats(new FirebaseManager.StatsListener() {
            @Override public void onStats(final int citizens, final int transfers,
                                          final int total, final int news, final int proposals) {
                runOnUiThread(() -> {
                    citizensView.setText(citizens + " مواطن");
                    transfersView.setText(transfers + " تحويل");
                    totalView.setText(total + " Đ");
                    newsView.setText(news + " خبر");
                    proposalsView.setText(proposals + " اقتراح");
                });
            }
        });
    }
}
