package com.ummah.app;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class WalletActivity extends Activity {
    private IdentityManager im;
    private WalletManager wm;
    private TextView internalBalanceView;
    private TextView ethBalanceView;
    private String ethAddress;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        im = new IdentityManager(this);
        wm = new WalletManager(this);

        Citizen c = im.getCitizen();
        if (c == null) { finish(); return; }
        ethAddress = CryptoWallet.deriveEthereumAddress(c.seedPhrase);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0A"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(36, 60, 36, 60);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("💰 محفظتك الحقيقية");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(26);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("Ethereum • USDT • كل عملات ERC-20");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 8, 0, 30);
        root.addView(sub);

        // رصيد ETH الحقيقي
        TextView ethLabel = new TextView(this);
        ethLabel.setText("💎 رصيد ETH الحقيقي");
        ethLabel.setTextColor(Color.parseColor("#9E9E9E"));
        ethLabel.setTextSize(13);
        ethLabel.setGravity(Gravity.CENTER);
        root.addView(ethLabel);

        ethBalanceView = new TextView(this);
        ethBalanceView.setText("...");
        ethBalanceView.setTextColor(Color.parseColor("#D4AF37"));
        ethBalanceView.setTextSize(42);
        ethBalanceView.setTypeface(null, Typeface.BOLD);
        ethBalanceView.setGravity(Gravity.CENTER);
        ethBalanceView.setPadding(0, 10, 0, 30);
        root.addView(ethBalanceView);

        // عنوان ETH
        TextView addrLabel = new TextView(this);
        addrLabel.setText("📮 عنوان محفظتك:");
        addrLabel.setTextColor(Color.parseColor("#9E9E9E"));
        addrLabel.setTextSize(12);
        addrLabel.setGravity(Gravity.CENTER);
        root.addView(addrLabel);

        TextView addrView = new TextView(this);
        addrView.setText(ethAddress);
        addrView.setTextColor(Color.WHITE);
        addrView.setTextSize(10);
        addrView.setTypeface(Typeface.MONOSPACE);
        addrView.setGravity(Gravity.CENTER);
        addrView.setPadding(20, 15, 20, 15);
        addrView.setBackgroundColor(Color.parseColor("#141414"));
        LinearLayout.LayoutParams al = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        al.setMargins(0, 8, 0, 0);
        addrView.setLayoutParams(al);
        root.addView(addrView);

        Button copyBtn = new Button(this);
        copyBtn.setText("📋  نسخ العنوان");
        copyBtn.setTextSize(14);
        copyBtn.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("ETH", ethAddress));
            Toast.makeText(this, "تم النسخ", Toast.LENGTH_SHORT).show();
        });
        root.addView(copyBtn);

        Button refreshBtn = new Button(this);
        refreshBtn.setText("🔄  تحديث الرصيد");
        refreshBtn.setTextSize(14);
        refreshBtn.setOnClickListener(v -> loadEthBalance());
        root.addView(refreshBtn);

        // فاصل
        TextView sep = new TextView(this);
        sep.setText("\n────────────────\n");
        sep.setTextColor(Color.parseColor("#333333"));
        sep.setGravity(Gravity.CENTER);
        root.addView(sep);

        // الرصيد الداخلي
        TextView dLabel = new TextView(this);
        dLabel.setText("🪙 الدينار الداخلي (Đ)");
        dLabel.setTextColor(Color.parseColor("#9E9E9E"));
        dLabel.setTextSize(13);
        dLabel.setGravity(Gravity.CENTER);
        root.addView(dLabel);

        internalBalanceView = new TextView(this);
        internalBalanceView.setText(wm.getBalance() + " Đ");
        internalBalanceView.setTextColor(Color.parseColor("#D4AF37"));
        internalBalanceView.setTextSize(28);
        internalBalanceView.setTypeface(null, Typeface.BOLD);
        internalBalanceView.setGravity(Gravity.CENTER);
        internalBalanceView.setPadding(0, 10, 0, 20);
        root.addView(internalBalanceView);

        Button daily = new Button(this);
        daily.setText("🎁  مكافأة اليوم (+5 Đ)");
        daily.setTextSize(14);
        daily.setEnabled(wm.canClaimDaily());
        daily.setOnClickListener(v -> {
            if (wm.claimDaily()) {
                internalBalanceView.setText(wm.getBalance() + " Đ");
                daily.setEnabled(false);
                Toast.makeText(this, "+5 Đ", Toast.LENGTH_SHORT).show();
            }
        });
        root.addView(daily);

        TextView hint = new TextView(this);
        hint.setText("\nالدينار: للتفاعل داخل الدولة.\nETH: قابل للتحويل لأي محفظة في العالم.");
        hint.setTextColor(Color.parseColor("#616161"));
        hint.setTextSize(11);
        hint.setGravity(Gravity.CENTER);
        hint.setLineSpacing(6, 1);
        hint.setPadding(0, 30, 0, 0);
        root.addView(hint);

        Button transferBtn = new Button(this);
        transferBtn.setText("💸  إرسال / استقبال دينار");
        transferBtn.setTextSize(14);
        transferBtn.setOnClickListener(v -> {
            startActivity(new android.content.Intent(WalletActivity.this, TransferActivity.class));
        });
        root.addView(transferBtn);

        setContentView(scroll);
        loadEthBalance();
    }

    private void loadEthBalance() {
        ethBalanceView.setText("...");
        CryptoRpc.getEthBalance(ethAddress, new CryptoRpc.BalanceCallback() {
            @Override public void onBalance(double eth) {
                if (eth < 0.000001) {
                    ethBalanceView.setText("0.000000\nETH");
                } else {
                    ethBalanceView.setText(String.format(Locale.US, "%.6f\nETH", eth));
                }
            }
            @Override public void onError(String message) {
                ethBalanceView.setText("—");
                Toast.makeText(WalletActivity.this, "تعذّر الاتصال بالشبكة", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
