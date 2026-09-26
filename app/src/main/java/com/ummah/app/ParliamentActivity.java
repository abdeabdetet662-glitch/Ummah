package com.ummah.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

public class ParliamentActivity extends Activity {
    private ProposalManager pm;
    private WalletManager wm;
    private IdentityManager im;
    private LinearLayout listContainer;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        pm = new ProposalManager(this);
        wm = new WalletManager(this);
        im = new IdentityManager(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0A"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 60, 40, 60);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("🗳️ البرلمان");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(30);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("اقترح. صوّت. اكسب.");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(14);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 10, 0, 40);
        root.addView(sub);

        Button addBtn = new Button(this);
        addBtn.setText("➕  تقديم اقتراح جديد (+20 Đ)");
        addBtn.setTextSize(15);
        addBtn.setOnClickListener(v -> showProposalDialog());
        root.addView(addBtn);

        listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        listContainer.setPadding(0, 30, 0, 0);
        root.addView(listContainer);

        setContentView(scroll);
        refreshList();
    }

    private void refreshList() {
        listContainer.removeAllViews();
        List<Proposal> list = pm.getAll();

        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("لا توجد اقتراحات بعد.\nكن أول من يقترح قانوناً!");
            empty.setTextColor(Color.parseColor("#616161"));
            empty.setTextSize(14);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 60, 0, 0);
            listContainer.addView(empty);
            return;
        }

        for (Proposal p : list) {
            listContainer.addView(buildCard(p));
        }
    }

    private LinearLayout buildCard(final Proposal p) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.parseColor("#141414"));
        card.setPadding(36, 28, 36, 28);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 20);
        card.setLayoutParams(lp);

        TextView author = new TextView(this);
        author.setText("مقدّم من: " + p.author);
        author.setTextColor(Color.parseColor("#9E9E9E"));
        author.setTextSize(12);
        card.addView(author);

        TextView title = new TextView(this);
        title.setText(p.title);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 8, 0, 10);
        card.addView(title);

        TextView body = new TextView(this);
        body.setText(p.body);
        body.setTextColor(Color.parseColor("#BDBDBD"));
        body.setTextSize(14);
        body.setLineSpacing(6, 1);
        body.setPadding(0, 0, 0, 16);
        card.addView(body);

        TextView stats = new TextView(this);
        stats.setText("✅ " + p.yes + "  |  ❌ " + p.no);
        stats.setTextColor(Color.parseColor("#D4AF37"));
        stats.setTextSize(14);
        stats.setGravity(Gravity.CENTER);
        stats.setPadding(0, 0, 0, 14);
        card.addView(stats);

        if (pm.hasVoted(p.id)) {
            TextView voted = new TextView(this);
            voted.setText("✔️ صوّتت على هذا الاقتراح");
            voted.setTextColor(Color.parseColor("#4CAF50"));
            voted.setTextSize(13);
            voted.setGravity(Gravity.CENTER);
            card.addView(voted);
        } else {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            Button yes = new Button(this);
            yes.setText("✅ موافق");
            yes.setTextSize(14);
            yes.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            yes.setOnClickListener(v -> {
                pm.vote(p.id, true);
                wm.add(5);
                Toast.makeText(this, "+5 Đ", Toast.LENGTH_SHORT).show();
                refreshList();
            });
            row.addView(yes);

            Button no = new Button(this);
            no.setText("❌ رافض");
            no.setTextSize(14);
            no.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            no.setOnClickListener(v -> {
                pm.vote(p.id, false);
                wm.add(5);
                Toast.makeText(this, "+5 Đ", Toast.LENGTH_SHORT).show();
                refreshList();
            });
            row.addView(no);

            card.addView(row);
        }

        return card;
    }

    private void showProposalDialog() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(40, 20, 40, 20);

        final EditText titleInput = new EditText(this);
        titleInput.setHint("عنوان الاقتراح");
        titleInput.setTextColor(Color.WHITE);
        titleInput.setHintTextColor(Color.GRAY);
        titleInput.setInputType(InputType.TYPE_CLASS_TEXT);
        container.addView(titleInput);

        final EditText bodyInput = new EditText(this);
        bodyInput.setHint("اشرح اقتراحك...");
        bodyInput.setTextColor(Color.WHITE);
        bodyInput.setHintTextColor(Color.GRAY);
        bodyInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        bodyInput.setMinLines(3);
        container.addView(bodyInput);

        new AlertDialog.Builder(this)
            .setTitle("اقتراح جديد")
            .setView(container)
            .setPositiveButton("نشر", (d, w) -> {
                String t = titleInput.getText().toString().trim();
                String bd = bodyInput.getText().toString().trim();
                if (t.isEmpty() || bd.isEmpty()) {
                    Toast.makeText(this, "املأ الحقول", Toast.LENGTH_SHORT).show();
                    return;
                }
                Citizen c = im.getCitizen();
                String author = c != null ? c.name : "مجهول";
                Proposal p = new Proposal(UUID.randomUUID().toString(), t, bd, author);
                pm.add(p);
                wm.add(20);
                Toast.makeText(this, "+20 Đ", Toast.LENGTH_SHORT).show();
                refreshList();
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }
}
