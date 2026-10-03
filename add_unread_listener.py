#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""إضافة مستمع Firestore للأرقام + تمريرها للـ NavDrawer"""
import re

PATH = "app/src/main/java/com/ummah/app/MainActivity.java"

with open(PATH, "r", encoding="utf-8") as f:
    c = f.read()

# ═══ 1. تعديل استدعاء NavDrawerHelper.show ═══
old_call = 'NavDrawerHelper.show(this, currentCitizen, wm.getBalance(), new NavDrawerHelper.OnDrawerClick() {'
new_call = 'NavDrawerHelper.show(this, currentCitizen, wm.getBalance(), unreadNotifs, unreadChat, new NavDrawerHelper.OnDrawerClick() {'

if old_call in c:
    c = c.replace(old_call, new_call)
    print("✅ عدلنا استدعاء NavDrawerHelper.show")
else:
    print("⚠️ ما لقيناش الاستدعاء — نجربو regex")
    c = re.sub(
        r'NavDrawerHelper\.show\(this,\s*currentCitizen,\s*wm\.getBalance\(\)',
        'NavDrawerHelper.show(this, currentCitizen, wm.getBalance(), unreadNotifs, unreadChat',
        c
    )

# ═══ 2. إضافة مستمع Firestore في onCreate ═══
# نلقاو مكان بدء notifListener
marker = "notifListener.start(uid);"

if "unreadReg = " not in c:
    listener_code = '''

            // ═══ مستمع الأرقام (unread) ═══
            unreadReg = com.google.firebase.firestore.FirebaseFirestore
                    .getInstance()
                    .collection("unread")
                    .document(uid)
                    .addSnapshotListener((snap, e) -> {
                        if (e != null) {
                            android.util.Log.e("UmmahUnread", "خطأ", e);
                            return;
                        }
                        if (snap == null || !snap.exists()) return;
                        
                        Long notifs = snap.getLong("notifications");
                        Long chats = snap.getLong("chat");
                        
                        int newNotifs = notifs != null ? notifs.intValue() : 0;
                        int newChats = chats != null ? chats.intValue() : 0;
                        
                        unreadNotifs = newNotifs;
                        unreadChat = newChats;
                        
                        // تحديث BottomNav
                        try {
                            BottomNavHelper.updateChatBadge(MainActivity.this, newChats);
                        } catch (Exception ignored) {}
                        
                        // تحديث App Icon Badge
                        try {
                            if (newNotifs > 0) {
                                BadgeHelper.updateAppBadge(MainActivity.this, newNotifs);
                            } else {
                                BadgeHelper.clearAppBadge(MainActivity.this);
                            }
                        } catch (Exception ignored) {}
                        
                        android.util.Log.d("UmmahUnread",
                            "📊 Notifs: " + newNotifs + " | Chats: " + newChats);
                    });'''

    if marker in c:
        c = c.replace(marker, marker + listener_code)
        print("✅ أضفنا مستمع unread")
    else:
        print("⚠️ ما لقيناش notifListener.start")

# ═══ 3. تحديث onDestroy ═══
if "unreadReg != null) unreadReg.remove()" not in c:
    old_destroy = 'if (notifListener != null) notifListener.stop();'
    if old_destroy in c:
        c = c.replace(old_destroy,
            'if (notifListener != null) notifListener.stop();\n        if (unreadReg != null) unreadReg.remove();')
        print("✅ أضفنا unreadReg.remove() في onDestroy")

with open(PATH, "w", encoding="utf-8") as f:
    f.write(c)

print(f"📊 الأقواس: {{ = {c.count('{')}, }} = {c.count('}')}")
