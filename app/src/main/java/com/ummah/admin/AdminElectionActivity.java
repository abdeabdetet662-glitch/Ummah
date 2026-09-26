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

public class AdminElectionActivity extends Activity {
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
        t.setText("👑 إدارة الانتخابات");
        t.setTextColor(Color.parseColor("#D4AF37"));
        t.setTextSize(24);
        t.setTypeface(null, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, 0, 0, 10);
        root.addView(t);

        Button reset = new Button(this);
        reset.setText("🔄 تصفير كل الأصوات");
        reset.setOnClickListener(v -> new AlertDialog.Builder(this)
            .setTitle("تصفير الانتخابات")
            .setMessage("سيتم حذف كل الأصوات وإرجاع كل المرشحين إلى 0.")
            .setPositiveButton("تصفير", (d, w) -> am.resetElection(new AdminManager.OnDone() {
                @Override public void onSuccess() { Toast.makeText(this, "✅", Toast.LENGTH_SHORT).show(); }
                @Override public void onError(String m) {}
            }))
            .setNegativeButton("إلغاء", null).show());
        root.addView(reset);

        c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(0, 20, 0, 0);
        root.addView(c);
        setContentView(s);
        r = am.listenCandidates(list -> runOnUiThread(() -> render(list)));
    }

    @Override protected void onDestroy() { super.onDestroy(); if (r != null) r.remove(); }

    private void render(List<Map<String, Object>> list) {
        c.removeAllViews();
        for (final Map<String, Object> k : list) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setBackgroundColor(Color.parseColor("#141414"));
            card.setPadding(20, 16, 20, 16);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.setMargins(0, 0, 0, 10); card.setLayoutParams(lp);

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            info.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));

            TextView n = new TextView(this);
            n.setText("👤 " + k.get("name")); n.setTextColor(Color.WHITE);
            n.setTextSize(14); n.setTypeface(null, Typeface.BOLD);
            info.addView(n);

            TextView sl = new TextView(this);
            sl.setText("«" + k.get("slogan") + "»"); sl.setTextColor(Color.parseColor("#9E9E9E"));
            sl.setTextSize(11);
            info.addView(sl);

            TextView v = new TextView(this);
            v.setText("🗳️ " + k.get("votes")); v.setTextColor(Color.parseColor("#FFD700"));
            v.setTextSize(13);
            info.addView(v);

            card.addView(info);

            Button edit = new Button(this);
            edit.setText("✏️"); edit.setTextSize(12);
            edit.setOnClickListener(v2 -> {
                final EditText in = new EditText(this);
                in.setTextColor(Color.WHITE);
                in.setInputType(InputType.TYPE_CLASS_NUMBER);
                Long cv = (Long) k.get("votes");
                in.setText(String.valueOf(cv != null ? cv : 0));
                LinearLayout box = new LinearLayout(this);
                box.setPadding(40, 20, 40, 20); box.addView(in);
                new AlertDialog.Builder(this)
                    .setTitle("تعديل الأصوات")
                    .setView(box)
                    .setPositiveButton("حفظ", (d, w) -> {
                        try {
                            am.setCandidateVotes((String) k.get("_id"),
                                Integer.parseInt(in.getText().toString().trim()),
                                new AdminManager.OnDone() {
                                    @Override public void onSuccess() {}
                                    @Override public void onError(String m) {}
                                });
                        } catch (Exception e) {}
                    })
                    .setNegativeButton("إلغاء", null).show();
            });
            card.addView(edit);

            Button del = new Button(this);
            del.setText("🗑️"); del.setTextSize(12);
            del.setOnClickListener(v2 -> am.deleteCandidate((String) k.get("_id"), new AdminManager.OnDone() {
                @Override public void onSuccess() {}
                @Override public void onError(String m) {}
            }));
            card.addView(del);
            c.addView(card);
        }
    }
}
