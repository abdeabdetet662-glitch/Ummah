package com.ummah.app.heist;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ummah.app.Citizen;
import com.ummah.app.IdentityManager;
import com.ummah.app.LocaleHelper;
import com.ummah.app.R;

import java.util.List;

/**
 * HeistLobbyActivity — لوبي سرقة القرن
 */
public class HeistLobbyActivity extends Activity {

    private IdentityManager im;
    private Citizen me;
    private HeistGame game;

    private LinearLayout root;
    private LinearLayout playersContainer;
    private LinearLayout rolesContainer;
    private TextView statusView;
    private Button startBtn;

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        im = new IdentityManager(this);
        me = im.getCitizen();

        if (me == null) {
            Toast.makeText(this, "سجل أولاً", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        game = new HeistGame();
        game.gameId = "demo_" + System.currentTimeMillis();
        game.hostId = me.nationalId;

        // نضيف اللاعب (أنا)
        HeistPlayer myPlayer = new HeistPlayer(me.nationalId, me.name, null);
        myPlayer.isHost = true;
        myPlayer.isReady = true;
        game.players.add(myPlayer);

        buildUI();
    }

    private void buildUI() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_heist);
        scroll.setFillViewport(true);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(50), dp(20), dp(50));
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        scroll.addView(root);

        // ═══ Back button ═══
        TextView back = new TextView(this);
        back.setText("← عودة");
        back.setTextColor(Color.parseColor("#D4AF37"));
        back.setTextSize(14);
        back.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        blp.gravity = Gravity.START;
        back.setLayoutParams(blp);
        back.setOnClickListener(v -> finish());
        root.addView(back);

        // ═══ Top icon ═══
        TextView icon = new TextView(this);
        icon.setText("🏴‍☠️");
        icon.setTextSize(80);
        icon.setGravity(Gravity.CENTER);
        icon.setPadding(0, dp(20), 0, dp(8));
        root.addView(icon);
        icon.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));

        // ═══ Title ═══
        TextView title = new TextView(this);
        title.setText("سرقة القرن");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(34);
        title.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        title.setGravity(Gravity.CENTER);
        title.setLetterSpacing(0.08f);
        root.addView(title);

        // ═══ Subtitle ═══
        TextView sub = new TextView(this);
        sub.setText("عملية النور — سرقة البنك المركزي");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, dp(6), 0, dp(24));
        root.addView(sub);

        // ═══ Gold divider ═══
        View div = new View(this);
        LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(
                dp(180), dp(2));
        divLp.gravity = Gravity.CENTER_HORIZONTAL;
        divLp.setMargins(0, 0, 0, dp(24));
        div.setLayoutParams(divLp);
        div.setBackgroundColor(Color.parseColor("#D4AF37"));
        root.addView(div);

        // ═══ Team size section ═══
        addSectionTitle("👥 الفريق (" + game.players.size() + "/5)");

        playersContainer = new LinearLayout(this);
        playersContainer.setOrientation(LinearLayout.VERTICAL);
        playersContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        root.addView(playersContainer);
        renderPlayers();

        // ═══ Roles section ═══
        addSectionTitle("🎭 اختر دورك");

        rolesContainer = new LinearLayout(this);
        rolesContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(rolesContainer);
        renderRoles();

        // ═══ Status ═══
        statusView = new TextView(this);
        statusView.setText("⏳ انتظر باقي الفريق...");
        statusView.setTextColor(Color.parseColor("#D4AF37"));
        statusView.setTextSize(14);
        statusView.setGravity(Gravity.CENTER);
        statusView.setPadding(0, dp(30), 0, dp(16));
        statusView.setTypeface(null, Typeface.BOLD);
        root.addView(statusView);

        // ═══ Start button ═══
        startBtn = new Button(this);
        startBtn.setText("▶️ ابدأ المهمة (مع Bots)");
        startBtn.setTextSize(16);
        startBtn.setTextColor(Color.parseColor("#0a0510"));
        startBtn.setAllCaps(false);
        startBtn.setTypeface(null, Typeface.BOLD);
        startBtn.setBackgroundResource(R.drawable.bg_heist_btn_gold);
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        btnLp.setMargins(0, dp(16), 0, 0);
        startBtn.setLayoutParams(btnLp);
        startBtn.setOnClickListener(v -> startWithBots());
        root.addView(startBtn);

        setContentView(scroll);
    }

    private void addSectionTitle(String text) {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, dp(20), 0, dp(12));

        View lineL = new View(this);
        LinearLayout.LayoutParams lLp = new LinearLayout.LayoutParams(0, dp(1), 1f);
        lineL.setLayoutParams(lLp);
        lineL.setBackgroundColor(Color.parseColor("#40D4AF37"));
        header.addView(lineL);

        TextView t = new TextView(this);
        t.setText("   " + text + "   ");
        t.setTextColor(Color.parseColor("#D4AF37"));
        t.setTextSize(14);
        t.setTypeface(null, Typeface.BOLD);
        header.addView(t);

        View lineR = new View(this);
        LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(0, dp(1), 1f);
        lineR.setLayoutParams(rLp);
        lineR.setBackgroundColor(Color.parseColor("#40D4AF37"));
        header.addView(lineR);

        root.addView(header);
    }

    private void renderPlayers() {
        playersContainer.removeAllViews();

        // نعرضو 5 slots
        for (int i = 0; i < 5; i++) {
            LinearLayout slot = new LinearLayout(this);
            slot.setOrientation(LinearLayout.HORIZONTAL);
            slot.setGravity(Gravity.CENTER_VERTICAL);
            slot.setPadding(dp(16), dp(14), dp(16), dp(14));

            LinearLayout.LayoutParams slotLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            slotLp.setMargins(0, 0, 0, dp(8));
            slot.setLayoutParams(slotLp);

            if (i < game.players.size()) {
                // Player exists
                HeistPlayer p = game.players.get(i);
                slot.setBackgroundResource(R.drawable.bg_heist_slot);

                TextView av = new TextView(this);
                av.setText(p.isHost ? "👑" : "👤");
                av.setTextSize(26);
                av.setPadding(0, 0, dp(12), 0);
                slot.addView(av);

                LinearLayout info = new LinearLayout(this);
                info.setOrientation(LinearLayout.VERTICAL);
                info.setLayoutParams(new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                TextView name = new TextView(this);
                name.setText(p.userName + (p.userId.equals(me.nationalId) ? " (أنت)" : ""));
                name.setTextColor(Color.WHITE);
                name.setTextSize(15);
                name.setTypeface(null, Typeface.BOLD);
                info.addView(name);

                TextView role = new TextView(this);
                if (p.role != null) {
                    role.setText(p.role.emoji + " " + p.role.nameAr);
                    role.setTextColor(Color.parseColor(p.role.colorHex));
                } else {
                    role.setText("🎲 لم يختر دوراً");
                    role.setTextColor(Color.parseColor("#888888"));
                }
                role.setTextSize(12);
                role.setPadding(0, dp(2), 0, 0);
                info.addView(role);

                slot.addView(info);

                if (p.isReady) {
                    TextView ready = new TextView(this);
                    ready.setText("✅");
                    ready.setTextSize(20);
                    slot.addView(ready);
                }
            } else {
                // Empty slot
                slot.setBackgroundResource(R.drawable.bg_heist_slot_empty);

                TextView av = new TextView(this);
                av.setText("➕");
                av.setTextSize(22);
                av.setPadding(0, 0, dp(12), 0);
                av.setAlpha(0.4f);
                slot.addView(av);

                TextView label = new TextView(this);
                label.setText("في انتظار مواطن...");
                label.setTextColor(Color.parseColor("#666666"));
                label.setTextSize(13);
                label.setLayoutParams(new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                slot.addView(label);
            }

            playersContainer.addView(slot);
        }
    }

    private void renderRoles() {
        rolesContainer.removeAllViews();

        HeistPlayer myPlayer = game.getPlayer(me.nationalId);

        for (HeistRole role : HeistRole.values()) {
            boolean isMine = myPlayer != null && myPlayer.role == role;
            boolean isTakenByOther = false;

            // إذا الدور مأخوذ من لاعب آخر
            for (HeistPlayer p : game.players) {
                if (p.role == role && !p.userId.equals(me.nationalId)) {
                    isTakenByOther = true;
                    break;
                }
            }

            LinearLayout card = createRoleCard(role, isMine, isTakenByOther);
            rolesContainer.addView(card);
        }
    }

    private LinearLayout createRoleCard(HeistRole role, boolean isMine, boolean isTakenByOther) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(18), dp(16), dp(18), dp(16));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(lp);

        if (isMine) {
            card.setBackgroundResource(R.drawable.bg_heist_card_selected);
        } else {
            card.setBackgroundResource(R.drawable.bg_heist_card);
        }

        if (isTakenByOther) {
            card.setAlpha(0.4f);
        }

        // Emoji
        TextView emoji = new TextView(this);
        emoji.setText(role.emoji);
        emoji.setTextSize(38);
        emoji.setPadding(0, 0, dp(16), 0);
        card.addView(emoji);

        // Info
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView name = new TextView(this);
        name.setText(role.nameAr);
        name.setTextColor(Color.parseColor(role.colorHex));
        name.setTextSize(17);
        name.setTypeface(null, Typeface.BOLD);
        info.addView(name);

        TextView desc = new TextView(this);
        desc.setText(role.description);
        desc.setTextColor(Color.parseColor("#CCCCCC"));
        desc.setTextSize(12);
        desc.setPadding(0, dp(4), 0, 0);
        info.addView(desc);

        // Stats
        TextView stats = new TextView(this);
        stats.setText("❤️ " + role.baseHp + "  •  ⚡ " + String.format("%.2f", role.speedMultiplier) + "x  •  " + role.weaponName);
        stats.setTextColor(Color.parseColor("#888888"));
        stats.setTextSize(10);
        stats.setPadding(0, dp(6), 0, 0);
        info.addView(stats);

        card.addView(info);

        // Status
        TextView status = new TextView(this);
        if (isMine) {
            status.setText("✅");
            status.setTextSize(24);
        } else if (isTakenByOther) {
            status.setText("🔒");
            status.setTextSize(20);
        } else {
            status.setText("○");
            status.setTextSize(24);
            status.setTextColor(Color.parseColor("#666666"));
        }
        card.addView(status);

        // Click
        if (!isTakenByOther && !isMine) {
            card.setOnClickListener(v -> selectRole(role));
            card.setClickable(true);
            card.setFocusable(true);
        }

        return card;
    }

    private void selectRole(HeistRole role) {
        HeistPlayer myPlayer = game.getPlayer(me.nationalId);
        if (myPlayer == null) return;

        // نمسحو الدور القديم
        myPlayer.role = role;

        Toast.makeText(this, role.emoji + " اخترت: " + role.nameAr, Toast.LENGTH_SHORT).show();
        renderPlayers();
        renderRoles();
        checkReady();
    }

    private void checkReady() {
        // نتحققو إذا كل اللاعبين عندهم دور
        boolean allReady = true;
        for (HeistPlayer p : game.players) {
            if (p.role == null) {
                allReady = false;
                break;
            }
        }

        if (allReady && game.players.size() >= HeistGame.MIN_PLAYERS) {
            statusView.setText("✅ الفريق جاهز! ابدأ المهمة");
            statusView.setTextColor(Color.parseColor("#10B981"));
        } else {
            statusView.setText("⏳ اختر دورك أولاً");
            statusView.setTextColor(Color.parseColor("#D4AF37"));
        }
    }

    private void startWithBots() {
        HeistPlayer myPlayer = game.getPlayer(me.nationalId);
        if (myPlayer == null || myPlayer.role == null) {
            Toast.makeText(this, "⚠️ اختر دورك أولاً", Toast.LENGTH_LONG).show();
            return;
        }

        // نزيدو bots (باقي الأدوار)
        List<HeistRole> remaining = game.availableRoles;
        String[] botNames = {"ليلى", "أحمد", "فاطمة", "يوسف"};
        int idx = 0;

        for (HeistRole role : HeistRole.values()) {
            if (role == myPlayer.role) continue;
            if (idx >= botNames.length) break;

            HeistPlayer bot = new HeistPlayer("bot_" + idx, botNames[idx], role);
            bot.isReady = true;
            bot.avatarEmoji = role.emoji;
            game.players.add(bot);
            idx++;
        }

        // ننتقل للـ Briefing
        Intent intent = new Intent(this, HeistBriefingActivity.class);
        intent.putExtra("game_id", game.gameId);
        intent.putExtra("my_role", myPlayer.role.id);
        startActivity(intent);
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }
}
