package com.ummah.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

/**
 * MurderMysteryVoteActivity — شاشة التصويت
 * كل محقق يصوت على من يعتقد أنه القاتل
 */
public class MurderMysteryVoteActivity extends Activity {

    private FirebaseFirestore db;
    private IdentityManager im;
    private String gameId;
    private Citizen me;
    private MurderMysteryManager manager;

    private LinearLayout playersContainer;
    private TextView statusView;
    private String selectedVote = null;
    private ListenerRegistration playersReg;

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        db = FirebaseFirestore.getInstance();
        im = new IdentityManager(this);
        me = im.getCitizen();
        manager = new MurderMysteryManager();
        gameId = getIntent().getStringExtra("gameId");

        if (gameId == null || me == null) {
            finish();
            return;
        }

        buildUI();
        loadPlayers();
    }

    private void buildUI() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_mystery_noir);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(60), dp(24), dp(40));
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        scroll.addView(root);

        TextView icon = new TextView(this);
        icon.setText("🗳️");
        icon.setTextSize(80);
        icon.setGravity(Gravity.CENTER);
        root.addView(icon);

        TextView title = new TextView(this);
        title.setText("من هو القاتل؟");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(28);
        title.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(16), 0, dp(8));
        root.addView(title);

        statusView = new TextView(this);
        statusView.setText("اختر واحداً واعتبر...");
        statusView.setTextColor(Color.parseColor("#9E9E9E"));
        statusView.setTextSize(13);
        statusView.setGravity(Gravity.CENTER);
        statusView.setPadding(0, 0, 0, dp(24));
        root.addView(statusView);

        playersContainer = new LinearLayout(this);
        playersContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(playersContainer);

        TextView voteBtn = new TextView(this);
        voteBtn.setText("🗳️ أكّد التصويت");
        voteBtn.setTextColor(Color.parseColor("#0A0510"));
        voteBtn.setTextSize(16);
        voteBtn.setTypeface(null, Typeface.BOLD);
        voteBtn.setGravity(Gravity.CENTER);
        voteBtn.setBackgroundResource(R.drawable.bg_btn_noir);
        voteBtn.setPadding(dp(30), dp(20), dp(30), dp(20));
        LinearLayout.LayoutParams vbLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        vbLp.setMargins(0, dp(30), 0, 0);
        voteBtn.setLayoutParams(vbLp);
        voteBtn.setOnClickListener(v -> submitVote());
        root.addView(voteBtn);

        setContentView(scroll);
    }

    private void loadPlayers() {
        playersReg = manager.listenPlayers(gameId, new MurderMysteryManager.PlayersCallback() {
            @Override
            public void onResult(List<MMPlayer> players) {
                runOnUiThread(() -> renderPlayers(players));
            }

            @Override
            public void onError(String error) {}
        });
    }

    private void renderPlayers(List<MMPlayer> players) {
        playersContainer.removeAllViews();

        for (MMPlayer p : players) {
            if (p.userId == null || p.userId.equals(me.nationalId)) continue;

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setBackgroundResource(R.drawable.bg_mystery_card);
            card.setPadding(dp(20), dp(18), dp(20), dp(18));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, dp(10));
            card.setLayoutParams(lp);

            TextView emoji = new TextView(this);
            emoji.setText(p.avatarEmoji != null ? p.avatarEmoji : "👤");
            emoji.setTextSize(26);
            emoji.setPadding(0, 0, dp(16), 0);
            card.addView(emoji);

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            info.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView name = new TextView(this);
            name.setText(p.userName != null ? p.userName : "محقق");
            name.setTextColor(Color.WHITE);
            name.setTextSize(15);
            name.setTypeface(null, Typeface.BOLD);
            info.addView(name);

            TextView role = new TextView(this);
            role.setText(p.character != null ? p.character : "مواطن");
            role.setTextColor(Color.parseColor("#9E9E9E"));
            role.setTextSize(11);
            role.setPadding(0, dp(2), 0, 0);
            info.addView(role);

            card.addView(info);

            TextView radio = new TextView(this);
            radio.setText("◯");
            radio.setTextSize(22);
            radio.setTextColor(Color.parseColor("#666666"));
            card.addView(radio);

            final String pid = p.userId;
            card.setOnClickListener(v -> {
                selectedVote = pid;
                // تحديث الـ UI
                for (int i = 0; i < playersContainer.getChildCount(); i++) {
                    View child = playersContainer.getChildAt(i);
                    if (child instanceof LinearLayout) {
                        LinearLayout ll = (LinearLayout) child;
                        TextView r = (TextView) ll.getChildAt(ll.getChildCount() - 1);
                        r.setText("◯");
                        r.setTextColor(Color.parseColor("#666666"));
                    }
                }
                radio.setText("◉");
                radio.setTextColor(Color.parseColor("#D4AF37"));
                statusView.setText("✅ اخترت: " + p.userName);
            });

            playersContainer.addView(card);
        }
    }

    private void submitVote() {
        if (selectedVote == null) {
            Toast.makeText(this, "⚠️ اختر واحداً أولاً", Toast.LENGTH_SHORT).show();
            return;
        }

        manager.vote(gameId, me.nationalId, selectedVote, new MurderMysteryManager.SimpleCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(MurderMysteryVoteActivity.this,
                            "✅ تم تسجيل صوتك!", Toast.LENGTH_LONG).show();
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(MurderMysteryVoteActivity.this,
                        "❌ " + error, Toast.LENGTH_LONG).show());
            }
        });
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }

    @Override
    protected void onDestroy() {
        if (playersReg != null) playersReg.remove();
        super.onDestroy();
    }
}
