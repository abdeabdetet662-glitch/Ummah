package com.ummah.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class DailyRewardActivity extends Activity {
    private FirebaseManager fm;
    private IdentityManager im;
    private WalletManager wm;
    private TextView statusView;
    private Button claimBtn;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        // Feature Check
        if (!FeatureFlags.checkOrToast(this, "daily_reward")) {
            finish();
            return;
        }

        fm = FirebaseManager.get();
        im = new IdentityManager(this);
        wm = new WalletManager(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0A"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(40, 80, 40, 80);
        scroll.addView(root);

        TextView icon = new TextView(this);
        icon.setText("🎁");
        icon.setTextSize(80);
        icon.setGravity(Gravity.CENTER);
        root.addView(icon);

        TextView title = new TextView(this);
        title.setText(getString(R.string.daily_title));
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(30);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 20, 0, 10);
        root.addView(title);

        TextView desc = new TextView(this);
        desc.setText(getString(R.string.daily_subtitle));
        desc.setTextColor(Color.parseColor("#9E9E9E"));
        desc.setTextSize(14);
        desc.setGravity(Gravity.CENTER);
        desc.setPadding(0, 0, 0, 40);
        root.addView(desc);

        statusView = new TextView(this);
        statusView.setTextSize(16);
        statusView.setGravity(Gravity.CENTER);
        statusView.setPadding(0, 20, 0, 30);
        root.addView(statusView);

        claimBtn = new Button(this);
        claimBtn.setText(getString(R.string.daily_claim));
        claimBtn.setTextSize(18);
        claimBtn.setPadding(40, 30, 40, 30);
        claimBtn.setOnClickListener(v -> claim());
        root.addView(claimBtn);

        TextView info = new TextView(this);
        info.setText(R.string.daily_reset_info);
        info.setTextColor(Color.parseColor("#616161"));
        info.setTextSize(12);
        info.setGravity(Gravity.CENTER);
        info.setPadding(0, 40, 0, 0);
        root.addView(info);

        setContentView(scroll);
        updateUI();
    }

    private void updateUI() {
        if (wm.canClaimDaily()) {
            statusView.setText(getString(R.string.daily_available));
            statusView.setTextColor(Color.parseColor("#4CAF50"));
            claimBtn.setEnabled(true);
        } else {
            long ms = wm.millisUntilNextDaily();
            long h = ms / (60 * 60 * 1000);
            long m = (ms % (60 * 60 * 1000)) / (60 * 1000);
            statusView.setText(getString(R.string.daily_next_format, h, m));
            statusView.setTextColor(Color.parseColor("#F44336"));
            claimBtn.setEnabled(false);
        }
    }

    private void claim() {
        if (!wm.claimDaily()) {
            Toast.makeText(this, getString(R.string.daily_cant_claim), Toast.LENGTH_SHORT).show();
            return;
        }
        Citizen c = im.getCitizen();
        if (c == null) return;
        fm.addBalance(c.nationalId, 5, new FirebaseManager.OnDone() {
            @Override public void onSuccess() {
                wm.add(5);
                Toast.makeText(DailyRewardActivity.this, getString(R.string.daily_added), Toast.LENGTH_LONG).show();
                updateUI();
            }
            @Override public void onError(String msg) {
                Toast.makeText(DailyRewardActivity.this, getString(R.string.common_error_prefix) + msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

}
