package com.ummah.app.heist;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ummah.app.LocaleHelper;
import com.ummah.app.R;

/**
 * HeistBriefingActivity — إحاطة المهمة (القصة)
 */
public class HeistBriefingActivity extends Activity {

    private LinearLayout root;
    private TextView storyText;
    private TextView typewriterText;
    private Handler handler = new Handler();
    private String gameId;
    private HeistRole myRole;

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
        startTypewriter();
    }

    private void buildUI() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_heist);
        scroll.setFillViewport(true);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(60), dp(24), dp(60));
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        scroll.addView(root);

        // ═══ Top Secret Badge ═══
        TextView topSecret = new TextView(this);
        topSecret.setText("🔴 TOP SECRET");
        topSecret.setTextColor(Color.WHITE);
        topSecret.setTextSize(11);
        topSecret.setTypeface(null, Typeface.BOLD);
        topSecret.setLetterSpacing(0.2f);
        topSecret.setBackgroundColor(Color.parseColor("#DC2626"));
        topSecret.setPadding(dp(16), dp(8), dp(16), dp(8));
        LinearLayout.LayoutParams tsLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        tsLp.setMargins(0, 0, 0, dp(24));
        topSecret.setLayoutParams(tsLp);

        // Animation
        AlphaAnimation blink = new AlphaAnimation(1f, 0.5f);
        blink.setDuration(800);
        blink.setRepeatMode(Animation.REVERSE);
        blink.setRepeatCount(Animation.INFINITE);
        topSecret.startAnimation(blink);

        root.addView(topSecret);

        // ═══ Icon ═══
        TextView icon = new TextView(this);
        icon.setText("🏴‍☠️");
        icon.setTextSize(100);
        icon.setGravity(Gravity.CENTER);
        icon.setPadding(0, dp(20), 0, dp(16));
        root.addView(icon);

        // ═══ Title ═══
        TextView title = new TextView(this);
        title.setText("عملية النور");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(38);
        title.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        title.setGravity(Gravity.CENTER);
        title.setLetterSpacing(0.15f);
        root.addView(title);

        // ═══ Subtitle ═══
        TextView subtitle = new TextView(this);
        subtitle.setText("سرقة البنك المركزي لأُمّة");
        subtitle.setTextColor(Color.parseColor("#9E9E9E"));
        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, dp(8), 0, dp(32));
        root.addView(subtitle);

        // ═══ Typewriter text (القصة) ═══
        LinearLayout storyCard = new LinearLayout(this);
        storyCard.setOrientation(LinearLayout.VERTICAL);
        storyCard.setBackgroundResource(R.drawable.bg_heist_card);
        storyCard.setPadding(dp(22), dp(24), dp(22), dp(24));
        LinearLayout.LayoutParams scLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        scLp.setMargins(0, 0, 0, dp(24));
        storyCard.setLayoutParams(scLp);

        typewriterText = new TextView(this);
        typewriterText.setText("");
        typewriterText.setTextColor(Color.parseColor("#e8e8e8"));
        typewriterText.setTextSize(14);
        typewriterText.setLineSpacing(0, 1.7f);
        typewriterText.setTypeface(Typeface.MONOSPACE);
        storyCard.addView(typewriterText);

        root.addView(storyCard);

        // ═══ My Role Card ═══
        TextView roleTitle = new TextView(this);
        roleTitle.setText("🎭 دورك في المهمة");
        roleTitle.setTextColor(Color.parseColor("#D4AF37"));
        roleTitle.setTextSize(14);
        roleTitle.setTypeface(null, Typeface.BOLD);
        roleTitle.setGravity(Gravity.CENTER);
        roleTitle.setPadding(0, dp(24), 0, dp(16));
        root.addView(roleTitle);

        LinearLayout roleCard = createRoleCard(myRole);
        root.addView(roleCard);

        // ═══ Warning ═══
        LinearLayout warningBox = new LinearLayout(this);
        warningBox.setOrientation(LinearLayout.VERTICAL);
        warningBox.setBackgroundResource(R.drawable.bg_heist_card);
        warningBox.setPadding(dp(20), dp(16), dp(20), dp(16));
        warningBox.setBackgroundColor(Color.parseColor("#30DC2626"));
        LinearLayout.LayoutParams wbLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        wbLp.setMargins(0, dp(24), 0, dp(24));
        warningBox.setLayoutParams(wbLp);

        TextView warnText = new TextView(this);
        warnText.setText("⚠️ تحذير أمني");
        warnText.setTextColor(Color.parseColor("#DC2626"));
        warnText.setTextSize(14);
        warnText.setTypeface(null, Typeface.BOLD);
        warnText.setGravity(Gravity.CENTER);
        warningBox.addView(warnText);

        TextView warnBody = new TextView(this);
        warnBody.setText("أحد أعضاء الفريق قد يكون عميلاً مزدوجاً\nيعمل لمجلس الظل...\n\nلا تثق بأحد");
        warnBody.setTextColor(Color.parseColor("#e8e8e8"));
        warnBody.setTextSize(13);
        warnBody.setGravity(Gravity.CENTER);
        warnBody.setLineSpacing(0, 1.5f);
        warnBody.setPadding(0, dp(10), 0, 0);
        warningBox.addView(warnBody);

        root.addView(warningBox);

        // ═══ Continue Button ═══
        Button contBtn = new Button(this);
        contBtn.setText("▶️ فهمت — ابدأ التخطيط");
        contBtn.setTextSize(16);
        contBtn.setTextColor(Color.parseColor("#0a0510"));
        contBtn.setAllCaps(false);
        contBtn.setTypeface(null, Typeface.BOLD);
        contBtn.setBackgroundResource(R.drawable.bg_heist_btn_gold);
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        btnLp.setMargins(0, dp(24), 0, 0);
        contBtn.setLayoutParams(btnLp);
        contBtn.setOnClickListener(v -> {
            // ننتقل للـ Planning
            Intent intent = new Intent(this, HeistPlanningActivity.class);
            intent.putExtra("game_id", gameId);
            intent.putExtra("my_role", myRole.id);
            startActivity(intent);
            finish();
        });
        root.addView(contBtn);

        setContentView(scroll);
    }

    private LinearLayout createRoleCard(HeistRole role) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackgroundResource(R.drawable.bg_heist_card_selected);
        card.setPadding(dp(24), dp(20), dp(24), dp(20));

        TextView emoji = new TextView(this);
        emoji.setText(role.emoji);
        emoji.setTextSize(48);
        emoji.setPadding(0, 0, dp(20), 0);
        card.addView(emoji);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView name = new TextView(this);
        name.setText(role.nameAr);
        name.setTextColor(Color.parseColor(role.colorHex));
        name.setTextSize(22);
        name.setTypeface(null, Typeface.BOLD);
        info.addView(name);

        TextView desc = new TextView(this);
        desc.setText(role.description);
        desc.setTextColor(Color.parseColor("#CCCCCC"));
        desc.setTextSize(12);
        desc.setPadding(0, dp(6), 0, 0);
        info.addView(desc);

        TextView stats = new TextView(this);
        stats.setText("❤️ " + role.baseHp + "  •  ⚡ " + String.format("%.2fx", role.speedMultiplier) + "\n" + role.weaponName);
        stats.setTextColor(Color.parseColor("#888888"));
        stats.setTextSize(11);
        stats.setPadding(0, dp(6), 0, 0);
        stats.setLineSpacing(0, 1.3f);
        info.addView(stats);

        card.addView(info);

        return card;
    }

    private void startTypewriter() {
        String story = getStory();
        final int[] i = {0};
        final int[] delay = {40};

        Runnable type = new Runnable() {
            @Override
            public void run() {
                if (i[0] <= story.length()) {
                    typewriterText.setText(story.substring(0, i[0]));
                    i[0]++;

                    // سرعة متغيرة (أسرع بعد الفراغات)
                    delay[0] = (i[0] > 0 && i[0] <= story.length() &&
                            (story.charAt(i[0] - 1) == ' ' || story.charAt(i[0] - 1) == '\n')) ? 20 : 35;

                    handler.postDelayed(this, delay[0]);
                }
            }
        };
        handler.postDelayed(type, 1500);
    }

    private String getStory() {
        return "📅 التاريخ: 15 أكتوبر 2026\n" +
               "🕐 الوقت: 3:47 صباحاً\n" +
               "📍 المكان: البنك المركزي لأُمّة\n\n" +
               "في هدوء الليل، تسلل 5 مجهولين إلى البنك المركزي.\n" +
               "عطلوا الكاميرات، فتحوا الخزنة الرئيسية،\n" +
               "وسرقوا 100,000,000 Đ.\n\n" +
               "الشرطة وصلت بعد 12 دقيقة...\n" +
               "لكن المجرمين اختفوا في الظلام.\n\n" +
               "─── بعد 72 ساعة ───\n\n" +
               "اكتشف المحققون أن السارقين ليسوا عاديين.\n" +
               "إنهم \"مجلس الظل\" - منظمة سرية.\n\n" +
               "وزير الداخلية قرر: \"لن نستسلم\".\n\n" +
               "اختار 5 مواطنين...\n" +
               "أنت واحد منهم.\n\n" +
               "🎯 مهمتك: استعادة 100,000,000 Đ\n" +
               "⏰ 20 دقيقة فقط\n\n" +
               "اللعبة بدأت...";
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
