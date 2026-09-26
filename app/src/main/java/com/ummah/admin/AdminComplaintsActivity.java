package com.ummah.admin;

import android.app.Activity;
import android.app.AlertDialog;
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
import java.util.List;

public class AdminComplaintsActivity extends Activity {
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
        t.setText("⚖️ الشكاوى");
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
        r = am.listenComplaints(list -> runOnUiThread(() -> render(list)));
    }

    @Override protected void onDestroy() { super.onDestroy(); if (r != null) r.remove(); }

    private void render(List<AdminManager.Complaint> list) {
        c.removeAllViews();
        if (list.isEmpty()) {
            TextView e = new TextView(this);
            e.setText("لا توجد شكاوى");
            e.setTextColor(Color.parseColor("#616161"));
            e.setGravity(Gravity.CENTER);
            e.setPadding(0, 40, 0, 0);
            c.addView(e); return;
        }
        for (final AdminManager.Complaint k : list) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundColor(Color.parseColor("#141414"));
            card.setPadding(24, 20, 24, 20);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.setMargins(0, 0, 0, 12); card.setLayoutParams(lp);

            TextView a = new TextView(this);
            a.setText("📢 " + k.plaintiffName + "  ضد  " + k.defendantName);
            a.setTextColor(Color.WHITE); a.setTextSize(14); a.setTypeface(null, Typeface.BOLD);
            card.addView(a);

            TextView cl = new TextView(this);
            cl.setText(k.claim); cl.setTextColor(Color.parseColor("#BDBDBD")); cl.setTextSize(13);
            cl.setPadding(0, 8, 0, 8); card.addView(cl);

            TextView st = new TextView(this);
            st.setText("الحالة: " + k.status); st.setTextColor(Color.parseColor("#FFD700")); st.setTextSize(12);
            card.addView(st);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            Button del = new Button(this);
            del.setText("🗑️ حذف"); del.setTextSize(12);
            del.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
            del.setOnClickListener(v -> {
                new AlertDialog.Builder(this).setTitle("حذف الشكوى").setPositiveButton("حذف", (d, w) ->
                    am.deleteComplaint(k.id, new AdminManager.OnDone() {
                        @Override public void onSuccess() { Toast.makeText(this, "✅", Toast.LENGTH_SHORT).show(); }
                        @Override public void onError(String m) {}
                    })).setNegativeButton("إلغاء", null).show();
            });
            row.addView(del);

            Button close = new Button(this);
            close.setText("🔒 إغلاق"); close.setTextSize(12);
            close.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
            close.setOnClickListener(v -> am.updateComplaintStatus(k.id, "closed", new AdminManager.OnDone() {
                @Override public void onSuccess() {}
                @Override public void onError(String m) {}
            }));
            row.addView(close);
            card.addView(row);
            c.addView(card);
        }
    }
}
