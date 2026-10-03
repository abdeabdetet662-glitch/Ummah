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

public class PresidentAnnounceActivity extends Activity {

    private IdentityManager im;
    private PresidentManager pm;

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

        TextView title = UiHelper.goldTitle(this, getString(R.string.pannounce_title_main), 26);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText(getString(R.string.pannounce_subtitle));
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 24);
        root.addView(sub);

        LinearLayout card = UiHelper.card(this);

        TextView lbl1 = new TextView(this);
        lbl1.setText(getString(R.string.pannounce_title_label));
        lbl1.setTextColor(Color.parseColor("#D4AF37"));
        lbl1.setTextSize(14);
        lbl1.setTypeface(null, Typeface.BOLD);
        lbl1.setGravity(Gravity.CENTER);
        lbl1.setPadding(0, 0, 0, 12);
        card.addView(lbl1);

        final EditText titleInput = UiHelper.input(this, getString(R.string.pannounce_title_hint));
        card.addView(titleInput);

        TextView lbl2 = new TextView(this);
        lbl2.setText(getString(R.string.pannounce_body_label));
        lbl2.setTextColor(Color.parseColor("#D4AF37"));
        lbl2.setTextSize(14);
        lbl2.setTypeface(null, Typeface.BOLD);
        lbl2.setGravity(Gravity.CENTER);
        lbl2.setPadding(0, 20, 0, 12);
        card.addView(lbl2);

        final EditText contentInput = UiHelper.input(this, getString(R.string.pannounce_body_hint));
        contentInput.setMinLines(5);
        contentInput.setGravity(Gravity.TOP | Gravity.START);
        card.addView(contentInput);

        root.addView(card);

        Button publish = UiHelper.primaryButton(this, getString(R.string.pannounce_publish));
        publish.setMinHeight(160);
        publish.setTextSize(18);
        publish.setOnClickListener(v -> {
            String t = titleInput.getText().toString().trim();
            String c = contentInput.getText().toString().trim();
            if (t.isEmpty() || c.isEmpty()) {
                Toast.makeText(this, R.string.toast_fill_fields, Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.pannounce_confirm))
                    .setMessage("العنوان: " + t + "\n\nسيراه كل مواطني أُمّة.")
                    .setPositiveButton("نشر", (d, w) -> {
                        Citizen me = im.getCitizen();
                        if (me == null) return;
                        pm.publishAnnouncement(me.nationalId, me.name, t, c,
                                new PresidentManager.OnDone() {
                            @Override public void onSuccess() {
                                Toast.makeText(PresidentAnnounceActivity.this,
                                        getString(R.string.pannounce_published), Toast.LENGTH_LONG).show();
                                finish();
                            }
                            @Override public void onError(String msg) {
                                Toast.makeText(PresidentAnnounceActivity.this,
                                        "❌ " + msg, Toast.LENGTH_LONG).show();
                            }
                        });
                    })
                    .setNegativeButton("إلغاء", null)
                    .show();
        });
        root.addView(publish);

        setContentView(scroll);
    }

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

}
