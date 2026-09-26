package com.ummah.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class WalletActivity extends Activity {
    private WalletManager wm;
    private TextView balanceView;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        wm = new WalletManager(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0A"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(48, 80, 48, 80);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("💰 محفظتك الرقمية");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView currency = new TextView(this);
        currency.setText("الدينار الرقمي (Đ)");
        currency.setTextColor(Color.parseColor("#9E9E9E"));
        currency.setTextSize(14);
        currency.setGravity(Gravity.CENTER);
        currency.setPadding(0, 10, 0, 60);
        root.addView(currency);

        balanceView = new TextView(this);
        balanceView.setText(wm.getBalance() + " Đ");
        balanceView.setTextColor(Color.parseColor("#D4AF37"));
        balanceView.setTextSize(64);
        balanceView.setTypeface(null, Typeface.BOLD);
        balanceView.setGravity(Gravity.CENTER);
        root.addView(balanceView);

        TextView hint = new TextView(this);
        hint.setText("\nرصيدك الحالي");
        hint.setTextColor(Color.parseColor("#616161"));
        hint.setTextSize(13);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, 0, 0, 60);
        root.addView(hint);

        Button daily = new Button(this);
        daily.setText("🎁  مكافأة اليوم (+5 Đ)");
        daily.setTextSize(16);
        daily.setEnabled(wm.canClaimDaily());
        daily.setOnClickListener(v -> {
            if (wm.claimDaily()) {
                balanceView.setText(wm.getBalance() + " Đ");
                daily.setEnabled(false);
                Toast.makeText(this, "+5 Đ أُضيفت", Toast.LENGTH_SHORT).show();
            }
        });
        root.addView(daily);

        TextView info = new TextView(this);
        info.setText("\n\nطرق كسب الدينار:\n• مكافأة يومية: +5 Đ\n• المشاركة في التصويت: +10 Đ\n• تقديم اقتراح: +20 Đ\n• الحصول على أصوات: +5 Đ لكل صوت");
        info.setTextColor(Color.parseColor("#9E9E9E"));
        info.setTextSize(13);
        info.setLineSpacing(6, 1);
        info.setGravity(Gravity.CENTER);
        info.setPadding(0, 60, 0, 0);
        root.addView(info);

        setContentView(scroll);
    }
}
