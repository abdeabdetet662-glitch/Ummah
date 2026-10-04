package com.ummah.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MurderMysteryGameActivity — شاشة التحقيق
 * فيها:
 *  - قصة الجريمة (Header)
 *  - الأدلة (تظهر تدريجياً)
 *  - الشات بين المحققين
 *  - زر التصويت
 */
public class MurderMysteryGameActivity extends Activity {

    private FirebaseFirestore db;
    private IdentityManager im;
    private String gameId;
    private MurderMystery game;
    private Citizen me;

    private LinearLayout cluesContainer;
    private LinearLayout chatContainer;
    private EditText chatInput;
    private TextView timerView;
    private TextView statusView;

    private ListenerRegistration cluesReg;
    private ListenerRegistration chatReg;

    private Handler handler = new Handler();

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        db = FirebaseFirestore.getInstance();
        im = new IdentityManager(this);
        me = im.getCitizen();
        gameId = getIntent().getStringExtra("gameId");

        if (gameId == null || me == null) {
            Toast.makeText(this, "خطأ في التحميل", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        buildUI();
        loadGame();
    }

    private void buildUI() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.bg_mystery_noir);
        root.setPadding(0, 0, 0, 0);

        // ═══ Top Bar ═══
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setBackgroundColor(Color.parseColor("#0a0510"));
        topBar.setPadding(dp(20), dp(50), dp(20), dp(14));

        TextView back = new TextView(this);
        back.setText("←");
        back.setTextColor(Color.parseColor("#D4AF37"));
        back.setTextSize(24);
        back.setPadding(0, 0, dp(20), 0);
        back.setOnClickListener(v -> finish());
        topBar.addView(back);

        TextView title = new TextView(this);
        title.setText("🔍 التحقيق");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        topBar.addView(title);

        timerView = new TextView(this);
        timerView.setText("--:--:--");
        timerView.setTextColor(Color.parseColor("#F59E0B"));
        timerView.setTextSize(14);
        timerView.setTypeface(null, Typeface.BOLD);
        topBar.addView(timerView);

        root.addView(topBar);

