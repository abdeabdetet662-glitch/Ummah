package com.ummah.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

/**
 * MMInterrogateActivity — استجواب مشتبه
 */
public class MMInterrogateActivity extends Activity {

    private MMSuspect suspect;
    private List<MMQuestion> questions;

    private LinearLayout chatContainer;
    private ScrollView scroll;

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        String suspectId = getIntent().getStringExtra("suspectId");
        String killerId = getIntent().getStringExtra("killerId");
        String caseId = getIntent().getStringExtra("caseId");

        List<MMSuspect> suspects = MMSuspectsManager.buildSuspects(caseId);
        for (MMSuspect s : suspects) {
            if (s.id.equals(suspectId)) {
                suspect = s;
                break;
            }
        }

        if (suspect != null) {
            suspect.isKiller = suspect.id.equals(killerId);
        }

        questions = MMSuspectsManager.getQuestions();

        buildUI();
    }

    private void buildUI() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.bg_mafia);

        // Top bar
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setBackgroundColor(Color.parseColor("#0a0510"));
        top.setPadding(dp(20), dp(50), dp(20), dp(14));

        TextView back = new TextView(this);
        back.setText("←");
        back.setTextColor(Color.parseColor("#D4AF37"));
        back.setTextSize(24);
        back.setPadding(0, 0, dp(16), 0);
        back.setOnClickListener(v -> finish());
        top.addView(back);

        TextView avatar = new TextView(this);
        avatar.setText(suspect != null ? suspect.emoji : "👤");
        avatar.setTextSize(30);
        avatar.setPadding(0, 0, dp(12), 0);
        top.addView(avatar);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView name = new TextView(this);
        name.setText(suspect != null ? suspect.name : "مشتبه");
        name.setTextColor(Color.parseColor("#D4AF37"));
        name.setTextSize(16);
        name.setTypeface(null, Typeface.BOLD);
        info.addView(name);

        TextView title = new TextView(this);
        title.setText(suspect != null ? suspect.title : "");
        title.setTextColor(Color.parseColor("#9E9E9E"));
        title.setTextSize(11);
        info.addView(title);

        top.addView(info);
        root.addView(top);

        // Description card
        LinearLayout descCard = new LinearLayout(this);
        descCard.setOrientation(LinearLayout.VERTICAL);
        descCard.setBackgroundResource(R.drawable.bg_mafia_role);
        descCard.setPadding(dp(20), dp(16), dp(20), dp(16));
        LinearLayout.LayoutParams dcLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        dcLp.setMargins(dp(16), dp(16), dp(16), dp(16));
        descCard.setLayoutParams(dcLp);

        TextView desc = new TextView(this);
        desc.setText("📋 " + (suspect != null ? suspect.description : ""));
        desc.setTextColor(Color.WHITE);
        desc.setTextSize(13);
        desc.setLineSpacing(0, 1.3f);
        descCard.addView(desc);

        root.addView(descCard);

        // Chat container
        scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout.LayoutParams scLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        scroll.setLayoutParams(scLp);

        chatContainer = new LinearLayout(this);
        chatContainer.setOrientation(LinearLayout.VERTICAL);
        chatContainer.setPadding(dp(16), dp(8), dp(16), dp(16));
        scroll.addView(chatContainer);
        root.addView(scroll);

        // Questions buttons
        LinearLayout qBar = new LinearLayout(this);
        qBar.setOrientation(LinearLayout.VERTICAL);
        qBar.setBackgroundColor(Color.parseColor("#0a0510"));
        qBar.setPadding(dp(12), dp(12), dp(12), dp(12));

        TextView qTitle = new TextView(this);
        qTitle.setText("🎤 اختر سؤالاً للاستجواب:");
        qTitle.setTextColor(Color.parseColor("#D4AF37"));
        qTitle.setTextSize(12);
        qTitle.setTypeface(null, Typeface.BOLD);
        qTitle.setPadding(0, 0, 0, dp(8));
        qBar.addView(qTitle);

        ScrollView qScroll = new ScrollView(this);
        qScroll.setHorizontalScrollBarEnabled(true);

        LinearLayout qRow = new LinearLayout(this);
        qRow.setOrientation(LinearLayout.HORIZONTAL);

        for (MMQuestion q : questions) {
            TextView btn = new TextView(this);
            btn.setText(q.emoji + " " + q.text);
            btn.setTextColor(Color.parseColor("#0A0510"));
            btn.setTextSize(12);
            btn.setTypeface(null, Typeface.BOLD);
            btn.setBackgroundResource(R.drawable.bg_btn_gold_hero);
            btn.setPadding(dp(14), dp(10), dp(14), dp(10));
            LinearLayout.LayoutParams qlp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            qlp.setMargins(0, 0, dp(8), 0);
            btn.setLayoutParams(qlp);
            btn.setOnClickListener(v -> askQuestion(q));
            qRow.addView(btn);
        }

        qScroll.addView(qRow);
        qBar.addView(qScroll);
        root.addView(qBar);

        setContentView(root);
    }

    private void askQuestion(MMQuestion q) {
        // سؤال المحقق
        addMessage("🕵️ أنت", q.text, Color.parseColor("#D4AF37"));

        // جواب المشتبه (بعد تأخير بسيط لمحاكاة التفكير)
        final String answer = suspect.getAnswer(q.id);

        new android.os.Handler().postDelayed(() -> {
            addMessage(suspect.emoji + " " + suspect.name, answer, Color.WHITE);
            scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
        }, 800);
    }

    private void addMessage(String name, String text, int nameColor) {
        LinearLayout msg = new LinearLayout(this);
        msg.setOrientation(LinearLayout.VERTICAL);
        msg.setBackgroundResource(R.drawable.bg_mafia_bubble);
        msg.setPadding(dp(14), dp(10), dp(14), dp(10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(4), 0, dp(4));
        msg.setLayoutParams(lp);

        TextView n = new TextView(this);
        n.setText(name);
        n.setTextColor(nameColor);
        n.setTextSize(11);
        n.setTypeface(null, Typeface.BOLD);
        msg.addView(n);

        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(Color.WHITE);
        t.setTextSize(13);
        t.setPadding(0, dp(2), 0, 0);
        t.setLineSpacing(0, 1.3f);
        msg.addView(t);

        chatContainer.addView(msg);

        // أنيميشن
        msg.setAlpha(0f);
        msg.animate().alpha(1f).setDuration(300).start();

        scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }
}
