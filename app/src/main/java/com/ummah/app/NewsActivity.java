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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewsActivity extends Activity {
    private FirebaseManager fm;
    private IdentityManager im;
    private LinearLayout listContainer;
    private ListenerRegistration reg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        // Feature Check
        if (!FeatureFlags.checkOrToast(this, "news")) {
            finish();
            return;
        }

        fm = FirebaseManager.get();
        im = new IdentityManager(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0A"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(36, 50, 36, 50);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText(getString(R.string.news_title));
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText(getString(R.string.news_subtitle));
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 8, 0, 30);
        root.addView(sub);

        Button postBtn = new Button(this);
        postBtn.setText(getString(R.string.news_publish));
        postBtn.setTextSize(15);
        postBtn.setOnClickListener(v -> showPostDialog());
        root.addView(postBtn);

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
        reg = fm.listenNews(list -> runOnUiThread(() -> refreshList(list)));
    }

    private void refreshList(List<FirebaseManager.NewsItem> list) {
        listContainer.removeAllViews();
        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(getString(R.string.news_empty));
            empty.setTextColor(Color.parseColor("#616161"));
            empty.setTextSize(14);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 40, 0, 0);
            listContainer.addView(empty);
            return;
        }
        for (FirebaseManager.NewsItem n : list) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundColor(Color.parseColor("#141414"));
            card.setPadding(30, 24, 30, 24);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 14);
            card.setLayoutParams(lp);

            TextView author = new TextView(this);
            author.setText("👤 " + (n.author != null ? n.author : getString(R.string.common_unknown)));
            author.setTextColor(Color.parseColor("#D4AF37"));
            author.setTextSize(14);
            author.setTypeface(null, Typeface.BOLD);
            card.addView(author);

            TextView content = new TextView(this);
            content.setText(n.content);
            content.setTextColor(Color.WHITE);
            content.setTextSize(15);
            content.setLineSpacing(6, 1);
            content.setPadding(0, 10, 0, 10);
            card.addView(content);

            TextView date = new TextView(this);
            String d = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date(n.timestamp));
            date.setText(d);
            date.setTextColor(Color.parseColor("#616161"));
            date.setTextSize(10);
            card.addView(date);

            listContainer.addView(card);
        }
    }

    private void showPostDialog() {
        final Citizen me = im.getCitizen();
        if (me == null) return;

        // تحقق من الحالة
        fm.checkMyStatus(me.nationalId, new FirebaseManager.StatusListener() {
            @Override public void onStatus(boolean blocked, boolean muted, long until) {
                if (blocked) {
                    runOnUiThread(() -> Toast.makeText(NewsActivity.this,
                            "🚫 أنت محظور من النشر", Toast.LENGTH_LONG).show());
                    return;
                }
                if (muted && until > System.currentTimeMillis()) {
                    runOnUiThread(() -> Toast.makeText(NewsActivity.this,
                            getString(R.string.news_muted), Toast.LENGTH_LONG).show());
                    return;
                }
                runOnUiThread(() -> showPostDialogReal(me));
            }
            @Override public void onError(String msg) {
                runOnUiThread(() -> showPostDialogReal(me));
            }
        });
    }

    private void showPostDialogReal(final Citizen me) {

        final EditText input = new EditText(this);
        input.setHint(getString(R.string.news_write));
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.GRAY);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setMinLines(4);

        LinearLayout c = new LinearLayout(this);
        c.setPadding(40, 20, 40, 20);
        c.addView(input);

        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.news_new_post))
            .setView(c)
            .setPositiveButton("نشر", (d, w) -> {
                String text = input.getText().toString().trim();
                if (text.isEmpty()) return;
                fm.postNews(me.name, text, new FirebaseManager.OnDone() {
                    @Override public void onSuccess() {
                        Toast.makeText(NewsActivity.this, getString(R.string.news_published), Toast.LENGTH_SHORT).show();
                    }
                    @Override public void onError(String msg) {
                        Toast.makeText(NewsActivity.this, "خطأ: " + msg, Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }
}
