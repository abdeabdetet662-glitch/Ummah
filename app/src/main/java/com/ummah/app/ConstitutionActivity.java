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

import com.google.firebase.firestore.ListenerRegistration;

public class ConstitutionActivity extends Activity {
    private VoteManager vm;
    private FirebaseManager fm;
    private IdentityManager im;
    private TextView[] yesViews = new TextView[11];
    private TextView[] noViews = new TextView[11];
    private ListenerRegistration reg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        vm = new VoteManager(this);
        fm = FirebaseManager.get();
        im = new IdentityManager(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0A"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(36, 50, 36, 50);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("🏛️ دستور أُمّة");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("صوّت مع كل مواطني العالم");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 8, 0, 30);
        root.addView(sub);

        for (Constitution.Article a : Constitution.getArticles()) {
            root.addView(buildCard(a));
        }

        setContentView(scroll);
        startVoteListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) reg.remove();
    }

    private void startVoteListener() {
        reg = fm.listenConstitutionVotes(new FirebaseManager.ConstitutionVotesListener() {
            @Override public void onVotes(final int[] yes, final int[] no) {
                runOnUiThread(() -> {
                    for (int i = 1; i <= 10; i++) {
                        if (yesViews[i] != null) yesViews[i].setText("✅ " + yes[i]);
                        if (noViews[i] != null) noViews[i].setText("❌ " + no[i]);
                    }
                });
            }
        });
    }

    private LinearLayout buildCard(Constitution.Article a) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.parseColor("#141414"));
        card.setPadding(36, 28, 36, 28);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 20);
        card.setLayoutParams(lp);

        TextView num = new TextView(this);
        num.setText("المادة " + a.number);
        num.setTextColor(Color.parseColor("#D4AF37"));
        num.setTextSize(13);
        num.setTypeface(null, Typeface.BOLD);
        card.addView(num);

        TextView title = new TextView(this);
        title.setText(a.title);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 6, 0, 10);
        card.addView(title);

        TextView text = new TextView(this);
        text.setText(a.text);
        text.setTextColor(Color.parseColor("#BDBDBD"));
        text.setTextSize(14);
        text.setLineSpacing(6, 1);
        text.setPadding(0, 0, 0, 14);
        card.addView(text);

        LinearLayout counts = new LinearLayout(this);
        counts.setOrientation(LinearLayout.HORIZONTAL);
        counts.setPadding(0, 0, 0, 14);

        yesViews[a.number] = new TextView(this);
        yesViews[a.number].setText("✅ 0");
        yesViews[a.number].setTextColor(Color.parseColor("#4CAF50"));
        yesViews[a.number].setTextSize(14);
        yesViews[a.number].setGravity(Gravity.CENTER);
        yesViews[a.number].setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        counts.addView(yesViews[a.number]);

        noViews[a.number] = new TextView(this);
        noViews[a.number].setText("❌ 0");
        noViews[a.number].setTextColor(Color.parseColor("#F44336"));
        noViews[a.number].setTextSize(14);
        noViews[a.number].setGravity(Gravity.CENTER);
        noViews[a.number].setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        counts.addView(noViews[a.number]);

        card.addView(counts);

        if (vm.getVote(a.number) != 0) {
            TextView voted = new TextView(this);
            voted.setText("✔️ صوّتت على هذه المادة");
            voted.setTextColor(Color.parseColor("#4CAF50"));
            voted.setTextSize(12);
            voted.setGravity(Gravity.CENTER);
            card.addView(voted);
        } else {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            Button y = new Button(this);
            y.setText("✅ موافق");
            y.setTextSize(13);
            y.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            y.setOnClickListener(v -> castVote(a.number, true));
            row.addView(y);

            Button n = new Button(this);
            n.setText("❌ رافض");
            n.setTextSize(13);
            n.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            n.setOnClickListener(v -> castVote(a.number, false));
            row.addView(n);

            card.addView(row);
        }

        return card;
    }

    private void castVote(int articleNum, boolean yes) {
        Citizen c = im.getCitizen();
        if (c == null) return;
        fm.submitConstitutionVote(c.nationalId, articleNum, yes, new FirebaseManager.OnDone() {
            @Override public void onSuccess() {
                vm.setVote(articleNum, yes ? 1 : -1);
                Toast.makeText(ConstitutionActivity.this, "✅ تم التصويت", Toast.LENGTH_SHORT).show();
                recreate();
            }
            @Override public void onError(String msg) {
                Toast.makeText(ConstitutionActivity.this, "خطأ: " + msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
