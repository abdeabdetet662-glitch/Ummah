package com.ummah.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * SupportChatActivity — شات مع الدعم
 */
public class SupportChatActivity extends Activity {

    private IdentityManager im;
    private Citizen me;
    private SupportManager manager;

    private String ticketId;
    private boolean isAdmin = false;

    private LinearLayout chatContainer;
    private EditText input;
    private ScrollView scroll;

    private ListenerRegistration msgsReg;

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        im = new IdentityManager(this);
        me = im.getCitizen();
        manager = new SupportManager();

        ticketId = getIntent().getStringExtra("ticket_id");
        isAdmin = getIntent().getBooleanExtra("is_admin", false);

        if (ticketId == null) { finish(); return; }

        buildUI();
        loadMessages();
    }

    private void buildUI() {
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setBackgroundResource(R.drawable.bg_screen);

        // Top
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setBackgroundColor(Color.parseColor("#0a0510"));
        top.setPadding(dp(16), dp(50), dp(16), dp(14));

        TextView back = new TextView(this);
        back.setText("←");
        back.setTextColor(Color.parseColor("#D4AF37"));
        back.setTextSize(24);
        back.setPadding(0, 0, dp(16), 0);
        back.setOnClickListener(v -> finish());
        top.addView(back);

        TextView title = new TextView(this);
        title.setText("🏛️ الديوان");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(title);

        main.addView(top);

        // Scroll
        scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout.LayoutParams sl = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        scroll.setLayoutParams(sl);

        chatContainer = new LinearLayout(this);
        chatContainer.setOrientation(LinearLayout.VERTICAL);
        chatContainer.setPadding(dp(16), dp(16), dp(16), dp(16));
        scroll.addView(chatContainer);
        main.addView(scroll);

        // Input
        LinearLayout inputBar = new LinearLayout(this);
        inputBar.setOrientation(LinearLayout.HORIZONTAL);
        inputBar.setGravity(Gravity.CENTER_VERTICAL);
        inputBar.setBackgroundColor(Color.parseColor("#0a0510"));
        inputBar.setPadding(dp(12), dp(10), dp(12), dp(10));

        input = new EditText(this);
        input.setHint("اكتب رسالة...");
        input.setHintTextColor(Color.parseColor("#666666"));
        input.setTextColor(Color.WHITE);
        input.setTextSize(14);
        input.setBackgroundResource(R.drawable.bg_input);
        input.setPadding(dp(14), dp(12), dp(14), dp(12));
        input.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        inputBar.addView(input);

        TextView send = new TextView(this);
        send.setText("➤");
        send.setTextColor(Color.parseColor("#0a0510"));
        send.setTextSize(22);
        send.setGravity(Gravity.CENTER);
        send.setBackgroundResource(R.drawable.bg_btn_gold);
        send.setPadding(dp(16), dp(8), dp(16), dp(8));
        LinearLayout.LayoutParams sendLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        sendLp.setMargins(dp(8), 0, 0, 0);
        send.setLayoutParams(sendLp);
        send.setOnClickListener(v -> sendMessage());
        inputBar.addView(send);

        main.addView(inputBar);
        setContentView(main);
    }

    private void loadMessages() {
        msgsReg = manager.listenMessages(ticketId, messages -> {
            runOnUiThread(() -> renderMessages(messages));
        });
    }

    private void renderMessages(List<SupportMessage> list) {
        chatContainer.removeAllViews();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);

        for (SupportMessage m : list) {
            boolean isMe;
            if (isAdmin) {
                isMe = "admin".equals(m.senderType);
            } else {
                isMe = "user".equals(m.senderType);
            }

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, dp(6), 0, dp(6));

            LinearLayout bubble = new LinearLayout(this);
            bubble.setOrientation(LinearLayout.VERTICAL);
            bubble.setBackgroundResource(R.drawable.bg_card);
            bubble.setPadding(dp(14), dp(10), dp(14), dp(10));
            android.view.ViewGroup.LayoutParams bubbleLp = bubble.getLayoutParams();
                        if (bubbleLp == null) {
                            bubbleLp = new LinearLayout.LayoutParams(
                                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                        }
                        bubbleLp.width = dp(240);
                        bubble.setLayoutParams(bubbleLp);

            // Name
            TextView name = new TextView(this);
            if ("admin".equals(m.senderType)) {
                name.setText("🏛️ ديوان أُمّة");
                name.setTextColor(Color.parseColor("#D4AF37"));
            } else if ("bot".equals(m.senderType)) {
                name.setText("\ud83e\udd16 \u0627\u0644\u0645\u0633\u0627\u0639\u062f \u0627\u0644\u0622\u0644\u064a");
                name.setTextColor(Color.parseColor("#9333EA"));
            } else {
                name.setText("👤 " + (m.senderName != null ? m.senderName : "مواطن"));
                name.setTextColor(Color.parseColor("#3B82F6"));
            }
            name.setTextSize(10);
            name.setTypeface(null, Typeface.BOLD);
            bubble.addView(name);

            // Text
            TextView text = new TextView(this);
            text.setText(m.text);
            text.setTextColor(Color.WHITE);
            text.setTextSize(13);
            text.setPadding(0, dp(4), 0, 0);
            text.setLineSpacing(0, 1.3f);
            bubble.addView(text);

            // Time
            TextView time = new TextView(this);
            time.setText(sdf.format(new Date(m.createdAt)));
            time.setTextColor(Color.parseColor("#666666"));
            time.setTextSize(9);
            time.setPadding(0, dp(2), 0, 0);
            bubble.addView(time);

            View spacer = new View(this);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 0.3f));

            if (isMe) {
                row.addView(spacer);
                row.addView(bubble);
            } else {
                row.addView(bubble);
                row.addView(spacer);
            }

            chatContainer.addView(row);
        }

        scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
    }

    private void sendMessage() {
        final String text = input.getText().toString().trim();
        if (text.isEmpty()) return;
        input.setText("");

        String senderId = isAdmin ? "admin" : (me != null ? me.nationalId : "unknown");
        String senderName = isAdmin ? "ديوان أُمّة" : (me != null ? me.name : "مواطن");
        String senderType = isAdmin ? "admin" : "user";

        manager.sendMessage(ticketId, senderId, senderName, senderType, text,
            new SupportManager.SimpleCallback() {
                @Override public void onSuccess(String t) {
                    if (!isAdmin) {
                        triggerBotIfNeeded(text);
                    }
                }
                @Override public void onError(String e) {}
            });
    }

    private void triggerBotIfNeeded(final String userText) {
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            manager.hasAdminReplied(ticketId, adminReplied -> {
                if (adminReplied) return;
                manager.hasBotReplied(ticketId, botReplied -> {
                    String reply;
                    if (!botReplied) {
                        String userName = me != null ? me.name : "";
                        reply = SupportBot.getGreeting(userName)
                              + "\n\n" + SupportBot.getResponse(userText);
                    } else {
                        reply = SupportBot.getResponse(userText);
                    }
                    manager.sendMessage(ticketId, SupportBot.BOT_ID,
                                       SupportBot.BOT_NAME, SupportBot.BOT_TYPE,
                                       reply,
                        new SupportManager.SimpleCallback() {
                            @Override public void onSuccess(String t) {}
                            @Override public void onError(String e) {}
                        });
                });
            });
        }, 1500);
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }

    @Override
    protected void onDestroy() {
        if (msgsReg != null) msgsReg.remove();
        super.onDestroy();
    }
}
