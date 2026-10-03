package com.ummah.app;

import android.app.Activity;
import android.app.AlertDialog;
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

import java.util.List;

public class PresidentPardonActivity extends Activity {

    private IdentityManager im;
    private PresidentManager pm;
    private LinearLayout container;
    private ListenerRegistration reg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        im = new IdentityManager(this);
        pm = new PresidentManager();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 50, 30, 60);
        scroll.addView(root);

        TextView title = UiHelper.goldTitle(this, "⚖️  عفو رئاسي", 26);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText(R.string.pardon_sub);
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 24);
        root.addView(sub);

        container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        root.addView(container);

        setContentView(scroll);
        startListener();
    }

    private void startListener() {
        if (reg != null) reg.remove();
        reg = pm.listenBlockedOrMuted(new PresidentManager.PresidentListListener() {
            @Override public void onList(List<PresidentManager.PresidentCitizen> list) {
                runOnUiThread(() -> render(list));
            }
        });
    }

    private void render(List<PresidentManager.PresidentCitizen> list) {
        container.removeAllViews();

        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(R.string.pardon_empty);
            empty.setTextColor(Color.parseColor("#4CAF50"));
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 60, 0, 0);
            container.addView(empty);
            return;
        }

        for (PresidentManager.PresidentCitizen pc : list) {
            addPardonCard(pc);
        }
    }

    private void addPardonCard(final PresidentManager.PresidentCitizen pc) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card_premium);
        card.setPadding(24, 24, 24, 24);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 8, 0, 8);
        card.setLayoutParams(lp);

        TextView name = new TextView(this);
        name.setText("👤  " + pc.name);
        name.setTextColor(Color.WHITE);
        name.setTextSize(17);
        name.setTypeface(null, Typeface.BOLD);
        card.addView(name);

        TextView id = new TextView(this);
        id.setText(pc.nationalId);
        id.setTextColor(Color.parseColor("#D4AF37"));
        id.setTextSize(11);
        id.setPadding(0, 6, 0, 12);
        card.addView(id);

        LinearLayout badges = new LinearLayout(this);
        badges.setOrientation(LinearLayout.HORIZONTAL);

        if (pc.blocked) {
            TextView b = new TextView(this);
            b.setText(R.string.pardon_blocked);
            b.setTextColor(Color.parseColor("#F44336"));
            b.setTextSize(12);
            badges.addView(b);
        }

        if (pc.muted) {
            TextView m = new TextView(this);
            m.setText(R.string.pardon_muted);
            m.setTextColor(Color.parseColor("#FF9800"));
            m.setTextSize(12);
            badges.addView(m);
        }

        card.addView(badges);

        Button pardon = UiHelper.primaryButton(this, "⚖️  إصدار عفو");
        pardon.setOnClickListener(v -> confirmPardon(pc));
        card.addView(pardon);

        container.addView(card);
    }

    private void confirmPardon(final PresidentManager.PresidentCitizen pc) {
        new AlertDialog.Builder(this)
                .setTitle("⚖️  عفو رئاسي")
                .setMessage("رفع الحظر والكتم عن:\n\n👤 " + pc.name + "\n🆔 " + pc.nationalId)
                .setPositiveButton("إصدار العفو", (d, w) -> {
                    Citizen me = im.getCitizen();
                    if (me == null) return;
                    pm.pardonCitizen(me.nationalId, me.name, pc.nationalId,
                            new PresidentManager.OnDone() {
                        @Override public void onSuccess() {
                            Toast.makeText(PresidentPardonActivity.this,
                                    "✅ تم إصدار العفو الرئاسي!", Toast.LENGTH_LONG).show();
                        }
                        @Override public void onError(String msg) {
                            Toast.makeText(PresidentPardonActivity.this,
                                    "❌ " + msg, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) reg.remove();
    }
}
