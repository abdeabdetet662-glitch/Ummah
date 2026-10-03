package com.ummah.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.firestore.ListenerRegistration;

public class HomeTabBuilder {

    public interface OnHomeReady {
        void onReady(TextView balanceView, TextView countView, TextView onlineView);
    }

    private final Activity act;
    private final LinearLayout root;
    private XpManager xpManager;
    private ListenerRegistration presidentReg;
    private LinearLayout presidentTagContainer;
    private boolean isUserPresident = false;
    private LinearLayout presidentDashboardBtn = null;

    public HomeTabBuilder(Activity act, LinearLayout root) {
        this.act = act;
        this.root = root;
        this.xpManager = new XpManager(act);
    }

    public void build(Citizen citizen, String countryName, final OnHomeReady cb) {
        // ═══ Hero Card ═══
        addHeroCard(citizen, countryName);

        // ═══ Mini Stats (3) ═══
        TextView[] stats = addMiniStats();
        TextView balanceView = stats[0];
        TextView countView = stats[1];
        TextView onlineView = stats[2];

        // ═══ Quick Actions Title ═══
        addSectionTitle("⚡  إجراءات سريعة");

        // ═══ Primary (3 big cards) ═══
        addBigFeature("💰", "محفظتي", "رصيدك وحوالاتك", WalletActivity.class, 0);
        addBigFeature("🛒", "السوق العام", "12 منتج متوفر", MarketActivity.class, 60);
        addBigFeature("🎒", "ممتلكاتي", "عرض وبيع الممتلكات", MyInventoryActivity.class, 120);

        // ═══ Section: الحياة اليومية ═══
        addSectionTitle("🎯  الحياة اليومية");

        addGridFeature(
                new Feature("🎁", "مكافأة اليوم", DailyRewardActivity.class),
                new Feature("💼", "الوظائف", JobsActivity.class)
        );

        addGridFeature(
                new Feature("❤️", "حياتي", LifeStatsActivity.class),
                new Feature("🚗", "مرآبي", GarageActivity.class)
        );

        addGridFeature(
                new Feature("👤", "ملفي", ProfileActivity.class),
                new Feature("🏙️", "المدينة", CityMapActivity.class)
        );

        addGridFeature(
                new Feature("🎡", "عجلة الحظ", WheelActivity.class),
                new Feature("💰", "الخزينة", TreasuryActivity.class)
        );

        // ═══ Section: المجتمع ═══
        addSectionTitle("👥  المجتمع");

        addGridFeature(
                new Feature("💬", "دردشة أُمّة", ChatActivity.class),
                new Feature("👥", "المواطنون", CitizensActivity.class)
        );

        addGridFeature(
                new Feature("🏆", "المتصدرون", LeaderboardActivity.class),
                new Feature("🤝", "سوق المواطنين", CitizenMarketActivity.class)
        );

        addGridFeature(
                new Feature("🎁", "الهدايا", GiftsActivity.class),
                new Feature("📊", "الإحصائيات", StatsActivity.class)
        );

        // ═══ Section: الحكم والسياسة ═══
        addSectionTitle("🏛️  الحكم والسياسة");

        addGridFeature(
                new Feature("🗳️", "البرلمان", ParliamentActivity.class),
                new Feature("👑", "الانتخابات", ElectionActivity.class)
        );

        addGridFeature(
                new Feature("📜", "الدستور", ConstitutionActivity.class),
                new Feature("⚖️", "المحكمة", CourtActivity.class)
        );

        addGridFeature(
                new Feature("🏦", "الخزينة", TreasuryActivity.class),
                new Feature("📰", "الأخبار", NewsActivity.class)
        );

        // ═══ Section: أخرى ═══
        addSectionTitle("📌  أخرى");

        addSecondarySmall("🔐  استعادة الحساب", AccountRecoveryActivity.class);
        addSecondarySmall("🔑  الكلمات السرية", null); // خاص

        // ═══ Footer ═══
        addFooter();

        // نرجعو الـ TextViews
        if (cb != null) cb.onReady(balanceView, countView, onlineView);

        // نراقبو حالة الرئيس
        startPresidentListener(citizen.nationalId);
    }

