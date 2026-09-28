package com.ummah.app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class BadgeHelper {

    public static FrameLayout withBadge(Context ctx, Button button, int count) {
        FrameLayout container = new FrameLayout(ctx);
        container.setClipChildren(false);
        container.setClipToPadding(false);

        FrameLayout.LayoutParams btnLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        button.setLayoutParams(btnLp);
        container.addView(button);

        if (count > 0) {
            TextView badge = createBadge(ctx, count);
            FrameLayout.LayoutParams badgeLp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            badgeLp.gravity = Gravity.TOP | Gravity.START;
            badgeLp.topMargin = 6;
            badgeLp.leftMargin = 12;
            badge.setLayoutParams(badgeLp);
            container.addView(badge);
            badge.bringToFront();
        }

        FrameLayout.LayoutParams containerLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        containerLp.setMargins(0, 8, 0, 0);
        container.setLayoutParams(containerLp);

        return container;
    }

    public static TextView createBadge(Context ctx, int count) {
        TextView badge = new TextView(ctx);
        String txt = count > 99 ? "99+" : String.valueOf(count);
        badge.setText(txt);
        badge.setTextColor(Color.WHITE);
        badge.setTextSize(count > 9 ? 11 : 13);
        badge.setTypeface(null, Typeface.BOLD);
        badge.setGravity(Gravity.CENTER);
        badge.setBackgroundResource(R.drawable.bg_badge_red);
        badge.setElevation(10f);

        int size = count > 9 ? 46 : 40;
        badge.setMinWidth(size);
        badge.setMinHeight(size);
        badge.setPadding(8, 0, 8, 0);

        return badge;
    }
}
