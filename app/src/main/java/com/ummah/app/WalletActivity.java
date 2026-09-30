package com.ummah.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration;

public class WalletActivity extends Activity {

    private IdentityManager im;
    private FirebaseManager fm;
    private TextView balanceView;
    private ListenerRegistration balReg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        // Feature Check
        if (!FeatureFlags.checkOrToast(this, "transfers")) {
            finish();
            return;
        }

        // FullscreenHelper.enable(this);  // DISABLED - crash
        im = new IdentityManager(this);
        fm = FirebaseManager.get();

        Citizen c = im.getCitizen();
        if (c == null) { finish(); return; }

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(40, 60, 40, 60);
        scroll.addView(root);

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER);
        header.setPadding(0, 0, 0, 20);

        TextView coinIcon = new TextView(this);
        coinIcon.setText("💰");
        coinIcon.setTextSize(36);
        header.addView(coinIcon);

        TextView title = new TextView(this);
        title.setText("  " + getString(R.string.btn_wallet).replace("💰", "").trim());
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);
        header.addView(title);

        root.addView(header);

        TextView sub = new TextView(this);
        sub.setText("الدينار الداخلي لدولة أُمّة");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        sub.setLetterSpacing(0.05f);
        sub.setPadding(0, 0, 0, 30);
        root.addView(sub);

        // بطاقة الرصيد الفخمة
        LinearLayout balanceCard = UiHelper.goldCard(this);
        balanceCard.setGravity(Gravity.CENTER);

        TextView balLabel = new TextView(this);
        balLabel.setText("✦  رصيدك الحالي  ✦");
        balLabel.setTextColor(Color.parseColor("#D4AF37"));
        balLabel.setTextSize(13);
        balLabel.setTypeface(null, Typeface.BOLD);
        balLabel.setLetterSpacing(0.1f);
        balLabel.setGravity(Gravity.CENTER);
        balanceCard.addView(balLabel);

        balanceView = new TextView(this);
        balanceView.setText("...");
        balanceView.setTextColor(Color.parseColor("#FFFFFF"));
        balanceView.setTextSize(72);
        balanceView.setTypeface(null, Typeface.BOLD);
        balanceView.setGravity(Gravity.CENTER);
        balanceView.setPadding(0, 16, 0, 8);
        balanceCard.addView(balanceView);

        TextView currencyLabel = new TextView(this);
        currencyLabel.setText("Đ  دينار أُمّة");
        currencyLabel.setTextColor(Color.parseColor("#9E9E9E"));
        currencyLabel.setTextSize(14);
        currencyLabel.setGravity(Gravity.CENTER);
        balanceCard.addView(currencyLabel);

        root.addView(balanceCard);

        // زر الإرسال / الاستقبال (رئيسي - ذهبي)
        Button transferBtn = UiHelper.primaryButton(this, "💸  إرسال / استقبال");
        transferBtn.setOnClickListener(v ->
            startActivity(new Intent(WalletActivity.this, TransferActivity.class)));
        root.addView(transferBtn);

        // بطاقة رقمي للاستقبال
        LinearLayout idCard = UiHelper.card(this);
        idCard.setGravity(Gravity.CENTER);

        TextView idLabel = new TextView(this);
        idLabel.setText("📥  رقمك للاستقبال");
        idLabel.setTextColor(Color.parseColor("#D4AF37"));
        idLabel.setTextSize(14);
        idLabel.setTypeface(null, Typeface.BOLD);
        idLabel.setGravity(Gravity.CENTER);
        idCard.addView(idLabel);

        TextView idValue = new TextView(this);
        idValue.setText(c.nationalId);
        idValue.setTextColor(Color.WHITE);
        idValue.setTextSize(16);
        idValue.setTypeface(null, Typeface.BOLD);
        idValue.setGravity(Gravity.CENTER);
        idValue.setPadding(0, 12, 0, 12);
        idValue.setLetterSpacing(0.05f);
        idCard.addView(idValue);

        Button showIdBtn = UiHelper.secondaryButton(this, "عرض الرقم");
        showIdBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(WalletActivity.this)
                .setTitle("رقمك للاستقبال")
                .setMessage("أعطِ هذا الرقم لمن يريد أن يرسل لك ديناراً:\n\n" + c.nationalId)
                .setPositiveButton(getString(R.string.btn_ok), null)
                .show();
        });
        idCard.addView(showIdBtn);

        root.addView(idCard);

        // زر تحديث
        Button refreshBtn = UiHelper.actionButton(this, "🔄  تحديث الرصيد", "#1B5E20");
        refreshBtn.setOnClickListener(v -> {
            Toast.makeText(WalletActivity.this, "جاري التحديث...", Toast.LENGTH_SHORT).show();
            startBalanceListener();
        });
        root.addView(refreshBtn);

        // فاصل
        View spacer = UiHelper.spacer(this, 30);
        root.addView(spacer);

        // عنوان قسم طرق الكسب
        LinearLayout sectionTitle = new LinearLayout(this);
        sectionTitle.setOrientation(LinearLayout.HORIZONTAL);
        sectionTitle.setGravity(Gravity.CENTER_VERTICAL);
        sectionTitle.setPadding(0, 20, 0, 20);

        View lineL = new View(this);
        LinearLayout.LayoutParams lineLlp = new LinearLayout.LayoutParams(0, 2, 1f);
        lineL.setLayoutParams(lineLlp);
        lineL.setBackgroundColor(Color.parseColor("#2A2A2A"));
        sectionTitle.addView(lineL);

        TextView secText = new TextView(this);
        secText.setText("  💎  طرق كسب الدينار  ");
        secText.setTextColor(Color.parseColor("#D4AF37"));
        secText.setTextSize(15);
        secText.setTypeface(null, Typeface.BOLD);
        sectionTitle.addView(secText);

        View lineR = new View(this);
        LinearLayout.LayoutParams lineRlp = new LinearLayout.LayoutParams(0, 2, 1f);
        lineR.setLayoutParams(lineRlp);
        lineR.setBackgroundColor(Color.parseColor("#2A2A2A"));
        sectionTitle.addView(lineR);

        root.addView(sectionTitle);

        // بطاقات طرق الكسب
        addEarningCard(root, "🎁", "مكافأة يومية", "+5 Đ كل يوم");
        addEarningCard(root, "🗳️", "التصويت على اقتراح", "+5 Đ لكل تصويت");
        addEarningCard(root, "📝", "تقديم اقتراح", "+20 Đ لكل اقتراح");
        addEarningCard(root, "👑", "الفوز بالانتخابات", "+500 Đ");

        // Footer
        TextView footer = new TextView(this);
        footer.setText("دولة أُمّة الرقمية\nدينار واحد = تفاعل واحد");
        footer.setTextColor(Color.parseColor("#616161"));
        footer.setTextSize(11);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, 40, 0, 0);
        root.addView(footer);

        setContentView(scroll);
        startBalanceListener();
    }

    private void addEarningCard(LinearLayout root, String emoji, String title, String subtitle) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card_premium);
        card.setPadding(40, 30, 40, 30);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 8, 0, 8);
        card.setLayoutParams(lp);

        TextView emojiView = new TextView(this);
        emojiView.setText(emoji);
        emojiView.setTextSize(28);
        emojiView.setPadding(0, 0, 20, 0);
        card.addView(emojiView);

        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tv = new TextView(this);
        tv.setText(title);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(15);
        tv.setTypeface(null, Typeface.BOLD);
        textCol.addView(tv);

        TextView sv = new TextView(this);
        sv.setText(subtitle);
        sv.setTextColor(Color.parseColor("#D4AF37"));
        sv.setTextSize(13);
        sv.setPadding(0, 4, 0, 0);
        textCol.addView(sv);

        card.addView(textCol);

        root.addView(card);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (balReg != null) balReg.remove();
    }

    private void startBalanceListener() {
        Citizen c = im.getCitizen();
        if (c == null) return;
        if (balReg != null) balReg.remove();
        balReg = fm.listenBalance(c.nationalId, new FirebaseManager.BalanceListener() {
            @Override public void onBalance(int balance) {
                runOnUiThread(() -> balanceView.setText(String.valueOf(balance)));
            }
            @Override public void onError(String message) {
                runOnUiThread(() -> balanceView.setText("—"));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // FullscreenHelper.enable(this);  // DISABLED - crash
    }
}
