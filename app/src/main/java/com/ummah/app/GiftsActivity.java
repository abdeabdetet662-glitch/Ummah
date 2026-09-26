package com.ummah.app;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.animation.OvershootInterpolator;
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

public class GiftsActivity extends Activity {
    private FirebaseManager fm;
    private IdentityManager im;
    private WalletManager wm;
    private TextView balanceView;
    private LinearLayout historyContainer;
    private ListenerRegistration recReg, sentReg;
    private int currentTab = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        fm = FirebaseManager.get();
        im = new IdentityManager(this);
        wm = new WalletManager(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0A"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 40, 20, 40);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("🎁 متجر الهدايا");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("أرسل هدايا فخمة لأصدقائك");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 6, 0, 20);
        root.addView(sub);

        LinearLayout balanceCard = new LinearLayout(this);
        balanceCard.setOrientation(LinearLayout.HORIZONTAL);
        balanceCard.setGravity(Gravity.CENTER);
        balanceCard.setBackground(makeGradient("#0B4F2C", "#1B7A4A", 30));
        balanceCard.setPadding(24, 16, 24, 16);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        blp.setMargins(0, 0, 0, 24);
        balanceCard.setLayoutParams(blp);

        TextView bIcon = new TextView(this);
        bIcon.setText("💰 ");
        bIcon.setTextSize(20);
        balanceCard.addView(bIcon);

        balanceView = new TextView(this);
        balanceView.setText("... Đ");
        balanceView.setTextColor(Color.parseColor("#FFD700"));
        balanceView.setTextSize(22);
        balanceView.setTypeface(null, Typeface.BOLD);
        balanceCard.addView(balanceView);
        root.addView(balanceCard);

        addSectionHeader(root, "🟢 هدايا عادية");
        addGiftGrid(root, GiftCatalog.getCommonGifts());

        addSectionHeader(root, "🔵 هدايا نادرة");
        addGiftGrid(root, GiftCatalog.getRareGifts());

        addSectionHeader(root, "🟡 هدايا أسطورية");
        addGiftGrid(root, GiftCatalog.getLegendaryGifts());

        addSectionHeader(root, "📜 سجل الهدايا");

        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tlp.setMargins(0, 8, 0, 16);
        tabs.setLayoutParams(tlp);

        Button recTab = new Button(this);
        recTab.setText("📥 استقبلت");
        recTab.setTextSize(13);
        recTab.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        recTab.setOnClickListener(v -> { currentTab = 0; loadHistory(); });
        tabs.addView(recTab);

        Button sentTab = new Button(this);
        sentTab.setText("📤 أرسلت");
        sentTab.setTextSize(13);
        sentTab.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        sentTab.setOnClickListener(v -> { currentTab = 1; loadHistory(); });
        tabs.addView(sentTab);

        root.addView(tabs);

        historyContainer = new LinearLayout(this);
        historyContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(historyContainer);

        setContentView(scroll);
        startBalanceListener();
        loadHistory();
    }

    private void addSectionHeader(LinearLayout root, String text) {
        TextView t = new TextView(this);
        t.setText("━━━━ " + text + " ━━━━");
        t.setTextColor(Color.parseColor("#D4AF37"));
        t.setTextSize(15);
        t.setTypeface(null, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, 24, 0, 12);
        root.addView(t);
    }

