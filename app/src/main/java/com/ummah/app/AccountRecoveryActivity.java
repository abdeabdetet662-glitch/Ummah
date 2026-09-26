package com.ummah.app;

import android.app.Activity;
import android.app.AlertDialog;
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

public class AccountRecoveryActivity extends Activity {
    private FirebaseManager fm;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        fm = FirebaseManager.get();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0A"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(40, 80, 40, 80);
        scroll.addView(root);

        TextView icon = new TextView(this);
        icon.setText("🔐");
        icon.setTextSize(70);
        icon.setGravity(Gravity.CENTER);
        root.addView(icon);

        TextView title = new TextView(this);
        title.setText("استعادة الحساب");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(26);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 20, 0, 10);
        root.addView(title);

        TextView desc = new TextView(this);
        desc.setText("أدخل كلماتك السرية الـ 12 لاستعادة\nرقمك الوطني ورصيدك على أي هاتف");
        desc.setTextColor(Color.parseColor("#9E9E9E"));
        desc.setTextSize(13);
        desc.setGravity(Gravity.CENTER);
        desc.setLineSpacing(6, 1);
        desc.setPadding(0, 0, 0, 40);
        root.addView(desc);

        final EditText input = new EditText(this);
        input.setHint("الكلمات السرية مفصولة بمسافات");
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.GRAY);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setMinLines(3);
        input.setBackgroundColor(Color.parseColor("#141414"));
        input.setPadding(30, 30, 30, 30);
        root.addView(input);

        Button recoverBtn = new Button(this);
        recoverBtn.setText("🔄  استعادة");
        recoverBtn.setTextSize(16);
        recoverBtn.setPadding(40, 30, 40, 30);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 30, 0, 0);
        recoverBtn.setLayoutParams(lp);
        recoverBtn.setOnClickListener(v -> recover(input.getText().toString().trim()));
        root.addView(recoverBtn);

        TextView warn = new TextView(this);
        warn.setText("\n⚠️ لا يمكن التراجع عن الاستعادة.\nقد تفقد حسابك الحالي.");
        warn.setTextColor(Color.parseColor("#F44336"));
        warn.setTextSize(11);
        warn.setGravity(Gravity.CENTER);
        warn.setPadding(0, 40, 0, 0);
        root.addView(warn);

        setContentView(scroll);
    }

    private void recover(String seed) {
        if (seed.isEmpty()) {
            Toast.makeText(this, "أدخل الكلمات السرية", Toast.LENGTH_SHORT).show();
            return;
        }

        // حساب البصمة
        String hash = hashSeed(seed);

        fm.lookupBySeedHash(hash, new FirebaseManager.SeedLookup() {
            @Override public void onFound(final FirebaseManager.CitizenItem c) {
                new AlertDialog.Builder(AccountRecoveryActivity.this)
                    .setTitle("✅ تم العثور على حسابك")
                    .setMessage("الاسم: " + c.name + "\n" +
                            "الرصيد: " + c.balance + " Đ\n\n" +
                            "هل تريد استعادة هذا الحساب؟")
                    .setPositiveButton("استعادة", (d, w) -> {
                        // إعادة التسجيل في IdentityManager
                        IdentityManager im = new IdentityManager(AccountRecoveryActivity.this);
                        im.restoreCitizen(c.nationalId, c.name, c.joinDate, seed);
                        Toast.makeText(AccountRecoveryActivity.this,
                                "✅ تم الاستعادة. أعد تشغيل التطبيق.", Toast.LENGTH_LONG).show();
                        finish();
                    })
                    .setNegativeButton("إلغاء", null)
                    .show();
            }
            @Override public void onNotFound() {
                Toast.makeText(AccountRecoveryActivity.this,
                        "❌ لم يتم العثور على حساب بهذه الكلمات", Toast.LENGTH_LONG).show();
            }
        });
    }

    private String hashSeed(String seed) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] h = md.digest(seed.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : h) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return seed;
        }
    }
}
