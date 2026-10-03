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
import android.widget.ImageView;
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
        // Feature Check
        if (!FeatureFlags.checkOrToast(this, "chat")) {
            finish();
            return;
        }

        // FullscreenHelper.enable(this);  // DISABLED - crash
        getWindow().setSoftInputMode(
                android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        fm = FirebaseManager.get();
        im = new IdentityManager(this);
        me = im.getCitizen();
        if (me == null) { finish(); return; }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.bg_screen);

        // ═══ Header ═══
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setGravity(Gravity.CENTER);
        header.setPadding(20, 40, 20, 20);

        TextView title = new TextView(this);
        title.setText(R.string.chat_title);
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(22);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setLetterSpacing(0.05f);
        header.addView(title);

        TextView sub = new TextView(this);
        sub.setText(R.string.chat_subtitle);
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(11);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 6, 0, 0);
        header.addView(sub);

        View line = new View(this);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        llp.setMargins(60, 16, 60, 0);
        line.setLayoutParams(llp);
        line.setBackgroundColor(Color.parseColor("#2A3D32"));
        header.addView(line);

        root.addView(header);

        // ═══ منطقة الرسائل ═══
        scroll = new ScrollView(this);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        messagesContainer = new LinearLayout(this);
        messagesContainer.setOrientation(LinearLayout.VERTICAL);
        messagesContainer.setPadding(20, 10, 20, 10);
        scroll.addView(messagesContainer);
        root.addView(scroll);

        // ═══ شريط الإدخال ═══
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(16, 14, 16, 20);
        bar.setBackgroundColor(Color.parseColor("#0F1A14"));

        input = new EditText(this);
        input.setHint("اكتب رسالة...");
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.parseColor("#666666"));
        input.setTextSize(15);
        input.setBackgroundResource(R.drawable.bg_chat_input);
        input.setPadding(40, 28, 40, 28);
        input.setMinHeight(110);
        LinearLayout.LayoutParams inputLp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        inputLp.setMargins(0, 0, 12, 0);
        input.setLayoutParams(inputLp);
        bar.addView(input);

        sendBtn = new Button(this);
        sendBtn.setText("➤");
        sendBtn.setTextSize(22);
        sendBtn.setTextColor(Color.parseColor("#0A0A0A"));
        sendBtn.setTypeface(null, Typeface.BOLD);
        sendBtn.setBackgroundResource(R.drawable.bg_send_btn);
        sendBtn.setPadding(0, 0, 0, 0);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(120, 120);
        sendBtn.setLayoutParams(slp);
        sendBtn.setElevation(10f);
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
            sendBtn.setAlpha(0.4f);
        } else if (isMuted) {
            long remaining = mutedUntil - System.currentTimeMillis();
            long minutes = remaining / 60000;
            input.setEnabled(false);
            input.setHint(getString(R.string.chat_muted) + minutes + getString(R.string.chat_minutes));
            sendBtn.setEnabled(false);
            sendBtn.setAlpha(0.4f);
        } else {
            input.setEnabled(true);
            input.setHint("اكتب رسالة...");
            sendBtn.setEnabled(true);
            sendBtn.setAlpha(1f);
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
                Toast.makeText(ChatActivity.this, getString(R.string.chat_error) + msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startListener() {
        reg = fm.listenGlobalChat(list -> runOnUiThread(() -> render(list)));
    }

    private void render(List<FirebaseManager.ChatMessage> list) {
        messagesContainer.removeAllViews();
        if (list.isEmpty()) {
            LinearLayout emptyBox = new LinearLayout(this);
            emptyBox.setOrientation(LinearLayout.VERTICAL);
            emptyBox.setGravity(Gravity.CENTER);
            emptyBox.setPadding(0, 80, 0, 0);

            TextView icon = new TextView(this);
            icon.setText("💬");
            icon.setTextSize(60);
            icon.setGravity(Gravity.CENTER);
            emptyBox.addView(icon);

            TextView empty = new TextView(this);
            empty.setText(R.string.chat_empty);
            empty.setTextColor(Color.parseColor("#D4AF37"));
            empty.setTextSize(16);
            empty.setTypeface(null, Typeface.BOLD);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 20, 0, 6);
            emptyBox.addView(empty);

            TextView hint = new TextView(this);
            hint.setText(R.string.chat_first_hint);
            hint.setTextColor(Color.parseColor("#9E9E9E"));
            hint.setTextSize(13);
            hint.setGravity(Gravity.CENTER);
            emptyBox.addView(hint);

            messagesContainer.addView(emptyBox);
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
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        wlp.setMargins(0, 6, 0, 6);
        wrapper.setLayoutParams(wlp);

        // Avatar للأخرين
        if (!mine) {
            ImageView avatar = new ImageView(this);
            LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(90, 90);
            alp.setMargins(0, 0, 12, 0);
            avatar.setLayoutParams(alp);
            avatar.setBackgroundColor(Color.parseColor("#1E1E1E"));
            avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);

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
        bubble.setBackgroundResource(mine ? R.drawable.bg_bubble_mine : R.drawable.bg_bubble_other);
        bubble.setPadding(30, 20, 30, 18);
        bubble.setElevation(4f);

        TextView author = new TextView(this);
        author.setText(m.author != null ? m.author : "مجهول");
        author.setTextColor(Color.parseColor(mine ? "#5D4037" : "#D4AF37"));
        author.setTextSize(11);
        author.setTypeface(null, Typeface.BOLD);
        bubble.addView(author);

        TextView text = new TextView(this);
        text.setText(m.text);
        text.setTextColor(Color.parseColor(mine ? "#0A0A0A" : "#FFFFFF"));
        text.setTextSize(15);
        text.setPadding(0, 6, 0, 6);
        text.setLineSpacing(3, 1);
        bubble.addView(text);

        LinearLayout bottomRow = new LinearLayout(this);
        bottomRow.setOrientation(LinearLayout.HORIZONTAL);
        bottomRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView date = new TextView(this);
        date.setText(new SimpleDateFormat("HH:mm", Locale.US).format(new Date(m.timestamp)));
        date.setTextColor(Color.parseColor(mine ? "#7A6520" : "#757575"));
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
            .setMessage(getString(R.string.chat_report_title) + "\n\n\"" + m.text + "\"\n\n" + getString(R.string.chat_from) + m.author)
            .setPositiveButton("إبلاغ", (d, w) -> {
                fm.reportMessage(me.nationalId, me.name, m.nationalId, m.author,
                        m.id, m.text, new FirebaseManager.OnDone() {
                    @Override public void onSuccess() {
                        Toast.makeText(ChatActivity.this,
                            "✅ تم الإبلاغ — شكراً لك", Toast.LENGTH_LONG).show();
                    }
                    @Override public void onError(String msg) {
                        Toast.makeText(ChatActivity.this, getString(R.string.chat_error) + msg, Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // FullscreenHelper.enable(this);  // DISABLED - crash
    }

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

}
