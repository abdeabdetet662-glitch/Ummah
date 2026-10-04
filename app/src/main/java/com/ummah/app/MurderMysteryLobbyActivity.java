package com.ummah.app;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

/**
 * MurderMysteryLobbyActivity — غرفة انتظار Noir
 */
public class MurderMysteryLobbyActivity extends Activity {

    private IdentityManager im;
    private MurderMysteryManager manager;
    private MurderMystery currentGame;

    private LinearLayout playersContainer;
    private TextView statusView;
    private TextView playersCountView;
    private TextView countdownView;
    private Button actionBtn;
    private FrameLayout magnifierWrapper;

    private ListenerRegistration playersReg;
    private android.os.Handler handler = new android.os.Handler();

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
        animateEntrance();
        loadGame();
    }

    private void buildUI() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_mystery_noir);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 60, 30, 80);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        scroll.addView(root);

        // ═══ زر الرجوع ═══
        TextView backBtn = new TextView(this);
        backBtn.setText("← عودة");
        backBtn.setTextColor(Color.parseColor("#D4AF37"));
        backBtn.setTextSize(14);
        backBtn.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams backLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        backLp.gravity = Gravity.START;
        backBtn.setLayoutParams(backLp);
        backBtn.setOnClickListener(v -> finish());
        root.addView(backBtn);

        // ═══ العدسة (Header) ═══
        magnifierWrapper = new FrameLayout(this);
        LinearLayout.LayoutParams mwLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mwLp.gravity = Gravity.CENTER_HORIZONTAL;
        mwLp.setMargins(0, 20, 0, 20);
        magnifierWrapper.setLayoutParams(mwLp);

        // توهج خلفي
        View glow = new View(this);
        FrameLayout.LayoutParams glowLp = new FrameLayout.LayoutParams(
                dp(180), dp(180));
        glowLp.gravity = Gravity.CENTER;
        glow.setLayoutParams(glowLp);
        glow.setBackgroundColor(Color.parseColor("#20D4AF37"));
        glow.setAlpha(0.4f);
        magnifierWrapper.addView(glow);

        TextView magnifier = new TextView(this);
        magnifier.setText("🔍");
        magnifier.setTextSize(80);
        FrameLayout.LayoutParams magLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        magLp.gravity = Gravity.CENTER;
        magnifier.setLayoutParams(magLp);
        magnifierWrapper.addView(magnifier);

        root.addView(magnifierWrapper);

        // ═══ العنوان ═══
        TextView title = new TextView(this);
        title.setText("جريمة أُمّة");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(36);
        title.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        title.setGravity(Gravity.CENTER);
        title.setLetterSpacing(0.12f);
        root.addView(title);

        // خط ذهبي
        View div = new View(this);
        LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(
                dp(180), dp(2));
        divLp.gravity = Gravity.CENTER_HORIZONTAL;
        divLp.setMargins(0, 12, 0, 24);
        div.setLayoutParams(divLp);
        div.setBackgroundColor(Color.parseColor("#D4AF37"));
        root.addView(div);

        // ═══ Countdown ═══
        countdownView = new TextView(this);
        countdownView.setTextColor(Color.parseColor("#F59E0B"));
        countdownView.setTextSize(16);
        countdownView.setTypeface(null, Typeface.BOLD);
        countdownView.setGravity(Gravity.CENTER);
        countdownView.setPadding(0, 0, 0, 24);
        root.addView(countdownView);

        // ═══ Status Card ═══
        LinearLayout statusCard = new LinearLayout(this);
        statusCard.setOrientation(LinearLayout.VERTICAL);
        statusCard.setBackgroundResource(R.drawable.bg_mystery_card);
        statusCard.setPadding(40, 32, 40, 32);
        LinearLayout.LayoutParams scLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        scLp.setMargins(0, 0, 0, 24);
        statusCard.setLayoutParams(scLp);

        statusView = new TextView(this);
        statusView.setTextColor(Color.WHITE);
        statusView.setTextSize(15);
        statusView.setGravity(Gravity.CENTER);
        statusView.setLineSpacing(0, 1.4f);
        statusCard.addView(statusView);

        // ═══ Players Count ═══
        playersCountView = new TextView(this);
        playersCountView.setTextColor(Color.parseColor("#10B981"));
        playersCountView.setTextSize(18);
        playersCountView.setTypeface(null, Typeface.BOLD);
        playersCountView.setGravity(Gravity.CENTER);
        playersCountView.setPadding(0, 20, 0, 0);
        statusCard.addView(playersCountView);

        root.addView(statusCard);

        // ═══ Players Container ═══
        playersContainer = new LinearLayout(this);
        playersContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(playersContainer);

        // ═══ Action Button ═══
        actionBtn = new Button(this);
        actionBtn.setTextSize(16);
        actionBtn.setTextColor(Color.parseColor("#0A0510"));
        actionBtn.setAllCaps(false);
        actionBtn.setTypeface(null, Typeface.BOLD);
        actionBtn.setBackgroundResource(R.drawable.bg_btn_noir);
        actionBtn.setPadding(60, 42, 60, 42);
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        btnLp.setMargins(0, 40, 0, 0);
        actionBtn.setLayoutParams(btnLp);
        root.addView(actionBtn);

        // ═══ Footer ═══
        TextView footer = new TextView(this);
        footer.setText("· الجائزة الكبرى 100,000 Đ ·");
        footer.setTextColor(Color.parseColor("#666666"));
        footer.setTextSize(12);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, 24, 0, 0);
        root.addView(footer);

        setContentView(scroll);
    }

    private void animateEntrance() {
        // نبض العدسة
        if (magnifierWrapper != null) {
            ObjectAnimator pulse = ObjectAnimator.ofFloat(magnifierWrapper, "scaleX", 1f, 1.05f);
            pulse.setRepeatCount(ValueAnimator.INFINITE);
            pulse.setRepeatMode(ValueAnimator.REVERSE);
            pulse.setDuration(1400);
            pulse.setInterpolator(new AccelerateDecelerateInterpolator());
            pulse.start();

            ObjectAnimator pulseY = ObjectAnimator.ofFloat(magnifierWrapper, "scaleY", 1f, 1.05f);
            pulseY.setRepeatCount(ValueAnimator.INFINITE);
            pulseY.setRepeatMode(ValueAnimator.REVERSE);
            pulseY.setDuration(1400);
            pulseY.setInterpolator(new AccelerateDecelerateInterpolator());
            pulseY.start();
        }
    }

    private void loadGame() {
        statusView.setText("⏳ جاري البحث عن جريمة...");

        manager.getCurrentMystery(new MurderMysteryManager.MysteryCallback() {
            @Override
            public void onResult(MurderMystery mystery) {
                currentGame = mystery;
                if (mystery == null) {
                    statusView.setText("🌫️ لا توجد جريمة حالياً\n\nعُد قريباً...");
                    actionBtn.setVisibility(View.GONE);
                    return;
                }

                updateUI();
                startCountdown();
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

        String status = "📖 " + currentGame.title + "\n\n" + currentGame.description;
        statusView.setText(status);

        playersCountView.setText("👥 " + currentGame.currentPlayers + " / " + currentGame.maxPlayers + " محقق");

        if (currentGame.isRegistration()) {
            actionBtn.setText("🎫 انضم للتحقيق · " + currentGame.entryFee + " Đ");
            actionBtn.setEnabled(true);
            actionBtn.setOnClickListener(v -> registerNow());
        } else if (currentGame.isPlaying()) {
            actionBtn.setText("🔍 ادخل للتحقيق");
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
            actionBtn.setText("🏆 شوف النتيجة");
            actionBtn.setEnabled(true);
            actionBtn.setOnClickListener(v -> {
                startActivity(new Intent(this, MurderMysteryResultActivity.class)
                        .putExtra("gameId", currentGame.id));
            });
        }
    }

    private void startCountdown() {
        if (currentGame == null) return;

        Runnable tick = new Runnable() {
            @Override
            public void run() {
                if (currentGame == null) return;
                long now = System.currentTimeMillis();
                long diff = currentGame.registrationEnd - now;

                if (diff <= 0) {
                    countdownView.setText("⏰ بدأت القضية!");
                } else {
                    long h = diff / (1000 * 60 * 60);
                    long m = (diff / (1000 * 60)) % 60;
                    long s = (diff / 1000) % 60;
                    countdownView.setText(String.format("⏰ %02d:%02d:%02d حتى بدء القضية", h, m, s));
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(tick);
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

        if (players.isEmpty()) return;

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(10), dp(24), dp(10), dp(16));
        playersContainer.addView(header);

        View lineL = new View(this);
        LinearLayout.LayoutParams lLp = new LinearLayout.LayoutParams(0, dp(1), 1f);
        lineL.setLayoutParams(lLp);
        lineL.setBackgroundColor(Color.parseColor("#30D4AF37"));
        header.addView(lineL);

        TextView t = new TextView(this);
        t.setText("   🎭 المحققون   ");
        t.setTextColor(Color.parseColor("#D4AF37"));
        t.setTextSize(14);
        t.setTypeface(null, Typeface.BOLD);
        t.setLetterSpacing(0.1f);
        header.addView(t);

        View lineR = new View(this);
        LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(0, dp(1), 1f);
        lineR.setLayoutParams(rLp);
        lineR.setBackgroundColor(Color.parseColor("#30D4AF37"));
        header.addView(lineR);

        // Players
        for (int i = 0; i < players.size(); i++) {
            MMPlayer p = players.get(i);
            View row = createPlayerCard(p, i);
            playersContainer.addView(row);

            // أنيميشن دخول متتالي
            row.setAlpha(0f);
            row.setTranslationY(50f);
            row.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(i * 80L)
                    .setDuration(400)
                    .start();
        }
    }

    private View createPlayerCard(MMPlayer p, int index) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundResource(R.drawable.bg_mystery_card);
        row.setPadding(dp(20), dp(16), dp(20), dp(16));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(10));
        row.setLayoutParams(lp);

        // Avatar emoji مع دائرية
        TextView emoji = new TextView(this);
        emoji.setText(p.avatarEmoji != null ? p.avatarEmoji : "👤");
        emoji.setTextSize(26);
        emoji.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams emojiLp = new LinearLayout.LayoutParams(dp(48), dp(48));
        emojiLp.setMargins(0, 0, dp(16), 0);
        emoji.setLayoutParams(emojiLp);
        row.addView(emoji);

        // Name + character
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
        role.setPadding(0, dp(4), 0, 0);
        info.addView(role);

        row.addView(info);

        // Badge
        if ("killer".equals(p.role) && currentGame != null && currentGame.isEnded()) {
            TextView badge = new TextView(this);
            badge.setText("🎭");
            badge.setTextSize(20);
            row.addView(badge);
        } else if (index == 0) {
            TextView badge = new TextView(this);
            badge.setText("⭐");
            badge.setTextSize(18);
            row.addView(badge);
        }

        return row;
    }

    private void registerNow() {
        if (currentGame == null) return;
        Citizen c = im.getCitizen();
        if (c == null) {
            Toast.makeText(this, "سجّل أولاً", Toast.LENGTH_SHORT).show();
            return;
        }

        actionBtn.setEnabled(false);
        actionBtn.setText("⏳ جاري الانضمام...");

        manager.register(currentGame.id, c, new MurderMysteryManager.SimpleCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(MurderMysteryLobbyActivity.this,
                            "✅ انضممت للتحقيق! استعد...", Toast.LENGTH_LONG).show();

                    // اهتزاز
                    android.os.Vibrator v = (android.os.Vibrator) getSystemService(VIBRATOR_SERVICE);
                    if (v != null) v.vibrate(200);

                    actionBtn.setText("✅ انضممت للتحقيق");
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(MurderMysteryLobbyActivity.this,
                            "❌ " + error, Toast.LENGTH_LONG).show();
                    actionBtn.setEnabled(true);
                    actionBtn.setText("🎫 انضم للتحقيق · " + currentGame.entryFee + " Đ");
                });
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
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
