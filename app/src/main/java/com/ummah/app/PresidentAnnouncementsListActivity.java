package com.ummah.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PresidentAnnouncementsListActivity extends Activity {

    private PresidentManager pm;
    private LinearLayout container;
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

        TextView title = UiHelper.goldTitle(this, getString(R.string.pann_list_title), 26);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText(getString(R.string.pann_list_subtitle));
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 24);
        root.addView(sub);

        container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        root.addView(container);

        setContentView(scroll);
        startListener();
    }

    private void startListener() {
        if (reg != null) reg.remove();
        reg = pm.listenAnnouncements(new PresidentManager.AnnouncementsListener() {
            @Override public void onList(List<Map<String, Object>> list) {
                runOnUiThread(() -> render(list));
            }
        });
    }

    private void render(List<Map<String, Object>> list) {
        container.removeAllViews();

        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(getString(R.string.pann_list_empty));
            empty.setTextColor(Color.parseColor("#9E9E9E"));
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 60, 0, 0);
            container.addView(empty);
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

        for (Map<String, Object> m : list) {
            addCard(m, sdf);
        }
    }

    private void addCard(Map<String, Object> m, SimpleDateFormat sdf) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_president_card);
        card.setPadding(24, 24, 24, 24);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 8, 0, 8);
        card.setLayoutParams(lp);

        TextView badge = new TextView(this);
        badge.setText(getString(R.string.pann_list_item));
        badge.setTextColor(Color.parseColor("#1A1A1A"));
        badge.setTextSize(10);
        badge.setTypeface(null, Typeface.BOLD);
        badge.setBackgroundResource(R.drawable.bg_gold_tag);
        badge.setPadding(16, 6, 16, 6);

        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        blp.gravity = Gravity.CENTER;
        blp.setMargins(0, 0, 0, 12);
        badge.setLayoutParams(blp);
        card.addView(badge);

        String title = (String) m.get("title");
        String content = (String) m.get("content");
        String president = (String) m.get("presidentName");
        Long ts = (Long) m.get("timestamp");

        TextView t = new TextView(this);
        t.setText(title != null ? title : getString(R.string.pann_list_announcement));
        t.setTextColor(Color.parseColor("#FFD700"));
        t.setTextSize(17);
        t.setTypeface(null, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        card.addView(t);

        TextView c = new TextView(this);
        c.setText(content != null ? content : "");
        c.setTextColor(Color.WHITE);
        c.setTextSize(13);
        c.setPadding(0, 12, 0, 8);
        c.setGravity(Gravity.CENTER);
        card.addView(c);

        TextView footer = new TextView(this);
        footer.setText("👑 " + (president != null ? president : getString(R.string.president_unknown))
                + "  •  " + (ts != null ? sdf.format(new Date(ts)) : ""));
        footer.setTextColor(Color.parseColor("#9E9E9E"));
        footer.setTextSize(10);
        footer.setGravity(Gravity.CENTER);
        card.addView(footer);

        container.addView(card);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) reg.remove();
    }
}
