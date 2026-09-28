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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration;

public class MainActivity extends Activity {

    private IdentityManager im;
    private FirebaseManager fm;
    private WalletManager wm;
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
        im = new IdentityManager(this);
        wm = new WalletManager(this);
        fm = FirebaseManager.get();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0A"));
        scroll.setFillViewport(true);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(40, 50, 40, 60);
        scroll.addView(root);

        setContentView(scroll);
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
                Toast.makeText(MainActivity.this, "خطأ اتصال: " + msg, Toast.LENGTH_LONG).show();
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
                Toast.makeText(MainActivity.this, "جاري مزامنة حسابك...", Toast.LENGTH_SHORT).show();
                fm.registerCitizen(c, wm.getBalance(), new FirebaseManager.OnDone() {
                    @Override public void onSuccess() { showCard(c); }
                    @Override public void onError(String msg) {
                        Toast.makeText(MainActivity.this, "خطأ: " + msg, Toast.LENGTH_LONG).show();
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
                if (countView != null) countView.setText("👥 " + c + " مواطن");
            }));

        if (balReg != null) balReg.remove();
        if (currentCitizen != null) {
            balReg = fm.listenBalance(currentCitizen.nationalId, new FirebaseManager.BalanceListener() {
                @Override public void onBalance(final int balance) {
                    runOnUiThread(() -> {
                        if (balanceView != null) balanceView.setText(balance + " Đ");
                    });
                }
                @Override public void onError(String m) {}
            });
        }
    }

    // ═══════════════════════════════════════
    //  شاشة الترحيب
    // ═══════════════════════════════════════
    private void showWelcome() {
        root.removeAllViews();

        // ═══ Hero ═══
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(Gravity.CENTER);
        hero.setPadding(0, 40, 0, 40);

        TextView flag = new TextView(this);
        flag.setText("🌍");
        flag.setTextSize(90);
        flag.setGravity(Gravity.CENTER);
        hero.addView(flag);

        TextView title = UiHelper.goldTitle(this, "أُمّة", 56);
        title.setPadding(0, 20, 0, 4);
        hero.addView(title);

        TextView sub = new TextView(this);
        sub.setText("أول دولة رقمية في العالم العربي");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(15);
        sub.setGravity(Gravity.CENTER);
        sub.setLetterSpacing(0.05f);
        hero.addView(sub);

        root.addView(hero);

        // ═══ العدّاد ═══
        LinearLayout counterCard = UiHelper.card(this);
        counterCard.setGravity(Gravity.CENTER);

        TextView counterLabel = new TextView(this);
        counterLabel.setText("المواطنون");
        counterLabel.setTextColor(Color.parseColor("#9E9E9E"));
        counterLabel.setTextSize(12);
        counterLabel.setGravity(Gravity.CENTER);
        counterCard.addView(counterLabel);

        countView = new TextView(this);
        countView.setText("...");
        countView.setTextColor(Color.parseColor("#D4AF37"));
        countView.setTextSize(36);
        countView.setTypeface(null, Typeface.BOLD);
        countView.setGravity(Gravity.CENTER);
        countView.setPadding(0, 6, 0, 0);
        counterCard.addView(countView);

        root.addView(counterCard);
        startListeners();

        // ═══ زر الانضمام ═══
        Button join = UiHelper.primaryButton(this, "🚀  انضم إلى الأمة");
        join.setOnClickListener(v -> askName());
        root.addView(join);

        // ═══ تلميح ═══
        TextView hint = new TextView(this);
        hint.setText("انضم إلى آلاف المواطنين في أول دولة رقمية عربية");
        hint.setTextColor(Color.parseColor("#666666"));
        hint.setTextSize(12);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, 30, 0, 0);
        root.addView(hint);
    }

    // ═══════════════════════════════════════
    //  شاشة المواطن
    // ═══════════════════════════════════════
    private void showCard(Citizen c) {
        currentCitizen = c;
        root.removeAllViews();

        // ═══ Header صغير ═══
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER);
        header.setPadding(0, 0, 0, 10);

        TextView flag = new TextView(this);
        flag.setText("🌍");
        flag.setTextSize(30);
        header.addView(flag);

        TextView appName = new TextView(this);
        appName.setText("  أُمّة");
        appName.setTextColor(Color.parseColor("#D4AF37"));
        appName.setTextSize(22);
        appName.setTypeface(null, Typeface.BOLD);
        header.addView(appName);

        root.addView(header);

        // ═══ بطاقة المواطنة الذهبية ═══
        LinearLayout card = UiHelper.goldCard(this);
        card.setGravity(Gravity.CENTER);

        TextView cardTitle = new TextView(this);
        cardTitle.setText("✦  بطاقة المواطنة  ✦");
        cardTitle.setTextColor(Color.parseColor("#D4AF37"));
        cardTitle.setTextSize(13);
        cardTitle.setTypeface(null, Typeface.BOLD);
        cardTitle.setGravity(Gravity.CENTER);
        cardTitle.setLetterSpacing(0.1f);
        card.addView(cardTitle);

        addRow(card, "الاسم", c.name, 24, "#FFFFFF");
        addRow(card, "الرقم الوطني", c.nationalId, 14, "#D4AF37");
        addRow(card, "البلد", CountryList.getName(c.country), 16, "#FFFFFF");
        addRow(card, "تاريخ الانضمام", c.joinDate, 13, "#9E9E9E");

        root.addView(card);

        // ═══ الرصيد ═══
        LinearLayout balCard = UiHelper.card(this);
        balCard.setGravity(Gravity.CENTER);

        TextView balLabel = new TextView(this);
        balLabel.setText("💰  رصيدك الحالي");
        balLabel.setTextColor(Color.parseColor("#9E9E9E"));
        balLabel.setTextSize(12);
        balLabel.setGravity(Gravity.CENTER);
        balCard.addView(balLabel);

        balanceView = new TextView(this);
        balanceView.setText("...");
        balanceView.setTextColor(Color.parseColor("#D4AF37"));
        balanceView.setTextSize(48);
        balanceView.setTypeface(null, Typeface.BOLD);
        balanceView.setGravity(Gravity.CENTER);
        balanceView.setPadding(0, 8, 0, 8);
        balCard.addView(balanceView);

        countView = new TextView(this);
        countView.setText("...");
        countView.setTextColor(Color.parseColor("#9E9E9E"));
        countView.setTextSize(12);
        countView.setGravity(Gravity.CENTER);
        balCard.addView(countView);

        root.addView(balCard);
        startListeners();

        // ═══════════ الأقسام ═══════════

        // ─── الرئيسية ───
        addSectionTitle("⭐  الرئيسية");
        addPrimaryButton("💰  محفظتي", WalletActivity.class);
        addPrimaryButton("🎁  مكافأة اليوم", DailyRewardActivity.class);
        addPrimaryButton("👤  ملفي الشخصي", ProfileActivity.class);

        // ─── التواصل ───
        addSectionTitle("💬  التواصل");
        addSecondaryButton("💬  دردشة أُمّة", ChatActivity.class);
        addSecondaryButton("👥  دليل المواطنين", CitizensActivity.class);
        addSecondaryButton("🏆  المتصدرون", LeaderboardActivity.class);

        // ─── الحكم والسياسة ───
        addSectionTitle("🏛️  الحكم والسياسة");
        addSecondaryButton("🗳️  البرلمان", ParliamentActivity.class);
        addSecondaryButton("👑  الانتخابات الرئاسية", ElectionActivity.class);
        addSecondaryButton("📜  دستور أُمّة", ConstitutionActivity.class);
        addSecondaryButton("⚖️  محكمة أُمّة", CourtActivity.class);
        addSecondaryButton("🏦  الخزينة العامة", TreasuryActivity.class);

        // ─── أخرى ───
        addSectionTitle("📌  أخرى");
        addSecondaryButton("📰  أخبار أُمّة", NewsActivity.class);
        addSecondaryButton("📊  إحصائيات الدولة", StatsActivity.class);
        addSecondaryButton("🎁  الهدايا", GiftsActivity.class);
        addSecondaryButton("🔐  استعادة الحساب", AccountRecoveryActivity.class);

        // ─── زر الكلمات السرية (أسفل، رمادي) ───
        Button seedBtn = new Button(this);
        seedBtn.setText("🔑  الكلمات السرية");
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
    }

    // ═══════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════

    private void addSectionTitle(String text) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        container.setPadding(0, 32, 0, 12);

        View lineLeft = new View(this);
        LinearLayout.LayoutParams lineLp = new LinearLayout.LayoutParams(
            0, 2, 1f);
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
        LinearLayout.LayoutParams lineRlp = new LinearLayout.LayoutParams(
            0, 2, 1f);
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

    // ═══════════════════════════════════════
    //  تسجيل المواطن
    // ═══════════════════════════════════════

    private void askName() {
        EditText input = UiHelper.input(this, "اسمك أو كنيتك");

        LinearLayout c = new LinearLayout(this);
        c.setPadding(40, 20, 40, 20);
        c.addView(input);

        new AlertDialog.Builder(this)
            .setTitle("اختر اسمك")
            .setView(c)
            .setPositiveButton("متابعة", (d, w) -> {
                String n = input.getText().toString().trim();
                if (n.isEmpty()) n = "مواطن مجهول";
                askCountry(n);
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    private void askCountry(final String name) {
        final String detected = im.detectCountry(this);
        final String detectedName = CountryList.getName(detected);

        new AlertDialog.Builder(this)
            .setTitle("🌍 من أين أنت؟")
            .setMessage("كشفنا تلقائياً:\n\n" + detectedName + "\n\nهل هذا بلدك؟")
            .setPositiveButton("✅ نعم", (d, w) -> registerNow(name, detected))
            .setNegativeButton("🖐️ اختيار يدوي", (d, w) -> showCountryPicker(name))
            .setCancelable(false)
            .show();
    }

    private void showCountryPicker(final String name) {
        final java.util.Map<String, String> countries = CountryList.getCountries();
        final String[] keys = countries.keySet().toArray(new String[0]);
        final String[] labels = new String[keys.length];
        for (int i = 0; i < keys.length; i++) labels[i] = countries.get(keys[i]);

        new AlertDialog.Builder(this)
            .setTitle("اختر بلدك")
            .setItems(labels, (d, which) -> registerNow(name, keys[which]))
            .setNegativeButton("رجوع", (d, w) -> askCountry(name))
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
                Toast.makeText(MainActivity.this, "خطأ: " + msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showSeedDialog(final Citizen c) {
        new AlertDialog.Builder(this)
            .setTitle("🔐 كلماتك السرية")
            .setMessage("احفظ هذه الكلمات الـ 12 في مكان آمن:\n\n" + c.seedPhrase +
                    "\n\nهي هويتك الوحيدة.")
            .setNeutralButton("📋 نسخ", (d, w) -> {
                android.content.ClipboardManager cm = (android.content.ClipboardManager)
                        getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(android.content.ClipData.newPlainText("Seed", c.seedPhrase));
                Toast.makeText(MainActivity.this, "✅ تم نسخ الكلمات",
                        Toast.LENGTH_LONG).show();
                showSeedDialog(c);
            })
            .setPositiveButton("حفظتها", (d, w) -> {
                showCard(c);
                Toast.makeText(this, "مرحباً بك في أُمّة", Toast.LENGTH_LONG).show();
            })
            .setCancelable(false)
            .show();
    }

    private void showSeed(final Citizen c) {
        new AlertDialog.Builder(this)
            .setTitle("🔐 الكلمات السرية")
            .setMessage(c.seedPhrase)
            .setNeutralButton("📋 نسخ", (d, w) -> {
                android.content.ClipboardManager cm = (android.content.ClipboardManager)
                        getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(android.content.ClipData.newPlainText("Seed", c.seedPhrase));
                Toast.makeText(MainActivity.this, "✅ تم نسخ الكلمات",
                        Toast.LENGTH_LONG).show();
            })
            .setPositiveButton("حسناً", null)
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
        Citizen c = im.getCitizen();
        if (c != null) fm.updateLastSeen(c.nationalId);
    }
}
