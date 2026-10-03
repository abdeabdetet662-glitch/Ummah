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

import com.google.firebase.firestore.ListenerRegistration;

public class TreasuryActivity extends Activity {
    private FirebaseManager fm;
    private IdentityManager im;
    private WalletManager wm;
    private TextView balanceView;
    private ListenerRegistration reg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        // Feature Check
        if (!FeatureFlags.checkOrToast(this, "treasury")) {
            finish();
            return;
        }

        fm = FirebaseManager.get();
        im = new IdentityManager(this);
        wm = new WalletManager(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0A"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(40, 80, 40, 80);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText(getString(R.string.treasury_title));
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText(getString(R.string.treasury_subtitle));
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 8, 0, 50);
        root.addView(sub);

        balanceView = new TextView(this);
        balanceView.setText("...");
        balanceView.setTextColor(Color.parseColor("#D4AF37"));
        balanceView.setTextSize(60);
        balanceView.setTypeface(null, Typeface.BOLD);
        balanceView.setGravity(Gravity.CENTER);
        root.addView(balanceView);

        TextView currency = new TextView(this);
        currency.setText(getString(R.string.treasury_dinar));
        currency.setTextColor(Color.parseColor("#9E9E9E"));
        currency.setTextSize(14);
        currency.setGravity(Gravity.CENTER);
        currency.setPadding(0, 10, 0, 50);
        root.addView(currency);

        Button contributeBtn = new Button(this);
        contributeBtn.setText(getString(R.string.treasury_donate));
        contributeBtn.setTextSize(16);
        contributeBtn.setOnClickListener(v -> showContributeDialog());
        root.addView(contributeBtn);

        TextView info = new TextView(this);
        info.setText("\n\nالخزينة العامة ممولة من تبرعات المواطنين.\nتُستخدم لتمويل المشاريع العامة، ومنح المحتاجين، ومكافآت المبدعين.");
        info.setTextColor(Color.parseColor("#9E9E9E"));
        info.setTextSize(13);
        info.setLineSpacing(8, 1);
        info.setGravity(Gravity.CENTER);
        info.setPadding(0, 40, 0, 0);
        root.addView(info);

        setContentView(scroll);
        startListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) reg.remove();
    }

    private void startListener() {
        reg = fm.listenTreasury(new FirebaseManager.BalanceListener() {
            @Override public void onBalance(int balance) {
                runOnUiThread(() -> balanceView.setText(String.valueOf(balance)));
            }
            @Override public void onError(String message) {
                runOnUiThread(() -> balanceView.setText("—"));
            }
        });
    }

    private void showContributeDialog() {
        final EditText input = new EditText(this);
        input.setHint(getString(R.string.treasury_amount));
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.GRAY);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        LinearLayout c = new LinearLayout(this);
        c.setPadding(40, 20, 40, 20);
        c.addView(input);

        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.treasury_contribute_title))
            .setMessage("رصيدك الحالي: " + wm.getBalance() + " Đ")
            .setView(c)
            .setPositiveButton(getString(R.string.treasury_contribute), (d, w) -> {
                String s = input.getText().toString().trim();
                if (s.isEmpty()) return;
                int parsedAmount;
                try { parsedAmount = Integer.parseInt(s); }
                catch (Exception e) { parsedAmount = 0; }
                if (parsedAmount <= 0) {
                    Toast.makeText(this, getString(R.string.treasury_invalid_amount), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!wm.spend(parsedAmount)) {
                    Toast.makeText(this, getString(R.string.treasury_insufficient), Toast.LENGTH_LONG).show();
                    return;
                }
                final int finalAmount = parsedAmount;
                fm.contributeToTreasury(finalAmount, new FirebaseManager.OnDone() {
                    @Override public void onSuccess() {
                        Citizen cit = im.getCitizen();
                        if (cit != null) {
                            fm.addBalance(cit.nationalId, -finalAmount, new FirebaseManager.OnDone() {
                                @Override public void onSuccess() {}
                                @Override public void onError(String m) {}
                            });
                        }
                        Toast.makeText(TreasuryActivity.this, getString(R.string.treasury_thanks), Toast.LENGTH_LONG).show();
                    }
                    @Override public void onError(String msg) {
                        wm.add(finalAmount);
                        Toast.makeText(TreasuryActivity.this, "خطأ: " + msg, Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }
}
