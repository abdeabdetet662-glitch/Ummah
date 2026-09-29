package com.ummah.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration;

public class MainActivity extends Activity {

    private IdentityManager im;
    private FirebaseManager fm;
    private WalletManager wm;
    private UnreadManager um;
    private GlobalChatListener globalListener;
    private ListenerRegistration unreadReg;
    private FrameLayout chatBadgeContainer;
    private FrameLayout giftsBadgeContainer;
    private FrameLayout electionBadgeContainer;
    private LinearLayout root;
    private ListenerRegistration countReg;
    private ListenerRegistration balReg;
    private TextView countView;
    private TextView balanceView;
    private Citizen currentCitizen;
    private android.os.Handler heartbeatHandler;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        // FullscreenHelper.enable(this);  // DISABLED - crash
        im = new IdentityManager(this);
        wm = new WalletManager(this);
        fm = FirebaseManager.get();
        um = new UnreadManager();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0A"));
        scroll.setFillViewport(true);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(40, 50, 40, 60);
        scroll.addView(root);

        setContentView(scroll);
        NotificationHelper.createChannels(this);
        PermissionHelper.requestAll(this);

        fm.signIn(new FirebaseManager.OnDone() {
            @Override public void onSuccess() {
                if (im.isCitizen()) {
                    currentCitizen = im.getCitizen();
                    syncAndShow(currentCitizen);
                } else {
                    showWelcome();
                }
            }
            @Override public void onError(String msg) {
                Toast.makeText(MainActivity.this, getString(R.string.connection_error, msg), Toast.LENGTH_LONG).show();
                if (im.isCitizen()) {
                    currentCitizen = im.getCitizen();
                    showCard(currentCitizen);
                } else {
                    showWelcome();
                }
            }
        });
    }

    private void syncAndShow(final Citizen c) {
        fm.lookupCitizen(c.nationalId, new FirebaseManager.LookupListener() {
            @Override public void onFound(String name) { showCard(c); }
            @Override public void onNotFound() {
                Toast.makeText(MainActivity.this, getString(R.string.syncing), Toast.LENGTH_SHORT).show();
                fm.registerCitizen(c, wm.getBalance(), new FirebaseManager.OnDone() {
                    @Override public void onSuccess() { showCard(c); }
                    @Override public void onError(String msg) {
                        Toast.makeText(MainActivity.this, getString(R.string.error, msg), Toast.LENGTH_LONG).show();
                        showCard(c);
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countReg != null) countReg.remove();
        if (balReg != null) balReg.remove();
    }

    private void startListeners() {
        if (countReg != null) countReg.remove();
        countReg = fm.listenCitizensCount(c ->
            runOnUiThread(() -> {
                if (countView != null) countView.setText(getString(R.string.citizens_count, c));
            }));

        if (balReg != null) balReg.remove();
        if (currentCitizen != null) {
            balReg = fm.listenBalance(currentCitizen.nationalId, new FirebaseManager.BalanceListener() {
                @Override public void onBalance(final int balance) {
                    runOnUiThread(() -> {
                        if (balanceView != null) balanceView.setText(balance + " " + getString(R.string.currency_short));
                    });
                }
                @Override public void onError(String m) {}
            });
        }
    }

    private void showWelcome() {
        root.removeAllViews();

        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(Gravity.CENTER);
        hero.setPadding(0, 40, 0, 40);

        TextView flag = new TextView(this);
        flag.setText("🌍");
        flag.setTextSize(90);
        flag.setGravity(Gravity.CENTER);
        hero.addView(flag);

        TextView title = UiHelper.goldTitle(this, getString(R.string.app_name), 56);
        title.setPadding(0, 20, 0, 4);
        hero.addView(title);

        TextView sub = new TextView(this);
        sub.setText(getString(R.string.slogan));
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(15);
        sub.setGravity(Gravity.CENTER);
        hero.addView(sub);

        root.addView(hero);

        LinearLayout counterCard = UiHelper.card(this);
        counterCard.setGravity(Gravity.CENTER);

        TextView counterLabel = new TextView(this);
        counterLabel.setText(getString(R.string.citizens_label));
        counterLabel.setTextColor(Color.parseColor("#9E9E9E"));
        counterLabel.setTextSize(12);
        counterLabel.setGravity(Gravity.CENTER);
        counterCard.addView(counterLabel);

        countView = new TextView(this);
        countView.setText(getString(R.string.loading));
        countView.setTextColor(Color.parseColor("#D4AF37"));
        countView.setTextSize(36);
        countView.setTypeface(null, Typeface.BOLD);
        countView.setGravity(Gravity.CENTER);
        countView.setPadding(0, 6, 0, 0);
        counterCard.addView(countView);

        root.addView(counterCard);
        startListeners();

        Button join = UiHelper.primaryButton(this, getString(R.string.join_button));
        join.setOnClickListener(v -> askName());
        root.addView(join);

        TextView hint = new TextView(this);
        hint.setText(getString(R.string.join_hint));
        hint.setTextColor(Color.parseColor("#666666"));
        hint.setTextSize(12);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, 30, 0, 0);
        root.addView(hint);
    }

    private void showCard(Citizen c) {
        currentCitizen = c;
        root.removeAllViews();

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER);
        header.setPadding(0, 0, 0, 10);

        TextView flag = new TextView(this);
        flag.setText("🌍");
        flag.setTextSize(30);
        header.addView(flag);

        TextView appName = new TextView(this);
        appName.setText("  " + getString(R.string.app_name));
        appName.setTextColor(Color.parseColor("#D4AF37"));
        appName.setTextSize(22);
        appName.setTypeface(null, Typeface.BOLD);
        header.addView(appName);

        root.addView(header);

        LinearLayout card = UiHelper.goldCard(this);
        card.setGravity(Gravity.CENTER);

        TextView cardTitle = new TextView(this);
        cardTitle.setText(getString(R.string.citizenship_card));
        cardTitle.setTextColor(Color.parseColor("#D4AF37"));
        cardTitle.setTextSize(13);
        cardTitle.setTypeface(null, Typeface.BOLD);
        cardTitle.setGravity(Gravity.CENTER);
        card.addView(cardTitle);

        addRow(card, getString(R.string.label_name), c.name, 24, "#FFFFFF");
        addRow(card, getString(R.string.label_national_id), c.nationalId, 14, "#D4AF37");
        addRow(card, getString(R.string.label_country), CountryList.getName(c.country), 16, "#FFFFFF");
        addRow(card, getString(R.string.label_join_date), c.joinDate, 13, "#9E9E9E");

        root.addView(card);

        LinearLayout balCard = UiHelper.card(this);
        balCard.setGravity(Gravity.CENTER);

        TextView balLabel = new TextView(this);
        balLabel.setText(getString(R.string.balance_label));
        balLabel.setTextColor(Color.parseColor("#9E9E9E"));
        balLabel.setTextSize(12);
        balLabel.setGravity(Gravity.CENTER);
        balCard.addView(balLabel);

        balanceView = new TextView(this);
        balanceView.setText(getString(R.string.loading));
        balanceView.setTextColor(Color.parseColor("#D4AF37"));
        balanceView.setTextSize(48);
        balanceView.setTypeface(null, Typeface.BOLD);
        balanceView.setGravity(Gravity.CENTER);
        balanceView.setPadding(0, 8, 0, 8);
        balCard.addView(balanceView);

        countView = new TextView(this);
        countView.setText(getString(R.string.loading));
        countView.setTextColor(Color.parseColor("#9E9E9E"));
        countView.setTextSize(12);
        countView.setGravity(Gravity.CENTER);
        balCard.addView(countView);

        root.addView(balCard);
        startListeners();

        addSectionTitle(getString(R.string.section_main));
        addPrimaryButton(getString(R.string.btn_wallet), WalletActivity.class);
        addPrimaryButton(getString(R.string.btn_daily_reward), DailyRewardActivity.class);
        addPrimaryButton(getString(R.string.btn_profile), ProfileActivity.class);

        addSectionTitle(getString(R.string.section_communication));
        chatBadgeContainer = addBadgedSecondaryButton(getString(R.string.btn_chat), ChatActivity.class, 0);
        addSecondaryButton(getString(R.string.btn_citizens), CitizensActivity.class);
        addSecondaryButton(getString(R.string.btn_leaderboard), LeaderboardActivity.class);

        addSectionTitle(getString(R.string.section_governance));
        addSecondaryButton(getString(R.string.btn_parliament), ParliamentActivity.class);
        addPrimaryButton("🛒  السوق العام", MarketActivity.class);
        addPrimaryButton("🎒  ممتلكاتي", MyInventoryActivity.class);
        addSeedButton();
        electionBadgeContainer = addBadgedSecondaryButton(getString(R.string.btn_election), ElectionActivity.class, 0);
        addSecondaryButton(getString(R.string.btn_constitution), ConstitutionActivity.class);
        addSecondaryButton(getString(R.string.btn_court), CourtActivity.class);
        addSecondaryButton(getString(R.string.btn_treasury), TreasuryActivity.class);

        addSectionTitle(getString(R.string.section_other));
        addSecondaryButton(getString(R.string.btn_news), NewsActivity.class);
        addSecondaryButton(getString(R.string.btn_stats), StatsActivity.class);
        giftsBadgeContainer = addBadgedSecondaryButton(getString(R.string.btn_gifts), GiftsActivity.class, 0);
        addSecondaryButton(getString(R.string.btn_recovery), AccountRecoveryActivity.class);

        Button seedBtn = new Button(this);
        seedBtn.setText(getString(R.string.btn_seed));
        seedBtn.setTextSize(14);
        seedBtn.setTextColor(Color.parseColor("#888888"));
        seedBtn.setAllCaps(false);
        seedBtn.setBackgroundResource(R.drawable.bg_button_secondary);
        seedBtn.setPadding(40, 28, 40, 28);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        slp.setMargins(0, 20, 0, 0);
        seedBtn.setLayoutParams(slp);
        seedBtn.setOnClickListener(v -> showSeed(c));
        root.addView(seedBtn);

        startUnreadListener();
    }

    private void addSectionTitle(String text) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        container.setPadding(0, 32, 0, 12);

        View lineLeft = new View(this);
        LinearLayout.LayoutParams lineLp = new LinearLayout.LayoutParams(0, 2, 1f);
        lineLeft.setLayoutParams(lineLp);
        lineLeft.setBackgroundColor(Color.parseColor("#2A2A2A"));
        container.addView(lineLeft);

        TextView title = new TextView(this);
        title.setText("  " + text + "  ");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(15);
        title.setTypeface(null, Typeface.BOLD);
        container.addView(title);

        View lineRight = new View(this);
        LinearLayout.LayoutParams lineRlp = new LinearLayout.LayoutParams(0, 2, 1f);
        lineRight.setLayoutParams(lineRlp);
        lineRight.setBackgroundColor(Color.parseColor("#2A2A2A"));
        container.addView(lineRight);

        root.addView(container);
    }

    private void addPrimaryButton(String text, final Class<?> activityClass) {
        Button btn = UiHelper.primaryButton(this, text);
        btn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, activityClass)));
        root.addView(btn);
    }

    private void addSecondaryButton(String text, final Class<?> activityClass) {
        Button btn = UiHelper.secondaryButton(this, text);
        btn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, activityClass)));
        root.addView(btn);
    }

    private FrameLayout addBadgedSecondaryButton(String text, final Class<?> activityClass, int badgeCount) {
        Button btn = UiHelper.secondaryButton(this, text);
        btn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, activityClass)));
        FrameLayout container = BadgeHelper.withBadge(this, btn, badgeCount);
        root.addView(container);
        return container;
    }

    private void addRow(LinearLayout p, String label, String val, int valSize, String valColor) {
        TextView l = new TextView(this);
        l.setText(label);
        l.setTextColor(Color.parseColor("#9E9E9E"));
        l.setTextSize(11);
        l.setGravity(Gravity.CENTER);
        l.setPadding(0, 10, 0, 2);
        p.addView(l);

        TextView v = new TextView(this);
        v.setText(val);
        v.setTextColor(Color.parseColor(valColor));
        v.setTextSize(valSize);
        v.setTypeface(null, Typeface.BOLD);
        v.setGravity(Gravity.CENTER);
        p.addView(v);
    }

    private void askName() {
        EditText input = UiHelper.input(this, getString(R.string.hint_name));

        LinearLayout c = new LinearLayout(this);
        c.setPadding(40, 20, 40, 20);
        c.addView(input);

        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_choose_name))
            .setView(c)
            .setPositiveButton(getString(R.string.btn_continue), (d, w) -> {
                String n = input.getText().toString().trim();
                if (n.isEmpty()) n = getString(R.string.unknown_citizen);
                askCountry(n);
            })
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show();
    }

    private void askCountry(final String name) {
        final String detected = im.detectCountry(this);
        final String detectedName = CountryList.getName(detected);

        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_country))
            .setMessage(getString(R.string.country_detected, detectedName))
            .setPositiveButton(getString(R.string.btn_yes), (d, w) -> registerNow(name, detected))
            .setNegativeButton(getString(R.string.btn_manual), (d, w) -> showCountryPicker(name))
            .setCancelable(false)
            .show();
    }

    private void showCountryPicker(final String name) {
        final java.util.Map<String, String> countries = CountryList.getCountries();
        final String[] keys = countries.keySet().toArray(new String[0]);
        final String[] labels = new String[keys.length];
        for (int i = 0; i < keys.length; i++) labels[i] = countries.get(keys[i]);

        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_choose_country))
            .setItems(labels, (d, which) -> registerNow(name, keys[which]))
            .setNegativeButton(getString(R.string.btn_back), (d, w) -> askCountry(name))
            .show();
    }

    private void registerNow(String name, String country) {
        Citizen citizen = im.registerCitizen(name, country);
        currentCitizen = citizen;
        fm.registerCitizen(citizen, wm.getBalance(), new FirebaseManager.OnDone() {
            @Override public void onSuccess() {
                String hash = im.hashSeed(citizen.seedPhrase);
                fm.saveSeedHash(citizen.nationalId, hash);
                showSeedDialog(citizen);
            }
            @Override public void onError(String msg) {
                Toast.makeText(MainActivity.this, getString(R.string.error, msg), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showSeedDialog(final Citizen c) {
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_seed_title))
            .setMessage(getString(R.string.seed_warning, c.seedPhrase))
            .setNeutralButton(getString(R.string.btn_copy), (d, w) -> {
                android.content.ClipboardManager cm = (android.content.ClipboardManager)
                        getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(android.content.ClipData.newPlainText("Seed", c.seedPhrase));
                Toast.makeText(MainActivity.this, getString(R.string.seed_copied), Toast.LENGTH_LONG).show();
                showSeedDialog(c);
            })
            .setPositiveButton(getString(R.string.btn_saved), (d, w) -> {
                showCard(c);
                Toast.makeText(this, getString(R.string.welcome_toast), Toast.LENGTH_LONG).show();
            })
            .setCancelable(false)
            .show();
    }

    private void showSeed(final Citizen c) {
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_seed_title))
            .setMessage(c.seedPhrase)
            .setNeutralButton(getString(R.string.btn_copy), (d, w) -> {
                android.content.ClipboardManager cm = (android.content.ClipboardManager)
                        getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(android.content.ClipData.newPlainText("Seed", c.seedPhrase));
                Toast.makeText(MainActivity.this, getString(R.string.seed_copied), Toast.LENGTH_LONG).show();
            })
            .setPositiveButton(getString(R.string.btn_ok), null)
            .show();
    }

    private void startHeartbeat(final Citizen c) {
        if (heartbeatHandler != null) return;
        heartbeatHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        final Runnable task = new Runnable() {
            @Override public void run() {
                fm.updateLastSeen(c.nationalId);
                if (heartbeatHandler != null) heartbeatHandler.postDelayed(this, 30000);
            }
        };
        heartbeatHandler.post(task);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Citizen c = im.getCitizen();
        if (c != null) fm.setOffline(c.nationalId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // FullscreenHelper.enable(this);  // DISABLED - crash
        Citizen c = im.getCitizen();
        if (c != null) fm.updateLastSeen(c.nationalId);
    }

    private void startUnreadListener() {
        if (currentCitizen == null) return;
        if (unreadReg != null) unreadReg.remove();
        unreadReg = um.listen(currentCitizen.nationalId, (chat, gifts, election) -> {
            runOnUiThread(() -> {
                updateBadge(chatBadgeContainer, chat);
                updateBadge(giftsBadgeContainer, gifts);
                updateBadge(electionBadgeContainer, election);

                if (chat > 0) {
                    NotificationHelper.showBadgeNotification(this, 1001,
                        "\uD83D\uDCAC \u0631\u0633\u0627\u0626\u0644 \u062C\u062F\u064A\u062F\u0629",
                        "\u0639\u0646\u062F\u0643 " + chat + " \u0631\u0633\u0627\u0644\u0629", chat);
                }
            });
        });
        startGlobalChatListener();
    }

    private void updateBadge(FrameLayout container, int count) {
        if (container == null) return;
        for (int i = container.getChildCount() - 1; i >= 1; i--) {
            container.removeViewAt(i);
        }
        if (count > 0) {
            TextView badge = BadgeHelper.createBadge(this, count);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.TOP | Gravity.START;
            lp.topMargin = 6;
            lp.leftMargin = 12;
            badge.setLayoutParams(lp);
            container.addView(badge);
            badge.bringToFront();
        }
    }

    private void startGlobalChatListener() {
        if (currentCitizen == null) return;
        if (globalListener != null) globalListener.stop();
        globalListener = new GlobalChatListener(this);
        globalListener.start(currentCitizen.nationalId, (name, content, ts) -> {
            runOnUiThread(() -> {
                NotificationHelper.showBadgeNotification(
                        MainActivity.this, 1001,
                        "\uD83D\uDCAC " + name,
                        content, 1);
            });
        });
    }


    private void addSeedButton() {
        Button seedBtn = new Button(this);
        seedBtn.setText("🌱 تعبئة السوق (تجريبي)");
        seedBtn.setTextSize(12);
        seedBtn.setTextColor(Color.parseColor("#FFC107"));
        seedBtn.setAllCaps(false);
        seedBtn.setBackgroundResource(R.drawable.bg_btn_outline);
        seedBtn.setPadding(40, 20, 40, 20);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 6, 0, 6);
        seedBtn.setLayoutParams(lp);
        seedBtn.setOnClickListener(v -> {
            com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
            MarketCleanup.cleanupOldItems(db);
            MarketSeed.seed(db);
            Toast.makeText(MainActivity.this, "✅ تمت إضافة المنتجات! افتح السوق", Toast.LENGTH_LONG).show();
        });
        root.addView(seedBtn);
    }
}
