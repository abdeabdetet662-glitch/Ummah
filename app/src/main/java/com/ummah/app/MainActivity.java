package com.ummah.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private IdentityManager im;
    private LinearLayout root;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        im = new IdentityManager(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0A"));
        scroll.setFillViewport(true);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(40, 60, 40, 60);
        scroll.addView(root);

        setContentView(scroll);

        if (im.isCitizen()) {
            showCard(im.getCitizen());
        } else {
            showWelcome();
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
        sub.setPadding(0, 0, 0, 40);
        root.addView(sub);

        TextView desc = new TextView(this);
        desc.setText("لا حدود. لا تأشيرة. لا جواز سفر.\nفقط هاتفك، وكلمتك، وأمتك.");
        desc.setTextColor(Color.parseColor("#BDBDBD"));
        desc.setTextSize(15);
        desc.setGravity(Gravity.CENTER);
        desc.setLineSpacing(8, 1);
        desc.setPadding(0, 0, 0, 60);
        root.addView(desc);

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
                showSeedDialog(citizen);
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    private void showSeedDialog(Citizen c) {
        new AlertDialog.Builder(this)
            .setTitle("🔐 كلماتك السرية")
            .setMessage("احفظ هذه الكلمات الـ 12 في مكان آمن.\n\nهي هويتك الوحيدة.\n\n——————————————\n\n" + c.seedPhrase)
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
        flag.setTextSize(60);
        flag.setGravity(Gravity.CENTER);
        root.addView(flag);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.parseColor("#0B4F2C"));
        card.setPadding(40, 40, 40, 40);
        card.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 30, 0, 30);
        card.setLayoutParams(lp);

        TextView header = new TextView(this);
        header.setText("بطاقة المواطنة");
        header.setTextColor(Color.parseColor("#D4AF37"));
        header.setTextSize(20);
        header.setTypeface(null, Typeface.BOLD);
        header.setGravity(Gravity.CENTER);
        card.addView(header);

        TextView country = new TextView(this);
        country.setText("دولة أُمّة الرقمية");
        country.setTextColor(Color.WHITE);
        country.setTextSize(14);
        country.setGravity(Gravity.CENTER);
        country.setPadding(0, 5, 0, 30);
        card.addView(country);

        addRow(card, "الاسم", c.name);
        addRow(card, "الرقم الوطني", c.nationalId);
        addRow(card, "تاريخ الانضمام", c.joinDate);

        root.addView(card);

        Button constBtn = new Button(this);
        constBtn.setText("🏛️  دستور أُمّة");
        constBtn.setTextSize(16);
        constBtn.setOnClickListener(v -> startActivity(new Intent(this, ConstitutionActivity.class)));
        root.addView(constBtn);

        Button walletBtn = new Button(this);
        walletBtn.setText("💰  محفظتي الرقمية");
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

        TextView footer = new TextView(this);
        footer.setText("\nالمرحلة 1-4 مكتملة\nالقادم: المحاكم، الوزارات، الجيش السيبراني");
        footer.setTextColor(Color.parseColor("#616161"));
        footer.setTextSize(12);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, 40, 0, 0);
        root.addView(footer);
    }

    private void addRow(LinearLayout p, String label, String val) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.VERTICAL);
        r.setPadding(0, 12, 0, 12);

        TextView l = new TextView(this);
        l.setText(label);
        l.setTextColor(Color.parseColor("#9E9E9E"));
        l.setTextSize(12);
        l.setGravity(Gravity.CENTER);
        r.addView(l);

        TextView v = new TextView(this);
        v.setText(val);
        v.setTextColor(Color.WHITE);
        v.setTextSize(18);
        v.setTypeface(null, Typeface.BOLD);
        v.setGravity(Gravity.CENTER);
        r.addView(v);

        p.addView(r);
    }

    private void showSeed(Citizen c) {
        new AlertDialog.Builder(this)
            .setTitle("🔐 الكلمات السرية")
            .setMessage(c.seedPhrase + "\n\n——————————————\n\nهذه الكلمات مفتاح هويتك.")
            .setPositiveButton("حسناً", null)
            .show();
    }
}
