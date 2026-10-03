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
        // Feature Check
        if (!FeatureFlags.checkOrToast(this, "leaderboard")) {
            finish();
            return;
        }

        fm = FirebaseManager.get();
        im = new IdentityManager(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0A"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(36, 50, 36, 50);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText(getString(R.string.leaderboard_title));
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText(getString(R.string.leaderboard_richest));
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
            row.setPadding(20, 16, 20, 16);
            row.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 10);
            row.setLayoutParams(lp);

            // الترتيب
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

            // الصورة
            android.widget.ImageView avatar = new android.widget.ImageView(this);
            LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(100, 100);
            alp.setMargins(10, 0, 20, 0);
            avatar.setLayoutParams(alp);
            avatar.setBackgroundColor(Color.parseColor("#1E1E1E"));
            avatar.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
            if (c.photoUrl != null && !c.photoUrl.isEmpty()) {
                com.bumptech.glide.Glide.with(this).load(c.photoUrl)
                    .placeholder(android.R.drawable.ic_menu_myplaces)
                    .circleCrop().into(avatar);
            } else {
                avatar.setImageResource(android.R.drawable.ic_menu_myplaces);
            }
            row.addView(avatar);

            // الاسم
            TextView name = new TextView(this);
            name.setText((c.name != null ? c.name : "مجهول") + (isMe ? getString(R.string.citizens_you) : ""));
            name.setTextColor(Color.WHITE);
            name.setTextSize(15);
            name.setTypeface(null, Typeface.BOLD);
            name.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            row.addView(name);

            // الرصيد
            TextView bal = new TextView(this);
            bal.setText(c.balance + " Đ");
            bal.setTextColor(Color.parseColor("#D4AF37"));
            bal.setTextSize(15);
            bal.setTypeface(null, Typeface.BOLD);
            row.addView(bal);

            listContainer.addView(row);
        }
    }

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

}
