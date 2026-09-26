package com.ummah.admin;

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
import android.widget.TextView;
import android.widget.Toast;

public class AdminTreasuryActivity extends Activity {
    private AdminManager am;
    private TextView balView;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        am = AdminManager.get();
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.parseColor("#0A0A0A"));
        root.setPadding(40, 80, 40, 80);

        TextView t = new TextView(this);
        t.setText("🏦 خزينة الدولة");
        t.setTextColor(Color.parseColor("#D4AF37"));
        t.setTextSize(24);
        t.setTypeface(null, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        root.addView(t);

        balView = new TextView(this);
        balView.setText("...");
        balView.setTextColor(Color.parseColor("#FFD700"));
        balView.setTextSize(56);
        balView.setTypeface(null, Typeface.BOLD);
        balView.setGravity(Gravity.CENTER);
        balView.setPadding(0, 40, 0, 40);
        root.addView(balView);

        am.loadStats(new AdminManager.StatsListener() {
            @Override public void onStats(int c, int g, int t2, int n, int p, int cp, long tr, int v) {
                runOnUiThread(() -> balView.setText(tr + " Đ"));
            }
        });

        Button add = new Button(this);
        add.setText("➕ إضافة للخزينة");
        add.setOnClickListener(v -> dialog(true));
        root.addView(add);

        Button set = new Button(this);
        set.setText("✏️ تعيين قيمة");
        set.setOnClickListener(v -> dialog(false));
        root.addView(set);

        setContentView(root);
    }

    private void dialog(final boolean isAdd) {
        final EditText input = new EditText(this);
        input.setHint("المبلغ");
        input.setTextColor(Color.WHITE);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        LinearLayout box = new LinearLayout(this);
        box.setPadding(40, 20, 40, 20);
        box.addView(input);

        new AlertDialog.Builder(this)
            .setTitle(isAdd ? "إضافة للخزينة" : "تعيين الخزينة")
            .setView(box)
            .setPositiveButton("تنفيذ", (d, w) -> {
                try {
                    long amt = Long.parseLong(input.getText().toString().trim());
                    if (isAdd) {
                        am.addTreasury(amt, new AdminManager.OnDone() {
                            @Override public void onSuccess() { Toast.makeText(this, "✅", Toast.LENGTH_SHORT).show(); }
                            @Override public void onError(String m) {}
                        });
                    } else {
                        am.setTreasury(amt, new AdminManager.OnDone() {
                            @Override public void onSuccess() { Toast.makeText(this, "✅", Toast.LENGTH_SHORT).show(); }
                            @Override public void onError(String m) {}
                        });
                    }
                } catch (Exception e) {}
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }
}
