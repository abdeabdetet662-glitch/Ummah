package com.ummah.app;

import android.app.Activity;
import android.app.AlertDialog;
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

public class JobsActivity extends Activity {

    private IdentityManager im;
    private JobManager jm;
    private LinearLayout container;
    private ListenerRegistration reg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        // Feature Check
        if (!FeatureFlags.checkOrToast(this, "jobs")) {
            finish();
            return;
        }

        im = new IdentityManager(this);
        jm = new JobManager();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 50, 30, 60);
        scroll.addView(root);

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER);
        header.setPadding(0, 0, 0, 20);

        TextView icon = new TextView(this);
        icon.setText("💼");
        icon.setTextSize(36);
        header.addView(icon);

        TextView title = new TextView(this);
        title.setText(getString(R.string.jobs_title));
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(26);
        title.setTypeface(null, Typeface.BOLD);
        header.addView(title);

        root.addView(header);

        TextView sub = new TextView(this);
        sub.setText(getString(R.string.jobs_subtitle));
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 30);
        root.addView(sub);

        container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        root.addView(container);

        setContentView(scroll);

        startListener();
    }

    private void startListener() {
        if (reg != null) reg.remove();
        reg = jm.listenAllJobs(new JobManager.JobsListener() {
            @Override public void onJobs(List<Job> jobs) {
                runOnUiThread(() -> renderJobs(jobs));
            }
            @Override public void onError(String msg) {
                runOnUiThread(() -> Toast.makeText(JobsActivity.this, getString(R.string.common_error_prefix) + msg, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void renderJobs(List<Job> jobs) {
        container.removeAllViews();

        if (jobs.isEmpty()) {
            LinearLayout box = new LinearLayout(this);
            box.setOrientation(LinearLayout.VERTICAL);
            box.setGravity(Gravity.CENTER);
            box.setPadding(0, 80, 0, 0);

            TextView ic = new TextView(this);
            ic.setText("💼");
            ic.setTextSize(60);
            ic.setGravity(Gravity.CENTER);
            box.addView(ic);

            TextView t = new TextView(this);
            t.setText(getString(R.string.jobs_empty));
            t.setTextColor(Color.parseColor("#D4AF37"));
            t.setTextSize(16);
            t.setTypeface(null, Typeface.BOLD);
            t.setGravity(Gravity.CENTER);
            t.setPadding(0, 20, 0, 0);
            box.addView(t);

            container.addView(box);
            return;
        }

        for (Job job : jobs) {
            container.addView(buildJobCard(job));
        }
    }

    private View buildJobCard(final Job job) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackgroundResource(R.drawable.bg_card_premium);
        card.setPadding(30, 30, 30, 30);
        card.setElevation(8f);
        card.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 10, 0, 10);
        card.setLayoutParams(lp);

        TextView emoji = new TextView(this);
        emoji.setText(job.emoji);
        emoji.setTextSize(48);
        emoji.setPadding(0, 0, 24, 0);
        card.addView(emoji);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView title = new TextView(this);
        title.setText(job.title);
        title.setTextColor(Color.WHITE);
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        info.addView(title);

        TextView desc = new TextView(this);
        desc.setText(job.description);
        desc.setTextColor(Color.parseColor("#9E9E9E"));
        desc.setTextSize(12);
        desc.setPadding(0, 4, 0, 8);
        info.addView(desc);

        TextView salary = new TextView(this);
        salary.setText(getString(R.string.jobs_salary_format, job.salary));
        salary.setTextColor(Color.parseColor("#D4AF37"));
        salary.setTextSize(14);
        salary.setTypeface(null, Typeface.BOLD);
        info.addView(salary);

        card.addView(info);

        Button chooseBtn = UiHelper.primaryButton(this, getString(R.string.jobs_choose));
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(200, LinearLayout.LayoutParams.WRAP_CONTENT);
        chooseBtn.setLayoutParams(btnLp);
        chooseBtn.setOnClickListener(v -> showChooseDialog(job));

        card.addView(chooseBtn);

        return card;
    }

    private void showChooseDialog(final Job job) {
        final Citizen me = im.getCitizen();
        if (me == null) return;

        // 🔍 نتحقق أول إذا الحساب مقفل
        jm.checkCanChoose(me.nationalId, (canChoose, reason) -> {
            if (!canChoose) {
                new AlertDialog.Builder(this)
                        .setTitle("🔒 الوظائف مقفلة")
                        .setMessage(reason)
                        .setPositiveButton("حسناً", null)
                        .show();
                return;
            }
            // ✅ مفتوح — نكمل
            showChooseDialogInternal(job, me);
        });
    }

    private void showChooseDialogInternal(final Job job, final Citizen me) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.jobs_choose_title))
                .setMessage("هل تريد العمل كـ:\n\n" + job.emoji + " " + job.title +
                        "\n\nالراتب: " + job.salary + " Đ / عملة" +
                        "\nالوصف: " + job.description)
                .setPositiveButton("نعم", (d, w) -> jm.chooseJob(me.nationalId, job, new JobManager.OnDone() {
                    @Override public void onSuccess(int earned) {
                        Toast.makeText(JobsActivity.this, getString(R.string.jobs_selected), Toast.LENGTH_LONG).show();
                        startActivity(new android.content.Intent(JobsActivity.this, MyJobActivity.class));
                        finish();
                    }
                    @Override public void onError(String msg) {
                        Toast.makeText(JobsActivity.this, "❌ " + msg, Toast.LENGTH_LONG).show();
                    }
                }))
                .setNegativeButton("إلغاء", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) reg.remove();
    }

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

}
