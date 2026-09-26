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

public class ConstitutionActivity extends Activity {
    private VoteManager vm;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        vm = new VoteManager(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0A"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 60, 40, 60);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("🏛️ دستور أُمّة");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(34);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("10 مواد أساسية. صوّت على كل مادة.");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(14);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 10, 0, 40);
        root.addView(sub);

        for (Constitution.Article a : Constitution.getArticles()) {
            root.addView(buildCard(a));
        }

        setContentView(scroll);
    }

    private LinearLayout buildCard(Constitution.Article a) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.parseColor("#141414"));
        card.setPadding(40, 30, 40, 30);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 24);
        card.setLayoutParams(lp);

        TextView num = new TextView(this);
        num.setText("المادة " + a.number);
        num.setTextColor(Color.parseColor("#D4AF37"));
        num.setTextSize(14);
        num.setTypeface(null, Typeface.BOLD);
        card.addView(num);

        TextView title = new TextView(this);
        title.setText(a.title);
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 8, 0, 12);
        card.addView(title);

        TextView text = new TextView(this);
        text.setText(a.text);
        text.setTextColor(Color.parseColor("#BDBDBD"));
        text.setTextSize(15);
        text.setLineSpacing(8, 1);
        text.setPadding(0, 0, 0, 20);
        card.addView(text);

        final TextView status = new TextView(this);
        status.setTextSize(13);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 0, 0, 14);
        updateStatus(status, vm.getVote(a.number));
        card.addView(status);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        Button yes = new Button(this);
        yes.setText("✅ موافق");
        yes.setTextSize(14);
        yes.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        yes.setOnClickListener(v -> {
            vm.setVote(a.number, 1);
            updateStatus(status, 1);
            Toast.makeText(this, "صوّتت موافق", Toast.LENGTH_SHORT).show();
        });
        row.addView(yes);

        Button no = new Button(this);
        no.setText("❌ غير موافق");
        no.setTextSize(14);
        no.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        no.setOnClickListener(v -> {
            vm.setVote(a.number, -1);
            updateStatus(status, -1);
            Toast.makeText(this, "صوّتت غير موافق", Toast.LENGTH_SHORT).show();
        });
        row.addView(no);

        card.addView(row);
        return card;
    }

    private void updateStatus(TextView s, int v) {
        if (v == 1) { s.setText("✅ موافق"); s.setTextColor(Color.parseColor("#4CAF50")); }
        else if (v == -1) { s.setText("❌ غير موافق"); s.setTextColor(Color.parseColor("#F44336")); }
        else { s.setText("⏳ لم تصوّت"); s.setTextColor(Color.parseColor("#757575")); }
    }
}
