package com.ummah.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
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

public class ChatActivity extends Activity {
    private FirebaseManager fm;
    private IdentityManager im;
    private LinearLayout messagesContainer;
    private ScrollView scroll;
    private EditText input;
    private Button sendBtn;
    private ListenerRegistration reg;
    private Citizen me;
    private boolean isBlocked = false;
    private boolean isMuted = false;
    private long mutedUntil = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        fm = FirebaseManager.get();
        im = new IdentityManager(this);
        me = im.getCitizen();
        if (me == null) { finish(); return; }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0A0A0A"));

        TextView title = new TextView(this);
        title.setText("💬 دردشة أُمّة العامة");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 40, 0, 10);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("كل مواطني العالم هنا");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(11);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 20);
        root.addView(sub);

        scroll = new ScrollView(this);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        messagesContainer = new LinearLayout(this);
        messagesContainer.setOrientation(LinearLayout.VERTICAL);
        messagesContainer.setPadding(20, 10, 20, 10);
        scroll.addView(messagesContainer);
        root.addView(scroll);

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setPadding(16, 10, 16, 16);
        bar.setBackgroundColor(Color.parseColor("#141414"));

        input = new EditText(this);
        input.setHint("اكتب رسالة...");
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.GRAY);
        input.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        bar.addView(input);

        sendBtn = new Button(this);
        sendBtn.setText("إرسال");
        sendBtn.setTextSize(14);
        sendBtn.setOnClickListener(v -> sendMessage());
        bar.addView(sendBtn);

        root.addView(bar);
        setContentView(root);

        startListener();
        checkStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) reg.remove();
    }

    private void checkStatus() {
        fm.checkMyStatus(me.nationalId, new FirebaseManager.StatusListener() {
            @Override public void onStatus(boolean blocked, boolean muted, long until) {
                isBlocked = blocked;
                isMuted = muted && until > System.currentTimeMillis();
                mutedUntil = until;
                runOnUiThread(() -> updateInputState());
            }
            @Override public void onError(String msg) {}
        });
    }

    private void updateInputState() {
        if (isBlocked) {
            input.setEnabled(false);
            input.setHint("🚫 أنت محظور من الإرسال");
            sendBtn.setEnabled(false);
        } else if (isMuted) {
            long remaining = mutedUntil - System.currentTimeMillis();
            long minutes = remaining / 60000;
            input.setEnabled(false);
            input.setHint("🔇 أنت مكتوم — " + minutes + " دقيقة");
            sendBtn.setEnabled(false);
        } else {
            input.setEnabled(true);
            input.setHint("اكتب رسالة...");
            sendBtn.setEnabled(true);
        }
    }

    private void sendMessage() {
        if (isBlocked) {
            Toast.makeText(this, "🚫 أنت محظور من الإرسال", Toast.LENGTH_LONG).show();
            return;
        }
        if (isMuted && mutedUntil > System.currentTimeMillis()) {
            Toast.makeText(this, "🔇 أنت مكتوم مؤقتاً", Toast.LENGTH_LONG).show();
            return;
        }
        String text = input.getText().toString().trim();
        if (text.isEmpty()) return;
        input.setText("");
        fm.sendGlobalMessage(me.name, me.nationalId, text, new FirebaseManager.OnDone() {
            @Override public void onSuccess() {}
            @Override public void onError(String msg) {
                Toast.makeText(ChatActivity.this, "خطأ: " + msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startListener() {
        reg = fm.listenGlobalChat(list -> runOnUiThread(() -> render(list)));
    }

    private void render(List<FirebaseManager.ChatMessage> list) {
        messagesContainer.removeAllViews();
        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("لا توجد رسائل بعد. كن أول من يتكلم!");
            empty.setTextColor(Color.parseColor("#616161"));
            empty.setTextSize(13);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 40, 0, 0);
            messagesContainer.addView(empty);
            return;
        }
        for (FirebaseManager.ChatMessage m : list) {
            messagesContainer.addView(buildBubble(m));
        }
        scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
    }

    private View buildBubble(final FirebaseManager.ChatMessage m) {
        boolean mine = me.nationalId.equals(m.nationalId);

        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.HORIZONTAL);
        wrapper.setGravity(mine ? Gravity.END : Gravity.START);
        LinearLayout.LayoutParams wlp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        wlp.setMargins(0, 6, 0, 6);
        wrapper.setLayoutParams(wlp);

        // الصورة (لرسائل الآخرين)
        android.widget.ImageView avatar = null;
        if (!mine) {
            avatar = new android.widget.ImageView(this);
            LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(80, 80);
            alp.setMargins(0, 0, 12, 0);
            avatar.setLayoutParams(alp);
            avatar.setBackgroundColor(Color.parseColor("#1E1E1E"));
            avatar.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);

            if (m.photoUrl != null && !m.photoUrl.isEmpty()) {
                com.bumptech.glide.Glide.with(this)
                    .load(m.photoUrl)
                    .placeholder(android.R.drawable.ic_menu_myplaces)
                    .circleCrop()
                    .into(avatar);
            } else {
                avatar.setImageResource(android.R.drawable.ic_menu_myplaces);
            }
            wrapper.addView(avatar);
        }

        LinearLayout bubble = new LinearLayout(this);
        bubble.setOrientation(LinearLayout.VERTICAL);
        bubble.setBackgroundColor(mine ? Color.parseColor("#0B4F2C") : Color.parseColor("#1E1E1E"));
        bubble.setPadding(24, 16, 24, 16);

        TextView author = new TextView(this);
        author.setText(m.author != null ? m.author : "مجهول");
        author.setTextColor(Color.parseColor("#D4AF37"));
        author.setTextSize(11);
        author.setTypeface(null, Typeface.BOLD);
        bubble.addView(author);

        TextView text = new TextView(this);
        text.setText(m.text);
        text.setTextColor(Color.WHITE);
        text.setTextSize(15);
        text.setPadding(0, 6, 0, 4);
        bubble.addView(text);

        LinearLayout bottomRow = new LinearLayout(this);
        bottomRow.setOrientation(LinearLayout.HORIZONTAL);
        bottomRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView date = new TextView(this);
        date.setText(new SimpleDateFormat("HH:mm", Locale.US).format(new Date(m.timestamp)));
        date.setTextColor(Color.parseColor("#757575"));
        date.setTextSize(9);
        bottomRow.addView(date);

        if (!mine && m.nationalId != null) {
            TextView reportBtn = new TextView(this);
            reportBtn.setText("  🚩");
            reportBtn.setTextSize(12);
            reportBtn.setPadding(12, 0, 0, 0);
            reportBtn.setOnClickListener(v -> reportMessage(m));
            bottomRow.addView(reportBtn);
        }

        bubble.addView(bottomRow);
        wrapper.addView(bubble);
        return wrapper;
    }

    private void reportMessage(final FirebaseManager.ChatMessage m) {
        new AlertDialog.Builder(this)
            .setTitle("🚩 إبلاغ عن الرسالة")
            .setMessage("هل تريد الإبلاغ عن هذه الرسالة؟\n\n\"" + m.text + "\"\n\nمن: " + m.author)
            .setPositiveButton("إبلاغ", (d, w) -> {
                fm.reportMessage(me.nationalId, me.name, m.nationalId, m.author,
                        m.id, m.text, new FirebaseManager.OnDone() {
                    @Override public void onSuccess() {
                        Toast.makeText(ChatActivity.this,
                            "✅ تم الإبلاغ — شكراً لك", Toast.LENGTH_LONG).show();
                    }
                    @Override public void onError(String msg) {
                        Toast.makeText(ChatActivity.this, "خطأ: " + msg, Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }
}
