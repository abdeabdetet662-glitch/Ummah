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
import java.util.Map;

public class AdminVotesActivity extends Activity {
    private AdminManager am;
    private LinearLayout c;
    private ListenerRegistration r;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        am = AdminManager.get();
        ScrollView s = new ScrollView(this);
        s.setBackgroundColor(Color.parseColor("#0A0A0A"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 40, 20, 40);
        s.addView(root);

        TextView t = new TextView(this);
        t.setText("🏛️ التصويت على الدستور");
        t.setTextColor(Color.parseColor("#D4AF37"));
        t.setTextSize(22);
        t.setTypeface(null, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, 0, 0, 10);
        root.addView(t);

        Button add = new Button(this);
        add.setText("➕ إضافة صوت إداري");
        add.setOnClickListener(v -> showAddDialog());
        root.addView(add);

        c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        root.addView(c);
        setContentView(s);
        r = am.listenConstitutionVotes(list -> runOnUiThread(() -> render(list)));
    }

    @Override protected void onDestroy() { super.onDestroy(); if (r != null) r.remove(); }

    private void render(List<Map<String, Object>> list) {
        c.removeAllViews();
        for (final Map<String, Object> v : list) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setBackgroundColor(Color.parseColor("#141414"));
            card.setPadding(20, 16, 20, 16);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.setMargins(0, 0, 0, 8); card.setLayoutParams(lp);

            TextView info = new TextView(this);
            Object nat = v.get("nationalId");
            Object art = v.get("article");
            Object y = v.get("yes");
            Boolean yes = (Boolean) y;
            info.setText((yes != null && yes ? "✅" : "❌") + " مادة " + art + " — " + nat);
            info.setTextColor(Color.WHITE);
            info.setTextSize(12);
            info.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
            card.addView(info);

            Button del = new Button(this);
            del.setText("🗑️");
            del.setTextSize(12);
            del.setOnClickListener(v2 -> am.deleteConstitutionVote((String) v.get("_id"), new AdminManager.OnDone() {
                @Override public void onSuccess() { Toast.makeText(this, "✅", Toast.LENGTH_SHORT).show(); }
                @Override public void onError(String m) {}
            }));
            card.addView(del);
            c.addView(card);
        }
    }

    private void showAddDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(40, 20, 40, 20);

        final EditText id = new EditText(this);
        id.setHint("الرقم الوطني"); id.setTextColor(Color.WHITE);
        id.setInputType(InputType.TYPE_CLASS_TEXT);
        box.addView(id);

        final EditText art = new EditText(this);
        art.setHint("رقم المادة (1-10)"); art.setTextColor(Color.WHITE);
        art.setInputType(InputType.TYPE_CLASS_NUMBER);
        box.addView(art);

        new AlertDialog.Builder(this)
            .setTitle("إضافة صوت")
            .setView(box)
            .setPositiveButton("موافق", (d, w) -> {
                try {
                    int a = Integer.parseInt(art.getText().toString().trim());
                    am.addConstitutionVote(id.getText().toString().trim(), a, true, new AdminManager.OnDone() {
                        @Override public void onSuccess() { Toast.makeText(this, "✅", Toast.LENGTH_SHORT).show(); }
                        @Override public void onError(String m) { Toast.makeText(this, m, Toast.LENGTH_SHORT).show(); }
                    });
                } catch (Exception e) { Toast.makeText(this, "خطأ", Toast.LENGTH_SHORT).show(); }
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }
}
