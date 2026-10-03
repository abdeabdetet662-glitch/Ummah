package com.ummah.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BottomNavHelper {

    public static final int TAB_HOME = 0;
    public static final int TAB_MARKET = 1;
    public static final int TAB_CHAT = 2;
    public static final int TAB_PROFILE = 3;

    public interface OnTabClick {
        void onTab(int tab);
    }

    public static LinearLayout build(Activity act, int activeTab, final OnTabClick listener) {
        LinearLayout wrapper = new LinearLayout(act);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setBackgroundColor(Color.parseColor("#0A0A0A"));
        wrapper.setElevation(20f);

        // خط علوي ذهبي
        View topLine = new View(act);
        topLine.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2));
        topLine.setBackgroundColor(Color.parseColor("#2A3D32"));
        wrapper.addView(topLine);

        LinearLayout nav = new LinearLayout(act);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setPadding(8, 10, 8, 10);
        wrapper.addView(nav);

        addTab(act, nav, act.getString(R.string.tab_home), act.getString(R.string.tab_home_label), TAB_HOME, activeTab == TAB_HOME, listener);
        addTab(act, nav, act.getString(R.string.tab_market), act.getString(R.string.tab_market_label), TAB_MARKET, activeTab == TAB_MARKET, listener);
        addTab(act, nav, act.getString(R.string.tab_chat), act.getString(R.string.tab_chat_label), TAB_CHAT, activeTab == TAB_CHAT, listener);
        addTab(act, nav, act.getString(R.string.tab_profile), act.getString(R.string.tab_profile_label), TAB_PROFILE, activeTab == TAB_PROFILE, listener);

        return wrapper;
    }

    private static void addTab(final Activity act, LinearLayout parent, String emoji,
                                String label, final int tab, boolean active,
                                final OnTabClick listener) {
        LinearLayout tabView = new LinearLayout(act);
        tabView.setOrientation(LinearLayout.VERTICAL);
        tabView.setGravity(Gravity.CENTER);
        tabView.setPadding(8, 12, 8, 12);
        tabView.setClickable(true);

        if (active) {
            tabView.setBackgroundResource(R.drawable.bg_nav_active);
        }

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(4, 0, 4, 0);
        tabView.setLayoutParams(lp);

        TextView icon = new TextView(act);
        icon.setText(emoji);
        icon.setTextSize(active ? 24 : 22);
        icon.setGravity(Gravity.CENTER);
        if (!active) icon.setAlpha(0.6f);
        tabView.addView(icon);

        TextView text = new TextView(act);
        text.setText(label);
        text.setTextSize(10);
        text.setGravity(Gravity.CENTER);
        text.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
        text.setTextColor(active ? Color.parseColor("#D4AF37") : Color.parseColor("#9E9E9E"));
        text.setPadding(0, 4, 0, 0);
        tabView.addView(text);

        tabView.setOnClickListener(v -> {
            AnimHelper.pressEffect(v);
            AnimHelper.lightHaptic(act);
            if (listener != null && !active) {
                listener.onTab(tab);
            }
        });

        parent.addView(tabView);
    }
}
