package com.ummah.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
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

    private String myJobTitle = "";
    private String myJobEmoji = "💼";
    private int mySalary = 0;
    private int myLevel = 1;
    private int myXp = 0;
    private long lastWorkTime = 0;
    private int todayWorks = 0;
    private long lockedUntil = 0;

    private TextView worksLeftView;
    private TextView dotsView;
    private TextView cooldownView;
    private Button workBtn;

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

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
            @Override
            public void onJob(String jobId, String title, String emoji, int salary,
                              long lastWork, int level, int xp,
                              int tw, long lu) {
                myJobTitle = title != null ? title : "";
                myJobEmoji = emoji != null ? emoji : "💼";
                mySalary = salary;
                lastWorkTime = lastWork;
                myLevel = level;
                myXp = xp;
                todayWorks = tw;
                lockedUntil = lu;

                runOnUiThread(() -> {
                    if (jobId == null) showNoJob();
                    else showMyJob();
                });
            }

            @Override
            public void onError(String msg) {
                runOnUiThread(() -> Toast.makeText(MyJobActivity.this,
                        getString(R.string.common_error_prefix) + msg,
                        Toast.LENGTH_SHORT).show());
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

        Button chooseBtn = UiHelper.primaryButton(this, getString(R.string.myjob_choose));
        chooseBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, JobsActivity.class));
            finish();
        });
        root.addView(chooseBtn);
    }

    private void showMyJob() {
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

        TextView titleView = new TextView(this);
        titleView.setText(myJobEmoji + "  " + myJobTitle);
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(28);
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setGravity(Gravity.CENTER);
        titleView.setPadding(0, 14, 0, 8);
        card.addView(titleView);

        TextView salaryView = new TextView(this);
        // \u200E = LTR mark
        salaryView.setText("💰 \u200E" + mySalary + "\u200E Đ / عملة");
        salaryView.setTextColor(Color.parseColor("#D4AF37"));
        salaryView.setTextSize(18);
        salaryView.setTypeface(null, Typeface.BOLD);
        salaryView.setGravity(Gravity.CENTER);
        salaryView.setTextDirection(View.TEXT_DIRECTION_LTR);
        card.addView(salaryView);

        root.addView(card);

        // بطاقة المستوى
        LinearLayout lvlCard = UiHelper.card(this);
        lvlCard.setGravity(Gravity.CENTER);

        TextView levelView = new TextView(this);
        levelView.setText("🏆 المستوى: " + myLevel);
        levelView.setTextColor(Color.WHITE);
        levelView.setTextSize(18);
        levelView.setTypeface(null, Typeface.BOLD);
        levelView.setGravity(Gravity.CENTER);
        lvlCard.addView(levelView);

        TextView xpView = new TextView(this);
        // \u202A = LTR embedding, \u202C = pop
        xpView.setText("\u202AXP: " + myXp + " / " + (myLevel * 100) + "\u202C");
        xpView.setTextColor(Color.parseColor("#9E9E9E"));
        xpView.setTextSize(13);
        xpView.setGravity(Gravity.CENTER);
        xpView.setPadding(0, 6, 0, 0);
        xpView.setTextDirection(View.TEXT_DIRECTION_LTR);
        lvlCard.addView(xpView);

        root.addView(lvlCard);

        // بطاقة الأعمال اليوم
        LinearLayout worksCard = UiHelper.card(this);
        worksCard.setGravity(Gravity.CENTER);

        TextView worksTitle = new TextView(this);
        worksTitle.setText("📊 الأعمال اليوم");
        worksTitle.setTextColor(Color.parseColor("#9CA3AF"));
        worksTitle.setTextSize(13);
        worksTitle.setGravity(Gravity.CENTER);
        worksCard.addView(worksTitle);

        worksLeftView = new TextView(this);
        worksLeftView.setTextSize(28);
        worksLeftView.setTypeface(null, Typeface.BOLD);
        worksLeftView.setGravity(Gravity.CENTER);
        worksLeftView.setPadding(0, 8, 0, 0);
        worksLeftView.setTextDirection(View.TEXT_DIRECTION_LTR);
        worksCard.addView(worksLeftView);

        dotsView = new TextView(this);
        dotsView.setGravity(Gravity.CENTER);
        dotsView.setTextSize(22);
        dotsView.setPadding(0, 10, 0, 0);
        worksCard.addView(dotsView);

        root.addView(worksCard);

        // الكولداون / القفل
        cooldownView = new TextView(this);
        cooldownView.setTextSize(16);
        cooldownView.setTypeface(null, Typeface.BOLD);
        cooldownView.setGravity(Gravity.CENTER);
        cooldownView.setPadding(0, 24, 0, 24);
        root.addView(cooldownView);

        // زر اعمل
        workBtn = UiHelper.primaryButton(this, "💰 اعمل الآن");
        workBtn.setMinHeight(160);
        workBtn.setTextSize(20);
        workBtn.setOnClickListener(v -> doWork());
        root.addView(workBtn);

        // زر تغيير
        Button changeBtn = UiHelper.actionButton(this, "🔄 غيّر الوظيفة", "#5D4037");
        changeBtn.setOnClickListener(v -> confirmChangeJob());
        root.addView(changeBtn);

        startTicker();
    }

    private void confirmChangeJob() {
        new AlertDialog.Builder(this)
                .setTitle("🔄 تغيير الوظيفة")
                .setMessage("هل أنت متأكد؟ راح تخسر تقدمك في الوظيفة الحالية.")
                .setPositiveButton("نعم، غيّر", (d, w) -> {
                    Citizen c = im.getCitizen();
                    if (c == null) return;
                    jm.quitJob(c.nationalId, new JobManager.OnDone() {
                        @Override public void onSuccess(int e) {
                            startActivity(new Intent(MyJobActivity.this, JobsActivity.class));
                            finish();
                        }
                        @Override public void onError(String msg) {
                            Toast.makeText(MyJobActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void startTicker() {
        if (ticker != null) handler.removeCallbacks(ticker);
        ticker = new Runnable() {
            @Override public void run() {
                refreshUI();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(ticker);
    }

    private void refreshUI() {
        if (workBtn == null || cooldownView == null) return;

        // الرصيد والحد
        long now = System.currentTimeMillis();
        boolean isLocked = (lockedUntil > 0 && now < lockedUntil);
        int worksLeft = JobManager.worksLeft(todayWorks, lockedUntil);

        // العمل المتبقي (LTR)
        if (worksLeftView != null) {
            worksLeftView.setText("\u202A" + worksLeft + " / " + JobManager.DAILY_LIMIT + "\u202C");
            worksLeftView.setTextColor(worksLeft > 0 ?
                    Color.parseColor("#10B981") :
                    Color.parseColor("#EF4444"));
        }

        // النقاط
        if (dotsView != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < JobManager.DAILY_LIMIT; i++) {
                sb.append(i < worksLeft ? "●" : "○");
                if (i < JobManager.DAILY_LIMIT - 1) sb.append("  ");
            }
            dotsView.setText(sb.toString());
            dotsView.setTextColor(worksLeft > 0 ?
                    Color.parseColor("#D4AF37") :
                    Color.parseColor("#6B7280"));
        }

        // الحالة
        if (isLocked) {
            long remaining = lockedUntil - now;
            long h = remaining / (60 * 60 * 1000);
            long m = (remaining % (60 * 60 * 1000)) / (60 * 1000);
            long s = (remaining % (60 * 1000)) / 1000;
            cooldownView.setText("🔒 مقفل — يفتح بعد \u202A" + h + ":" 
                    + (m < 10 ? "0" : "") + m + ":" 
                    + (s < 10 ? "0" : "") + s + "\u202C");
            cooldownView.setTextColor(Color.parseColor("#EF4444"));
            workBtn.setEnabled(false);
            workBtn.setAlpha(0.4f);
            workBtn.setText("🔒 مقفل 24 ساعة");
        } else {
            String msg = JobManager.canWorkMessage(lastWorkTime, todayWorks, lockedUntil);
            if (msg == null) {
                cooldownView.setText("✅ يمكنك العمل الآن!");
                cooldownView.setTextColor(Color.parseColor("#10B981"));
                workBtn.setEnabled(true);
                workBtn.setAlpha(1f);
                workBtn.setText("💰 اعمل الآن");
            } else {
                cooldownView.setText(msg);
                cooldownView.setTextColor(Color.parseColor("#FFC107"));
                workBtn.setEnabled(false);
                workBtn.setAlpha(0.5f);
                workBtn.setText("⏰ في انتظار...");
            }
        }
    }

    private void doWork() {
        Citizen c = im.getCitizen();
        if (c == null) return;

        workBtn.setEnabled(false);
        workBtn.setText("⏳ جارٍ العمل...");

        jm.work(c.nationalId, new JobManager.OnDone() {
            @Override public void onSuccess(int earned) {
                runOnUiThread(() -> {
                    Toast.makeText(MyJobActivity.this,
                            "✅ ربحت \u202A" + earned + "\u202C Đ!",
                            Toast.LENGTH_LONG).show();
                });
            }
            @Override public void onError(String msg) {
                runOnUiThread(() -> {
                    Toast.makeText(MyJobActivity.this, "❌ " + msg, Toast.LENGTH_LONG).show();
                    refreshUI();
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
}
