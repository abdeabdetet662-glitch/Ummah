package com.ummah.app.heist;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ummah.app.Citizen;
import com.ummah.app.IdentityManager;
import com.ummah.app.LocaleHelper;
import com.ummah.app.PaymentManager;
import com.ummah.app.R;

/**
 * HeistGameActivity — المحرك الرئيسي
 */
public class HeistGameActivity extends Activity implements HeistGameView.GameListener {

    private HeistGameView gameView;
    private IdentityManager im;
    private Citizen me;
    private HeistRole myRole;

    private LinearLayout overlay;
    private TextView objectiveView;

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        im = new IdentityManager(this);
        me = im.getCitizen();

        String roleId = getIntent().getStringExtra("my_role");
        myRole = HeistRole.fromId(roleId);

        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor("#0a0510"));

        gameView = new HeistGameView(this);
        gameView.setRole(myRole);
        gameView.setListener(this);
        root.addView(gameView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // Toast-ish overlay
        overlay = new LinearLayout(this);
        overlay.setOrientation(LinearLayout.VERTICAL);
        overlay.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams olp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        olp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        olp.topMargin = dp(120);
        overlay.setLayoutParams(olp);
        overlay.setVisibility(View.GONE);

        objectiveView = new TextView(this);
        objectiveView.setTextColor(Color.parseColor("#0a0510"));
        objectiveView.setTextSize(16);
        objectiveView.setTypeface(null, Typeface.BOLD);
        objectiveView.setBackgroundColor(Color.parseColor("#D4AF37"));
        objectiveView.setPadding(dp(20), dp(10), dp(20), dp(10));
        overlay.addView(objectiveView);

        root.addView(overlay);

        setContentView(root);

        // Start after layout
        gameView.post(() -> {
            if (gameView.getWidth() > 0) {
                gameView.startGame();
            } else {
                gameView.postDelayed(() -> gameView.startGame(), 500);
            }
        });
    }

    @Override
    public void onGameUpdate(int hp, int money, int alivePolice, long timeLeft) {
        // UI updates تحدث داخل الـ View نفسه
    }

    @Override
    public void onObjective(String text) {
        runOnUiThread(() -> {
            objectiveView.setText(text);
            overlay.setVisibility(View.VISIBLE);
            overlay.setAlpha(1f);
            overlay.animate().alpha(0f).setStartDelay(1500).setDuration(500)
                .withEndAction(() -> overlay.setVisibility(View.GONE)).start();
        });
    }

    @Override
    public void onGameEnd(boolean won, int moneyCollected, int kills, long duration) {
        runOnUiThread(() -> showResult(won, moneyCollected, kills, duration));
    }

    private void showResult(boolean won, int money, int kills, long duration) {
        int reward;
        if (won) {
            reward = 5000;
        } else if (money >= 60_000) {
            reward = 2000;
        } else if (money >= 20_000) {
            reward = 500;
        } else {
            reward = 100;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(won ? "🏆 نجحت السرقة!" : "💀 فشلت السرقة");
        sb.append("\n\n");
        sb.append("💰 جمعت: ").append(money).append(" Đ\n");
        sb.append("💀 قتلات: ").append(kills).append("\n");
        sb.append("⏱️ المدة: ").append(duration).append(" ثانية\n\n");
        sb.append("🎁 مكافأة: +").append(reward).append(" Đ");

        // نمنحو المكافأة
        if (me != null) {
            PaymentManager.addBalance(me.nationalId, reward,
                "heist_reward", won ? "فوز في سرقة القرن" : "مشاركة في سرقة",
                new PaymentManager.PayCallback() {
                    @Override public void onSuccess(long newBalance) {}
                    @Override public void onInsufficient(long b, long r) {}
                    @Override public void onError(String e) {}
                });
        }

        new AlertDialog.Builder(this)
            .setTitle(won ? "🎉 نجحت!" : "💀 فشلت")
            .setMessage(sb.toString())
            .setCancelable(false)
            .setPositiveButton("🎮 مرة أخرى", (d, w) -> {
                gameView.startGame();
            })
            .setNegativeButton("← خروج", (d, w) -> finish())
            .show();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
            .setTitle("🚪 الخروج")
            .setMessage("هل تريد الخروج من المهمة؟")
            .setPositiveButton("نعم", (d, w) -> {
                gameView.stopGame();
                finish();
            })
            .setNegativeButton("لا", null)
            .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.stopGame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) {
            // نعاودو نبداو اللعبة
        }
    }

    @Override
    protected void onDestroy() {
        if (gameView != null) gameView.stopGame();
        super.onDestroy();
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }
}
