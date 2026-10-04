package com.ummah.app.heist;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ummah.app.LocaleHelper;
import com.ummah.app.R;

/**
 * HeistPlanningActivity — مرحلة التخطيط
 * الفريق يتناقش ويحدد المهام
 */
public class HeistPlanningActivity extends Activity {

    private String gameId;
    private HeistRole myRole;

    private LinearLayout root;
    private LinearLayout chatContainer;
    private LinearLayout missionsContainer;
    private EditText chatInput;
    private ScrollView chatScroll;

    // المهام
    private String[] missions = {
        "🚪 دخول المبنى",
        "📷 تعطيل الكاميرات",
        "🔐 فتح الخزنة",
        "💰 جمع المال",
        "🚗 تأمين الهروب"
    };

    private boolean[] missionsAssigned = new boolean[5];

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        gameId = getIntent().getStringExtra("game_id");
        myRole = HeistRole.fromId(getIntent().getStringExtra("my_role"));

        buildUI();
        addSystemMessage("📋 مرحلة التخطيط — 3 دقائق");
        addSystemMessage("💡 ناقشوا مع الفريق وحددوا المهام");
    }

    private void buildUI() {
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setBackgroundResource(R.drawable.bg_heist);

        // ═══ Top Bar ═══
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setBackgroundColor(Color.parseColor("#0a0510"));
        topBar.setPadding(dp(20), dp(50), dp(20), dp(14));

        TextView phaseIcon = new TextView(this);
        phaseIcon.setText("📋");
        phaseIcon.setTextSize(22);
        phaseIcon.setPadding(0, 0, dp(10), 0);
        topBar.addView(phaseIcon);

        TextView title = new TextView(this);
        title.setText("مرحلة التخطيط");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        topBar.addView(title);

        TextView timer = new TextView(this);
        timer.setText("⏰ 03:00");
        timer.setTextColor(Color.parseColor("#F59E0B"));
        timer.setTextSize(16);
        timer.setTypeface(null, Typeface.BOLD);
        topBar.addView(timer);

        main.addView(topBar);

        // ═══ My Role ═══
        LinearLayout roleBar = new LinearLayout(this);
        roleBar.setOrientation(LinearLayout.HORIZONTAL);
        roleBar.setGravity(Gravity.CENTER_VERTICAL);
        roleBar.setBackgroundColor(Color.parseColor("#15081a"));
        roleBar.setPadding(dp(16), dp(12), dp(16), dp(12));

        TextView roleEmoji = new TextView(this);
        roleEmoji.setText(myRole.emoji);
        roleEmoji.setTextSize(24);
        roleEmoji.setPadding(0, 0, dp(10), 0);
        roleBar.addView(roleEmoji);

        TextView roleName = new TextView(this);
        roleName.setText("دورك: " + myRole.nameAr);
        roleName.setTextColor(Color.parseColor(myRole.colorHex));
        roleName.setTextSize(14);
        roleName.setTypeface(null, Typeface.BOLD);
        roleBar.addView(roleName);

        main.addView(roleBar);

        // ═══ Scroll Content ═══
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout.LayoutParams scLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        scroll.setLayoutParams(scLp);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(16), dp(16), dp(16));

        // ═══ Missions ═══
        TextView mTitle = new TextView(this);
        mTitle.setText("📌 المهام المطلوبة");
        mTitle.setTextColor(Color.parseColor("#D4AF37"));
        mTitle.setTextSize(15);
        mTitle.setTypeface(null, Typeface.BOLD);
        mTitle.setPadding(0, 0, 0, dp(12));
        content.addView(mTitle);

        missionsContainer = new LinearLayout(this);
        missionsContainer.setOrientation(LinearLayout.VERTICAL);
        content.addView(missionsContainer);
        renderMissions();

        // ═══ Chat ═══
        TextView cTitle = new TextView(this);
        cTitle.setText("💬 شات الفريق");
        cTitle.setTextColor(Color.parseColor("#D4AF37"));
        cTitle.setTextSize(15);
        cTitle.setTypeface(null, Typeface.BOLD);
        cTitle.setPadding(0, dp(24), 0, dp(12));
        content.addView(cTitle);

        chatScroll = new ScrollView(this);
        chatScroll.setBackgroundResource(R.drawable.bg_heist_card);
        chatScroll.setPadding(dp(12), dp(12), dp(12), dp(12));
        LinearLayout.LayoutParams csLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(220));
        chatScroll.setLayoutParams(csLp);

        chatContainer = new LinearLayout(this);
        chatContainer.setOrientation(LinearLayout.VERTICAL);
        chatScroll.addView(chatContainer);
        content.addView(chatScroll);

        scroll.addView(content);
        main.addView(scroll);

        // ═══ Chat Input ═══
        LinearLayout inputBar = new LinearLayout(this);
        inputBar.setOrientation(LinearLayout.HORIZONTAL);
        inputBar.setGravity(Gravity.CENTER_VERTICAL);
        inputBar.setBackgroundColor(Color.parseColor("#0a0510"));
        inputBar.setPadding(dp(12), dp(10), dp(12), dp(10));

        chatInput = new EditText(this);
        chatInput.setHint("اكتب رسالة...");
        chatInput.setHintTextColor(Color.parseColor("#666666"));
        chatInput.setTextColor(Color.WHITE);
        chatInput.setTextSize(14);
        chatInput.setBackgroundResource(R.drawable.bg_heist_card);
        chatInput.setPadding(dp(14), dp(10), dp(14), dp(10));
        LinearLayout.LayoutParams ciLp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        ciLp.setMargins(0, 0, dp(8), 0);
        chatInput.setLayoutParams(ciLp);
        inputBar.addView(chatInput);

        TextView sendBtn = new TextView(this);
        sendBtn.setText("➤");
        sendBtn.setTextColor(Color.parseColor("#0a0510"));
        sendBtn.setTextSize(22);
        sendBtn.setGravity(Gravity.CENTER);
        sendBtn.setBackgroundResource(R.drawable.bg_heist_btn_gold);
        sendBtn.setPadding(dp(14), dp(6), dp(14), dp(6));
        sendBtn.setOnClickListener(v -> sendMessage());
        inputBar.addView(sendBtn);

        main.addView(inputBar);

        // ═══ Bottom: Start Heist ═══
        Button startBtn = new Button(this);
        startBtn.setText("▶️ ابدأ السرقة");
        startBtn.setTextSize(16);
        startBtn.setTextColor(Color.parseColor("#0a0510"));
        startBtn.setAllCaps(false);
        startBtn.setTypeface(null, Typeface.BOLD);
        startBtn.setBackgroundResource(R.drawable.bg_heist_btn_gold);
        LinearLayout.LayoutParams sblp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        sblp.setMargins(dp(16), dp(8), dp(16), dp(16));
        startBtn.setLayoutParams(sblp);
        startBtn.setOnClickListener(v -> startHeist());
        main.addView(startBtn);

        setContentView(main);
    }

    private void renderMissions() {
        missionsContainer.removeAllViews();

        for (int i = 0; i < missions.length; i++) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setBackgroundResource(R.drawable.bg_heist_card);
            card.setPadding(dp(16), dp(14), dp(16), dp(14));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, dp(8));
            card.setLayoutParams(lp);

            TextView num = new TextView(this);
            num.setText(String.valueOf(i + 1));
            num.setTextColor(Color.parseColor("#D4AF37"));
            num.setTextSize(18);
            num.setTypeface(null, Typeface.BOLD);
            num.setPadding(0, 0, dp(16), 0);
            card.addView(num);

            TextView mission = new TextView(this);
            mission.setText(missions[i]);
            mission.setTextColor(Color.WHITE);
            mission.setTextSize(14);
            mission.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            card.addView(mission);

            TextView status = new TextView(this);
            if (missionsAssigned[i]) {
                status.setText("✅");
                status.setTextSize(18);
            } else {
                status.setText("○");
                status.setTextSize(18);
                status.setTextColor(Color.parseColor("#666666"));
            }
            card.addView(status);

            final int idx = i;
            if (!missionsAssigned[i]) {
                card.setOnClickListener(v -> assignMission(idx));
                card.setClickable(true);
            }

            missionsContainer.addView(card);
        }
    }

    private void assignMission(int idx) {
        // نتحققو إذا الدور يقدر ياخذ المهمة
        boolean canDo = false;
        switch (idx) {
            case 0: // دخول
                canDo = true;
                break;
            case 1: // كاميرات
                canDo = myRole == HeistRole.HACKER || myRole == HeistRole.LEADER;
                break;
            case 2: // خزنة
                canDo = myRole == HeistRole.DEMOLITION || myRole == HeistRole.HACKER || myRole == HeistRole.LEADER;
                break;
            case 3: // مال
                canDo = true;
                break;
            case 4: // هروب
                canDo = myRole == HeistRole.DRIVER || myRole == HeistRole.LEADER;
                break;
        }

        if (!canDo) {
            Toast.makeText(this, "⚠️ دورك ما يقدر ياخذ هذي المهمة", Toast.LENGTH_SHORT).show();
            return;
        }

        missionsAssigned[idx] = true;
        renderMissions();
        addSystemMessage("📌 تم إسناد المهمة: " + missions[idx]);
    }

    private void sendMessage() {
        String text = chatInput.getText().toString().trim();
        if (text.isEmpty()) return;
        chatInput.setText("");
        addChatMessage(text, true);
    }

    private void addChatMessage(String text, boolean isMe) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(4), 0, dp(4));

        LinearLayout bubble = new LinearLayout(this);
        bubble.setOrientation(LinearLayout.VERTICAL);
        bubble.setBackgroundResource(R.drawable.bg_heist_card);
        bubble.setPadding(dp(12), dp(8), dp(12), dp(8));

        TextView name = new TextView(this);
        name.setText(isMe ? "أنت (" + myRole.emoji + ")" : "زميل");
        name.setTextColor(Color.parseColor(myRole.colorHex));
        name.setTextSize(10);
        name.setTypeface(null, Typeface.BOLD);
        bubble.addView(name);

        TextView msg = new TextView(this);
        msg.setText(text);
        msg.setTextColor(Color.WHITE);
        msg.setTextSize(13);
        msg.setPadding(0, dp(2), 0, 0);
        bubble.addView(msg);

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
        chatScroll.post(() -> chatScroll.fullScroll(View.FOCUS_DOWN));
    }

    private void addSystemMessage(String text) {
        TextView sys = new TextView(this);
        sys.setText("🔹 " + text);
        sys.setTextColor(Color.parseColor("#D4AF37"));
        sys.setTextSize(11);
        sys.setGravity(Gravity.CENTER);
        sys.setPadding(dp(8), dp(6), dp(8), dp(6));
        sys.setBackgroundColor(Color.parseColor("#30D4AF37"));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        lp.setMargins(0, dp(4), 0, dp(4));
        sys.setLayoutParams(lp);

        chatContainer.addView(sys);
        chatScroll.post(() -> chatScroll.fullScroll(View.FOCUS_DOWN));
    }

    private void startHeist() {
        // نتحققو إذا المهام موزعة
        int assigned = 0;
        for (boolean b : missionsAssigned) if (b) assigned++;

        if (assigned < 3) {
            Toast.makeText(this, "⚠️ وزّعوا 3 مهام على الأقل", Toast.LENGTH_LONG).show();
            return;
        }

        // ننتقل للمحرك
        Intent intent = new Intent(this, HeistGameActivity.class);
        intent.putExtra("game_id", gameId);
        intent.putExtra("my_role", myRole.id);
        startActivity(intent);
        finish();
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }
}
