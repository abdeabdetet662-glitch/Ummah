#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""إضافة Badges لكل العناصر + App Icon Badge"""
import re, os

BASE = "app/src/main/java/com/ummah/app"

# ═══════════════════════════════════════════════
# 1. NavDrawerHelper — Badge support
# ═══════════════════════════════════════════════
NAV = f"{BASE}/NavDrawerHelper.java"
with open(NAV, "r", encoding="utf-8") as f:
    c = f.read()

# ═══ 1.1 نضيفو overload لـ show() ═══
old_show = '''public static void show(final Activity activity,
                                final Citizen citizen,
                                final int balance,
                                final OnDrawerClick listener) {
        try {'''

new_show = '''public static void show(final Activity activity,
                                final Citizen citizen,
                                final int balance,
                                final OnDrawerClick listener) {
        show(activity, citizen, balance, 0, 0, listener);
    }
    
    /** نسخة جديدة مع Badges */
    public static void show(final Activity activity,
                                final Citizen citizen,
                                final int balance,
                                final int notifCount,
                                final int chatCount,
                                final OnDrawerClick listener) {
        try {'''

if old_show in c:
    c = c.replace(old_show, new_show)
    print("✅ NavDrawerHelper: أضفنا overload للـ show()")
else:
    print("⚠️ NavDrawerHelper: ما لقيناش show القديمة")
    # نحاولو نلقاو show بأي طريقة
    m = re.search(r'(public static void show\(final Activity activity,\s*\n\s*final Citizen citizen,\s*\n\s*final int balance,\s*\n\s*final OnDrawerClick listener\) \{)', c)
    if m:
        c = c.replace(m.group(1), m.group(1).replace("final OnDrawerClick listener) {", "final OnDrawerClick listener) {\n        show(activity, citizen, balance, 0, 0, listener);\n    }\n    \n    public static void show(final Activity activity,\n                                final Citizen citizen,\n                                final int balance,\n                                final int notifCount,\n                                final int chatCount,\n                                final OnDrawerClick listener) {"))
        print("✅ NavDrawerHelper: أضفنا overload (بطريقة ثانية)")

# ═══ 1.2 نضيفو badge للعناصر المهمة ═══
# إشعارات (لو موجودة) - نضيفو عنصر جديد بعد ITEM_NEWS
old_news = 'addItem(content, activity, "📰", activity.getString(R.string.nav_news), ITEM_NEWS, listener);'
if old_news in c and 'ITEM_NOTIFICATIONS' in c:
    # نضيفو الإشعارات بعد الأخبار
    new_news = '''addItem(content, activity, "📰", activity.getString(R.string.nav_news), ITEM_NEWS, listener, 0);
                  addItem(content, activity, "🔔", "الإشعارات", ITEM_NOTIFICATIONS, listener, notifCount);'''
    c = c.replace(old_news, new_news)
    print("✅ NavDrawerHelper: أضفنا عنصر الإشعارات مع badge")

# ═══ 1.3 نعدلو addItem باش يدعم badge ═══
old_addItem_sig = '''private static void addItem(LinearLayout parent, final Activity activity,
                                 String emoji, String title, final int itemId,
                                 final OnDrawerClick listener) {'''

new_addItem_sig = '''private static void addItem(LinearLayout parent, final Activity activity,
                                 String emoji, String title, final int itemId,
                                 final OnDrawerClick listener) {
        addItem(parent, activity, emoji, title, itemId, listener, 0);
    }
    
    /** نسخة جديدة مع Badge */
    private static void addItem(LinearLayout parent, final Activity activity,
                                 String emoji, String title, final int itemId,
                                 final OnDrawerClick listener, int badgeCount) {'''

if old_addItem_sig in c:
    c = c.replace(old_addItem_sig, new_addItem_sig)
    print("✅ NavDrawerHelper: أضفنا overload لـ addItem")
else:
    print("⚠️ NavDrawerHelper: ما لقيناش addItem signature")

# ═══ 1.4 نضيفو badge قبل الـ arrow ═══
old_arrow = '''        // Arrow (سهم صغير)
        TextView arrow = new TextView(activity);
        arrow.setText("‹");
        arrow.setTextColor(Color.parseColor("#666666"));
        arrow.setTextSize(20);
        row.addView(arrow);'''

new_arrow = '''        // Badge (إذا كان > 0)
        if (badgeCount > 0) {
            TextView badge = new TextView(activity);
            badge.setText(badgeCount > 99 ? "99+" : String.valueOf(badgeCount));
            badge.setTextColor(Color.WHITE);
            badge.setTextSize(11);
            badge.setTypeface(null, Typeface.BOLD);
            badge.setGravity(Gravity.CENTER);
            badge.setBackgroundResource(R.drawable.bg_badge_red);
            badge.setMinWidth(dp(activity, 40));
            badge.setMinHeight(dp(activity, 40));
            badge.setPadding(dp(activity, 8), 0, dp(activity, 8), 0);
            LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            badgeLp.setMargins(0, 0, dp(activity, 15), 0);
            badge.setLayoutParams(badgeLp);
            row.addView(badge);
        }
        
        // Arrow (سهم صغير)
        TextView arrow = new TextView(activity);
        arrow.setText("‹");
        arrow.setTextColor(Color.parseColor("#666666"));
        arrow.setTextSize(20);
        row.addView(arrow);'''

if old_arrow in c:
    c = c.replace(old_arrow, new_arrow)
    print("✅ NavDrawerHelper: أضفنا Badge قبل السهم")
else:
    print("⚠️ NavDrawerHelper: ما لقيناش arrow")

# ═══ 1.5 نضيفو دالة dp ═══
if 'private static int dp(' not in c:
    last = c.rfind("}")
    c = c[:last] + '''
    
    private static int dp(Activity act, int dp) {
        return (int) (dp * act.getResources().getDisplayMetrics().density);
    }
}
'''
    print("✅ NavDrawerHelper: أضفنا دالة dp()")

with open(NAV, "w", encoding="utf-8") as f:
    f.write(c)
print(f"📊 NavDrawerHelper: {{ = {c.count('{')}, }} = {c.count('}')}")
print()

# ═══════════════════════════════════════════════
# 2. MainActivity — استماع + تمرير
# ═══════════════════════════════════════════════
MAIN = f"{BASE}/MainActivity.java"
with open(MAIN, "r", encoding="utf-8") as f:
    m = f.read()

# ═══ 2.1 نضيفو fields ═══
if "int unreadNotifs = 0" not in m:
    marker = "private NotificationListener notifListener;"
    if marker in m:
        m = m.replace(marker, marker + "\n    private int unreadNotifs = 0;\n    private int unreadChat = 0;\n    private com.google.firebase.firestore.ListenerRegistration unreadReg;")
        print("✅ MainActivity: أضفنا fields للعدادات")

# ═══ 2.2 نلقاو مكان show() تاع NavDrawerHelper ═══
# نشوفو كيفاش كيتسمى
matches = re.findall(r'NavDrawerHelper\.show\([^;]+\)', m)
print(f"📊 MainActivity: نلقاو {len(matches)} استدعاء لـ NavDrawerHelper.show")

with open(MAIN, "w", encoding="utf-8") as f:
    f.write(m)

print(f"📊 MainActivity: {{ = {m.count('{')}, }} = {m.count('}')}")
print()
print("═══════════════════════════════════════════")
print("✅ انتهى — الخطوة الأولى")
print("═══════════════════════════════════════════")
