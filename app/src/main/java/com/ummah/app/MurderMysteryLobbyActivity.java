package com.ummah.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

/**
 * MurderMysteryLobbyActivity — غرفة انتظار جريمة أُمّة
 */
public class MurderMysteryLobbyActivity extends Activity {

    private IdentityManager im;
    private MurderMysteryManager manager;
    private MurderMystery currentGame;

    private LinearLayout root;
    private TextView statusView;
    private TextView playersCountView;
    private LinearLayout playersContainer;
    private Button actionBtn;

    private ListenerRegistration playersReg;

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        im = new IdentityManager(this);
        manager = new MurderMysteryManager();

        buildUI();
        loadGame();
    }

    private void buildUI() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 50, 30, 60);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        scroll.addView(root);

        // ═══ Header ═══
        TextView icon = new TextView(this);
        icon.setText("🕵️");
        icon.setTextSize(72);
        icon.setGravity(Gravity.CENTER);
        root.addView(icon);

        TextView title = new TextView(this);
        title.setText("جريمة أُمّة");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(32);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("كل أسبوع... جريمة غامضة");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(14);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 8, 0, 32);
        root.addView(sub);

        // ═══ Status ═══
        statusView = new TextView(this);
        statusView.setTextColor(Color.WHITE);
        statusView.setTextSize(16);
        statusView.setGravity(Gravity.CENTER);
        statusView.setPadding(0, 0, 0, 24);
        root.addView(statusView);

        // ═══ Players Count ═══
        playersCountView = new TextView(this);
        playersCountView.setTextColor(Color.parseColor("#10B981"));
        playersCountView.setTextSize(18);
        playersCountView.setTypeface(null, Typeface.BOLD);
        playersCountView.setGravity(Gravity.CENTER);
        playersCountView.setPadding(0, 0, 0, 24);
        root.addView(playersCountView);

        // ═══ Players Container ═══
        playersContainer = new LinearLayout(this);
        playersContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(playersContainer);

        // ═══ Action Button ═══
        actionBtn = new Button(this);
        actionBtn.setTextSize(16);
        actionBtn.setTextColor(Color.parseColor("#0A0E1A"));
        actionBtn.setAllCaps(false);
        actionBtn.setTypeface(null, Typeface.BOLD);
        actionBtn.setBackgroundResource(R.drawable.bg_btn_gold);
        actionBtn.setPadding(40, 30, 40, 30);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 40, 0, 0);
        actionBtn.setLayoutParams(lp);
        root.addView(actionBtn);

        setContentView(scroll);
    }

    private void loadGame() {
        statusView.setText("⏳ جاري التحميل...");

        manager.getCurrentMystery(new MurderMysteryManager.MysteryCallback() {
            @Override
            public void onResult(MurderMystery mystery) {
                currentGame = mystery;
                if (mystery == null) {
                    statusView.setText("⏸️ ما فيهاش جلسة حالياً\nعاود بعد قليل");
                    actionBtn.setVisibility(View.GONE);
                    return;
                }

                updateUI();
                startListeningPlayers();
            }

            @Override
            public void onError(String error) {
                statusView.setText("❌ " + error);
            }
        });
    }

    private void updateUI() {
        if (currentGame == null) return;

        statusView.setText("📖 " + currentGame.title + "\n\n" + currentGame.description);
        playersCountView.setText("👥 " + currentGame.currentPlayers + " / " + currentGame.maxPlayers);

        if (currentGame.isRegistration()) {
            actionBtn.setText("🎫 سجّل الآن (" + currentGame.entryFee + " Đ)");
            actionBtn.setEnabled(true);
            actionBtn.setOnClickListener(v -> registerNow());
        } else if (currentGame.isPlaying()) {
            actionBtn.setText("🔍 ادخل التحقيق");
            actionBtn.setEnabled(true);
            actionBtn.setOnClickListener(v -> {
                startActivity(new Intent(this, MurderMysteryGameActivity.class)
                        .putExtra("gameId", currentGame.id));
            });
        } else if (currentGame.isVoting()) {
            actionBtn.setText("🗳️ صوّت الآن");
            actionBtn.setEnabled(true);
            actionBtn.setOnClickListener(v -> {
                startActivity(new Intent(this, MurderMysteryVoteActivity.class)
                        .putExtra("gameId", currentGame.id));
            });
        } else {
            actionBtn.setText("⏸️ الجلسة انتهت");
            actionBtn.setEnabled(false);
        }
    }

    private void startListeningPlayers() {
        if (currentGame == null) return;

        if (playersReg != null) playersReg.remove();
        playersReg = manager.listenPlayers(currentGame.id, new MurderMysteryManager.PlayersCallback() {
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

        TextView header = new TextView(this);
        header.setText("═══ المحققون ═══");
        header.setTextColor(Color.parseColor("#D4AF37"));
        header.setTextSize(16);
        header.setTypeface(null, Typeface.BOLD);
        header.setGravity(Gravity.CENTER);
        header.setPadding(0, 0, 0, 16);
        playersContainer.addView(header);

        for (MMPlayer p : players) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackgroundResource(R.drawable.bg_card);
            row.setPadding(24, 16, 24, 16);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 8);
            row.setLayoutParams(lp);

            TextView emoji = new TextView(this);
            emoji.setText(p.avatarEmoji != null ? p.avatarEmoji : "👤");
            emoji.setTextSize(28);
            emoji.setPadding(0, 0, 16, 0);
            row.addView(emoji);

            TextView name = new TextView(this);
            name.setText(p.userName);
            name.setTextColor(Color.WHITE);
            name.setTextSize(15);
            name.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(name);

            if ("killer".equals(p.role) && currentGame.isEnded()) {
                TextView badge = new TextView(this);
                badge.setText("🎭 القاتل");
                badge.setTextColor(Color.parseColor("#F44336"));
                badge.setTextSize(12);
                row.addView(badge);
            }

            playersContainer.addView(row);
        }
    }

    private void registerNow() {
        if (currentGame == null) return;
        Citizen c = im.getCitizen();
        if (c == null) {
            Toast.makeText(this, "سجّل أولاً", Toast.LENGTH_SHORT).show();
            return;
        }

        actionBtn.setEnabled(false);
        actionBtn.setText("⏳ جاري التسجيل...");

        manager.register(currentGame.id, c, new MurderMysteryManager.SimpleCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(MurderMysteryLobbyActivity.this,
                            "✅ تم التسجيل! انتظر بدء القضية", Toast.LENGTH_LONG).show();
                    actionBtn.setText("✅ مسجل");
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(MurderMysteryLobbyActivity.this,
                            "❌ " + error, Toast.LENGTH_LONG).show();
                    actionBtn.setEnabled(true);
                    actionBtn.setText("🎫 سجّل الآن");
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (playersReg != null) playersReg.remove();
        super.onDestroy();
    }
}
