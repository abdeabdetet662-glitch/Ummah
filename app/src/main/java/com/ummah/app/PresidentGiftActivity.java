package com.ummah.app;

import android.app.Activity;
import android.app.AlertDialog;
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

public class PresidentGiftActivity extends Activity {

    private IdentityManager im;
    private PresidentManager pm;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        im = new IdentityManager(this);
        pm = new PresidentManager();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 50, 30, 60);
        scroll.addView(root);

        TextView title = UiHelper.goldTitle(this, "🎁  هدية رئاسية", 26);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("وزّع فلوس من الخزينة لمواطن");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 24);
        root.addView(sub);

        LinearLayout card = UiHelper.card(this);

        TextView lbl1 = new TextView(this);
        lbl1.setText("🎯 الرقم الوطني");
        lbl1.setTextColor(Color.parseColor("#D4AF37"));
        lbl1.setTextSize(14);
        lbl1.setTypeface(null, Typeface.BOLD);
        lbl1.setGravity(Gravity.CENTER);
        lbl1.setPadding(0, 0, 0, 12);
        card.addView(lbl1);

        final EditText idInput = UiHelper.input(this, "UMM-XXXX-XXXX-XXXX");
        card.addView(idInput);

        TextView lbl2 = new TextView(this);
        lbl2.setText("💰 المبلغ (Đ)");
        lbl2.setTextColor(Color.parseColor("#D4AF37"));
        lbl2.setTextSize(14);
        lbl2.setTypeface(null, Typeface.BOLD);
        lbl2.setGravity(Gravity.CENTER);
        lbl2.setPadding(0, 20, 0, 12);
        card.addView(lbl2);

        final EditText amountInput = UiHelper.input(this, "المبلغ بالدينار");
        amountInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        card.addView(amountInput);

        TextView lbl3 = new TextView(this);
        lbl3.setText("📝 رسالة (اختياري)");
        lbl3.setTextColor(Color.parseColor("#D4AF37"));
        lbl3.setTextSize(14);
        lbl3.setTypeface(null, Typeface.BOLD);
        lbl3.setGravity(Gravity.CENTER);
        lbl3.setPadding(0, 20, 0, 12);
        card.addView(lbl3);

        final EditText msgInput = UiHelper.input(this, "رسالة للمواطن...");
        card.addView(msgInput);

        root.addView(card);

        LinearLayout quick = new LinearLayout(this);
        quick.setOrientation(LinearLayout.HORIZONTAL);
        quick.setPadding(0, 10, 0, 10);

        addQuick(quick, amountInput, 100, "100 Đ");
        addQuick(quick, amountInput, 500, "500 Đ");
        addQuick(quick, amountInput, 1000, "1000 Đ");
        addQuick(quick, amountInput, 5000, "5000 Đ");

        root.addView(quick);

        Button send = UiHelper.primaryButton(this, "🎁  إرسال الهدية");
        send.setMinHeight(160);
        send.setTextSize(18);
        send.setOnClickListener(v -> {
            String id = idInput.getText().toString().trim();
            String amtS = amountInput.getText().toString().trim();
            String msg = msgInput.getText().toString().trim();
            if (id.isEmpty() || amtS.isEmpty()) {
                Toast.makeText(this, "أكمل الحقول", Toast.LENGTH_SHORT).show();
                return;
            }
            int amt;
            try { amt = Integer.parseInt(amtS); } catch (Exception e) {
                Toast.makeText(this, "مبلغ غير صحيح", Toast.LENGTH_SHORT).show();
                return;
            }
            if (amt < 1) { Toast.makeText(this, "المبلغ > 0", Toast.LENGTH_SHORT).show(); return; }

            final int finalAmt = amt;
            new AlertDialog.Builder(this)
                    .setTitle("تأكيد الهدية")
                    .setMessage("إرسال " + amt + " Đ إلى:\n" + id + "؟")
                    .setPositiveButton("إرسال", (d, w) -> {
                        Citizen me = im.getCitizen();
                        if (me == null) return;
                        pm.sendPresidentialGift(me.nationalId, me.name, id, finalAmt, msg,
                                new PresidentManager.OnDone() {
                            @Override public void onSuccess() {
                                Toast.makeText(PresidentGiftActivity.this,
                                        "✅ تم إرسال " + finalAmt + " Đ", Toast.LENGTH_LONG).show();
                                finish();
                            }
                            @Override public void onError(String m) {
                                Toast.makeText(PresidentGiftActivity.this,
                                        "❌ " + m, Toast.LENGTH_LONG).show();
                            }
                        });
                    })
                    .setNegativeButton("إلغاء", null)
                    .show();
        });
        root.addView(send);

        setContentView(scroll);
    }

    private void addQuick(LinearLayout parent, final EditText input, final int val, String label) {
        Button btn = new Button(this);
        btn.setText(label);
        btn.setTextSize(11);
        btn.setTextColor(Color.parseColor("#D4AF37"));
        btn.setAllCaps(false);
        btn.setBackgroundResource(R.drawable.bg_btn_outline);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(4, 0, 4, 0);
        btn.setLayoutParams(lp);
        btn.setOnClickListener(v -> input.setText(String.valueOf(val)));
        parent.addView(btn);
    }
}
