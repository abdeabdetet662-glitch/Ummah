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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class AdminCitizensActivity extends Activity {

    private AdminManager am;
    private LinearLayout listContainer;
    private ListenerRegistration reg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        am = AdminManager.get();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0A"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 40, 20, 40);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("👥 إدارة المواطنين");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(24);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("تحكم كامل بكل مواطن");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 8, 0, 20);
        root.addView(sub);

        listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(listContainer);

        setContentView(scroll);
        reg = am.listenAllCitizens(list -> runOnUiThread(() -> render(list)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) reg.remove();
    }

    private void render(List<AdminManager.Citizen> list) {
        listContainer.removeAllViews();
        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("لا يوجد مواطنون");
            empty.setTextColor(Color.parseColor("#616161"));
            empty.setTextSize(13);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 40, 0, 0);
            listContainer.addView(empty);
            return;
        }
        for (AdminManager.Citizen c : list) listContainer.addView(buildCard(c));
    }

    private LinearLayout buildCard(final AdminManager.Citizen c) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.parseColor("#141414"));
        card.setPadding(24, 20, 24, 20);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 12);
        card.setLayoutParams(lp);

        TextView name = new TextView(this);
        name.setText("👤 " + (c.name != null ? c.name : "مجهول"));
        name.setTextColor(Color.WHITE);
        name.setTextSize(16);
        name.setTypeface(null, Typeface.BOLD);
        card.addView(name);

        TextView id = new TextView(this);
        id.setText(c.nationalId);
        id.setTextColor(Color.parseColor("#757575"));
        id.setTextSize(10);
        id.setTypeface(Typeface.MONOSPACE);
        card.addView(id);

        TextView bal = new TextView(this);
        bal.setText("💰 " + c.balance + " Đ");
        bal.setTextColor(Color.parseColor("#FFD700"));
        bal.setTextSize(16);
        bal.setTypeface(null, Typeface.BOLD);
        bal.setPadding(0, 8, 0, 12);
        card.addView(bal);

        // أزرار التحكم
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);

        row1.addView(makeBtn("➕ 100", "#2E7D32", v -> am.addBalance(c.nationalId, 100, done())));
        row1.addView(makeBtn("➖ 100", "#C62828", v -> am.addBalance(c.nationalId, -100, done())));
        row1.addView(makeBtn("✏️ تعديل", "#1565C0", v -> editBalance(c)));
        card.addView(row1);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);

        row2.addView(makeBtn("🎁 هدية", "#C2185B", v -> quickGift(c)));
        row2.addView(makeBtn("💸 500", "#6A1B9A", v -> am.addBalance(c.nationalId, 500, done())));
        row2.addView(makeBtn("🗑️ تصفير", "#424242", v -> {
            new AlertDialog.Builder(this)
                .setTitle("تصفير الرصيد")
                .setMessage("هل تريد تصفير رصيد " + c.name + "؟")
                .setPositiveButton("نعم", (d, w) -> am.setBalance(c.nationalId, 0, done()))
                .setNegativeButton("لا", null)
                .show();
        }));
        card.addView(row2);

        return card;
    }

    private Button makeBtn(String text, String color, android.view.View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(12);
        b.setTextColor(Color.WHITE);
        b.setBackgroundColor(Color.parseColor(color));
        b.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        b.setOnClickListener(l);
        return b;
    }

    private AdminManager.OnDone done() {
        return new AdminManager.OnDone() {
            @Override public void onSuccess() { Toast.makeText(AdminCitizensActivity.this, "✅ تم", Toast.LENGTH_SHORT).show(); }
            @Override public void onError(String m) { Toast.makeText(AdminCitizensActivity.this, "❌ " + m, Toast.LENGTH_SHORT).show(); }
        };
    }

    private void editBalance(final AdminManager.Citizen c) {
        final EditText input = new EditText(this);
        input.setHint("الرصيد الجديد");
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setTextColor(Color.WHITE);
        input.setText(String.valueOf(c.balance));

        LinearLayout cc = new LinearLayout(this);
        cc.setPadding(40, 20, 40, 20);
        cc.addView(input);

        new AlertDialog.Builder(this)
            .setTitle("تعديل رصيد " + c.name)
            .setView(cc)
            .setPositiveButton("حفظ", (d, w) -> {
                try {
                    int val = Integer.parseInt(input.getText().toString().trim());
                    am.setBalance(c.nationalId, val, done());
                } catch (Exception e) {
                    Toast.makeText(this, "رقم غير صالح", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    private void quickGift(final AdminManager.Citizen c) {
        final String[] gifts = {"🌹 وردة (1Đ)", "❤️ قلب (2Đ)", "🏆 كأس (25Đ)", "👑 تاج (50Đ)", "💎 ألماسة (100Đ)", "🕋 مكة (25000Đ)"};
        final String[][] data = {
            {"🌹", "وردة", "تقدير بسيط", "1"},
            {"❤️", "قلب", "محبة صافية", "2"},
            {"🏆", "كأس", "أنت الأفضل", "25"},
            {"👑", "تاج", "يا ملك", "50"},
            {"💎", "ألماسة", "أنت جوهرة", "100"},
            {"🕋", "مكة", "أعظم هدية", "25000"}
        };

        new AlertDialog.Builder(this)
            .setTitle("🎁 إرسال هدية إلى " + c.name)
            .setItems(gifts, (d, which) -> {
                String[] g = data[which];
                am.sendGift("الإدارة", c.nationalId, g[0], g[1], g[2], Integer.parseInt(g[3]),
                    new AdminManager.OnDone() {
                        @Override public void onSuccess() {
                            Toast.makeText(this, "✅ تم إرسال الهدية", Toast.LENGTH_LONG).show();
                        }
                        @Override public void onError(String m) {
                            Toast.makeText(this, "❌ " + m, Toast.LENGTH_SHORT).show();
                        }
                    });
            })
            .show();
    }
}
