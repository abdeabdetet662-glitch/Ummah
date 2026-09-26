package com.ummah.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
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
    private TextView countView;

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
        root.setPadding(40, 60, 40, 60);
        scroll.addView(root);

        setContentView(scroll);

        fm.signIn(new FirebaseManager.OnDone() {
            @Override public void onSuccess() {
                if (im.isCitizen()) {
                    checkAndSyncCitizen(im.getCitizen());
                } else {
                    showWelcome();
                }
            }
            @Override public void onError(String msg) {
                Toast.makeText(MainActivity.this, "خطأ اتصال: " + msg, Toast.LENGTH_LONG).show();
                if (im.isCitizen()) showCard(im.getCitizen());
                else showWelcome();
            }
        });
    }

    private void checkAndSyncCitizen(final Citizen c) {
        fm.lookupCitizen(c.nationalId, new FirebaseManager.LookupListener() {
            @Override public void onFound(String name) {
                // موجود في Firebase، اعرض البطاقة
                showCard(c);
            }
            @Override public void onNotFound() {
                // غير موجود، سجّله في Firebase
                Toast.makeText(MainActivity.this, "جاري مزامنة حسابك...", Toast.LENGTH_SHORT).show();
                fm.registerCitizen(c, wm.getBalance(), new FirebaseManager.OnDone() {
                    @Override public void onSuccess() {
                        showCard(c);
                        Toast.makeText(MainActivity.this, "✅ تم تفعيل حسابك", Toast.LENGTH_LONG).show();
                    }
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
    }

    private void startCountListener() {
        if (countReg != null) countReg.remove();
        countReg = fm.listenCitizensCount(c ->
            runOnUiThread(() -> {
                if (countView != null) countView.setText("👥  " + c + " مواطن في الدولة");
            }));
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
        countView.setTextSize(20);
        countView.setTypeface(null, Typeface.BOLD);
        countView.setGravity(Gravity.CENTER);
        countView.setPadding(0, 0, 0, 40);
        root.addView(countView);

        startCountListener();

        Button join = new Button(this);
        join.setText("انضم إلى الأمة");
        join.setTextSize(18);
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
                Citizen citizen = im.registerCitizen(n);
                fm.registerCitizen(citizen, wm.getBalance(), new FirebaseManager.OnDone() {
                    @Override public void onSuccess() { showSeedDialog(citizen); }
                    @Override public void onError(String msg) {
                        Toast.makeText(MainActivity.this, "خطأ: " + msg, Toast.LENGTH_LONG).show();
                    }
                });
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    private void showSeedDialog(Citizen c) {
        new AlertDialog.Builder(this)
            .setTitle("🔐 كلماتك السرية")
            .setMessage("احفظ هذه الكلمات الـ 12 في مكان آمن.\n\n" + c.seedPhrase)
            .setPositiveButton("حفظتها", (d, w) -> {
                showCard(c);
                Toast.makeText(this, "مرحباً بك", Toast.LENGTH_LONG).show();
            })
            .setCancelable(false)
            .show();
    }

    private void showCard(Citizen c) {
        root.removeAllViews();

        TextView flag = new TextView(this);
        flag.setText("🌍");
        flag.setTextSize(50);
        flag.setGravity(Gravity.CENTER);
        root.addView(flag);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.parseColor("#0B4F2C"));
        card.setPadding(40, 40, 40, 40);
        card.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 20, 0, 20);
        card.setLayoutParams(lp);

        TextView h = new TextView(this);
        h.setText("بطاقة المواطنة");
        h.setTextColor(Color.parseColor("#D4AF37"));
        h.setTextSize(18);
        h.setTypeface(null, Typeface.BOLD);
        h.setGravity(Gravity.CENTER);
        card.addView(h);

        addRow(card, "الاسم", c.name);
        addRow(card, "الرقم الوطني", c.nationalId);
        addRow(card, "تاريخ الانضمام", c.joinDate);

        root.addView(card);

        countView = new TextView(this);
        countView.setText("...");
        countView.setTextColor(Color.parseColor("#D4AF37"));
        countView.setTextSize(15);
        countView.setTypeface(null, Typeface.BOLD);
        countView.setGravity(Gravity.CENTER);
        countView.setPadding(0, 0, 0, 24);
        root.addView(countView);

        startCountListener();

        Button constBtn = new Button(this);
        constBtn.setText("🏛️  دستور أُمّة");
        constBtn.setTextSize(16);
        constBtn.setOnClickListener(v -> startActivity(new Intent(this, ConstitutionActivity.class)));
        root.addView(constBtn);

        Button walletBtn = new Button(this);
        walletBtn.setText("💰  محفظتي");
        walletBtn.setTextSize(16);
        walletBtn.setOnClickListener(v -> startActivity(new Intent(this, WalletActivity.class)));
        root.addView(walletBtn);

        Button parlBtn = new Button(this);
        parlBtn.setText("🗳️  البرلمان");
        parlBtn.setTextSize(16);
        parlBtn.setOnClickListener(v -> startActivity(new Intent(this, ParliamentActivity.class)));
        root.addView(parlBtn);

        Button seedBtn = new Button(this);
        seedBtn.setText("🔐  الكلمات السرية");
        seedBtn.setTextSize(16);
        seedBtn.setOnClickListener(v -> showSeed(c));
        root.addView(seedBtn);
    }

    private void addRow(LinearLayout p, String label, String val) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.VERTICAL);
        r.setPadding(0, 10, 0, 10);

        TextView l = new TextView(this);
        l.setText(label);
        l.setTextColor(Color.parseColor("#9E9E9E"));
        l.setTextSize(11);
        l.setGravity(Gravity.CENTER);
        r.addView(l);

        TextView v = new TextView(this);
        v.setText(val);
        v.setTextColor(Color.WHITE);
        v.setTextSize(16);
        v.setTypeface(null, Typeface.BOLD);
        v.setGravity(Gravity.CENTER);
        r.addView(v);

        p.addView(r);
    }

    private void showSeed(Citizen c) {
        new AlertDialog.Builder(this)
            .setTitle("🔐 الكلمات السرية")
            .setMessage(c.seedPhrase)
            .setPositiveButton("حسناً", null)
            .show();
    }
}
