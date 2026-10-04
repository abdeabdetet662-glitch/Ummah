#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""حل مشكل NavDrawer — إحصائيات مكررة + الرصيد الوهمي"""
import re

# ═══════════════════════════════════════════
# 1. تنظيف NavDrawer — حذف ITEM_STATS القديم
# ═══════════════════════════════════════════
NAV = "app/src/main/java/com/ummah/app/NavDrawerHelper.java"
with open(NAV, "r", encoding="utf-8") as f:
    c = f.read()

# حذف السطر 139 (ITEM_STATS القديم)
old_stat = 'addItem(content, activity, "📊", activity.getString(R.string.nav_stats), ITEM_STATS, listener);'
if old_stat in c:
    c = c.replace(old_stat, '')
    print("✅ حذفنا ITEM_STATS القديم")

# ═══ تنظيم السطور ═══
# نلقاو السطر 135-136 لتنظيف المسافات
c = c.replace(
    '                  addItem(content, activity, "🔔", "الإشعارات", ITEM_NOTIFICATIONS, listener, notifCount);',
    '                addItem(content, activity, "🔔", "الإشعارات", ITEM_NOTIFICATIONS, listener, notifCount);'
)

# حذف الثابت القديم (اختياري — نبقيه للـ compatibility)
# نخليه عادي، ما يضر

with open(NAV, "w", encoding="utf-8") as f:
    f.write(c)
print(f"📊 NavDrawer: {c.count('{')}/{c.count('}')}")

# ═══════════════════════════════════════════
# 2. WalletManager — إضافة setBalance
# ═══════════════════════════════════════════
WM = "app/src/main/java/com/ummah/app/WalletManager.java"
with open(WM, "r", encoding="utf-8") as f:
    wc = f.read()

if "public void setBalance" not in wc:
    old = 'public int getBalance() { return prefs.getInt("balance", INITIAL); }'
    new = '''public int getBalance() { return prefs.getInt("balance", INITIAL); }

    /** تحديث الرصيد من Firestore */
    public void setBalance(int amount) {
        prefs.edit().putInt("balance", amount).apply();
    }'''
    if old in wc:
        wc = wc.replace(old, new)
        with open(WM, "w", encoding="utf-8") as f:
            f.write(wc)
        print("✅ أضفنا setBalance لـ WalletManager")

# ═══════════════════════════════════════════
# 3. MainActivity — Firestore Listener للرصيد
# ═══════════════════════════════════════════
MAIN = "app/src/main/java/com/ummah/app/MainActivity.java"
with open(MAIN, "r", encoding="utf-8") as f:
    mc = f.read()

# 3.1 Field جديد
if "realBalance" not in mc:
    mc = mc.replace(
        "private WalletManager wm;",
        "private WalletManager wm;\n    private com.google.firebase.firestore.ListenerRegistration balanceReg;\n    private long realBalance = 0;"
    )

# 3.2 Listener بعد wm = new WalletManager(this);
marker = "wm = new WalletManager(this);"
if marker in mc and "balanceReg =" not in mc:
    listener_code = '''

        // ═══ الرصيد الحقيقي من Firestore ═══
        try {
            IdentityManager imBal = new IdentityManager(this);
            if (imBal.isCitizen() && imBal.getCitizen() != null) {
                String uidBal = imBal.getCitizen().nationalId;
                balanceReg = com.google.firebase.firestore.FirebaseFirestore
                    .getInstance()
                    .collection("citizens").document(uidBal)
                    .addSnapshotListener((doc, e) -> {
                        if (doc != null && doc.exists()) {
                            Long bal = doc.getLong("balance");
                            if (bal != null) {
                                realBalance = bal;
                                wm.setBalance((int) bal.longValue());
                                android.util.Log.d("MainBal", "💰 " + bal);
                            }
                        }
                    });
            }
        } catch (Exception ex) {
            android.util.Log.e("MainBal", "خطأ", ex);
        }'''

    mc = mc.replace(marker, marker + listener_code, 1)
    print("✅ أضفنا listener")

# 3.3 نستعملو realBalance بدل wm.getBalance()
old_show = "NavDrawerHelper.show(this, currentCitizen, wm.getBalance(), freshNotif, freshChat,"
new_show = "NavDrawerHelper.show(this, currentCitizen, (int) realBalance, freshNotif, freshChat,"

if old_show in mc:
    mc = mc.replace(old_show, new_show)
    print("✅ عدّلنا NavDrawerHelper.show")

# 3.4 Cleanup
if "balanceReg != null) balanceReg.remove()" not in mc:
    old_destroy = "if (notifListener != null) notifListener.stop();"
    if old_destroy in mc:
        mc = mc.replace(old_destroy,
            "if (notifListener != null) notifListener.stop();\n        if (balanceReg != null) balanceReg.remove();")
        print("✅ Cleanup")

with open(MAIN, "w", encoding="utf-8") as f:
    f.write(mc)
print(f"📊 MainActivity: {mc.count('{')}/{mc.count('}')}")

print()
print("═══════════════════════════════════════════")
print("✅ تم الإصلاح!")
print("═══════════════════════════════════════════")
