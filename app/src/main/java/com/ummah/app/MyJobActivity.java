package com.ummah.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration;

public class MyJobActivity extends Activity {

    private IdentityManager im;
    private JobManager jm;
    private LinearLayout root;
    private ListenerRegistration reg;
    private Handler handler = new Handler();
    private Runnable ticker;

    private TextView titleView;
    private TextView salaryView;
    private TextView levelView;
    private TextView xpView;
    private TextView cooldownView;
    private Button workBtn;
    private String myJobTitle = "";
    private int mySalary = 0;
    private long lastWorkTime = 0;
    private int cooldownMin = 30;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        im = new IdentityManager(this);
        jm = new JobManager();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(30, 60, 30, 60);
        scroll.addView(root);

        setContentView(scroll);

        startListener();
    }

    private void startListener() {
        Citizen c = im.getCitizen();
        if (c == null) { finish(); return; }

        if (reg != null) reg.remove();
        reg = jm.listenMyJob(c.nationalId, new JobManager.MyJobListener() {
            @Override public void onJob(String jobId, long lastWork, int level, int xp) {
                lastWorkTime = lastWork;
                runOnUiThread(() -> {
                    if (jobId == null) {
                        showNoJob();
                    } else {
                        showMyJob(level, xp);
                    }
                });
            }
            @Override public void onError(String msg) {
                runOnUiThread(() -> Toast.makeText(MyJobActivity.this, getString(R.string.common_error_prefix) + msg, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void showNoJob() {
        root.removeAllViews();

        TextView ic = new TextView(this);
        ic.setText("💼");
        ic.setTextSize(80);
        ic.setGravity(Gravity.CENTER);
        root.addView(ic);

        TextView t = new TextView(this);
        t.setText(getString(R.string.myjob_no_job));
        t.setTextColor(Color.parseColor("#D4AF37"));
        t.setTextSize(22);
        t.setTypeface(null, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, 20, 0, 10);
        root.addView(t);

        TextView s = new TextView(this);
        s.setText(getString(R.string.myjob_subtitle));
        s.setTextColor(Color.parseColor("#9E9E9E"));
        s.setTextSize(13);
        s.setGravity(Gravity.CENTER);
        s.setPadding(0, 0, 0, 30);
        root.addView(s);

        Button chooseBtn = UiHelper.primaryButton(this, getString(R.string.myjob_choose));
        chooseBtn.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, JobsActivity.class));
            finish();
        });
        root.addView(chooseBtn);
    }

    private void showMyJob(int level, int xp) {
        root.removeAllViews();

        // Header
        TextView title = UiHelper.goldTitle(this, getString(R.string.myjob_my_job), 32);
        root.addView(title);

        // بطاقة الوظيفة
        LinearLayout card = UiHelper.goldCard(this);
        card.setGravity(Gravity.CENTER);

        TextView label = new TextView(this);
        label.setText(getString(R.string.myjob_current));
        label.setTextColor(Color.parseColor("#D4AF37"));
        label.setTextSize(13);
        label.setGravity(Gravity.CENTER);
        card.addView(label);

        titleView = new TextView(this);
        titleView.setText("...");
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(28);
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setGravity(Gravity.CENTER);
        titleView.setPadding(0, 14, 0, 8);
        card.addView(titleView);

        salaryView = new TextView(this);
        salaryView.setText("💰 ...");
        salaryView.setTextColor(Color.parseColor("#D4AF37"));
        salaryView.setTextSize(18);
        salaryView.setTypeface(null, Typeface.BOLD);
        salaryView.setGravity(Gravity.CENTER);
        card.addView(salaryView);

        root.addView(card);

        // بطاقة المستوى
        LinearLayout lvlCard = UiHelper.card(this);
        lvlCard.setGravity(Gravity.CENTER);

        levelView = new TextView(this);
        levelView.setText(getString(R.string.job_level) + level);
        levelView.setTextColor(Color.WHITE);
        levelView.setTextSize(18);
        levelView.setTypeface(null, Typeface.BOLD);
        levelView.setGravity(Gravity.CENTER);
        lvlCard.addView(levelView);

        xpView = new TextView(this);
        xpView.setText("XP: " + xp + " / " + (level * 100));
        xpView.setTextColor(Color.parseColor("#9E9E9E"));
        xpView.setTextSize(13);
        xpView.setGravity(Gravity.CENTER);
        xpView.setPadding(0, 6, 0, 0);
        lvlCard.addView(xpView);

        root.addView(lvlCard);

        // cooldown
        cooldownView = new TextView(this);
        cooldownView.setText("");
        cooldownView.setTextColor(Color.parseColor("#FFC107"));
        cooldownView.setTextSize(15);
        cooldownView.setGravity(Gravity.CENTER);
        cooldownView.setPadding(0, 20, 0, 20);
        root.addView(cooldownView);

        // زر اعمل
        workBtn = UiHelper.primaryButton(this, getString(R.string.myjob_work_now));
        workBtn.setMinHeight(160);
        workBtn.setTextSize(20);
        workBtn.setOnClickListener(v -> doWork());
        root.addView(workBtn);

        // زر تغيير الوظيفة
        Button changeBtn = UiHelper.actionButton(this, "🔄  غيّر الوظيفة", "#5D4037");
        changeBtn.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(this)
                    .setTitle(getString(R.string.myjob_change))
                    .setMessage(getString(R.string.myjob_leave_confirm))
                    .setPositiveButton("نعم", (d, w) -> {
                        Citizen c = im.getCitizen();
                        if (c == null) return;
                        jm.quitJob(c.nationalId, new JobManager.OnDone() {
                            @Override public void onSuccess(int e) {
                                startActivity(new android.content.Intent(MyJobActivity.this, JobsActivity.class));
                                finish();
                            }
                            @Override public void onError(String msg) {
                                Toast.makeText(MyJobActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("إلغاء", null)
                    .show();
        });
        root.addView(changeBtn);

        startTicker();
    }

    private void startTicker() {
        if (ticker != null) handler.removeCallbacks(ticker);
        ticker = new Runnable() {
            @Override public void run() {
                updateCooldown();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(ticker);
    }

    private void updateCooldown() {
        if (cooldownView == null || workBtn == null) return;

        long now = System.currentTimeMillis();
        long cooldownMs = (long) cooldownMin * 60 * 1000;
        long elapsed = now - lastWorkTime;

        if (elapsed >= cooldownMs) {
            cooldownView.setText(getString(R.string.myjob_can_work));
            cooldownView.setTextColor(Color.parseColor("#4CAF50"));
            workBtn.setEnabled(true);
            workBtn.setAlpha(1f);
        } else {
            long remaining = cooldownMs - elapsed;
            long min = remaining / 60000;
            long sec = (remaining % 60000) / 1000;
            cooldownView.setText(getString(R.string.job_cooldown) + min + ":" + String.format("%02d", sec));
            cooldownView.setTextColor(Color.parseColor("#FFC107"));
            workBtn.setEnabled(false);
            workBtn.setAlpha(0.5f);
        }
    }

    private void doWork() {
        Citizen c = im.getCitizen();
        if (c == null) return;

        workBtn.setEnabled(false);
        workBtn.setText(getString(R.string.myjob_working));

        jm.work(c.nationalId, new JobManager.OnDone() {
            @Override public void onSuccess(int earned) {
                runOnUiThread(() -> {
                    Toast.makeText(MyJobActivity.this,
                            "✅ ربحت " + earned + " Đ!", Toast.LENGTH_LONG).show();
                    workBtn.setText(getString(R.string.myjob_work_now));
                    startTicker();
                });
            }
            @Override public void onError(String msg) {
                runOnUiThread(() -> {
                    Toast.makeText(MyJobActivity.this, "❌ " + msg, Toast.LENGTH_LONG).show();
                    workBtn.setText(getString(R.string.myjob_work_now));
                    startTicker();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) reg.remove();
        if (ticker != null) handler.removeCallbacks(ticker);
    }

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

}
