package com.ummah.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
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
        root.setGravity(Gravity.CENTER);
        root.setPadding(40, 50, 40, 50);
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

    private void showWelcome() {
        root.removeAllViews();

        TextView flag = new TextView(this);
        flag.setText("🌍");
        flag.setTextSize(80);
        flag.setGravity(Gravity.CENTER);
        root.addView(flag);

        TextView title = new TextView(this);
        title.setText("أُمّة");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(48);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 20, 0, 10);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("أول دولة رقمية في العالم العربي");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(16);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 30);
        root.addView(sub);

        countView = new TextView(this);
        countView.setText("...");
        countView.setTextColor(Color.parseColor("#D4AF37"));
        countView.setTextSize(18);
        countView.setTypeface(null, Typeface.BOLD);
        countView.setGravity(Gravity.CENTER);
        countView.setPadding(0, 0, 0, 40);
        root.addView(countView);

        startListeners();

        Button join = new Button(this);
        join.setText("انضم إلى الأمة");
        join.setTextSize(18);
        join.setPadding(40, 30, 40, 30);
        join.setTextColor(Color.WHITE);
        join.setBackground(makeBg("#0B4F2C", 12));
        join.setOnClickListener(v -> askName());
        root.addView(join);
    }

    private void askName() {
        EditText input = new EditText(this);
        input.setHint("اسمك أو كنيتك");
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.GRAY);

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
            .setMessage("كشفنا تلقائياً:\n\n" + detectedName + "\n\nهل هذا بلدك؟ أو اختر يدوياً.")
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
                Toast.makeText(MainActivity.this, "✅ تم نسخ الكلمات. احفظها في مكان آمن!",
                        Toast.LENGTH_LONG).show();
                // إعادة عرض النافذة
                showSeedDialog(c);
            })
            .setPositiveButton("حفظتها", (d, w) -> {
                showCard(c);
                Toast.makeText(this, "مرحباً بك في أُمّة", Toast.LENGTH_LONG).show();
            })
            .setCancelable(false)
            .show();
    }

    private void showCard(Citizen c) {
        currentCitizen = c;
        root.removeAllViews();

        // علم
        TextView flag = new TextView(this);
        flag.setText("🌍");
        flag.setTextSize(36);
        flag.setGravity(Gravity.CENTER);
        root.addView(flag);

        // البطاقة الخضراء
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(makeBg("#0B4F2C", 16));
        card.setPadding(30, 24, 30, 24);
        card.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 12, 0, 16);
        card.setLayoutParams(lp);

        TextView h = new TextView(this);
        h.setText("بطاقة المواطنة");
        h.setTextColor(Color.parseColor("#D4AF37"));
        h.setTextSize(14);
        h.setTypeface(null, Typeface.BOLD);
        h.setGravity(Gravity.CENTER);
        card.addView(h);

        addRow(card, "الاسم", c.name, 20);
        addRow(card, "الرقم الوطني", c.nationalId, 13);
        addRow(card, "البلد", CountryList.getName(c.country), 16);
        addRow(card, "تاريخ الانضمام", c.joinDate, 14);

        root.addView(card);

        // الرصيد الكبير
        TextView balLabel = new TextView(this);
        balLabel.setText("رصيدك");
        balLabel.setTextColor(Color.parseColor("#9E9E9E"));
        balLabel.setTextSize(12);
        balLabel.setGravity(Gravity.CENTER);
        root.addView(balLabel);

        balanceView = new TextView(this);
        balanceView.setText("...");
        balanceView.setTextColor(Color.parseColor("#D4AF37"));
        balanceView.setTextSize(44);
        balanceView.setTypeface(null, Typeface.BOLD);
        balanceView.setGravity(Gravity.CENTER);
        balanceView.setPadding(0, 6, 0, 6);
        root.addView(balanceView);

        countView = new TextView(this);
        countView.setText("...");
        countView.setTextColor(Color.parseColor("#9E9E9E"));
        countView.setTextSize(12);
        countView.setGravity(Gravity.CENTER);
        countView.setPadding(0, 0, 0, 30);
        root.addView(countView);

        startListeners();

        // ============ الأزرار المرتّبة بالألوان ============

        // 0. ملفي الشخصي (بني)
        addColoredButton("👤  ملفي الشخصي", "#5D4037", ProfileActivity.class);

        // 1. المحفظة (أخضر - الأهم)
        addColoredButton("💰  محفظتي", "#1B5E20", WalletActivity.class);

        // 2. الدردشة (أزرق)
        addColoredButton("💬  دردشة أُمّة", "#0D47A1", ChatActivity.class);

        // 3. دليل المواطنين (أزرق فاتح)
        addColoredButton("👥  دليل المواطنين", "#1565C0", CitizensActivity.class);

        // 4. المتصدرون (ذهبي)
        addColoredButton("🏆  المتصدرون", "#B8860B", LeaderboardActivity.class);

        // 5. الأخبار (بنفسجي)
        addColoredButton("📰  أخبار أُمّة", "#4A148C", NewsActivity.class);

        // 6. البرلمان (تركوازي)
        addColoredButton("🗳️  البرلمان", "#00695C", ParliamentActivity.class);

        // 7. الانتخابات (أحمر داكن)
        addColoredButton("👑  الانتخابات الرئاسية", "#7B1FA2", ElectionActivity.class);

        // 8. الدستور (ذهبي داكن)
        addColoredButton("🏛️  دستور أُمّة", "#795548", ConstitutionActivity.class);

        // 9. الخزينة (أخضر مزرق)
        addColoredButton("🏦  الخزينة العامة", "#1A237E", TreasuryActivity.class);

        // 10. المكافأة اليومية (برتقالي)
        addColoredButton("🎁  مكافأة اليوم", "#E65100", DailyRewardActivity.class);

        // 11. الإحصائيات (رمادي فاتح)
        addColoredButton("📊  إحصائيات الدولة", "#37474F", StatsActivity.class);

        // 12. المحكمة
        addColoredButton("⚖️  محكمة أُمّة", "#5D4037", CourtActivity.class);

        // 13. الهدايا
        addColoredButton("🎁  الهدايا", "#C2185B", GiftsActivity.class);

        // 14. استعادة الحساب (بني فاتح)
        addColoredButton("🔐  استعادة الحساب", "#4E342E", AccountRecoveryActivity.class);

        // 13. الكلمات السرية (رمادي - الأسفل)
        Button seedBtn = new Button(this);
        seedBtn.setText("🔐  الكلمات السرية");
        seedBtn.setTextSize(15);
        seedBtn.setTextColor(Color.parseColor("#CCCCCC"));
        seedBtn.setBackground(makeBg("#212121", 10));
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        slp.setMargins(0, 8, 0, 0);
        seedBtn.setLayoutParams(slp);
        seedBtn.setOnClickListener(v -> showSeed(c));
        root.addView(seedBtn);
    }

    private void addColoredButton(String text, String colorHex, final Class<?> activityClass) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextSize(16);
        btn.setTextColor(Color.WHITE);
        btn.setTypeface(null, Typeface.BOLD);
        btn.setBackground(makeBg(colorHex, 10));
        btn.setPadding(20, 32, 20, 32);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 8, 0, 0);
        btn.setLayoutParams(lp);

        btn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, activityClass)));
        root.addView(btn);
    }

    private GradientDrawable makeBg(String colorHex, int cornerRadius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.parseColor(colorHex));
        g.setCornerRadius(cornerRadius * 3);
        return g;
    }

    private void addRow(LinearLayout p, String label, String val, int valSize) {
        TextView l = new TextView(this);
        l.setText(label);
        l.setTextColor(Color.parseColor("#9E9E9E"));
        l.setTextSize(11);
        l.setGravity(Gravity.CENTER);
        l.setPadding(0, 8, 0, 2);
        p.addView(l);

        TextView v = new TextView(this);
        v.setText(val);
        v.setTextColor(Color.WHITE);
        v.setTextSize(valSize);
        v.setTypeface(null, Typeface.BOLD);
        v.setGravity(Gravity.CENTER);
        p.addView(v);
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
}
