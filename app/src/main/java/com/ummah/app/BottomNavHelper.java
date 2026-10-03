package com.ummah.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BottomNavHelper {

    public static final int TAB_HOME = 0;
    public static final int TAB_MARKET = 1;
    public static final int TAB_CHAT = 2;
    public static final int TAB_PROFILE = 3;

    // مراجع للأيقونات باش نحدّثو الـ badges
    private static TextView chatBadge = null;
    private static TextView notifBadge = null;

    public interface OnTabClick {
        void onTab(int tab);
    }

    public static LinearLayout build(Activity act, int activeTab, final OnTabClick listener) {
        LinearLayout wrapper = new LinearLayout(act);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setBackgroundColor(Color.parseColor("#0A0A0A"));
        wrapper.setElevation(20f);

        View topLine = new View(act);
        topLine.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2));
        topLine.setBackgroundColor(Color.parseColor("#2A3D32"));
        wrapper.addView(topLine);

        LinearLayout nav = new LinearLayout(act);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setPadding(8, 10, 8, 10);
        wrapper.addView(nav);

        addTab(act, nav, act.getString(R.string.tab_home), act.getString(R.string.tab_home_label),
                TAB_HOME, activeTab == TAB_HOME, listener, false);
        addTab(act, nav, act.getString(R.string.tab_market), act.getString(R.string.tab_market_label),
                TAB_MARKET, activeTab == TAB_MARKET, listener, false);
        addTab(act, nav, act.getString(R.string.tab_chat), act.getString(R.string.tab_chat_label),
                TAB_CHAT, activeTab == TAB_CHAT, listener, true);
        addTab(act, nav, act.getString(R.string.tab_profile), act.getString(R.string.tab_profile_label),
                TAB_PROFILE, activeTab == TAB_PROFILE, listener, false);

        return wrapper;
    }

    private static void addTab(final Activity act, LinearLayout parent, String emoji,
                                String label, final int tab, boolean active,
                                final OnTabClick listener, boolean showBadge) {
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

        // ═══ Icon + Badge ═══
        FrameLayout iconWrapper = new FrameLayout(act);
        iconWrapper.setClipChildren(false);
        iconWrapper.setClipToPadding(false);

        TextView icon = new TextView(act);
        icon.setText(emoji);
        icon.setTextSize(active ? 24 : 22);
        icon.setGravity(Gravity.CENTER);
        if (!active) icon.setAlpha(0.6f);
        iconWrapper.addView(icon);

        // Badge (نقطة حمراء + رقم)
        if (showBadge && tab == TAB_CHAT) {
            chatBadge = createBadge(act);
            chatBadge.setVisibility(View.GONE);
            FrameLayout.LayoutParams blp = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            blp.gravity = Gravity.TOP | Gravity.END;
            blp.setMargins(0, -8, -12, 0);
            chatBadge.setLayoutParams(blp);
            iconWrapper.addView(chatBadge);
        }

        tabView.addView(iconWrapper);

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

    /** إنشاء Badge */
    private static TextView createBadge(Activity act) {
        TextView badge = new TextView(act);
        badge.setTextColor(Color.WHITE);
        badge.setTextSize(10);
        badge.setTypeface(null, Typeface.BOLD);
        badge.setGravity(Gravity.CENTER);
        badge.setBackgroundResource(R.drawable.bg_badge_red);
        badge.setElevation(10f);
        badge.setMinWidth(dp(act, 18));
        badge.setMinHeight(dp(act, 18));
        badge.setPadding(dp(act, 5), 0, dp(act, 5), 0);
        return badge;
    }

    /** تحديث Badge الدردشة */
    public static void updateChatBadge(Activity act, int count) {
        if (chatBadge == null) return;
        if (count <= 0) {
            chatBadge.setVisibility(View.GONE);
        } else {
            chatBadge.setVisibility(View.VISIBLE);
            chatBadge.setText(count > 99 ? "99+" : String.valueOf(count));
        }
    }

    private static int dp(Activity act, int dp) {
        return (int) (dp * act.getResources().getDisplayMetrics().density);
    }
}
