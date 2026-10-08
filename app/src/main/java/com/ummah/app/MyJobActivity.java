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

    // بيانات الوظيفة الحالية
    private String myJobTitle = "";
    private String myJobEmoji = "💼";
    private int mySalary = 0;
    private int myLevel = 1;
    private int myXp = 0;
    private long lastWorkTime = 0;
    private int todayWorks = 0;
    private String todayDate = "";

    // عناصر الواجهة
    private TextView titleView;
    private TextView salaryView;
    private TextView levelView;
    private TextView xpView;
    private TextView cooldownView;
    private TextView worksLeftView;
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
            public void onJob(String jobId, String title, int salary,
                              long lastWork, int level, int xp,
                              int tw, String td) {
                myJobTitle = title != null ? title : "";
                mySalary = salary;
                lastWorkTime = lastWork;
                myLevel = level;
                myXp = xp;
                todayWorks = tw;
                todayDate = td != null ? td : "";

                runOnUiThread(() -> {
                    if (jobId == null) {
                        showNoJob();
                    } else {
                        showMyJob();
                    }
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

        TextView s = new TextView(this);
        s.setText(getString(R.string.myjob_subtitle));
        s.setTextColor(Color.parseColor("#9E9E9E"));
        s.setTextSize(13);
        s.setGravity(Gravity.CENTER);
        s.setPadding(0, 0, 0, 30);
        root.addView(s);

        Button chooseBtn = UiHelper.primaryButton(this, getString(R.string.myjob_choose));
        chooseBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, JobsActivity.class));
            finish();
        });
        root.addView(chooseBtn);
    }

    private void showMyJob() {
        root.removeAllViews();

        // ═══ Header ═══
        TextView title = UiHelper.goldTitle(this, getString(R.string.myjob_my_job), 32);
        root.addView(title);

        // ═══ بطاقة الوظيفة ═══
        LinearLayout card = UiHelper.goldCard(this);
        card.setGravity(Gravity.CENTER);

        TextView label = new TextView(this);
        label.setText(getString(R.string.myjob_current));
        label.setTextColor(Color.parseColor("#D4AF37"));
        label.setTextSize(13);
        label.setGravity(Gravity.CENTER);
        card.addView(label);

        titleView = new TextView(this);
        titleView.setText(myJobEmoji + "  " + myJobTitle);
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(28);
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setGravity(Gravity.CENTER);
        titleView.setPadding(0, 14, 0, 8);
        card.addView(titleView);

        salaryView = new TextView(this);
        salaryView.setText("💰 " + mySalary + " Đ / عملة");
        salaryView.setTextColor(Color.parseColor("#D4AF37"));
        salaryView.setTextSize(18);
        salaryView.setTypeface(null, Typeface.BOLD);
        salaryView.setGravity(Gravity.CENTER);
        card.addView(salaryView);

        root.addView(card);

        // ═══ بطاقة المستوى ═══
        LinearLayout lvlCard = UiHelper.card(this);
        lvlCard.setGravity(Gravity.CENTER);

        levelView = new TextView(this);
        levelView.setText("🏆 " + getString(R.string.job_level) + " " + myLevel);
        levelView.setTextColor(Color.WHITE);
        levelView.setTextSize(18);
        levelView.setTypeface(null, Typeface.BOLD);
        levelView.setGravity(Gravity.CENTER);
        lvlCard.addView(levelView);

        xpView = new TextView(this);
        xpView.setText("XP: " + myXp + " / " + (myLevel * 100));
        xpView.setTextColor(Color.parseColor("#9E9E9E"));
        xpView.setTextSize(13);
        xpView.setGravity(Gravity.CENTER);
        xpView.setPadding(0, 6, 0, 0);
        lvlCard.addView(xpView);

        root.addView(lvlCard);

        // ═══ عدّاد الأعمال اليومي ═══
        LinearLayout worksCard = UiHelper.card(this);
        worksCard.setGravity(Gravity.CENTER);
        worksCard.setPadding(20, 20, 20, 20);

        TextView worksTitle = new TextView(this);
        worksTitle.setText("📊 الأعمال اليوم");
        worksTitle.setTextColor(Color.parseColor("#9CA3AF"));
        worksTitle.setTextSize(13);
        worksTitle.setGravity(Gravity.CENTER);
        worksCard.addView(worksTitle);

        worksLeftView = new TextView(this);
        worksLeftView.setTextColor(Color.WHITE);
        worksLeftView.setTextSize(26);
        worksLeftView.setTypeface(null, Typeface.BOLD);
        worksLeftView.setGravity(Gravity.CENTER);
        worksLeftView.setPadding(0, 8, 0, 0);
        worksCard.addView(worksLeftView);

        // شريط تقدّم بصري
        TextView dotsView = new TextView(this);
        dotsView.setGravity(Gravity.CENTER);
        dotsView.setTextSize(20);
        dotsView.setPadding(0, 8, 0, 0);
        worksCard.addView(dotsView);

        root.addView(worksCard);

        // ═══ حالة الكولداون ═══
        cooldownView = new TextView(this);
        cooldownView.setText("");
        cooldownView.setTextColor(Color.parseColor("#FFC107"));
        cooldownView.setTextSize(16);
        cooldownView.setTypeface(null, Typeface.BOLD);
        cooldownView.setGravity(Gravity.CENTER);
        cooldownView.setPadding(0, 24, 0, 24);
        root.addView(cooldownView);

        // ═══ زر اعمل ═══
        workBtn = UiHelper.primaryButton(this, getString(R.string.myjob_work_now));
        workBtn.setMinHeight(160);
        workBtn.setTextSize(20);
        workBtn.setOnClickListener(v -> doWork());
        root.addView(workBtn);

        // ═══ زر تغيير الوظيفة ═══
        Button changeBtn = UiHelper.actionButton(this, "🔄  غيّر الوظيفة", "#5D4037");
        changeBtn.setOnClickListener(v -> confirmChangeJob());
        root.addView(changeBtn);

        // نشغّل المؤقّت
        startTicker();

        // نحدّث العرض فوراً
        refreshUI(dotsView);
    }

    private void confirmChangeJob() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.myjob_change))
                .setMessage(getString(R.string.myjob_leave_confirm))
                .setPositiveButton("نعم", (d, w) -> {
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
                TextView dots = null;
                // نلقاو dots view
                for (int i = 0; i < root.getChildCount(); i++) {
                    View v = root.getChildAt(i);
                    if (v instanceof LinearLayout) {
                        LinearLayout ll = (LinearLayout) v;
                        for (int j = 0; j < ll.getChildCount(); j++) {
                            View c = ll.getChildAt(j);
                            if (c instanceof TextView) {
                                TextView tv = (TextView) c;
                                CharSequence txt = tv.getText();
                                if (txt != null && (txt.toString().contains("●") || txt.toString().contains("○"))) {
                                    dots = tv;
                                    break;
                                }
                            }
                        }
                    }
                }
                refreshUI(dots);
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(ticker);
    }

    private void refreshUI(TextView dotsView) {
        if (workBtn == null || cooldownView == null) return;

        // تحديث العدّاد
        int worksLeft = JobManager.worksLeft(todayWorks, todayDate);
        if (worksLeftView != null) {
            worksLeftView.setText(worksLeft + " / " + JobManager.DAILY_LIMIT);
            worksLeftView.setTextColor(worksLeft > 0 ?
                    Color.parseColor("#10B981") :
                    Color.parseColor("#EF4444"));
        }

        // النقاط
        if (dotsView != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < JobManager.DAILY_LIMIT; i++) {
                if (i < worksLeft) sb.append("●");
                else sb.append("○");
                if (i < JobManager.DAILY_LIMIT - 1) sb.append("  ");
            }
            dotsView.setText(sb.toString());
            dotsView.setTextColor(worksLeft > 0 ?
                    Color.parseColor("#D4AF37") :
                    Color.parseColor("#6B7280"));
        }

        // حالة الزر
        String msg = JobManager.canWorkMessage(lastWorkTime, todayWorks, todayDate);
        if (msg == null) {
            cooldownView.setText("✅ يمكنك العمل الآن!");
            cooldownView.setTextColor(Color.parseColor("#10B981"));
            workBtn.setEnabled(true);
            workBtn.setAlpha(1f);
        } else {
            cooldownView.setText(msg);
            cooldownView.setTextColor(Color.parseColor("#FFC107"));
            workBtn.setEnabled(false);
            workBtn.setAlpha(0.5f);
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
                            "✅ ربحت " + earned + " Đ!", Toast.LENGTH_LONG).show();
                    workBtn.setText(getString(R.string.myjob_work_now));
                    // الـ listener راح يحدّث القيم تلقائياً
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
}
