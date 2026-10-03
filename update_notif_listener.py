#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""تحديث NotificationListener — يزيد عداد unread"""
import re

PATH = "app/src/main/java/com/ummah/app/NotificationListener.java"

with open(PATH, "r", encoding="utf-8") as f:
    c = f.read()

# ═══ 1. نضيفو الميثودات الجديدة ═══
if "incrementUnread" not in c:
    last = c.rfind("}")
    new_method = '''
    
    /** زيادة عداد unread في Firestore */
    private void incrementUnread(String uid) {
        if (uid == null || uid.isEmpty()) return;
        
        final com.google.firebase.firestore.DocumentReference ref =
                com.google.firebase.firestore.FirebaseFirestore
                    .getInstance()
                    .collection("unread")
                    .document(uid);
        
        ref.get().addOnSuccessListener(snap -> {
            long current = 0;
            if (snap.exists() && snap.getLong("notifications") != null) {
                current = snap.getLong("notifications");
            }
            java.util.Map<String, Object> update = new java.util.HashMap<>();
            update.put("notifications", current + 1);
            ref.set(update, com.google.firebase.firestore.SetOptions.merge());
        });
    }
    
    /** تصفير عداد الإشعارات */
    public static void clearNotifCount(String uid) {
        if (uid == null || uid.isEmpty()) return;
        java.util.Map<String, Object> update = new java.util.HashMap<>();
        update.put("notifications", 0);
        com.google.firebase.firestore.FirebaseFirestore
            .getInstance()
            .collection("unread")
            .document(uid)
            .set(update, com.google.firebase.firestore.SetOptions.merge());
    }
}
'''
    c = c[:last] + new_method
    print("✅ أضفنا incrementUnread + clearNotifCount")
else:
    print("✅ الميثودات موجودة")

# ═══ 2. نضيفو الاستدعاء ═══
old = 'NotificationHelper.show(ctx, fullTitle, message, type, notifId, id);'
if old in c and "incrementUnread(uid);" not in c:
    new = old + '\n                            \n                            // زيادة العداد\n                            incrementUnread(uid);'
    c = c.replace(old, new, 1)
    print("✅ أضفنا استدعاء incrementUnread")

with open(PATH, "w", encoding="utf-8") as f:
    f.write(c)

print(f"📊 الأقواس: {{ = {c.count('{')}, }} = {c.count('}')}")
