package com.ummah.app;

import android.app.Activity;
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

public class PrivateChatActivity extends Activity {
    private FirebaseManager fm;
    private IdentityManager im;
    private LinearLayout messagesContainer;
    private ScrollView scroll;
    private EditText input;
    private ListenerRegistration reg;
    private Citizen me;
    private String otherId;
    private String otherName;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        // Feature Check
        if (!FeatureFlags.checkOrToast(this, "private_chat")) {
            finish();
            return;
        }

        fm = FirebaseManager.get();
        im = new IdentityManager(this);
        me = im.getCitizen();
        if (me == null) { finish(); return; }

        otherId = getIntent().getStringExtra("other_id");
        otherName = getIntent().getStringExtra("other_name");
        if (otherId == null) { finish(); return; }
        if (otherName == null) otherName = getString(R.string.pchat_citizen);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0A0A0A"));

        TextView title = new TextView(this);
        title.setText(getString(R.string.pchat_title));
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(16);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 40, 0, 6);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText(getString(R.string.pchat_with) + otherName);
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
        input.setHint(getString(R.string.pchat_write));
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.GRAY);
        input.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        bar.addView(input);

        Button send = new Button(this);
        send.setText(getString(R.string.pchat_send));
        send.setTextSize(14);
        send.setOnClickListener(v -> sendMessage());
        bar.addView(send);

        root.addView(bar);
        setContentView(root);

        startListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) reg.remove();
    }

    private void sendMessage() {
        String text = input.getText().toString().trim();
        if (text.isEmpty()) return;
        input.setText("");
        fm.sendPrivateMessage(me.nationalId, me.name, otherId, text, new FirebaseManager.OnDone() {
            @Override public void onSuccess() {}
            @Override public void onError(String msg) {
                Toast.makeText(PrivateChatActivity.this, "خطأ: " + msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startListener() {
        reg = fm.listenPrivateChat(me.nationalId, otherId, list -> runOnUiThread(() -> render(list)));
    }

    private void render(List<FirebaseManager.ChatMessage> list) {
        messagesContainer.removeAllViews();
        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(R.string.pchat_empty_full);
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

    private View buildBubble(FirebaseManager.ChatMessage m) {
        boolean mine = me.nationalId.equals(m.nationalId);

        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setGravity(mine ? Gravity.END : Gravity.START);
        LinearLayout.LayoutParams wlp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        wlp.setMargins(0, 6, 0, 6);
        wrapper.setLayoutParams(wlp);

        LinearLayout bubble = new LinearLayout(this);
        bubble.setOrientation(LinearLayout.VERTICAL);
        bubble.setBackgroundColor(mine ? Color.parseColor("#0B4F2C") : Color.parseColor("#1E1E1E"));
        bubble.setPadding(24, 16, 24, 16);

        TextView text = new TextView(this);
        text.setText(m.text);
        text.setTextColor(Color.WHITE);
        text.setTextSize(15);
        bubble.addView(text);

        TextView date = new TextView(this);
        date.setText(new SimpleDateFormat("HH:mm", Locale.US).format(new Date(m.timestamp)));
        date.setTextColor(Color.parseColor("#757575"));
        date.setTextSize(9);
        date.setPadding(0, 6, 0, 0);
        bubble.addView(date);

        wrapper.addView(bubble);
        return wrapper;
    }
}