    private void startPresidentListener(final String nationalId) {
        if (presidentReg != null) presidentReg.remove();
        presidentReg = FirebaseManager.get().listenPresidentStatus(nationalId,
                new FirebaseManager.PresidentListener() {
            @Override public void onStatus(final boolean isPresident) {
                act.runOnUiThread(() -> {
                    isUserPresident = isPresident;
                    if (presidentTagContainer != null) {
                        presidentTagContainer.setVisibility(isPresident ? View.VISIBLE : View.GONE);
                    }
                    updatePresidentDashboardButton();
                });
            }
            @Override public void onError(String msg) {}
        });
    }

    private void updatePresidentDashboardButton() {
        if (isUserPresident) {
            if (presidentDashboardBtn == null) {
                addSectionTitle("\uD83D\uDC51  صلاحيات الرئيس");

                presidentDashboardBtn = new LinearLayout(act);
                presidentDashboardBtn.setOrientation(LinearLayout.HORIZONTAL);
                presidentDashboardBtn.setGravity(Gravity.CENTER_VERTICAL);
                presidentDashboardBtn.setBackgroundResource(R.drawable.bg_president_card);
                presidentDashboardBtn.setPadding(28, 28, 28, 28);
                presidentDashboardBtn.setClickable(true);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 8, 0, 8);
                presidentDashboardBtn.setLayoutParams(lp);

                TextView crown = new TextView(act);
                crown.setText("\uD83D\uDC51");
                crown.setTextSize(36);
                crown.setPadding(0, 0, 20, 0);
                presidentDashboardBtn.addView(crown);

                LinearLayout info = new LinearLayout(act);
                info.setOrientation(LinearLayout.VERTICAL);
                info.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                TextView t1 = new TextView(act);
                t1.setText("لوحة الرئيس");
                t1.setTextColor(Color.parseColor("#FFD700"));
                t1.setTextSize(18);
                t1.setTypeface(null, Typeface.BOLD);
                info.addView(t1);

                TextView t2 = new TextView(act);
                t2.setText("إعلانات \u2022 هدايا \u2022 تعيينات \u2022 مراسيم");
                t2.setTextColor(Color.parseColor("#FFFFFF"));
                t2.setTextSize(11);
                t2.setPadding(0, 6, 0, 0);
                info.addView(t2);

                presidentDashboardBtn.addView(info);

                TextView arrow = new TextView(act);
                arrow.setText("\u203A");
                arrow.setTextColor(Color.parseColor("#FFD700"));
                arrow.setTextSize(32);
                presidentDashboardBtn.addView(arrow);

                presidentDashboardBtn.setOnClickListener(v -> {
                    AnimHelper.pressEffect(v);
                    AnimHelper.mediumHaptic(act);
                    act.startActivity(new Intent(act, PresidentDashboardActivity.class));
                });

                root.addView(presidentDashboardBtn);
                AnimHelper.fadeInUp(presidentDashboardBtn, 0);
            }
        } else {
            if (presidentDashboardBtn != null) {
                root.removeView(presidentDashboardBtn);
                presidentDashboardBtn = null;
            }
        }
    }

    // ═══════════════════════════════════════
    //  Hero Card
    // ═══════════════════════════════════════
    private void addHeroCard(Citizen c, String countryName) {
        LinearLayout card = new LinearLayout(act);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_hero_card);
        card.setPadding(32, 32, 32, 32);
        card.setElevation(14f);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 10, 0, 20);
        card.setLayoutParams(lp);

        // Header (Avatar + Name + ID)
        LinearLayout header = new LinearLayout(act);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        // Avatar Circle
        TextView avatar = new TextView(act);
        avatar.setText("👤");
        avatar.setTextSize(32);
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackgroundResource(R.drawable.bg_avatar_circle);
        LinearLayout.LayoutParams avLp = new LinearLayout.LayoutParams(100, 100);
        avLp.setMargins(0, 0, 20, 0);
        avatar.setLayoutParams(avLp);
        header.addView(avatar);

        // Name + ID
        LinearLayout nameBox = new LinearLayout(act);
        nameBox.setOrientation(LinearLayout.VERTICAL);
        nameBox.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView name = new TextView(act);
        name.setText(c.name);
        name.setTextColor(Color.WHITE);
        name.setTextSize(22);
        name.setTypeface(null, Typeface.BOLD);
        nameBox.addView(name);

        TextView id = new TextView(act);
        id.setText(c.nationalId);
        id.setTextColor(Color.parseColor("#D4AF37"));
        id.setTextSize(11);
        id.setPadding(0, 6, 0, 0);
        nameBox.addView(id);

        header.addView(nameBox);
        card.addView(header);

        // Line
        View line = new View(act);
        LinearLayout.LayoutParams lineLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        lineLp.setMargins(0, 20, 0, 16);
        line.setLayoutParams(lineLp);
        line.setBackgroundColor(Color.parseColor("#2A3D32"));
        card.addView(line);

        // ═══ President Tag (يظهر فقط للرؤساء) ═══
        presidentTagContainer = new LinearLayout(act);
        presidentTagContainer.setOrientation(LinearLayout.HORIZONTAL);
        presidentTagContainer.setGravity(Gravity.CENTER);
        presidentTagContainer.setVisibility(View.GONE);
        presidentTagContainer.setPadding(0, 16, 0, 0);

        LinearLayout tag = PresidentBadgeHelper.createPresidentTag(act);
        presidentTagContainer.addView(tag);
        card.addView(presidentTagContainer);

        // Country + Join Date
        LinearLayout infoRow = new LinearLayout(act);
        infoRow.setOrientation(LinearLayout.HORIZONTAL);

        addInfoItem(infoRow, "🌍", countryName);
        addInfoItem(infoRow, "📅", c.joinDate);

        card.addView(infoRow);

        // ═══ XP Bar ═══
        addXpBar(card);

        root.addView(card);
        AnimHelper.fadeInUp(card, 0);
    }


    // ═══════════════════════════════════════════
    //  XP Bar (المستوى + التقدم)
    // ═══════════════════════════════════════════
    private void addXpBar(LinearLayout card) {
        if (xpManager == null) return;

        int level = xpManager.getLevel();
        int progress = xpManager.getProgressPercent();
        int xpRemaining = xpManager.getXpRemaining();
        String title = xpManager.getTitle();

        // ═══ فاصل ═══
        View sep = new View(act);
        LinearLayout.LayoutParams sepLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        sepLp.setMargins(0, 20, 0, 16);
        sep.setLayoutParams(sepLp);
        sep.setBackgroundColor(Color.parseColor("#2A3D32"));
        card.addView(sep);

        // ═══ الصف الأول: Title + Level ═══
        LinearLayout topRow = new LinearLayout(act);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView titleView = new TextView(act);
        titleView.setText(title);
        titleView.setTextColor(Color.parseColor("#D4AF37"));
        titleView.setTextSize(14);
        titleView.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        titleView.setLayoutParams(titleLp);
        topRow.addView(titleView);

        TextView levelView = new TextView(act);
        levelView.setText("📊 " + level + "/100");
        levelView.setTextColor(Color.WHITE);
        levelView.setTextSize(13);
        levelView.setTypeface(null, Typeface.BOLD);
        topRow.addView(levelView);

        card.addView(topRow);

        // ═══ Progress Bar ═══
        LinearLayout progressBg = new LinearLayout(act);
        progressBg.setOrientation(LinearLayout.HORIZONTAL);
        progressBg.setBackgroundColor(Color.parseColor("#1A1A1A"));
        LinearLayout.LayoutParams bgLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 20);
        bgLp.setMargins(0, 12, 0, 8);
        progressBg.setLayoutParams(bgLp);
        progressBg.setPadding(2, 2, 2, 2);

        View progressFill = new View(act);
        LinearLayout.LayoutParams fillLp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, (float) progress / 100);
        progressFill.setLayoutParams(fillLp);
        // لون متدرج حسب النسبة
        int fillColor;
        if (progress >= 80) fillColor = Color.parseColor("#4CAF50");
        else if (progress >= 50) fillColor = Color.parseColor("#FFC107");
        else if (progress >= 25) fillColor = Color.parseColor("#FF9800");
        else fillColor = Color.parseColor("#F44336");
        progressFill.setBackgroundColor(fillColor);
        progressBg.addView(progressFill);

        // نضيفو عنصر وهمي باش ياخذ المساحة المتبقية
        View progressRest = new View(act);
        LinearLayout.LayoutParams restLp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, (float) (100 - progress) / 100);
        progressRest.setLayoutParams(restLp);
        progressBg.addView(progressRest);

        card.addView(progressBg);

        // ═══ الصف السفلي: XP المتبقي ═══
        TextView xpView = new TextView(act);
        xpView.setText("💯 " + xpRemaining + " XP للمستوى " + (level + 1));
        xpView.setTextColor(Color.parseColor("#9E9E9E"));
        xpView.setTextSize(11);
        card.addView(xpView);
    }

    private void addInfoItem(LinearLayout parent, String emoji, String text) {

        LinearLayout box = new LinearLayout(act);
        box.setOrientation(LinearLayout.HORIZONTAL);
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView e = new TextView(act);
        e.setText(emoji);
        e.setTextSize(14);
        e.setPadding(0, 0, 8, 0);
        box.addView(e);

        TextView t = new TextView(act);
        t.setText(text);
        t.setTextColor(Color.parseColor("#CCCCCC"));
        t.setTextSize(12);
        box.addView(t);

        parent.addView(box);
    }

    // ═══════════════════════════════════════
    //  Mini Stats (3 in a row)
    // ═══════════════════════════════════════
    private TextView[] addMiniStats() {
        LinearLayout row = new LinearLayout(act);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 10);
        row.setLayoutParams(lp);

        TextView balance = createMiniStat(row, "💰", "الرصيد", "...", 0);
        TextView count = createMiniStat(row, "👥", "مواطن", "...", 1);
        TextView online = createMiniStat(row, "🟢", "متصل", "...", 2);

        root.addView(row);
        AnimHelper.fadeInUp(row, 100);

        return new TextView[]{balance, count, online};
    }

    private TextView createMiniStat(LinearLayout parent, String emoji, String label,
                                     String value, int index) {
        LinearLayout box = new LinearLayout(act);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setBackgroundResource(R.drawable.bg_mini_stat);
        box.setPadding(20, 24, 20, 24);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        int ml = index == 0 ? 0 : 6;
        int mr = index == 2 ? 0 : 6;
        lp.setMargins(ml, 0, mr, 0);
        box.setLayoutParams(lp);

        TextView e = new TextView(act);
        e.setText(emoji);
        e.setTextSize(22);
        e.setGravity(Gravity.CENTER);
        box.addView(e);

        TextView v = new TextView(act);
        v.setText(value);
        v.setTextColor(Color.parseColor("#D4AF37"));
        v.setTextSize(16);
        v.setTypeface(null, Typeface.BOLD);
        v.setGravity(Gravity.CENTER);
        v.setPadding(0, 8, 0, 2);
        box.addView(v);

        TextView l = new TextView(act);
        l.setText(label);
        l.setTextColor(Color.parseColor("#9E9E9E"));
        l.setTextSize(10);
        l.setGravity(Gravity.CENTER);
        box.addView(l);

        parent.addView(box);
        return v;
    }

    // ═══════════════════════════════════════
    //  Section Title
    // ═══════════════════════════════════════
    private void addSectionTitle(String text) {
        LinearLayout container = new LinearLayout(act);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        container.setPadding(0, 32, 0, 14);

        View lineL = new View(act);
        LinearLayout.LayoutParams lLp = new LinearLayout.LayoutParams(0, 2, 1f);
        lineL.setLayoutParams(lLp);
        lineL.setBackgroundColor(Color.parseColor("#2A3D32"));
        container.addView(lineL);

        TextView t = new TextView(act);
        t.setText("  " + text + "  ");
        t.setTextColor(Color.parseColor("#D4AF37"));
        t.setTextSize(15);
        t.setTypeface(null, Typeface.BOLD);
        container.addView(t);

        View lineR = new View(act);
        LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(0, 2, 1f);
        lineR.setLayoutParams(rLp);
        lineR.setBackgroundColor(Color.parseColor("#2A3D32"));
        container.addView(lineR);

        root.addView(container);
    }

    // ═══════════════════════════════════════
    //  Big Feature Card (full width)
    // ═══════════════════════════════════════
    private void addBigFeature(String emoji, String title, String subtitle,
                                final Class<?> cls, long delay) {
        LinearLayout card = new LinearLayout(act);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackgroundResource(R.drawable.bg_feature_card);
        card.setPadding(24, 24, 24, 24);
        card.setClickable(true);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 8, 0, 8);
        card.setLayoutParams(lp);

        // Icon Circle
        TextView icon = new TextView(act);
        icon.setText(emoji);
        icon.setTextSize(28);
        icon.setGravity(Gravity.CENTER);
        icon.setBackgroundResource(R.drawable.bg_icon_circle);
        LinearLayout.LayoutParams icLp = new LinearLayout.LayoutParams(90, 90);
        icLp.setMargins(0, 0, 20, 0);
        icon.setLayoutParams(icLp);
        card.addView(icon);

        // Text
        LinearLayout txt = new LinearLayout(act);
        txt.setOrientation(LinearLayout.VERTICAL);
        txt.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView t1 = new TextView(act);
        t1.setText(title);
        t1.setTextColor(Color.WHITE);
        t1.setTextSize(17);
        t1.setTypeface(null, Typeface.BOLD);
        txt.addView(t1);

        TextView t2 = new TextView(act);
        t2.setText(subtitle);
        t2.setTextColor(Color.parseColor("#9E9E9E"));
        t2.setTextSize(12);
        t2.setPadding(0, 4, 0, 0);
        txt.addView(t2);

        card.addView(txt);

        // Arrow
        TextView arrow = new TextView(act);
        arrow.setText("›");
        arrow.setTextColor(Color.parseColor("#D4AF37"));
        arrow.setTextSize(28);
        card.addView(arrow);

        card.setOnClickListener(v -> {
            AnimHelper.pressEffect(v);
            AnimHelper.lightHaptic(act);
            act.startActivity(new Intent(act, cls));
        });

        root.addView(card);
        AnimHelper.fadeInUp(card, delay);
    }

    // ═══════════════════════════════════════
    //  Grid Feature (2 columns)
    // ═══════════════════════════════════════
    private static class Feature {
        String emoji, label;
        Class<?> cls;
        Feature(String e, String l, Class<?> c) { emoji = e; label = l; cls = c; }
    }

    private void addGridFeature(Feature a, Feature b) {
        LinearLayout row = new LinearLayout(act);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 6, 0, 6);
        row.setLayoutParams(lp);

        row.addView(buildGridCard(a, 0));
        row.addView(buildGridCard(b, 6));

        root.addView(row);
    }

    private View buildGridCard(final Feature f, int startMargin) {
        LinearLayout card = new LinearLayout(act);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setBackgroundResource(R.drawable.bg_feature_card);
        card.setPadding(20, 26, 20, 26);
        card.setClickable(true);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(startMargin, 0, 0, 0);
        card.setLayoutParams(lp);

        TextView e = new TextView(act);
        e.setText(f.emoji);
        e.setTextSize(34);
        e.setGravity(Gravity.CENTER);
        card.addView(e);

        TextView l = new TextView(act);
        l.setText(f.label);
        l.setTextColor(Color.WHITE);
        l.setTextSize(13);
        l.setTypeface(null, Typeface.BOLD);
        l.setGravity(Gravity.CENTER);
        l.setPadding(0, 12, 0, 0);
        card.addView(l);

        card.setOnClickListener(v -> {
            AnimHelper.pressEffect(v);
            AnimHelper.lightHaptic(act);
            act.startActivity(new Intent(act, f.cls));
        });

        return card;
    }

    // ═══════════════════════════════════════
    //  Secondary Small Button
    // ═══════════════════════════════════════
    private void addSecondarySmall(String text, final Class<?> cls) {
        TextView t = new TextView(act);
        t.setText(text);
        t.setTextColor(Color.parseColor("#9E9E9E"));
        t.setTextSize(13);
        t.setGravity(Gravity.CENTER);
        t.setPadding(20, 24, 20, 24);
        t.setBackgroundResource(R.drawable.bg_feature_card);
        t.setClickable(true);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 6, 0, 6);
        t.setLayoutParams(lp);

        if (cls != null) {
            t.setOnClickListener(v -> {
                AnimHelper.pressEffect(v);
                AnimHelper.lightHaptic(act);
                act.startActivity(new Intent(act, cls));
            });
        }

        root.addView(t);
    }

    // ═══════════════════════════════════════
    //  Footer
    // ═══════════════════════════════════════
    private void addFooter() {
        TextView footer = new TextView(act);
        footer.setText("دولة أُمّة الرقمية  •  v6.0");
        footer.setTextColor(Color.parseColor("#616161"));
        footer.setTextSize(11);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, 60, 0, 20);
        root.addView(footer);
    }
}
