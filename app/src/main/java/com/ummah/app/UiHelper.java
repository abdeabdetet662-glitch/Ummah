package com.ummah.app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class UiHelper {

    // زر رئيسي ذهبي
    public static Button primaryButton(Context ctx, String text) {
        Button b = new Button(ctx);
        b.setText(text);
        b.setBackgroundResource(R.drawable.bg_button_primary);
        b.setTextColor(Color.parseColor("#0A0A0A"));
        b.setTextSize(16);
        b.setTypeface(null, Typeface.BOLD);
        b.setAllCaps(false);
        b.setPadding(50, 32, 50, 32);
        b.setMinHeight(120);
        b.setElevation(8f);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 12, 0, 12);
        b.setLayoutParams(lp);
        return b;
    }

    // زر ثانوي (خلفية داكنة + إطار ذهبي)
    public static Button secondaryButton(Context ctx, String text) {
        Button b = new Button(ctx);
        b.setText(text);
        b.setBackgroundResource(R.drawable.bg_button_secondary);
        b.setTextColor(Color.parseColor("#D4AF37"));
        b.setTextSize(16);
        b.setTypeface(null, Typeface.BOLD);
        b.setAllCaps(false);
        b.setPadding(50, 32, 50, 32);
        b.setMinHeight(120);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 12, 0, 12);
        b.setLayoutParams(lp);
        return b;
    }

    // زر خطر
    public static Button dangerButton(Context ctx, String text) {
        Button b = new Button(ctx);
        b.setText(text);
        b.setBackgroundResource(R.drawable.bg_button_danger);
        b.setTextColor(Color.WHITE);
        b.setTextSize(16);
        b.setTypeface(null, Typeface.BOLD);
        b.setAllCaps(false);
        b.setPadding(50, 32, 50, 32);
        b.setMinHeight(120);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 12, 0, 12);
        b.setLayoutParams(lp);
        return b;
    }

    // بطاقة
    public static LinearLayout card(Context ctx) {
        LinearLayout card = new LinearLayout(ctx);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card);
        card.setPadding(50, 50, 50, 50);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 16, 0, 16);
        card.setLayoutParams(lp);
        return card;
    }

    // بطاقة ذهبية (للمواطنة)
    public static LinearLayout goldCard(Context ctx) {
        LinearLayout card = new LinearLayout(ctx);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card_gold);
        card.setPadding(50, 50, 50, 50);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 16, 0, 16);
        card.setLayoutParams(lp);
        return card;
    }

    // حقل إدخال
    public static EditText input(Context ctx, String hint) {
        EditText e = new EditText(ctx);
        e.setHint(hint);
        e.setBackgroundResource(R.drawable.bg_input);
        e.setTextColor(Color.WHITE);
        e.setHintTextColor(Color.parseColor("#666666"));
        e.setTextSize(16);
        e.setPadding(40, 30, 40, 30);
        e.setMinHeight(120);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 10, 0, 10);
        e.setLayoutParams(lp);
        return e;
    }

    // نص عنوان ذهبي
    public static TextView goldTitle(Context ctx, String text, int size) {
        TextView t = new TextView(ctx);
        t.setText(text);
        t.setTextColor(Color.parseColor("#D4AF37"));
        t.setTextSize(size);
        t.setTypeface(null, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 12, 0, 12);
        t.setLayoutParams(lp);
        return t;
    }

    // نص عادي أبيض
    public static TextView text(Context ctx, String text, int size) {
        TextView t = new TextView(ctx);
        t.setText(text);
        t.setTextColor(Color.WHITE);
        t.setTextSize(size);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    // نص رمادي فرعي
    public static TextView subText(Context ctx, String text, int size) {
        TextView t = new TextView(ctx);
        t.setText(text);
        t.setTextColor(Color.parseColor("#9E9E9E"));
        t.setTextSize(size);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    // مسافة فارغة
    public static View spacer(Context ctx, int heightDp) {
        View v = new View(ctx);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                1, (int)(heightDp * ctx.getResources().getDisplayMetrics().density));
        v.setLayoutParams(lp);
        return v;
    }
}
