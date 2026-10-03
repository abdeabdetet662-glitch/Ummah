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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElectionActivity extends Activity {

    private FirebaseFirestore db;
    private IdentityManager im;
    private LinearLayout listContainer;
    private ListenerRegistration reg;
    private TextView totalVotesView;
    private TextView myVoteView;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        // Feature Check
        if (!FeatureFlags.checkOrToast(this, "elections")) {
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        im = new IdentityManager(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0A"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(36, 50, 36, 50);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText(getString(R.string.elec_title));
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(26);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText(getString(R.string.elec_elect_president));
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 8, 0, 20);
        root.addView(sub);

        totalVotesView = new TextView(this);
        totalVotesView.setText("...");
        totalVotesView.setTextColor(Color.parseColor("#9E9E9E"));
        totalVotesView.setTextSize(13);
        totalVotesView.setGravity(Gravity.CENTER);
        totalVotesView.setPadding(0, 0, 0, 6);
        root.addView(totalVotesView);

        myVoteView = new TextView(this);
        myVoteView.setText("");
        myVoteView.setTextColor(Color.parseColor("#4CAF50"));
        myVoteView.setTextSize(13);
        myVoteView.setGravity(Gravity.CENTER);
        myVoteView.setPadding(0, 0, 0, 30);
        root.addView(myVoteView);

        Button candBtn = new Button(this);
        candBtn.setText(getString(R.string.elec_announce));
        candBtn.setTextSize(15);
        candBtn.setOnClickListener(v -> showCandidateDialog());
        root.addView(candBtn);

        Button celebrateBtn = new Button(this);
        celebrateBtn.setText(getString(R.string.elec_inauguration));
        celebrateBtn.setTextSize(15);
        celebrateBtn.setTextColor(Color.WHITE);
        celebrateBtn.setBackgroundColor(Color.parseColor("#B8860B"));
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        clp.setMargins(0, 10, 0, 0);
        celebrateBtn.setLayoutParams(clp);
        celebrateBtn.setOnClickListener(v -> celebrateWinner());
        root.addView(celebrateBtn);

        listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        listContainer.setPadding(0, 24, 0, 0);
        root.addView(listContainer);

        setContentView(scroll);
        checkMyVote();
        startListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) reg.remove();
    }

    private void checkMyVote() {
        Citizen me = im.getCitizen();
        if (me == null) return;
        db.collection("votes").document(me.nationalId).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String candidate = doc.getString("candidateName");
                    myVoteView.setText(getString(R.string.election_voted_for) + (candidate != null ? candidate : ""));
                } else {
                    myVoteView.setText(getString(R.string.elec_not_voted));
                    myVoteView.setTextColor(Color.parseColor("#9E9E9E"));
                }
            });
    }

    private void startListener() {
        reg = db.collection("candidates")
            .orderBy("votes", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener((snap, e) -> {
                if (snap == null) return;
                List<Map<String, Object>> list = new ArrayList<>();
                int total = 0;
                for (QueryDocumentSnapshot d : snap) {
                    Map<String, Object> m = d.getData();
                    m.put("_id", d.getId());
                    Long v = d.getLong("votes");
                    if (v != null) total += v.intValue();
                    list.add(m);
                }
                final int fTotal = total;
                runOnUiThread(() -> {
                    totalVotesView.setText(getString(R.string.election_total_votes) + fTotal);
                    renderList(list);
                });
            });
    }

    private void renderList(List<Map<String, Object>> list) {
        listContainer.removeAllViews();

        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(getString(R.string.elec_be_first));
            empty.setTextColor(Color.parseColor("#616161"));
            empty.setTextSize(14);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 40, 0, 0);
            listContainer.addView(empty);
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            listContainer.addView(buildCandidateCard(list.get(i), i + 1));
        }
    }

    private LinearLayout buildCandidateCard(final Map<String, Object> c, int rank) {
        String id = (String) c.get("_id");
        String name = (String) c.get("name");
        String slogan = (String) c.get("slogan");
        String nationalId = (String) c.get("nationalId");
        Long votesL = (Long) c.get("votes");
        int votes = votesL != null ? votesL.intValue() : 0;

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(rank == 1 && votes > 0 ?
            Color.parseColor("#0B4F2C") : Color.parseColor("#141414"));
        card.setPadding(32, 26, 32, 26);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 16);
        card.setLayoutParams(lp);

        TextView rankView = new TextView(this);
        rankView.setText(rank == 1 ? getString(R.string.elec_current_president) : "المركز " + rank);
        rankView.setTextColor(rank == 1 ? Color.parseColor("#D4AF37") : Color.parseColor("#9E9E9E"));
        rankView.setTextSize(12);
        rankView.setTypeface(null, Typeface.BOLD);
        card.addView(rankView);

        TextView nameView = new TextView(this);
        nameView.setText("👤 " + (name != null ? name : getString(R.string.common_unknown)));
        nameView.setTextColor(Color.WHITE);
        nameView.setTextSize(20);
        nameView.setTypeface(null, Typeface.BOLD);
        nameView.setPadding(0, 8, 0, 6);
        card.addView(nameView);

        if (slogan != null && !slogan.isEmpty()) {
            TextView s = new TextView(this);
            s.setText("« " + slogan + " »");
            s.setTextColor(Color.parseColor("#BDBDBD"));
            s.setTextSize(14);
            s.setPadding(0, 0, 0, 12);
            card.addView(s);
        }

        TextView votesView = new TextView(this);
        votesView.setText("🗳️  " + votes + getString(R.string.elec_vote_count));
        votesView.setTextColor(Color.parseColor("#D4AF37"));
        votesView.setTextSize(15);
        votesView.setGravity(Gravity.CENTER);
        votesView.setPadding(0, 6, 0, 14);
        card.addView(votesView);

        final Citizen me = im.getCitizen();
        boolean isMySelf = me != null && me.nationalId.equals(nationalId);

        if (isMySelf) {
            TextView mine = new TextView(this);
            mine.setText(getString(R.string.elec_you));
            mine.setTextColor(Color.parseColor("#9E9E9E"));
            mine.setTextSize(12);
            mine.setGravity(Gravity.CENTER);
            card.addView(mine);
        } else {
            Button voteBtn = new Button(this);
            voteBtn.setText(getString(R.string.elec_elect_btn));
            voteBtn.setTextSize(14);
            voteBtn.setOnClickListener(v -> castVote(id, name));
            card.addView(voteBtn);
        }

        return card;
    }

    private void castVote(final String candidateId, final String candidateName) {
        final Citizen me = im.getCitizen();
        if (me == null) return;

        db.collection("votes").document(me.nationalId).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    Toast.makeText(this, getString(R.string.elec_already_voted), Toast.LENGTH_LONG).show();
                    return;
                }
                Map<String, Object> v = new HashMap<>();
                v.put("nationalId", me.nationalId);
                v.put("candidateId", candidateId);
                v.put("candidateName", candidateName);
                v.put("timestamp", System.currentTimeMillis());

                db.collection("votes").document(me.nationalId).set(v)
                    .addOnSuccessListener(a -> {
                        db.collection("candidates").document(candidateId)
                            .update("votes", com.google.firebase.firestore.FieldValue.increment(1))
                            .addOnSuccessListener(x -> {
                                Toast.makeText(this, "صوّتت لـ " + candidateName, Toast.LENGTH_SHORT).show();
                                myVoteView.setText(getString(R.string.election_voted_for) + candidateName);
                                myVoteView.setTextColor(Color.parseColor("#4CAF50"));
                            });
                    });
            });
    }

    private void celebrateWinner() {
        db.collection("candidates")
            .orderBy("votes", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener(q -> {
                if (q.isEmpty()) {
                    Toast.makeText(this, "لا يوجد مرشحون", Toast.LENGTH_SHORT).show();
                    return;
                }
                com.google.firebase.firestore.DocumentSnapshot d = q.getDocuments().get(0);
                String name = d.getString("name");
                String slogan = d.getString("slogan");
                Long v = d.getLong("votes");
                int votes = v != null ? v.intValue() : 0;
                if (votes == 0) {
                    Toast.makeText(this, getString(R.string.elec_no_votes), Toast.LENGTH_SHORT).show();
                    return;
                }
                android.content.Intent i = new android.content.Intent(
                    ElectionActivity.this, ElectionCelebrationActivity.class);
                i.putExtra("president_name", name);
                i.putExtra("president_slogan", slogan);
                i.putExtra("votes", votes);
                startActivity(i);
            })
            .addOnFailureListener(e ->
                Toast.makeText(this, "خطأ: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showCandidateDialog() {
        final Citizen me = im.getCitizen();
        if (me == null) return;

        db.collection("candidates").whereEqualTo("nationalId", me.nationalId).get()
            .addOnSuccessListener(q -> {
                if (!q.isEmpty()) {
                    Toast.makeText(this, getString(R.string.elec_already_candidate), Toast.LENGTH_LONG).show();
                    return;
                }

                LinearLayout c = new LinearLayout(this);
                c.setOrientation(LinearLayout.VERTICAL);
                c.setPadding(40, 20, 40, 20);

                final EditText sloganInput = new EditText(this);
                sloganInput.setHint(getString(R.string.elec_slogan));
                sloganInput.setTextColor(Color.WHITE);
                sloganInput.setHintTextColor(Color.GRAY);
                sloganInput.setInputType(InputType.TYPE_CLASS_TEXT);
                c.addView(sloganInput);

                new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.elec_run_for_president))
                    .setMessage("ستظهر باسم: " + me.name)
                    .setView(c)
                    .setPositiveButton("ترشّح", (d, w) -> {
                        String slogan = sloganInput.getText().toString().trim();
                        Map<String, Object> cand = new HashMap<>();
                        cand.put("name", me.name);
                        cand.put("slogan", slogan);
                        cand.put("nationalId", me.nationalId);
                        cand.put("votes", 0);
                        cand.put("timestamp", System.currentTimeMillis());

                        db.collection("candidates").add(cand)
                            .addOnSuccessListener(x -> Toast.makeText(this, getString(R.string.elec_registered), Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "خطأ: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("إلغاء", null)
                    .show();
            });
    }

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

}
