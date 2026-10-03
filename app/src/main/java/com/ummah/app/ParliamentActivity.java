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

import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class ParliamentActivity extends Activity {
    private FirebaseManager fm;
    private WalletManager wm;
    private IdentityManager im;
    private LinearLayout listContainer;
    private ListenerRegistration reg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        // Feature Check
        if (!FeatureFlags.checkOrToast(this, "parliament")) {
            finish();
            return;
        }

        fm = FirebaseManager.get();
        wm = new WalletManager(this);
        im = new IdentityManager(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0A"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(36, 50, 36, 50);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText(getString(R.string.parl_header));
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText(getString(R.string.parl_subtitle));
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 8, 0, 30);
        root.addView(sub);

        Button addBtn = new Button(this);
        addBtn.setText(getString(R.string.parl_submit));
        addBtn.setTextSize(15);
        addBtn.setOnClickListener(v -> showProposalDialog());
        root.addView(addBtn);

        listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        listContainer.setPadding(0, 24, 0, 0);
        root.addView(listContainer);

        setContentView(scroll);
        startListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) reg.remove();
    }

    private void startListener() {
        reg = fm.listenProposals(list ->
            runOnUiThread(() -> refreshList(list)));
    }

    private void refreshList(List<Proposal> list) {
        listContainer.removeAllViews();
        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(getString(R.string.parl_no_proposals));
            empty.setTextColor(Color.parseColor("#616161"));
            empty.setTextSize(14);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 40, 0, 0);
            listContainer.addView(empty);
            return;
        }
        for (Proposal p : list) listContainer.addView(buildCard(p));
    }

    private LinearLayout buildCard(final Proposal p) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.parseColor("#141414"));
        card.setPadding(32, 24, 32, 24);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 16);
        card.setLayoutParams(lp);

        TextView author = new TextView(this);
        author.setText(getString(R.string.parl_by) + p.author);
        author.setTextColor(Color.parseColor("#9E9E9E"));
        author.setTextSize(11);
        card.addView(author);

        TextView title = new TextView(this);
        title.setText(p.title);
        title.setTextColor(Color.WHITE);
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 6, 0, 8);
        card.addView(title);

        TextView body = new TextView(this);
        body.setText(p.body);
        body.setTextColor(Color.parseColor("#BDBDBD"));
        body.setTextSize(13);
        body.setLineSpacing(5, 1);
        body.setPadding(0, 0, 0, 14);
        card.addView(body);

        TextView stats = new TextView(this);
        stats.setText("✅ " + p.yes + "  |  ❌ " + p.no);
        stats.setTextColor(Color.parseColor("#D4AF37"));
        stats.setTextSize(14);
        stats.setGravity(Gravity.CENTER);
        stats.setPadding(0, 0, 0, 12);
        card.addView(stats);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        Button y = new Button(this);
        y.setText(getString(R.string.parl_agree));
        y.setTextSize(13);
        y.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        y.setOnClickListener(v -> castProposalVote(p, true));
        row.addView(y);

        Button n = new Button(this);
        n.setText(getString(R.string.parl_reject));
        n.setTextSize(13);
        n.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        n.setOnClickListener(v -> castProposalVote(p, false));
        row.addView(n);

        card.addView(row);
        return card;
    }

    private void castProposalVote(Proposal p, boolean yes) {
        Citizen c = im.getCitizen();
        if (c == null) return;
        fm.voteProposal(p.id, c.nationalId, yes, new FirebaseManager.OnDone() {
            @Override public void onSuccess() {
                wm.add(5);
                Toast.makeText(ParliamentActivity.this, "+5 Đ", Toast.LENGTH_SHORT).show();
            }
            @Override public void onError(String msg) {
                Toast.makeText(ParliamentActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProposalDialog() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(40, 20, 40, 20);

        final EditText titleInput = new EditText(this);
        titleInput.setHint(getString(R.string.parl_title_hint));
        titleInput.setTextColor(Color.WHITE);
        titleInput.setHintTextColor(Color.GRAY);
        c.addView(titleInput);

        final EditText bodyInput = new EditText(this);
        bodyInput.setHint(getString(R.string.parl_describe));
        bodyInput.setTextColor(Color.WHITE);
        bodyInput.setHintTextColor(Color.GRAY);
        bodyInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        bodyInput.setMinLines(3);
        c.addView(bodyInput);

        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.parl_new_proposal))
            .setView(c)
            .setPositiveButton(getString(R.string.parl_publish), (d, w) -> {
                String t = titleInput.getText().toString().trim();
                String bd = bodyInput.getText().toString().trim();
                if (t.isEmpty() || bd.isEmpty()) return;
                Citizen citizen = im.getCitizen();
                String author = citizen != null ? citizen.name : "مجهول";
                Proposal p = new Proposal("", t, bd, author);
                fm.submitProposal(p, new FirebaseManager.OnDone() {
                    @Override public void onSuccess() {
                        wm.add(20);
                        Toast.makeText(ParliamentActivity.this, "+20 Đ", Toast.LENGTH_SHORT).show();
                    }
                    @Override public void onError(String msg) {
                        Toast.makeText(ParliamentActivity.this, "خطأ: " + msg, Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

}
