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

import java.util.List;

public class LeaderboardActivity extends Activity {
    private FirebaseManager fm;
    private IdentityManager im;
    private LinearLayout listContainer;
    private ListenerRegistration reg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        fm = FirebaseManager.get();
        im = new IdentityManager(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0A"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(36, 50, 36, 50);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("🏆 المتصدرون");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("أغنى 20 مواطناً في أُمّة");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 8, 0, 30);
        root.addView(sub);

        listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(listContainer);

        setContentView(scroll);
        startListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) reg.remove();
    }

    private void startListener() {
        reg = fm.listenTopCitizens(20, list ->
            runOnUiThread(() -> refresh(list)));
    }

    private void refresh(List<FirebaseManager.CitizenItem> list) {
        listContainer.removeAllViews();
        Citizen me = im.getCitizen();

        for (int i = 0; i < list.size(); i++) {
            FirebaseManager.CitizenItem c = list.get(i);
            boolean isMe = me != null && me.nationalId.equals(c.nationalId);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setBackgroundColor(isMe ? Color.parseColor("#0B4F2C") : Color.parseColor("#141414"));
            row.setPadding(28, 22, 28, 22);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 10);
            row.setLayoutParams(lp);
            row.setGravity(Gravity.CENTER_VERTICAL);

            TextView rank = new TextView(this);
            String r;
            if (i == 0) r = "🥇";
            else if (i == 1) r = "🥈";
            else if (i == 2) r = "🥉";
            else r = String.valueOf(i + 1) + ".";
            rank.setText(r);
            rank.setTextColor(Color.parseColor("#D4AF37"));
            rank.setTextSize(20);
            rank.setTypeface(null, Typeface.BOLD);
            rank.setMinWidth(80);
            row.addView(rank);

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            info.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            TextView name = new TextView(this);
            name.setText((c.name != null ? c.name : "مجهول") + (isMe ? "  (أنت)" : ""));
            name.setTextColor(Color.WHITE);
            name.setTextSize(16);
            name.setTypeface(null, Typeface.BOLD);
            info.addView(name);

            TextView idView = new TextView(this);
            idView.setText(c.nationalId);
            idView.setTextColor(Color.parseColor("#616161"));
            idView.setTextSize(9);
            idView.setTypeface(Typeface.MONOSPACE);
            info.addView(idView);

            row.addView(info);

            TextView bal = new TextView(this);
            bal.setText(c.balance + " Đ");
            bal.setTextColor(Color.parseColor("#D4AF37"));
            bal.setTextSize(16);
            bal.setTypeface(null, Typeface.BOLD);
            row.addView(bal);

            listContainer.addView(row);
        }
    }
}
