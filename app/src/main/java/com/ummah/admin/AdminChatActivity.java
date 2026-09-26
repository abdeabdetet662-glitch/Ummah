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

public class AdminChatActivity extends Activity {
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
        t.setText("💬 إدارة الدردشة");
        t.setTextColor(Color.parseColor("#D4AF37"));
        t.setTextSize(24);
        t.setTypeface(null, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, 0, 0, 10);
        root.addView(t);

        Button bc = new Button(this);
        bc.setText("📢  بث رسالة عامة");
        bc.setOnClickListener(v -> broadcastDialog());
        root.addView(bc);

        c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(0, 20, 0, 0);
        root.addView(c);
        setContentView(s);
        r = am.listenGlobalChat(list -> runOnUiThread(() -> render(list)));
    }

    @Override protected void onDestroy() { super.onDestroy(); if (r != null) r.remove(); }

    private void render(List<Map<String, Object>> list) {
        c.removeAllViews();
        for (final Map<String, Object> m : list) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setBackgroundColor(Color.parseColor("#141414"));
            card.setPadding(20, 14, 20, 14);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.setMargins(0, 0, 0, 8); card.setLayoutParams(lp);

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            info.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));

            TextView a = new TextView(this);
            a.setText((String) m.get("author")); a.setTextColor(Color.parseColor("#FFD700")); a.setTextSize(12);
            info.addView(a);

            TextView msg = new TextView(this);
            msg.setText((String) m.get("text")); msg.setTextColor(Color.WHITE); msg.setTextSize(13);
            info.addView(msg);
            card.addView(info);

            Button del = new Button(this);
            del.setText("🗑️");
            del.setTextSize(11);
            del.setOnClickListener(v -> am.deleteChatMessage((String) m.get("_id"), new AdminManager.OnDone() {
                @Override public void onSuccess() { Toast.makeText(AdminChatActivity.this, "✅", Toast.LENGTH_SHORT).show(); }
                @Override public void onError(String msg) {}
            }));
            card.addView(del);
            c.addView(card);
        }
    }

    private void broadcastDialog() {
        final EditText input = new EditText(this);
        input.setHint("نص الرسالة العامة");
        input.setTextColor(Color.WHITE);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setMinLines(4);

        LinearLayout box = new LinearLayout(this);
        box.setPadding(40, 20, 40, 20);
        box.addView(input);

        new AlertDialog.Builder(this)
            .setTitle("📢 بث رسالة لكل المواطنين")
            .setView(box)
            .setPositiveButton("بث", (d, w) -> {
                String msg = input.getText().toString().trim();
                if (msg.isEmpty()) return;
                am.broadcast(msg, new AdminManager.OnDone() {
                    @Override public void onSuccess() { Toast.makeText(AdminChatActivity.this, "✅ تم البث", Toast.LENGTH_LONG).show(); }
                    @Override public void onError(String m) { Toast.makeText(AdminChatActivity.this, "❌ " + m, Toast.LENGTH_SHORT).show(); }
                });
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }
}
