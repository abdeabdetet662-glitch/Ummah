package com.ummah.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private IdentityManager identityManager;
    private LinearLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        identityManager = new IdentityManager(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0A"));
        scroll.setFillViewport(true);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(48, 80, 48, 80);
        scroll.addView(root);

        setContentView(scroll);

        if (identityManager.isCitizen()) {
            showCitizenshipCard(identityManager.getCitizen());
        } else {
            showWelcomeScreen();
        }
    }

    private void showWelcomeScreen() {
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

        TextView subtitle = new TextView(this);
        subtitle.setText("أول دولة رقمية في العالم العربي");
        subtitle.setTextColor(Color.parseColor("#9E9E9E"));
        subtitle.setTextSize(16);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, 40);
        root.addView(subtitle);

        TextView desc = new TextView(this);
        desc.setText("لا حدود. لا تأشيرة. لا جواز سفر.\nفقط هاتفك، وكلمتك، وأمتك.");
        desc.setTextColor(Color.parseColor("#BDBDBD"));
        desc.setTextSize(15);
        desc.setGravity(Gravity.CENTER);
        desc.setLineSpacing(8, 1);
        desc.setPadding(0, 0, 0, 60);
        root.addView(desc);

        Button joinBtn = new Button(this);
        joinBtn.setText("انضم إلى الأمة");
        joinBtn.setTextSize(18);
        joinBtn.setPadding(40, 30, 40, 30);
        joinBtn.setOnClickListener(v -> askForName());
        root.addView(joinBtn);

        TextView footer = new TextView(this);
        footer.setText("\n\nمواطنة مجانية. لا تحتاج بريداً أو رقم هاتف.");
        footer.setTextColor(Color.parseColor("#616161"));
        footer.setTextSize(12);
        footer.setGravity(Gravity.CENTER);
        root.addView(footer);
    }

    private void askForName() {
        EditText input = new EditText(this);
        input.setHint("اسمك أو كنيتك");
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.GRAY);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        LinearLayout container = new LinearLayout(this);
        container.setPadding(40, 20, 40, 20);
        container.addView(input);

        new AlertDialog.Builder(this)
                .setTitle("اختر اسمك كمواطن")
                .setMessage("هذا الاسم سيظهر على بطاقة مواطنتك. يمكنك تغييره لاحقاً.")
                .setView(container)
                .setPositiveButton("متابعة", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) name = "مواطن مجهول";
                    Citizen c = identityManager.registerCitizen(name);
                    showSeedPhraseDialog(c);
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void showSeedPhraseDialog(Citizen citizen) {
        new AlertDialog.Builder(this)
                .setTitle("🔐 كلماتك السرية")
                .setMessage("احفظ هذه الكلمات الـ 12 في مكان آمن.\n\n" +
                        "هي هويتك الوحيدة في الأمة.\n" +
                        "لا تشاركها مع أحد.\n" +
                        "لن تستطيع استعادتها إذا فقدتها.\n\n" +
                        "——————————————\n\n" +
                        citizen.seedPhrase)
                .setPositiveButton("حفظتها", (d, w) -> {
                    showCitizenshipCard(citizen);
                    Toast.makeText(this, "مرحباً بك في أُمّة", Toast.LENGTH_LONG).show();
                })
                .setCancelable(false)
                .show();
    }

    private void showCitizenshipCard(Citizen c) {
        root.removeAllViews();

        TextView flag = new TextView(this);
        flag.setText("🌍");
        flag.setTextSize(60);
        flag.setGravity(Gravity.CENTER);
        root.addView(flag);

        // البطاقة
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.parseColor("#0B4F2C"));
        card.setPadding(40, 40, 40, 40);
        card.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 30, 0, 30);
        card.setLayoutParams(cardParams);

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

        addCardRow(card, "الاسم", c.name);
        addCardRow(card, "الرقم الوطني", c.nationalId);
        addCardRow(card, "تاريخ الانضمام", c.joinDate);

        root.addView(card);

        // الأزرار
        Button showSeedBtn = new Button(this);
        showSeedBtn.setText("🔐  عرض الكلمات السرية");
        showSeedBtn.setOnClickListener(v -> showSeedPhrase(c));
        root.addView(showSeedBtn);

        Button shareBtn = new Button(this);
        shareBtn.setText("📤  مشاركة الرقم الوطني");
        shareBtn.setOnClickListener(v -> {
            Toast.makeText(this, c.nationalId, Toast.LENGTH_LONG).show();
        });
        root.addView(shareBtn);

        TextView footer = new TextView(this);
        footer.setText("\nمرحلة 1: المواطنة\nالمراحل القادمة: الدستور، العملة، البرلمان");
        footer.setTextColor(Color.parseColor("#616161"));
        footer.setTextSize(12);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, 40, 0, 0);
        root.addView(footer);
    }

    private void addCardRow(LinearLayout parent, String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, 12, 0, 12);

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextColor(Color.parseColor("#9E9E9E"));
        labelView.setTextSize(12);
        labelView.setGravity(Gravity.CENTER);
        row.addView(labelView);

        TextView valueView = new TextView(this);
        valueView.setText(value);
        valueView.setTextColor(Color.WHITE);
        valueView.setTextSize(18);
        valueView.setTypeface(null, Typeface.BOLD);
        valueView.setGravity(Gravity.CENTER);
        row.addView(valueView);

        parent.addView(row);
    }

    private void showSeedPhrase(Citizen c) {
        new AlertDialog.Builder(this)
                .setTitle("🔐 كلماتك السرية")
                .setMessage(c.seedPhrase + "\n\n——————————————\n\n" +
                        "هذه الكلمات هي مفتاح هويتك.\n" +
                        "احفظها في مكان آمن.")
                .setPositiveButton("حسناً", null)
                .show();
    }
}
