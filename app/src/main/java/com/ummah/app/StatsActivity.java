package com.ummah.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * StatsActivity — إحصائيات المستخدم
 */
public class StatsActivity extends Activity {

    private IdentityManager im;
    private Citizen me;
    private StatsManager manager;

    private LinearLayout root;
    private LinearLayout activitiesContainer;
    private SimpleChartView chartView;

    private TextView visitsValue, daysValue, earnedValue, balanceValue;

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        im = new IdentityManager(this);
        me = im.getCitizen();
        manager = new StatsManager();

        if (me == null) {
            finish();
            return;
        }

        buildUI();
        loadData();
    }

    private void buildUI() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0a0510"));
        scroll.setFillViewport(true);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(50), dp(20), dp(50));
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        scroll.addView(root);

        // ═══ Header ═══
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, 0, 0, dp(20));

        TextView back = new TextView(this);
        back.setText("←");
        back.setTextColor(Color.parseColor("#D4AF37"));
        back.setTextSize(28);
        back.setPadding(0, 0, dp(16), 0);
        back.setOnClickListener(v -> finish());
        header.addView(back);

        TextView title = new TextView(this);
        title.setText("📊 إحصائياتي");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(24);
        title.setTypeface(null, Typeface.BOLD);
        title.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        header.addView(title);

        root.addView(header);

        // ═══ Profile Card ═══
        LinearLayout profileCard = new LinearLayout(this);
        profileCard.setOrientation(LinearLayout.HORIZONTAL);
        profileCard.setGravity(Gravity.CENTER_VERTICAL);
        profileCard.setBackgroundResource(R.drawable.bg_card);
        profileCard.setPadding(dp(20), dp(20), dp(20), dp(20));
        LinearLayout.LayoutParams pcLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        pcLp.setMargins(0, 0, 0, dp(20));
        profileCard.setLayoutParams(pcLp);

        TextView avatar = new TextView(this);
        avatar.setText("👤");
        avatar.setTextSize(40);
        avatar.setPadding(0, 0, dp(16), 0);
        profileCard.addView(avatar);

        LinearLayout profileInfo = new LinearLayout(this);
        profileInfo.setOrientation(LinearLayout.VERTICAL);
        profileInfo.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView name = new TextView(this);
        name.setText(me.name);
        name.setTextColor(Color.WHITE);
        name.setTextSize(18);
        name.setTypeface(null, Typeface.BOLD);
        profileInfo.addView(name);

        TextView uid = new TextView(this);
        uid.setText(me.nationalId);
        uid.setTextColor(Color.parseColor("#888888"));
        uid.setTextSize(11);
        profileInfo.addView(uid);

        profileCard.addView(profileInfo);
        root.addView(profileCard);

        // ═══ Stats Grid ═══
        LinearLayout grid1 = new LinearLayout(this);
        grid1.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams g1Lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        g1Lp.setMargins(0, 0, 0, dp(10));
        grid1.setLayoutParams(g1Lp);

        visitsValue = addStatCard(grid1, "👣", "زيارات", "0", "#3B82F6");
        daysValue = addStatCard(grid1, "📅", "أيام نشاط", "0", "#10B981");
        root.addView(grid1);

        LinearLayout grid2 = new LinearLayout(this);
        grid2.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams g2Lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        g2Lp.setMargins(0, 0, 0, dp(20));
        grid2.setLayoutParams(g2Lp);

        earnedValue = addStatCard(grid2, "💎", "كسبت", "0 Đ", "#F59E0B");
        balanceValue = addStatCard(grid2, "💰", "رصيدك", "0 Đ", "#D4AF37");
        root.addView(grid2);

        // ═══ Chart Section ═══
        TextView chartTitle = new TextView(this);
        chartTitle.setText("📈 تطور الرصيد (7 أيام)");
        chartTitle.setTextColor(Color.parseColor("#D4AF37"));
        chartTitle.setTextSize(16);
        chartTitle.setTypeface(null, Typeface.BOLD);
        chartTitle.setPadding(0, 0, 0, dp(12));
        root.addView(chartTitle);

        chartView = new SimpleChartView(this);
        LinearLayout.LayoutParams cvLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(220));
        cvLp.setMargins(0, 0, 0, dp(24));
        chartView.setLayoutParams(cvLp);
        chartView.setBackgroundResource(R.drawable.bg_card);
        root.addView(chartView);

        // ═══ Activities Section ═══
        TextView actTitle = new TextView(this);
        actTitle.setText("🕐 آخر النشاطات");
        actTitle.setTextColor(Color.parseColor("#D4AF37"));
        actTitle.setTextSize(16);
        actTitle.setTypeface(null, Typeface.BOLD);
        actTitle.setPadding(0, 0, 0, dp(12));
        root.addView(actTitle);

        activitiesContainer = new LinearLayout(this);
        activitiesContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(activitiesContainer);

        setContentView(scroll);
    }

    private TextView addStatCard(LinearLayout parent, String emoji, String label,
                                  String value, String color) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(5, 0, 5, 0);
        card.setLayoutParams(lp);

        TextView em = new TextView(this);
        em.setText(emoji);
        em.setTextSize(24);
        card.addView(em);

        TextView val = new TextView(this);
        val.setText(value);
        val.setTextColor(Color.parseColor(color));
        val.setTextSize(20);
        val.setTypeface(null, Typeface.BOLD);
        val.setPadding(0, dp(6), 0, dp(2));
        card.addView(val);

        TextView lab = new TextView(this);
        lab.setText(label);
        lab.setTextColor(Color.parseColor("#888888"));
        lab.setTextSize(11);
        card.addView(lab);

        parent.addView(card);
        return val;
    }

    private void loadData() {
        // ═══ الإحصائيات ═══
        manager.fetchStats(me.nationalId, new StatsManager.StatsCallback() {
            @Override
            public void onResult(StatsManager.UserStats stats) {
                runOnUiThread(() -> {
                    visitsValue.setText(String.valueOf(stats.totalVisits));
                    daysValue.setText(String.valueOf(stats.daysActive));
                    earnedValue.setText(formatAmount(stats.totalBalanceEarned) + " Đ");
                });
            }

            @Override
            public void onError(String e) {}
        });

        // ═══ الرصيد الحالي ═══
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("citizens").document(me.nationalId).get()
            .addOnSuccessListener(doc -> {
                Long bal = doc.getLong("balance");
                if (bal != null) {
                    balanceValue.setText(formatAmount(bal) + " Đ");
                }
            });

        // ═══ النشاطات ═══
        manager.fetchActivities(me.nationalId, 15, new StatsManager.ActivitiesCallback() {
            @Override
            public void onResult(List<StatsManager.ActivityItem> items) {
                runOnUiThread(() -> renderActivities(items));
            }
        });

        // ═══ الرسم البياني ═══
        manager.fetchBalanceHistory(me.nationalId, new StatsManager.ChartCallback() {
            @Override
            public void onResult(List<StatsManager.ChartPoint> points) {
                runOnUiThread(() -> chartView.setData(points));
            }
        });
    }

    private void renderActivities(List<StatsManager.ActivityItem> items) {
        activitiesContainer.removeAllViews();

        if (items.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("لا توجد نشاطات بعد");
            empty.setTextColor(Color.parseColor("#666666"));
            empty.setTextSize(13);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(30), 0, dp(30));
            activitiesContainer.addView(empty);
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.US);

        for (StatsManager.ActivityItem item : items) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackgroundResource(R.drawable.bg_card);
            row.setPadding(dp(16), dp(12), dp(16), dp(12));
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rlp.setMargins(0, 0, 0, dp(8));
            row.setLayoutParams(rlp);

            TextView icon = new TextView(this);
            icon.setText(getActivityIcon(item.type));
            icon.setTextSize(22);
            icon.setPadding(0, 0, dp(14), 0);
            row.addView(icon);

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            info.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView desc = new TextView(this);
            desc.setText(item.description != null ? item.description : item.type);
            desc.setTextColor(Color.WHITE);
            desc.setTextSize(13);
            info.addView(desc);

            TextView date = new TextView(this);
            date.setText(sdf.format(new Date(item.timestamp)));
            date.setTextColor(Color.parseColor("#666666"));
            date.setTextSize(10);
            info.addView(date);

            row.addView(info);

            if (item.amount != 0) {
                TextView amt = new TextView(this);
                String sign = item.amount > 0 ? "+" : "";
                amt.setText(sign + item.amount + " Đ");
                amt.setTextColor(item.amount > 0
                        ? Color.parseColor("#10B981")
                        : Color.parseColor("#DC2626"));
                amt.setTextSize(13);
                amt.setTypeface(null, Typeface.BOLD);
                row.addView(amt);
            }

            activitiesContainer.addView(row);
        }
    }

    private String getActivityIcon(String type) {
        if (type == null) return "📌";
        switch (type) {
            case "visit": return "👣";
            case "transfer": return "💸";
            case "gift": return "🎁";
            case "purchase": return "🛒";
            case "reward": return "🏆";
            case "sale": return "💰";
            default: return "📌";
        }
    }

    private String formatAmount(long v) {
        if (v >= 1_000_000) return String.format(Locale.US, "%.1fM", v / 1_000_000.0);
        if (v >= 1_000) return String.format(Locale.US, "%.1fK", v / 1_000.0);
        return String.valueOf(v);
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }
}
