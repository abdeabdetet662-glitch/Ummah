package com.ummah.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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

import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransferActivity extends Activity {
    private FirebaseManager fm;
    private IdentityManager im;
    private TextView balanceView;
    private LinearLayout historyContainer;
    private ListenerRegistration balReg;
    private ListenerRegistration txReg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        // Feature Check
        if (!FeatureFlags.checkOrToast(this, "transfers")) {
            finish();
            return;
        }

        fm = FirebaseManager.get();
        im = new IdentityManager(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0A"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(36, 60, 36, 60);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("💸 التحويلات");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(26);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("أرسل ديناراً لمواطن آخر في أي مكان في العالم");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 8, 0, 30);
        root.addView(sub);

        TextView bLabel = new TextView(this);
        bLabel.setText("رصيدك:");
        bLabel.setTextColor(Color.parseColor("#9E9E9E"));
        bLabel.setTextSize(12);
        bLabel.setGravity(Gravity.CENTER);
        root.addView(bLabel);

        balanceView = new TextView(this);
        balanceView.setText("...");
        balanceView.setTextColor(Color.parseColor("#D4AF37"));
        balanceView.setTextSize(36);
        balanceView.setTypeface(null, Typeface.BOLD);
        balanceView.setGravity(Gravity.CENTER);
        balanceView.setPadding(0, 8, 0, 30);
        root.addView(balanceView);

        Button sendBtn = new Button(this);
        sendBtn.setText("📤  إرسال دينار");
        sendBtn.setTextSize(16);
        sendBtn.setOnClickListener(v -> showSendDialog());
        root.addView(sendBtn);

        Button myIdBtn = new Button(this);
        myIdBtn.setText("📥  رقمي للاستقبال");
        myIdBtn.setTextSize(14);
        myIdBtn.setOnClickListener(v -> showMyId());
        root.addView(myIdBtn);

        TextView sep = new TextView(this);
        sep.setText("\n━━━ سجل التحويلات ━━━\n");
        sep.setTextColor(Color.parseColor("#D4AF37"));
        sep.setTextSize(14);
        sep.setTypeface(null, Typeface.BOLD);
        sep.setGravity(Gravity.CENTER);
        sep.setPadding(0, 30, 0, 16);
        root.addView(sep);

        historyContainer = new LinearLayout(this);
        historyContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(historyContainer);

        setContentView(scroll);

        startBalance();
        startTxListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (balReg != null) balReg.remove();
        if (txReg != null) txReg.remove();
    }

    private void startBalance() {
        Citizen c = im.getCitizen();
        if (c == null) return;
        balReg = fm.listenBalance(c.nationalId, new FirebaseManager.BalanceListener() {
            @Override public void onBalance(int balance) {
                runOnUiThread(() -> balanceView.setText(balance + " Đ"));
            }
            @Override public void onError(String m) {}
        });
    }

    private void startTxListener() {
        Citizen c = im.getCitizen();
        if (c == null) return;
        txReg = fm.listenMyTransfers(c.nationalId, list ->
            runOnUiThread(() -> refreshHistory(list)));
    }

    private void refreshHistory(List<FirebaseManager.TransferItem> list) {
        historyContainer.removeAllViews();
        Citizen me = im.getCitizen();
        if (me == null) return;

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

        for (FirebaseManager.TransferItem t : list) {
            boolean isSender = me.nationalId.equals(t.from);
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundColor(Color.parseColor("#141414"));
            card.setPadding(28, 20, 28, 20);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 12);
            card.setLayoutParams(lp);

            TextView dir = new TextView(this);
            dir.setText(isSender ? "📤 أرسلت" : "📥 استقبلت");
            dir.setTextColor(isSender ? Color.parseColor("#F44336") : Color.parseColor("#4CAF50"));
            dir.setTextSize(13);
            dir.setTypeface(null, Typeface.BOLD);
            card.addView(dir);

            TextView other = new TextView(this);
            other.setText((isSender ? "إلى: " : "من: ") + (isSender ? t.to : t.from));
            other.setTextColor(Color.WHITE);
            other.setTextSize(12);
            other.setPadding(0, 6, 0, 4);
            card.addView(other);

            TextView amt = new TextView(this);
            amt.setText((isSender ? "- " : "+ ") + t.amount + " Đ");
            amt.setTextColor(isSender ? Color.parseColor("#F44336") : Color.parseColor("#4CAF50"));
            amt.setTextSize(20);
            amt.setTypeface(null, Typeface.BOLD);
            amt.setPadding(0, 4, 0, 4);
            card.addView(amt);

            if (t.note != null && !t.note.isEmpty()) {
                TextView n = new TextView(this);
                n.setText("📝 " + t.note);
                n.setTextColor(Color.parseColor("#9E9E9E"));
                n.setTextSize(12);
                card.addView(n);
            }

            TextView date = new TextView(this);
            String d = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date(t.timestamp));
            date.setText(d);
            date.setTextColor(Color.parseColor("#616161"));
            date.setTextSize(10);
            date.setPadding(0, 6, 0, 0);
            card.addView(date);

            historyContainer.addView(card);
        }
    }

    private void showSendDialog() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(40, 20, 40, 20);

        final EditText idInput = new EditText(this);
        idInput.setHint("الرقم الوطني (UMM-XXXX-XXXX-XXXX)");
        idInput.setTextColor(Color.WHITE);
        idInput.setHintTextColor(Color.GRAY);
        idInput.setInputType(InputType.TYPE_CLASS_TEXT);

        // prefill_id من CitizensActivity
        String prefill = getIntent().getStringExtra("prefill_id");
        if (prefill != null) idInput.setText(prefill);

        c.addView(idInput);

        final EditText amtInput = new EditText(this);
        amtInput.setHint("المبلغ (دينار)");
        amtInput.setTextColor(Color.WHITE);
        amtInput.setHintTextColor(Color.GRAY);
        amtInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        c.addView(amtInput);

        final EditText noteInput = new EditText(this);
        noteInput.setHint("ملاحظة (اختياري)");
        noteInput.setTextColor(Color.WHITE);
        noteInput.setHintTextColor(Color.GRAY);
        c.addView(noteInput);

        new AlertDialog.Builder(this)
            .setTitle("تحويل دينار")
            .setView(c)
            .setPositiveButton("إرسال", (d, w) -> {
                String toId = idInput.getText().toString().trim();
                String amtStr = amtInput.getText().toString().trim();
                String note = noteInput.getText().toString().trim();
                Citizen me = im.getCitizen();
                if (me == null) return;
                if (toId.isEmpty() || amtStr.isEmpty()) {
                    Toast.makeText(this, "املأ الحقول", Toast.LENGTH_SHORT).show();
                    return;
                }
                int amount;
                try { amount = Integer.parseInt(amtStr); }
                catch (Exception e) { amount = 0; }
                if (amount <= 0) {
                    Toast.makeText(this, "مبلغ غير صالح", Toast.LENGTH_SHORT).show();
                    return;
                }
                fm.transfer(me.nationalId, toId, amount, note, new FirebaseManager.OnDone() {
                    @Override public void onSuccess() {
                        Toast.makeText(TransferActivity.this, "✅ تم التحويل", Toast.LENGTH_LONG).show();
                    }
                    @Override public void onError(String msg) {
                        Toast.makeText(TransferActivity.this, "❌ " + msg, Toast.LENGTH_LONG).show();
                    }
                });
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    private void showMyId() {
        Citizen me = im.getCitizen();
        if (me == null) return;
        new AlertDialog.Builder(this)
            .setTitle("📥 رقمك للاستقبال")
            .setMessage("أعطِ هذا الرقم لمن يريد أن يرسل لك:\n\n" + me.nationalId)
            .setPositiveButton("نسخ", (d, w) -> {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText("ID", me.nationalId));
                Toast.makeText(this, "تم النسخ", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("إغلاق", null)
            .show();
    }
}