    private void addGiftGrid(LinearLayout root, List<Gift> gifts) {
        for (int i = 0; i < gifts.size(); i += 3) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rlp.setMargins(0, 6, 0, 6);
            row.setLayoutParams(rlp);

            for (int j = i; j < i + 3 && j < gifts.size(); j++) {
                row.addView(buildGiftCard(gifts.get(j)));
            }
            for (int j = gifts.size(); j < i + 3; j++) {
                View spacer = new View(this);
                spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1));
                row.addView(spacer);
            }

            root.addView(row);
        }
    }

    private View buildGiftCard(final Gift gift) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(8, 16, 8, 16);
        card.setBackground(GiftCatalog.getTierBackground(gift.tier));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        lp.setMargins(6, 0, 6, 0);
        card.setLayoutParams(lp);

        TextView emoji = new TextView(this);
        emoji.setText(gift.emoji);
        emoji.setTextSize(44);
        emoji.setGravity(Gravity.CENTER);
        card.addView(emoji);

        TextView name = new TextView(this);
        name.setText(gift.name);
        name.setTextColor(Color.WHITE);
        name.setTextSize(14);
        name.setTypeface(null, Typeface.BOLD);
        name.setGravity(Gravity.CENTER);
        name.setPadding(0, 6, 0, 2);
        card.addView(name);

        TextView meaning = new TextView(this);
        meaning.setText(gift.meaning);
        meaning.setTextColor(Color.parseColor("#EEEEEE"));
        meaning.setTextSize(10);
        meaning.setGravity(Gravity.CENTER);
        meaning.setPadding(0, 0, 0, 6);
        card.addView(meaning);

        TextView price = new TextView(this);
        price.setText(gift.price + " Đ");
        price.setTextColor(Color.parseColor("#FFD700"));
        price.setTextSize(13);
        price.setTypeface(null, Typeface.BOLD);
        price.setGravity(Gravity.CENTER);
        card.addView(price);

        card.setOnClickListener(v -> {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(card, "scaleX", 1f, 1.15f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(card, "scaleY", 1f, 1.15f, 1f);
            scaleX.setDuration(250);
            scaleY.setDuration(250);
            scaleX.start();
            scaleY.start();
            showSendDialog(gift);
        });

        return card;
    }

    private GradientDrawable makeGradient(String c1, String c2, int radius) {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor(c1), Color.parseColor(c2)});
        g.setCornerRadius(radius);
        return g;
    }

    private void startBalanceListener() {
        Citizen c = im.getCitizen();
        if (c == null) return;
        fm.listenBalance(c.nationalId, new FirebaseManager.BalanceListener() {
            @Override public void onBalance(final int balance) {
                runOnUiThread(() -> balanceView.setText(balance + " Đ"));
            }
            @Override public void onError(String m) {}
        });
    }

    private void loadHistory() {
        Citizen me = im.getCitizen();
        if (me == null) return;
        historyContainer.removeAllViews();

        if (recReg != null) recReg.remove();
        if (sentReg != null) sentReg.remove();

        if (currentTab == 0) {
            recReg = fm.listenReceivedGifts(me.nationalId, list -> runOnUiThread(() -> {
                historyContainer.removeAllViews();
                if (list.isEmpty()) {
                    historyContainer.addView(emptyView("لم تستقبل هدايا بعد"));
                } else {
                    for (FirebaseManager.GiftEntry g : list) {
                        historyContainer.addView(buildHistoryCard(g, false));
                    }
                }
            }));
        } else {
            sentReg = fm.listenSentGifts(me.nationalId, list -> runOnUiThread(() -> {
                historyContainer.removeAllViews();
                if (list.isEmpty()) {
                    historyContainer.addView(emptyView("لم ترسل هدايا بعد"));
                } else {
                    for (FirebaseManager.GiftEntry g : list) {
                        historyContainer.addView(buildHistoryCard(g, true));
                    }
                }
            }));
        }
    }

    private View emptyView(String text) {
        TextView empty = new TextView(this);
        empty.setText(text);
        empty.setTextColor(Color.parseColor("#616161"));
        empty.setTextSize(13);
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(0, 40, 0, 0);
        return empty;
    }

    private View buildHistoryCard(FirebaseManager.GiftEntry g, boolean isSent) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(20, 16, 20, 16);
        card.setBackground(makeGradient("#1A1A1A", "#252525", 20));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 10);
        card.setLayoutParams(lp);

        TextView emoji = new TextView(this);
        emoji.setText(g.emoji);
        emoji.setTextSize(36);
        emoji.setPadding(0, 0, 16, 0);
        card.addView(emoji);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView nameView = new TextView(this);
        nameView.setText(isSent ? "إلى: " + g.toId : "من: " + (g.fromName != null ? g.fromName : "مجهول"));
        nameView.setTextColor(Color.WHITE);
        nameView.setTextSize(13);
        nameView.setTypeface(null, Typeface.BOLD);
        info.addView(nameView);

        TextView giftView = new TextView(this);
        giftView.setText(g.giftName + " — " + g.meaning);
        giftView.setTextColor(Color.parseColor("#D4AF37"));
        giftView.setTextSize(12);
        info.addView(giftView);

        TextView dateView = new TextView(this);
        dateView.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date(g.timestamp)));
        dateView.setTextColor(Color.parseColor("#757575"));
        dateView.setTextSize(10);
        info.addView(dateView);

        card.addView(info);

        TextView priceView = new TextView(this);
        priceView.setText((isSent ? "-" : "") + g.price + " Đ");
        priceView.setTextColor(isSent ? Color.parseColor("#F44336") : Color.parseColor("#4CAF50"));
        priceView.setTextSize(15);
        priceView.setTypeface(null, Typeface.BOLD);
        card.addView(priceView);

        return card;
    }

    private void showSendDialog(final Gift gift) {
        final Citizen me = im.getCitizen();
        if (me == null) return;

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);
        content.setPadding(40, 20, 40, 20);

        TextView preview = new TextView(this);
        preview.setText(gift.emoji);
        preview.setTextSize(72);
        preview.setGravity(Gravity.CENTER);
        content.addView(preview);

        TextView nameView = new TextView(this);
        nameView.setText(gift.name + " — " + gift.meaning);
        nameView.setTextColor(Color.parseColor("#D4AF37"));
        nameView.setTextSize(16);
        nameView.setTypeface(null, Typeface.BOLD);
        nameView.setGravity(Gravity.CENTER);
        nameView.setPadding(0, 10, 0, 4);
        content.addView(nameView);

        TextView priceView = new TextView(this);
        priceView.setText("السعر: " + gift.price + " Đ");
        priceView.setTextColor(Color.WHITE);
        priceView.setTextSize(14);
        priceView.setGravity(Gravity.CENTER);
        priceView.setPadding(0, 0, 0, 20);
        content.addView(priceView);

        final EditText idInput = new EditText(this);
        idInput.setHint("رقم المواطن الوطني");
        idInput.setTextColor(Color.WHITE);
        idInput.setHintTextColor(Color.GRAY);
        idInput.setInputType(InputType.TYPE_CLASS_TEXT);
        content.addView(idInput);

        new AlertDialog.Builder(this)
            .setTitle("🎁 إرسال " + gift.name)
            .setView(content)
            .setPositiveButton("🎁 إرسال", (d, w) -> {
                String toId = idInput.getText().toString().trim();
                if (toId.isEmpty()) return;
                if (toId.equals(me.nationalId)) {
                    Toast.makeText(this, "لا يمكنك الإرسال لنفسك", Toast.LENGTH_SHORT).show();
                    return;
                }
                fm.sendGift(me.nationalId, me.name, toId,
                        gift.emoji, gift.name, gift.meaning, gift.price,
                        new FirebaseManager.OnDone() {
                    @Override public void onSuccess() {
                        wm.spend(gift.price);
                        showCelebration(gift);
                    }
                    @Override public void onError(String msg) {
                        Toast.makeText(GiftsActivity.this, "خطأ: " + msg, Toast.LENGTH_LONG).show();
                    }
                });
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    private void showCelebration(Gift gift) {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);
        content.setPadding(60, 40, 60, 40);

        final TextView emojiView = new TextView(this);
        emojiView.setText(gift.emoji);
        emojiView.setTextSize(120);
        emojiView.setGravity(Gravity.CENTER);
        emojiView.setScaleX(0f);
        emojiView.setScaleY(0f);
        content.addView(emojiView);

        TextView msg = new TextView(this);
        msg.setText("✅ تم إرسال " + gift.name + "!");
        msg.setTextColor(Color.parseColor("#FFD700"));
        msg.setTextSize(18);
        msg.setTypeface(null, Typeface.BOLD);
        msg.setGravity(Gravity.CENTER);
        msg.setPadding(0, 20, 0, 0);
        content.addView(msg);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(content)
            .setPositiveButton("رائع!", null)
            .create();
        dialog.show();

        emojiView.animate()
            .scaleX(1f).scaleY(1f)
            .setDuration(600)
            .setInterpolator(new OvershootInterpolator())
            .start();

        ObjectAnimator rotation = ObjectAnimator.ofFloat(emojiView, "rotation", 0f, 15f, -15f, 0f);
        rotation.setDuration(800);
        rotation.setRepeatCount(2);
        rotation.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recReg != null) recReg.remove();
        if (sentReg != null) sentReg.remove();
    }
}
