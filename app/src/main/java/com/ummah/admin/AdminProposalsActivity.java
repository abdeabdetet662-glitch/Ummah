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

public class AdminProposalsActivity extends Activity {
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
        t.setText("📰 الاقتراحات");
        t.setTextColor(Color.parseColor("#D4AF37"));
        t.setTextSize(24);
        t.setTypeface(null, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, 0, 0, 20);
        root.addView(t);

        c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        root.addView(c);
        setContentView(s);
        r = am.listenProposals(list -> runOnUiThread(() -> render(list)));
    }

    @Override protected void onDestroy() { super.onDestroy(); if (r != null) r.remove(); }

    private void render(List<Map<String, Object>> list) {
        c.removeAllViews();
        for (final Map<String, Object> p : list) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundColor(Color.parseColor("#141414"));
            card.setPadding(20, 16, 20, 16);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.setMargins(0, 0, 0, 10); card.setLayoutParams(lp);

            TextView ti = new TextView(this);
            ti.setText((String) p.get("title"));
            ti.setTextColor(Color.WHITE); ti.setTextSize(15); ti.setTypeface(null, Typeface.BOLD);
            card.addView(ti);

            TextView au = new TextView(this);
            au.setText("بقلم: " + p.get("author"));
            au.setTextColor(Color.parseColor("#757575")); au.setTextSize(11);
            card.addView(au);

            TextView st = new TextView(this);
            st.setText("✅ " + p.get("yes") + "  ❌ " + p.get("no"));
            st.setTextColor(Color.parseColor("#FFD700")); st.setTextSize(13);
            st.setPadding(0, 8, 0, 8);
            card.addView(st);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            Button addY = new Button(this);
            addY.setText("➕ ✅"); addY.setTextSize(11);
            addY.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
            addY.setOnClickListener(v -> {
                Long y = (Long) p.get("yes");
                Long n = (Long) p.get("no");
                am.setProposalVotes((String) p.get("_id"),
                    (y != null ? y.intValue() : 0) + 1,
                    n != null ? n.intValue() : 0, done());
            });
            row.addView(addY);

            Button addN = new Button(this);
            addN.setText("➕ ❌"); addN.setTextSize(11);
            addN.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
            addN.setOnClickListener(v -> {
                Long y = (Long) p.get("yes");
                Long n = (Long) p.get("no");
                am.setProposalVotes((String) p.get("_id"),
                    y != null ? y.intValue() : 0,
                    (n != null ? n.intValue() : 0) + 1, done());
            });
            row.addView(addN);

            Button edit = new Button(this);
            edit.setText("✏️"); edit.setTextSize(11);
            edit.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
            edit.setOnClickListener(v -> editVotes(p));
            row.addView(edit);

            Button del = new Button(this);
            del.setText("🗑️"); del.setTextSize(11);
            del.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
            del.setOnClickListener(v -> am.deleteProposal((String) p.get("_id"), done()));
            row.addView(del);

            card.addView(row);
            c.addView(card);
        }
    }

    private void editVotes(final Map<String, Object> p) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(40, 20, 40, 20);

        final EditText y = new EditText(this);
        y.setHint("عدد الموافقين"); y.setTextColor(Color.WHITE);
        y.setInputType(InputType.TYPE_CLASS_NUMBER);
        Long cy = (Long) p.get("yes");
        y.setText(String.valueOf(cy != null ? cy : 0));
        box.addView(y);

        final EditText n = new EditText(this);
        n.setHint("عدد الرافضين"); n.setTextColor(Color.WHITE);
        n.setInputType(InputType.TYPE_CLASS_NUMBER);
        Long cn = (Long) p.get("no");
        n.setText(String.valueOf(cn != null ? cn : 0));
        box.addView(n);

        new AlertDialog.Builder(this)
            .setTitle("تعديل الأصوات")
            .setView(box)
            .setPositiveButton("حفظ", (d, w) -> {
                try {
                    am.setProposalVotes((String) p.get("_id"),
                        Integer.parseInt(y.getText().toString().trim()),
                        Integer.parseInt(n.getText().toString().trim()), done());
                } catch (Exception e) {}
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    private AdminManager.OnDone done() {
        return new AdminManager.OnDone() {
            @Override public void onSuccess() { Toast.makeText(AdminProposalsActivity.this, "✅", Toast.LENGTH_SHORT).show(); }
            @Override public void onError(String m) { Toast.makeText(AdminProposalsActivity.this, "❌", Toast.LENGTH_SHORT).show(); }
        };
    }
}
