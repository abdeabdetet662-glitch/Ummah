package com.ummah.admin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class AdminMainActivity extends Activity {

    private AdminManager am;
    private LinearLayout statsContainer;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        am = AdminManager.get();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0A"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 40, 20, 40);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("👑 لوحة تحكم أُمّة");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(26);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("تحكم كامل في الدولة");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 8, 0, 24);
        root.addView(sub);

        // إحصائيات
        statsContainer = new LinearLayout(this);
        statsContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(statsContainer);

        // الأزرار
        addBtn(root, "👥  المواطنون", "#0D47A1", AdminCitizensActivity.class);
        addBtn(root, "⚖️  الشكاوى", "#5D4037", AdminComplaintsActivity.class);
        addBtn(root, "🏛️  التصويت على الدستور", "#4E342E", AdminVotesActivity.class);
        addBtn(root, "📰  الاقتراحات", "#4A148C", AdminProposalsActivity.class);
        addBtn(root, "💬  إدارة الدردشة", "#00695C", AdminChatActivity.class);
        addBtn(root, "💰  الخزينة", "#1A237E", AdminTreasuryActivity.class);
        addBtn(root, "👑  الانتخابات", "#7B1FA2", AdminElectionActivity.class);

        // زر تسجيل الخروج
        Button logout = new Button(this);
        logout.setText("🚪 تسجيل الخروج");
        logout.setTextSize(14);
        logout.setTextColor(Color.parseColor("#F44336"));
        logout.setBackground(makeBg("#212121", 10));
        logout.setPadding(20, 30, 20, 30);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 30, 0, 0);
        logout.setLayoutParams(lp);
        logout.setOnClickListener(v -> {
            getSharedPreferences("admin", MODE_PRIVATE).edit().clear().apply();
            startActivity(new Intent(this, AdminLoginActivity.class));
            finish();
        });
        root.addView(logout);

        setContentView(scroll);
        loadStats();
    }

    private void loadStats() {
        am.loadStats(new AdminManager.StatsListener() {
            @Override public void onStats(final int citizens, final int gifts, final int transfers,
                                          final int news, final int proposals, final int complaints,
                                          final long treasury, final int votes) {
                runOnUiThread(() -> {
                    statsContainer.removeAllViews();
                    addStat(statsContainer, "👥", "المواطنون", String.valueOf(citizens), "#0D47A1");
                    addStat(statsContainer, "🎁", "الهدايا المُرسلة", String.valueOf(gifts), "#C2185B");
                    addStat(statsContainer, "💸", "التحويلات", String.valueOf(transfers), "#1B5E20");
                    addStat(statsContainer, "📰", "الأخبار", String.valueOf(news), "#4A148C");
                    addStat(statsContainer, "🗳️", "الاقتراحات", String.valueOf(proposals), "#00695C");
                    addStat(statsContainer, "⚖️", "الشكاوى", String.valueOf(complaints), "#5D4037");
                    addStat(statsContainer, "🏦", "الخزينة", treasury + " Đ", "#1A237E");
                    addStat(statsContainer, "🏛️", "أصوات الدستور", String.valueOf(votes), "#4E342E");
                });
            }
        });
    }

    private void addStat(LinearLayout parent, String emoji, String label, String value, String color) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackground(makeGradient(color, "#1A1A1A", 20));
        card.setPadding(24, 18, 24, 18);
        card.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 10);
        card.setLayoutParams(lp);

        TextView e = new TextView(this);
        e.setText(emoji);
        e.setTextSize(28);
        e.setPadding(0, 0, 20, 0);
        card.addView(e);

        TextView l = new TextView(this);
        l.setText(label);
        l.setTextColor(Color.WHITE);
        l.setTextSize(14);
        l.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        card.addView(l);

        TextView v = new TextView(this);
        v.setText(value);
        v.setTextColor(Color.parseColor("#FFD700"));
        v.setTextSize(20);
        v.setTypeface(null, Typeface.BOLD);
        card.addView(v);

        parent.addView(card);
    }

    private void addBtn(LinearLayout parent, String text, String color, final Class<?> cls) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextSize(16);
        btn.setTextColor(Color.WHITE);
        btn.setTypeface(null, Typeface.BOLD);
        btn.setBackground(makeBg(color, 10));
        btn.setPadding(20, 30, 20, 30);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 8, 0, 0);
        btn.setLayoutParams(lp);
        btn.setOnClickListener(v -> startActivity(new Intent(this, cls)));
        parent.addView(btn);
    }

    private GradientDrawable makeBg(String color, int r) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.parseColor(color));
        g.setCornerRadius(r * 3);
        return g;
    }

    private GradientDrawable makeGradient(String c1, String c2, int r) {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor(c1), Color.parseColor(c2)});
        g.setCornerRadius(r);
        return g;
    }
}