        // ═══ Content Scroll ═══
        ScrollView contentScroll = new ScrollView(this);
        contentScroll.setFillViewport(true);
        LinearLayout.LayoutParams csLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        contentScroll.setLayoutParams(csLp);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(20), dp(20), dp(20));

        // ═══ Story Card ═══
        LinearLayout storyCard = new LinearLayout(this);
        storyCard.setOrientation(LinearLayout.VERTICAL);
        storyCard.setBackgroundResource(R.drawable.bg_mystery_card);
        storyCard.setPadding(dp(24), dp(20), dp(24), dp(20));
        LinearLayout.LayoutParams scLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        scLp.setMargins(0, 0, 0, dp(16));
        storyCard.setLayoutParams(scLp);

        statusView = new TextView(this);
        statusView.setText("⏳ تحميل...");
        statusView.setTextColor(Color.WHITE);
        statusView.setTextSize(14);
        statusView.setLineSpacing(0, 1.5f);
        storyCard.addView(statusView);
        content.addView(storyCard);

        // ═══ Clues Section ═══
        LinearLayout cluesHeader = new LinearLayout(this);
        cluesHeader.setOrientation(LinearLayout.HORIZONTAL);
        cluesHeader.setGravity(Gravity.CENTER_VERTICAL);
        cluesHeader.setPadding(0, dp(16), 0, dp(12));

        View lineL = new View(this);
        LinearLayout.LayoutParams lLp = new LinearLayout.LayoutParams(0, dp(1), 1f);
        lineL.setLayoutParams(lLp);
        lineL.setBackgroundColor(Color.parseColor("#30D4AF37"));
        cluesHeader.addView(lineL);

        TextView chText = new TextView(this);
        chText.setText("   🔍 الأدلة   ");
        chText.setTextColor(Color.parseColor("#D4AF37"));
        chText.setTextSize(14);
        chText.setTypeface(null, Typeface.BOLD);
        cluesHeader.addView(chText);

        View lineR = new View(this);
        LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(0, dp(1), 1f);
        lineR.setLayoutParams(rLp);
        lineR.setBackgroundColor(Color.parseColor("#30D4AF37"));
        cluesHeader.addView(lineR);

        content.addView(cluesHeader);

        cluesContainer = new LinearLayout(this);
        cluesContainer.setOrientation(LinearLayout.VERTICAL);
        content.addView(cluesContainer);
        
        // ═══ قسم المشتبهين ═══
        LinearLayout suspectsHeader = new LinearLayout(this);
        suspectsHeader.setOrientation(LinearLayout.HORIZONTAL);
        suspectsHeader.setGravity(Gravity.CENTER_VERTICAL);
        suspectsHeader.setPadding(0, dp(24), 0, dp(12));
        
        View sLineL = new View(this);
        sLineL.setLayoutParams(new LinearLayout.LayoutParams(0, dp(1), 1f));
        sLineL.setBackgroundColor(Color.parseColor("#30D4AF37"));
        suspectsHeader.addView(sLineL);
        
        TextView sText = new TextView(this);
        sText.setText("   🎭 المشتبهون (اضغط للاستجواب)   ");
        sText.setTextColor(Color.parseColor("#D4AF37"));
        sText.setTextSize(13);
        sText.setTypeface(null, Typeface.BOLD);
        suspectsHeader.addView(sText);
        
        View sLineR = new View(this);
        sLineR.setLayoutParams(new LinearLayout.LayoutParams(0, dp(1), 1f));
        sLineR.setBackgroundColor(Color.parseColor("#30D4AF37"));
        suspectsHeader.addView(sLineR);
        
        content.addView(suspectsHeader);
        
        LinearLayout suspectsContainer = new LinearLayout(this);
        suspectsContainer.setOrientation(LinearLayout.VERTICAL);
        suspectsContainer.setId(View.generateViewId());
        content.addView(suspectsContainer);
        
        // نبنيو المشتبهين
        java.util.List<MMSuspect> suspectsList = MMSuspectsManager.buildSuspects("default");
        for (MMSuspect s : suspectsList) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setBackgroundResource(R.drawable.bg_mafia_player);
            card.setPadding(dp(16), dp(12), dp(16), dp(12));
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            clp.setMargins(0, 0, 0, dp(8));
            card.setLayoutParams(clp);
            
            TextView em = new TextView(this);
            em.setText(s.emoji);
            em.setTextSize(28);
            em.setPadding(0, 0, dp(12), 0);
            card.addView(em);
            
            LinearLayout sinfo = new LinearLayout(this);
            sinfo.setOrientation(LinearLayout.VERTICAL);
            sinfo.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            
            TextView sn = new TextView(this);
            sn.setText(s.name);
            sn.setTextColor(Color.WHITE);
            sn.setTextSize(14);
            sn.setTypeface(null, Typeface.BOLD);
            sinfo.addView(sn);
            
            TextView st = new TextView(this);
            st.setText(s.title);
            st.setTextColor(Color.parseColor("#9E9E9E"));
            st.setTextSize(11);
            sinfo.addView(st);
            
            card.addView(sinfo);
            
            TextView arrow = new TextView(this);
            arrow.setText("›");
            arrow.setTextColor(Color.parseColor("#D4AF37"));
            arrow.setTextSize(24);
            card.addView(arrow);
            
            final String sid = s.id;
            card.setOnClickListener(v -> {
                startActivity(new Intent(MurderMysteryGameActivity.this, MMInterrogateActivity.class)
                        .putExtra("suspectId", sid)
                        .putExtra("killerId", game != null ? game.killerId : null)
                        .putExtra("caseId", gameId));
            });
            
            suspectsContainer.addView(card);
        }
        

        // ═══ Chat Section ═══
        LinearLayout chatHeader = new LinearLayout(this);
        chatHeader.setOrientation(LinearLayout.HORIZONTAL);
        chatHeader.setGravity(Gravity.CENTER_VERTICAL);
        chatHeader.setPadding(0, dp(24), 0, dp(12));

        View cLineL = new View(this);
        cLineL.setLayoutParams(new LinearLayout.LayoutParams(0, dp(1), 1f));
        cLineL.setBackgroundColor(Color.parseColor("#30D4AF37"));
        chatHeader.addView(cLineL);

        TextView cText = new TextView(this);
        cText.setText("   💬 التحقيق   ");
        cText.setTextColor(Color.parseColor("#D4AF37"));
        cText.setTextSize(14);
        cText.setTypeface(null, Typeface.BOLD);
        chatHeader.addView(cText);

        View cLineR = new View(this);
        cLineR.setLayoutParams(new LinearLayout.LayoutParams(0, dp(1), 1f));
        cLineR.setBackgroundColor(Color.parseColor("#30D4AF37"));
        chatHeader.addView(cLineR);

        content.addView(chatHeader);

        chatContainer = new LinearLayout(this);
        chatContainer.setOrientation(LinearLayout.VERTICAL);
        content.addView(chatContainer);

        contentScroll.addView(content);
        root.addView(contentScroll);

        // ═══ Chat Input ═══
        LinearLayout inputBar = new LinearLayout(this);
        inputBar.setOrientation(LinearLayout.HORIZONTAL);
        inputBar.setGravity(Gravity.CENTER_VERTICAL);
        inputBar.setBackgroundColor(Color.parseColor("#0a0510"));
        inputBar.setPadding(dp(16), dp(12), dp(16), dp(12));

        chatInput = new EditText(this);
        chatInput.setHint("اكتب...");
        chatInput.setHintTextColor(Color.parseColor("#666666"));
        chatInput.setTextColor(Color.WHITE);
        chatInput.setTextSize(14);
        chatInput.setBackgroundColor(Color.parseColor("#1a0a15"));
        chatInput.setPadding(dp(16), dp(12), dp(16), dp(12));
        LinearLayout.LayoutParams ciLp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        ciLp.setMargins(0, 0, dp(10), 0);
        chatInput.setLayoutParams(ciLp);
        inputBar.addView(chatInput);

        TextView sendBtn = new TextView(this);
        sendBtn.setText("➤");
        sendBtn.setTextColor(Color.parseColor("#D4AF37"));
        sendBtn.setTextSize(22);
        sendBtn.setGravity(Gravity.CENTER);
        sendBtn.setBackgroundResource(R.drawable.bg_mystery_card);
        sendBtn.setPadding(dp(16), dp(8), dp(16), dp(8));
        sendBtn.setOnClickListener(v -> sendMessage());
        inputBar.addView(sendBtn);

        root.addView(inputBar);

        // ═══ Vote Button (Overlay) ═══
        Button voteBtn = new Button(this);
        voteBtn.setText("🗳️ صوّت الآن");
        voteBtn.setTextSize(14);
        voteBtn.setTextColor(Color.parseColor("#0A0510"));
        voteBtn.setAllCaps(false);
        voteBtn.setTypeface(null, Typeface.BOLD);
        voteBtn.setBackgroundResource(R.drawable.bg_btn_noir);
        FrameLayout.LayoutParams vbLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        vbLp.gravity = Gravity.BOTTOM;
        vbLp.setMargins(dp(16), 0, dp(16), dp(80));
        voteBtn.setLayoutParams(vbLp);
        voteBtn.setVisibility(View.GONE);
        voteBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, MurderMysteryVoteActivity.class)
                    .putExtra("gameId", gameId));
        });

        FrameLayout container = new FrameLayout(this);
        container.addView(root);
        container.addView(voteBtn);

        setContentView(container);
    }

    private void loadGame() {
        db.collection("murder_mysteries").document(gameId).get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) {
                    Toast.makeText(this, "الجلسة ماكانتش", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                game = doc.toObject(MurderMystery.class);
                if (game != null) game.id = doc.getId();

                updateHeader();
                startTimer();
                listenClues();
                listenChat();
            });
    }

    private void updateHeader() {
        if (game == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("📖 ").append(game.title).append("\n\n");
        sb.append(game.description).append("\n\n");
        if (game.story != null && !game.story.isEmpty()) {
            sb.append("📜 ").append(game.story);
        }
        statusView.setText(sb.toString());
    }

    private void startTimer() {
        Runnable tick = new Runnable() {
            @Override
            public void run() {
                if (game == null) {
                    handler.postDelayed(this, 1000);
                    return;
                }
                long end = game.endTime > 0 ? game.endTime : (game.startTime + 24*60*60*1000L);
                long diff = end - System.currentTimeMillis();
                if (diff <= 0) {
                    timerView.setText("⏰ انتهى");
                } else {
                    long h = diff / (1000*60*60);
                    long m = (diff / (1000*60)) % 60;
                    long s = (diff / 1000) % 60;
                    timerView.setText(String.format("%02d:%02d:%02d", h, m, s));
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(tick);
    }

    private void listenClues() {
        cluesReg = db.collection("murder_mysteries").document(gameId)
            .collection("clues")
            .orderBy("order", Query.Direction.ASCENDING)
            .addSnapshotListener((snap, e) -> {
                if (e != null || snap == null) return;

                runOnUiThread(() -> {
                    cluesContainer.removeAllViews();
                    int index = 0;
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        MMClue c = doc.toObject(MMClue.class);
                        if (c == null) continue;

                        // نعرضو فقط الأدلة العامة أو الخاصة
                        String reveal = c.revealsFor;
                        if (reveal != null && !reveal.equals("all")
                                && !reveal.equals(me.nationalId)) {
                            continue;
                        }

                        View card = createClueCard(c, index);
                        cluesContainer.addView(card);
                        index++;
                    }

                    if (index == 0) {
                        TextView empty = new TextView(this);
                        empty.setText("⏳ لم تظهر أدلة بعد...");
                        empty.setTextColor(Color.parseColor("#666666"));
                        empty.setTextSize(12);
                        empty.setGravity(Gravity.CENTER);
                        empty.setPadding(0, dp(20), 0, dp(20));
                        cluesContainer.addView(empty);
                    }
                });
            });
    }

    private View createClueCard(MMClue c, int index) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackgroundResource(R.drawable.bg_mystery_card);
        card.setPadding(dp(20), dp(16), dp(20), dp(16));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(lp);

        TextView icon = new TextView(this);
        icon.setText(c.icon != null ? c.icon : "🔍");
        icon.setTextSize(28);
        icon.setPadding(0, 0, dp(16), 0);
        card.addView(icon);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView title = new TextView(this);
        title.setText(c.title != null ? c.title : "دليل");
        title.setTextColor(c.isKeyClue ? Color.parseColor("#F44336") : Color.parseColor("#D4AF37"));
        title.setTextSize(15);
        title.setTypeface(null, Typeface.BOLD);
        info.addView(title);

        TextView desc = new TextView(this);
        desc.setText(c.description != null ? c.description : "");
        desc.setTextColor(Color.parseColor("#CCCCCC"));
        desc.setTextSize(12);
        desc.setPadding(0, dp(4), 0, 0);
        desc.setLineSpacing(0, 1.3f);
        info.addView(desc);

        card.addView(info);

        // أنيميشن ظهور
        card.setAlpha(0f);
        card.setTranslationX(-50f);
        card.animate().alpha(1f).translationX(0f)
                .setStartDelay(index * 100L).setDuration(400).start();

        return card;
    }

    private void listenChat() {
        chatReg = db.collection("murder_mysteries").document(gameId)
            .collection("chat")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .limitToLast(50)
            .addSnapshotListener((snap, e) -> {
                if (e != null || snap == null) return;

                runOnUiThread(() -> {
                    chatContainer.removeAllViews();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        MMMessage m = doc.toObject(MMMessage.class);
                        if (m == null) continue;
                        View msg = createMessageView(m);
                        chatContainer.addView(msg);
                    }
                });
            });
    }

    private View createMessageView(MMMessage m) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(6), 0, dp(6));

        boolean isMe = m.userId != null && m.userId.equals(me.nationalId);

        if (isMe) {
            row.setGravity(Gravity.END);
        }

        LinearLayout bubble = new LinearLayout(this);
        bubble.setOrientation(LinearLayout.VERTICAL);
        bubble.setBackgroundResource(R.drawable.bg_mystery_card);
        bubble.setPadding(dp(14), dp(10), dp(14), dp(10));
        bubble.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);

        TextView emoji = new TextView(this);
        emoji.setText(m.userEmoji != null ? m.userEmoji : "👤");
        emoji.setTextSize(16);
        emoji.setPadding(0, 0, dp(8), 0);
        head.addView(emoji);

        TextView name = new TextView(this);
        name.setText(m.userName != null ? m.userName : "مجهول");
        name.setTextColor(Color.parseColor("#D4AF37"));
        name.setTextSize(12);
        name.setTypeface(null, Typeface.BOLD);
        head.addView(name);

        bubble.addView(head);

        TextView text = new TextView(this);
        text.setText(m.text != null ? m.text : "");
        text.setTextColor(Color.WHITE);
        text.setTextSize(13);
        text.setPadding(0, dp(4), 0, 0);
        bubble.addView(text);

        // Spacer (نص)
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

        return row;
    }

    private void sendMessage() {
        String text = chatInput.getText().toString().trim();
        if (text.isEmpty()) return;

        chatInput.setText("");

        Map<String, Object> msg = new HashMap<>();
        msg.put("userId", me.nationalId);
        msg.put("userName", me.name);
        msg.put("userEmoji", "🕵️");
        msg.put("text", text);
        msg.put("type", "chat");
        msg.put("createdAt", System.currentTimeMillis());

        db.collection("murder_mysteries").document(gameId)
            .collection("chat").add(msg);
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }

    @Override
    protected void onDestroy() {
        if (cluesReg != null) cluesReg.remove();
        if (chatReg != null) chatReg.remove();
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
