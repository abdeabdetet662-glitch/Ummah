package com.ummah.app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PresidentBadgeHelper {

    // ═══ Badge صغير (يظهر جنب الاسم) ═══
    public static TextView createSmallBadge(Context ctx) {
        TextView b = new TextView(ctx);
        b.setText("👑");
        b.setTextSize(11);
        b.setGravity(Gravity.CENTER);
        b.setBackgroundResource(R.drawable.bg_president_badge);
        b.setPadding(8, 4, 8, 4);

        // نبض
        AlphaAnimation pulse = new AlphaAnimation(1f, 0.6f);
        pulse.setDuration(1200);
        pulse.setRepeatCount(Animation.INFINITE);
        pulse.setRepeatMode(Animation.REVERSE);
        b.startAnimation(pulse);

        return b;
    }

    // ═══ Tag كبير (اسم + تاج) ═══
    public static LinearLayout createPresidentTag(Context ctx) {
        LinearLayout tag = new LinearLayout(ctx);
        tag.setOrientation(LinearLayout.HORIZONTAL);
        tag.setGravity(Gravity.CENTER_VERTICAL);
        tag.setBackgroundResource(R.drawable.bg_gold_tag);
        tag.setPadding(16, 6, 16, 6);

        TextView crown = new TextView(ctx);
        crown.setText("👑");
        crown.setTextSize(12);
        crown.setPadding(0, 0, 6, 0);
        tag.addView(crown);

        TextView text = new TextView(ctx);
        text.setText("رئيس معتمد");
        text.setTextColor(Color.parseColor("#1A1A1A"));
        text.setTextSize(10);
        text.setTypeface(null, Typeface.BOLD);
        tag.addView(text);

        // نبض
        AlphaAnimation pulse = new AlphaAnimation(1f, 0.7f);
        pulse.setDuration(1500);
        pulse.setRepeatCount(Animation.INFINITE);
        pulse.setRepeatMode(Animation.REVERSE);
        tag.startAnimation(pulse);

        return tag;
    }

    // ═══ إضافة Badge على TextView (يضيف 👑 جنب الاسم) ═══
    public static void addBadgeToName(TextView nameView) {
        String current = nameView.getText().toString();
        if (!current.contains("👑")) {
            nameView.setText(current + " 👑");
        }
    }

    // ═══ رأسي: اسم + badge تحتو ═══
    public static LinearLayout createNameWithBadge(Context ctx, String name,
                                                     int nameSize, int nameColor) {
        LinearLayout container = new LinearLayout(ctx);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);

        TextView nameView = new TextView(ctx);
        nameView.setText(name);
        nameView.setTextColor(nameColor);
        nameView.setTextSize(nameSize);
        nameView.setTypeface(null, Typeface.BOLD);
        container.addView(nameView);

        TextView badge = createSmallBadge(ctx);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        blp.setMargins(8, 0, 0, 0);
        badge.setLayoutParams(blp);
        container.addView(badge);

        return container;
    }
}
