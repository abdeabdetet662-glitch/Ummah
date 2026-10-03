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

import com.google.firebase.firestore.ListenerRegistration;

public class PresidentTreasuryActivity extends Activity {

    private PresidentManager pm;
    private TextView balanceView;
    private ListenerRegistration reg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        pm = new PresidentManager();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 50, 30, 60);
        scroll.addView(root);

        TextView title = UiHelper.goldTitle(this, getString(R.string.ptreasury_title), 26);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText(getString(R.string.ptreasury_subtitle));
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 24);
        root.addView(sub);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setBackgroundResource(R.drawable.bg_president_card);
        card.setPadding(40, 50, 40, 50);

        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        clp.setMargins(0, 20, 0, 30);
        card.setLayoutParams(clp);

        TextView lbl = new TextView(this);
        lbl.setText(getString(R.string.ptreasury_balance));
        lbl.setTextColor(Color.parseColor("#9E9E9E"));
        lbl.setTextSize(13);
        lbl.setGravity(Gravity.CENTER);
        card.addView(lbl);

        balanceView = new TextView(this);
        balanceView.setText("...");
        balanceView.setTextColor(Color.parseColor("#FFD700"));
        balanceView.setTextSize(52);
        balanceView.setTypeface(null, Typeface.BOLD);
        balanceView.setGravity(Gravity.CENTER);
        balanceView.setPadding(0, 16, 0, 0);
        card.addView(balanceView);

        TextView currency = new TextView(this);
        currency.setText("Đ دينار أُمّة");
        currency.setTextColor(Color.parseColor("#9E9E9E"));
        currency.setTextSize(13);
        currency.setGravity(Gravity.CENTER);
        card.addView(currency);

        root.addView(card);

        Button addBtn = UiHelper.primaryButton(this, getString(R.string.ptreasury_add_section));
        addBtn.setOnClickListener(v -> showAmountDialog(true));
        root.addView(addBtn);

        Button withdrawBtn = UiHelper.actionButton(this, getString(R.string.ptreasury_withdraw_section), "#C62828");
        withdrawBtn.setOnClickListener(v -> showAmountDialog(false));
        root.addView(withdrawBtn);

        setContentView(scroll);
        startListener();
    }

    private void startListener() {
        if (reg != null) reg.remove();
        reg = pm.listenTreasury(balance -> runOnUiThread(() -> {
            if (balanceView != null) balanceView.setText(String.valueOf(balance));
        }));
    }

    private void showAmountDialog(final boolean isAdd) {
        final EditText input = UiHelper.input(this, "المبلغ بالدينار");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        LinearLayout box = new LinearLayout(this);
        box.setPadding(40, 20, 40, 20);
        box.addView(input);

        new AlertDialog.Builder(this)
                .setTitle(isAdd ? getString(R.string.ptreasury_add_btn) : getString(R.string.ptreasury_withdraw_btn))
                .setView(box)
                .setPositiveButton(isAdd ? getString(R.string.ptreasury_add) : getString(R.string.ptreasury_withdraw), (d, w) -> {
                    String txt = input.getText().toString().trim();
                    if (txt.isEmpty()) return;
                    long amt;
                    try { amt = Long.parseLong(txt); } catch (Exception e) { return; }
                    if (amt <= 0) return;

                    if (isAdd) {
                        pm.addToTreasury(amt, new PresidentManager.OnDone() {
                            @Override public void onSuccess() {
                                Toast.makeText(PresidentTreasuryActivity.this,
                                        getString(R.string.ptreasury_added), Toast.LENGTH_SHORT).show();
                            }
                            @Override public void onError(String msg) {
                                Toast.makeText(PresidentTreasuryActivity.this,
                                        "❌ " + msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        pm.withdrawFromTreasury(amt, new PresidentManager.OnDone() {
                            @Override public void onSuccess() {
                                Toast.makeText(PresidentTreasuryActivity.this,
                                        getString(R.string.ptreasury_withdrawn), Toast.LENGTH_SHORT).show();
                            }
                            @Override public void onError(String msg) {
                                Toast.makeText(PresidentTreasuryActivity.this,
                                        "❌ " + msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) reg.remove();
    }
}
