package com.ummah.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
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
    private LinearLayout giftsContainer;
    private ListenerRegistration reg;

    private static final String[][] GIFTS = {
        {"🌹", "وردة", "1"},
        {"🍫", "شوكولاتة", "2"},
        {"⭐", "نجمة", "5"},
        {"🏆", "كأس", "10"},
        {"💎", "ألماسة", "25"},
        {"👑", "تاج", "50"}
    };

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        fm = FirebaseManager.get();
        im = new IdentityManager(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0A"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(36, 50, 36, 50);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("🎁 الهدايا");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("أرسل هدية لمواطن — أو استقبل هدايا");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 8, 0, 30);
        root.addView(sub);

        // شبكة الهدايا
        TextView shopTitle = new TextView(this);
        shopTitle.setText("🛒 متجر الهدايا");
        shopTitle.setTextColor(Color.parseColor("#D4AF37"));
        shopTitle.setTextSize(15);
        shopTitle.setTypeface(null, Typeface.BOLD);
        shopTitle.setPadding(0, 0, 0, 12);
        root.addView(shopTitle);

        // صفان من 3 هدايا
        for (int row = 0; row < 2; row++) {
            LinearLayout rowL = new LinearLayout(this);
            rowL.setOrientation(LinearLayout.HORIZONTAL);
            for (int i = 0; i < 3; i++) {
                int idx = row * 3 + i;
                if (idx >= GIFTS.length) break;
                rowL.addView(buildGiftButton(GIFTS[idx]));
            }
            root.addView(rowL);
        }

        // سجل الهدايا المستلمة
        TextView myTitle = new TextView(this);
        myTitle.setText("\n━━━ الهدايا التي استقبلتها ━━━\n");
        myTitle.setTextColor(Color.parseColor("#D4AF37"));
        myTitle.setTextSize(14);
        myTitle.setTypeface(null, Typeface.BOLD);
        myTitle.setGravity(Gravity.CENTER);
        myTitle.setPadding(0, 30, 0, 16);
        root.addView(myTitle);

        giftsContainer = new LinearLayout(this);
        giftsContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(giftsContainer);

        setContentView(scroll);
        startListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) reg.remove();
    }

    private Button buildGiftButton(final String[] gift) {
        Button btn = new Button(this);
        btn.setText(gift[0] + "\n" + gift[1] + "\n" + gift[2] + " Đ");
        btn.setTextSize(12);
        btn.setPadding(20, 24, 20, 24);
        btn.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        btn.setOnClickListener(v -> showSendGiftDialog(gift));
        return btn;
    }

    private void showSendGiftDialog(final String[] gift) {
        final Citizen me = im.getCitizen();
        if (me == null) return;

        final EditText idInput = new EditText(this);
        idInput.setHint("رقم المواطن الوطني");
        idInput.setTextColor(Color.WHITE);
        idInput.setHintTextColor(Color.GRAY);
        idInput.setInputType(InputType.TYPE_CLASS_TEXT);

        LinearLayout c = new LinearLayout(this);
        c.setPadding(40, 20, 40, 20);
        c.addView(idInput);

        new AlertDialog.Builder(this)
            .setTitle("إرسال " + gift[0] + " " + gift[1])
            .setMessage("التكلفة: " + gift[2] + " Đ")
            .setView(c)
            .setPositiveButton("إرسال", (d, w) -> {
                String toId = idInput.getText().toString().trim();
                if (toId.isEmpty()) return;
                if (toId.equals(me.nationalId)) {
                    Toast.makeText(this, "لا يمكنك إرسال هدية لنفسك", Toast.LENGTH_SHORT).show();
                    return;
                }
                final int cost = Integer.parseInt(gift[2]);
                fm.sendGift(me.nationalId, me.name, toId,
                        gift[0], gift[1], cost, new FirebaseManager.OnDone() {
                    @Override public void onSuccess() {
                        Toast.makeText(GiftsActivity.this, "🎁 تم الإرسال", Toast.LENGTH_LONG).show();
                    }
                    @Override public void onError(String msg) {
                        Toast.makeText(GiftsActivity.this, "خطأ: " + msg, Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    private void startListener() {
        Citizen me = im.getCitizen();
        if (me == null) return;
        reg = fm.listenGiftsFor(me.nationalId, list -> runOnUiThread(() -> render(list)));
    }

    private void render(List<FirebaseManager.GiftItem> list) {
        giftsContainer.removeAllViews();
        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("لم تستقبل هدايا بعد.");
            empty.setTextColor(Color.parseColor("#616161"));
            empty.setTextSize(13);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 20, 0, 0);
            giftsContainer.addView(empty);
            return;
        }
        for (FirebaseManager.GiftItem g : list) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setBackgroundColor(Color.parseColor("#141414"));
            card.setPadding(24, 18, 24, 18);
            card.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 10);
            card.setLayoutParams(lp);

            TextView emoji = new TextView(this);
            emoji.setText(g.giftEmoji);
            emoji.setTextSize(36);
            emoji.setPadding(0, 0, 20, 0);
            card.addView(emoji);

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            info.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            TextView from = new TextView(this);
            from.setText("من: " + (g.fromName != null ? g.fromName : "مجهول"));
            from.setTextColor(Color.WHITE);
            from.setTextSize(14);
            from.setTypeface(null, Typeface.BOLD);
            info.addView(from);

            TextView name = new TextView(this);
            name.setText(g.giftName);
            name.setTextColor(Color.parseColor("#D4AF37"));
            name.setTextSize(13);
            info.addView(name);

            TextView date = new TextView(this);
            date.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date(g.timestamp)));
            date.setTextColor(Color.parseColor("#616161"));
            date.setTextSize(10);
            info.addView(date);

            card.addView(info);
            giftsContainer.addView(card);
        }
    }
}
