package com.ummah.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class PresidentTitlesActivity extends Activity {

    private IdentityManager im;
    private PresidentManager pm;
    private String[] TITLES;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        TITLES = new String[]{
                getString(R.string.ptitles_knight),
                getString(R.string.ptitles_star),
                getString(R.string.ptitles_hero),
                getString(R.string.ptitles_noble),
                getString(R.string.ptitles_role_model),
                getString(R.string.ptitles_jewel),
                getString(R.string.ptitles_falcon),
                getString(R.string.ptitles_lion),
        };
        im = new IdentityManager(this);
        pm = new PresidentManager();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 50, 30, 60);
        scroll.addView(root);

        TextView title = UiHelper.goldTitle(this, getString(R.string.ptitles_section), 26);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText(getString(R.string.ptitles_subtitle));
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 24);
        root.addView(sub);

        LinearLayout card = UiHelper.card(this);

        TextView lbl = new TextView(this);
        lbl.setText(getString(R.string.ptitles_citizen_id));
        lbl.setTextColor(Color.parseColor("#D4AF37"));
        lbl.setTextSize(14);
        lbl.setTypeface(null, Typeface.BOLD);
        lbl.setGravity(Gravity.CENTER);
        lbl.setPadding(0, 0, 0, 12);
        card.addView(lbl);

        final EditText idInput = UiHelper.input(this, "UMM-XXXX-XXXX-XXXX");
        card.addView(idInput);

        root.addView(card);

        TextView pickLbl = new TextView(this);
        pickLbl.setText(getString(R.string.ptitles_choose));
        pickLbl.setTextColor(Color.parseColor("#D4AF37"));
        pickLbl.setTextSize(15);
        pickLbl.setTypeface(null, Typeface.BOLD);
        pickLbl.setGravity(Gravity.CENTER);
        pickLbl.setPadding(0, 32, 0, 16);
        root.addView(pickLbl);

        for (String titleText : TITLES) {
            addTitleButton(root, titleText, idInput);
        }

        setContentView(scroll);
    }

    private void addTitleButton(LinearLayout root, final String titleText, final EditText idInput) {
        Button btn = UiHelper.actionButton(this, titleText, "#4E342E");
        btn.setTextSize(17);
        btn.setOnClickListener(v -> {
            String id = idInput.getText().toString().trim();
            if (id.isEmpty()) {
                Toast.makeText(this, getString(R.string.ptitles_enter_id), Toast.LENGTH_SHORT).show();
                return;
            }
            confirmGrant(id, titleText);
        });
        root.addView(btn);
    }

    private void confirmGrant(final String citizenId, final String titleText) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.ptitles_grant_title))
                .setMessage("منح اللقب:\n\n" + titleText + "\n\nللمواطن:\n" + citizenId + "؟")
                .setPositiveButton(getString(R.string.ptitles_grant), (d, w) -> {
                    Citizen me = im.getCitizen();
                    if (me == null) return;
                    pm.grantTitle(me.nationalId, me.name, citizenId, titleText,
                            new PresidentManager.OnDone() {
                        @Override public void onSuccess() {
                            Toast.makeText(PresidentTitlesActivity.this,
                                    getString(R.string.ptitles_granted), Toast.LENGTH_LONG).show();
                        }
                        @Override public void onError(String m) {
                            Toast.makeText(PresidentTitlesActivity.this,
                                    "❌ " + m, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }
}
