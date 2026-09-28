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

    public static Button primaryButton(Context ctx, String text) {
        Button b = new Button(ctx);
        b.setText(text);
        b.setBackgroundResource(R.drawable.bg_btn_gold_hero);
        b.setTextColor(Color.parseColor("#0A0A0A"));
        b.setTextSize(17);
        b.setTypeface(null, Typeface.BOLD);
        b.setAllCaps(false);
        b.setPadding(50, 38, 50, 38);
        b.setMinHeight(140);
        b.setElevation(12f);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 14, 0, 14);
        b.setLayoutParams(lp);
        return b;
    }

    public static Button secondaryButton(Context ctx, String text) {
        Button b = new Button(ctx);
        b.setText(text);
        b.setBackgroundResource(R.drawable.bg_btn_outline);
        b.setTextColor(Color.parseColor("#D4AF37"));
        b.setTextSize(16);
        b.setTypeface(null, Typeface.BOLD);
        b.setAllCaps(false);
        b.setPadding(50, 34, 50, 34);
        b.setMinHeight(130);
        b.setElevation(6f);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 10, 0, 10);
        b.setLayoutParams(lp);
        return b;
    }

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

    public static LinearLayout card(Context ctx) {
        LinearLayout card = new LinearLayout(ctx);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card_premium);
        card.setPadding(55, 55, 55, 55);
        card.setElevation(8f);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 18, 0, 18);
        card.setLayoutParams(lp);
        return card;
    }

    public static LinearLayout goldCard(Context ctx) {
        LinearLayout card = new LinearLayout(ctx);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_vip_card);
        card.setPadding(55, 55, 55, 55);
        card.setElevation(14f);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 20, 0, 20);
        card.setLayoutParams(lp);
        return card;
    }

    public static EditText input(Context ctx, String hint) {
        EditText e = new EditText(ctx);
        e.setHint(hint);
        e.setBackgroundResource(R.drawable.bg_input);
        e.setTextColor(Color.WHITE);
        e.setHintTextColor(Color.parseColor("#666666"));
        e.setTextSize(16);
        e.setPadding(45, 35, 45, 35);
        e.setMinHeight(130);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 12, 0, 12);
        e.setLayoutParams(lp);
        return e;
    }

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

    public static TextView text(Context ctx, String text, int size) {
        TextView t = new TextView(ctx);
        t.setText(text);
        t.setTextColor(Color.WHITE);
        t.setTextSize(size);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    public static TextView subText(Context ctx, String text, int size) {
        TextView t = new TextView(ctx);
        t.setText(text);
        t.setTextColor(Color.parseColor("#9E9E9E"));
        t.setTextSize(size);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    public static View spacer(Context ctx, int heightDp) {
        View v = new View(ctx);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                1, (int)(heightDp * ctx.getResources().getDisplayMetrics().density));
        v.setLayoutParams(lp);
        return v;
    }
}
