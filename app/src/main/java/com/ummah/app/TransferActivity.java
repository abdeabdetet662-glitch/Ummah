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

public class TransferActivity extends Activity {
    private WalletManager wm;
    private IdentityManager im;
    private TransferManager tm;
    private TextView balanceView;
    private LinearLayout historyContainer;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        wm = new WalletManager(this);
        im = new IdentityManager(this);
        tm = new TransferManager(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0A"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(36, 60, 36, 60);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("💸 إرسال دينار");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("حوّل الدينار لمواطن آخر");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 8, 0, 30);
        root.addView(sub);

        // الرصيد الحالي
        TextView bLabel = new TextView(this);
        bLabel.setText("رصيدك الحالي:");
        bLabel.setTextColor(Color.parseColor("#9E9E9E"));
        bLabel.setTextSize(12);
        bLabel.setGravity(Gravity.CENTER);
        root.addView(bLabel);

        balanceView = new TextView(this);
        balanceView.setText(wm.getBalance() + " Đ");
        balanceView.setTextColor(Color.parseColor("#D4AF37"));
        balanceView.setTextSize(38);
        balanceView.setTypeface(null, Typeface.BOLD);
        balanceView.setGravity(Gravity.CENTER);
        balanceView.setPadding(0, 8, 0, 30);
        root.addView(balanceView);

        // زر الإرسال
        Button sendBtn = new Button(this);
        sendBtn.setText("📤  إرسال دينار");
        sendBtn.setTextSize(16);
        sendBtn.setOnClickListener(v -> showSendDialog());
        root.addView(sendBtn);

        // زر عرض رقمي للاستقبال
        Button receiveBtn = new Button(this);
        receiveBtn.setText("📥  رقمي للاستقبال");
        receiveBtn.setTextSize(16);
        receiveBtn.setOnClickListener(v -> showMyId());
        root.addView(receiveBtn);

        // زر استقبال تجريبي (للتجربة)
        Button fakeBtn = new Button(this);
        fakeBtn.setText("🧪  استقبال تجريبي (+50 Đ)");
        fakeBtn.setTextSize(14);
        fakeBtn.setOnClickListener(v -> {
            tm.addFakeReceive(50);
            wm.add(50);
            balanceView.setText(wm.getBalance() + " Đ");
            Toast.makeText(this, "+50 Đ", Toast.LENGTH_SHORT).show();
            refreshHistory();
        });
        root.addView(fakeBtn);

        // فاصل
        TextView sep = new TextView(this);
        sep.setText("\n━━━ سجل التحويلات ━━━\n");
        sep.setTextColor(Color.parseColor("#D4AF37"));
        sep.setTextSize(14);
        sep.setTypeface(null, Typeface.BOLD);
        sep.setGravity(Gravity.CENTER);
        sep.setPadding(0, 40, 0, 20);
        root.addView(sep);

        historyContainer = new LinearLayout(this);
        historyContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(historyContainer);

        setContentView(scroll);
        refreshHistory();
    }

    private void refreshHistory() {
        historyContainer.removeAllViews();
        java.util.List<TransferManager.Transfer> list = tm.getSent();

        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("لا توجد تحويلات بعد.");
            empty.setTextColor(Color.parseColor("#616161"));
            empty.setTextSize(13);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 20, 0, 0);
            historyContainer.addView(empty);
            return;
        }

        for (TransferManager.Transfer t : list) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundColor(Color.parseColor("#141414"));
            card.setPadding(28, 20, 28, 20);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 12);
            card.setLayoutParams(lp);

            TextView to = new TextView(this);
            to.setText("← إلى: " + t.toId);
            to.setTextColor(Color.WHITE);
            to.setTextSize(14);
            to.setTypeface(null, Typeface.BOLD);
            card.addView(to);

            TextView amt = new TextView(this);
            amt.setText("- " + t.amount + " Đ");
            amt.setTextColor(Color.parseColor("#F44336"));
            amt.setTextSize(18);
            amt.setTypeface(null, Typeface.BOLD);
            amt.setPadding(0, 6, 0, 4);
            card.addView(amt);

            TextView date = new TextView(this);
            date.setText(t.date);
            date.setTextColor(Color.parseColor("#757575"));
            date.setTextSize(11);
            card.addView(date);

            if (t.note != null && !t.note.isEmpty()) {
                TextView n = new TextView(this);
                n.setText("📝 " + t.note);
                n.setTextColor(Color.parseColor("#9E9E9E"));
                n.setTextSize(12);
                n.setPadding(0, 6, 0, 0);
                card.addView(n);
            }

            historyContainer.addView(card);
        }
    }

    private void showSendDialog() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(40, 20, 40, 20);

        final EditText idInput = new EditText(this);
        idInput.setHint("الرقم الوطني للمواطن (UMM-XXXX-...)");
        idInput.setTextColor(Color.WHITE);
        idInput.setHintTextColor(Color.GRAY);
        idInput.setInputType(InputType.TYPE_CLASS_TEXT);
        c.addView(idInput);

        final EditText amtInput = new EditText(this);
        amtInput.setHint("المبلغ (بالدينار)");
        amtInput.setTextColor(Color.WHITE);
        amtInput.setHintTextColor(Color.GRAY);
        amtInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        c.addView(amtInput);

        final EditText noteInput = new EditText(this);
        noteInput.setHint("ملاحظة (اختياري)");
        noteInput.setTextColor(Color.WHITE);
        noteInput.setHintTextColor(Color.GRAY);
        noteInput.setInputType(InputType.TYPE_CLASS_TEXT);
        c.addView(noteInput);

        new AlertDialog.Builder(this)
            .setTitle("تحويل دينار")
            .setView(c)
            .setPositiveButton("إرسال", (d, w) -> {
                String toId = idInput.getText().toString().trim();
                String amtStr = amtInput.getText().toString().trim();
                String note = noteInput.getText().toString().trim();

                if (toId.isEmpty()) {
                    Toast.makeText(this, "أدخل الرقم الوطني", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (amtStr.isEmpty()) {
                    Toast.makeText(this, "أدخل المبلغ", Toast.LENGTH_SHORT).show();
                    return;
                }

                int amount;
                try { amount = Integer.parseInt(amtStr); }
                catch (Exception e) { amount = 0; }

                if (amount <= 0) {
                    Toast.makeText(this, "المبلغ غير صالح", Toast.LENGTH_SHORT).show();
                    return;
                }

                Citizen me = im.getCitizen();
                if (me != null && me.nationalId.equals(toId)) {
                    Toast.makeText(this, "لا يمكنك الإرسال لنفسك", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!wm.spend(amount)) {
                    Toast.makeText(this, "رصيدك غير كافٍ", Toast.LENGTH_LONG).show();
                    return;
                }

                tm.addSent(toId, amount, note);
                balanceView.setText(wm.getBalance() + " Đ");
                refreshHistory();
                Toast.makeText(this, "✅ أُرسل " + amount + " Đ", Toast.LENGTH_LONG).show();
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    private void showMyId() {
        Citizen me = im.getCitizen();
        if (me == null) return;

        new AlertDialog.Builder(this)
            .setTitle("📥 رقمك للاستقبال")
            .setMessage("أعطِ هذا الرقم لمن يريد أن يرسل لك ديناراً:\n\n" +
                    me.nationalId + "\n\n" +
                    "اسمك: " + me.name)
            .setPositiveButton("حسناً", null)
            .show();
    }
}
