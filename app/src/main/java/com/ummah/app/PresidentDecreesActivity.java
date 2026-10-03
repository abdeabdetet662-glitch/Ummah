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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PresidentDecreesActivity extends Activity {

    private IdentityManager im;
    private PresidentManager pm;
    private LinearLayout container;
    private ListenerRegistration reg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        im = new IdentityManager(this);
        pm = new PresidentManager();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 50, 30, 60);
        scroll.addView(root);

        TextView title = UiHelper.goldTitle(this, getString(R.string.pdecree_history), 26);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText(getString(R.string.pdecree_subtitle));
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 24);
        root.addView(sub);

        Button addBtn = UiHelper.primaryButton(this, getString(R.string.pdecree_new));
        addBtn.setOnClickListener(v -> showIssueDialog());
        root.addView(addBtn);

        TextView listTitle = new TextView(this);
        listTitle.setText(getString(R.string.pdecree_previous));
        listTitle.setTextColor(Color.parseColor("#D4AF37"));
        listTitle.setTextSize(15);
        listTitle.setTypeface(null, Typeface.BOLD);
        listTitle.setGravity(Gravity.CENTER);
        listTitle.setPadding(0, 32, 0, 16);
        root.addView(listTitle);

        container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        root.addView(container);

        setContentView(scroll);
        startListener();
    }

    private void startListener() {
        if (reg != null) reg.remove();
        reg = pm.listenDecrees(new PresidentManager.AnnouncementsListener() {
            @Override public void onList(List<Map<String, Object>> list) {
                runOnUiThread(() -> render(list));
            }
        });
    }

    private void render(List<Map<String, Object>> list) {
        container.removeAllViews();

        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(getString(R.string.pdecree_empty));
            empty.setTextColor(Color.parseColor("#9E9E9E"));
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 40, 0, 0);
            container.addView(empty);
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

        for (Map<String, Object> m : list) {
            addDecreeCard(m, sdf);
        }
    }

    private void addDecreeCard(Map<String, Object> m, SimpleDateFormat sdf) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_president_card);
        card.setPadding(24, 24, 24, 24);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 8, 0, 8);
        card.setLayoutParams(lp);

        String title = (String) m.get("title");
        String content = (String) m.get("content");
        String president = (String) m.get("presidentName");
        Long ts = (Long) m.get("timestamp");

        TextView t = new TextView(this);
        t.setText("📜 " + (title != null ? title : getString(R.string.pdecree_decree)));
        t.setTextColor(Color.parseColor("#FFD700"));
        t.setTextSize(16);
        t.setTypeface(null, Typeface.BOLD);
        card.addView(t);

        TextView c = new TextView(this);
        c.setText(content != null ? content : "");
        c.setTextColor(Color.WHITE);
        c.setTextSize(13);
        c.setPadding(0, 10, 0, 8);
        card.addView(c);

        TextView footer = new TextView(this);
        footer.setText("👑 " + (president != null ? president : getString(R.string.pdecree_president))
                + "  •  " + (ts != null ? sdf.format(new Date(ts)) : ""));
        footer.setTextColor(Color.parseColor("#9E9E9E"));
        footer.setTextSize(10);
        card.addView(footer);

        container.addView(card);
    }

    private void showIssueDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(40, 20, 40, 20);

        final EditText titleInput = UiHelper.input(this, getString(R.string.pdecree_title_hint));
        box.addView(titleInput);

        final EditText contentInput = UiHelper.input(this, getString(R.string.pdecree_body_hint));
        contentInput.setMinLines(4);
        contentInput.setGravity(Gravity.TOP | Gravity.START);
        box.addView(contentInput);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.pdecree_issue_title))
                .setView(box)
                .setPositiveButton(getString(R.string.pdecree_issue), (d, w) -> {
                    String t = titleInput.getText().toString().trim();
                    String c = contentInput.getText().toString().trim();
                    if (t.isEmpty() || c.isEmpty()) {
                        Toast.makeText(this, "أكمل الحقول", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Citizen me = im.getCitizen();
                    if (me == null) return;
                    pm.issueDecree(me.nationalId, me.name, t, c,
                            new PresidentManager.OnDone() {
                        @Override public void onSuccess() {
                            Toast.makeText(PresidentDecreesActivity.this,
                                    getString(R.string.pdecree_issued), Toast.LENGTH_LONG).show();
                        }
                        @Override public void onError(String m) {
                            Toast.makeText(PresidentDecreesActivity.this,
                                    "❌ " + m, Toast.LENGTH_LONG).show();
                        }
                    });
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
