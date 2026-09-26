package com.ummah.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration;

public class WalletActivity extends Activity {
    private IdentityManager im;
    private FirebaseManager fm;
    private TextView balanceView;
    private ListenerRegistration balReg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        im = new IdentityManager(this);
        fm = FirebaseManager.get();

        Citizen c = im.getCitizen();
        if (c == null) { finish(); return; }

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0A"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(36, 60, 36, 60);
        scroll.addView(root);

        // ============ العنوان الرئيسي ============
        TextView title = new TextView(this);
        title.setText("💰 محفظتي");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("الدينار الداخلي لدولة أُمّة");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 8, 0, 50);
        root.addView(sub);

        // ============ الرصيد (الأكبر) ============
        TextView balanceLabel = new TextView(this);
        balanceLabel.setText("رصيدك:");
        balanceLabel.setTextColor(Color.parseColor("#9E9E9E"));
        balanceLabel.setTextSize(14);
        balanceLabel.setGravity(Gravity.CENTER);
        root.addView(balanceLabel);

        balanceView = new TextView(this);
        balanceView.setText("...");
        balanceView.setTextColor(Color.parseColor("#D4AF37"));
        balanceView.setTextSize(64);
        balanceView.setTypeface(null, Typeface.BOLD);
        balanceView.setGravity(Gravity.CENTER);
        balanceView.setPadding(0, 15, 0, 10);
        root.addView(balanceView);

        TextView currencyLabel = new TextView(this);
        currencyLabel.setText("Đ دينار أُمّة");
        currencyLabel.setTextColor(Color.parseColor("#9E9E9E"));
        currencyLabel.setTextSize(14);
        currencyLabel.setGravity(Gravity.CENTER);
        currencyLabel.setPadding(0, 0, 0, 40);
        root.addView(currencyLabel);

        // ============ الأزرار ============

        // زر الإرسال / الاستقبال (الأهم)
        Button transferBtn = new Button(this);
        transferBtn.setText("💸  إرسال / استقبال");
        transferBtn.setTextSize(17);
        transferBtn.setPadding(20, 25, 20, 25);
        transferBtn.setOnClickListener(v ->
            startActivity(new Intent(WalletActivity.this, TransferActivity.class)));
        root.addView(transferBtn);

        // زر رقمي للاستقبال
        Button myIdBtn = new Button(this);
        myIdBtn.setText("📥  رقمي للاستقبال");
        myIdBtn.setTextSize(15);
        myIdBtn.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(WalletActivity.this)
                .setTitle("رقمك للاستقبال")
                .setMessage("أعطِ هذا الرقم لمن يريد أن يرسل لك ديناراً:\n\n" + c.nationalId)
                .setPositiveButton("حسناً", null)
                .show();
        });
        root.addView(myIdBtn);


        // زر تحديث يدوي
        Button refreshBtn = new Button(this);
        refreshBtn.setText("🔄  تحديث الرصيد");
        refreshBtn.setTextSize(14);
        refreshBtn.setOnClickListener(v -> {
            Toast.makeText(WalletActivity.this, "جاري التحديث...", Toast.LENGTH_SHORT).show();
            startBalanceListener();
        });
        root.addView(refreshBtn);

        // ============ معلومات ============
        TextView sep = new TextView(this);
        sep.setText("\n━━━━━━━━━━━━━━━━\n");
        sep.setTextColor(Color.parseColor("#333333"));
        sep.setGravity(Gravity.CENTER);
        sep.setPadding(0, 40, 0, 20);
        root.addView(sep);

        TextView info = new TextView(this);
        info.setText("طرق كسب الدينار:\n\n" +
                "🎁  مكافأة يومية: +5 Đ\n" +
                "🗳️  التصويت على اقتراح: +5 Đ\n" +
                "📝  تقديم اقتراح: +20 Đ\n" +
                "👑  الفوز بالانتخابات: +500 Đ");
        info.setTextColor(Color.parseColor("#9E9E9E"));
        info.setTextSize(13);
        info.setLineSpacing(8, 1);
        info.setGravity(Gravity.CENTER);
        info.setPadding(0, 20, 0, 40);
        root.addView(info);

        TextView footer = new TextView(this);
        footer.setText("دولة أُمّة الرقمية\nدينار واحد = تفاعل واحد");
        footer.setTextColor(Color.parseColor("#616161"));
        footer.setTextSize(11);
        footer.setGravity(Gravity.CENTER);
        root.addView(footer);

        setContentView(scroll);
        startBalanceListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (balReg != null) balReg.remove();
    }

    private void startBalanceListener() {
        Citizen c = im.getCitizen();
        if (c == null) return;
        if (balReg != null) balReg.remove();
        balReg = fm.listenBalance(c.nationalId, new FirebaseManager.BalanceListener() {
            @Override public void onBalance(int balance) {
                runOnUiThread(() -> balanceView.setText(String.valueOf(balance)));
            }
            @Override public void onError(String message) {
                runOnUiThread(() -> balanceView.setText("—"));
            }
        });
    }
}
