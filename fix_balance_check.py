#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""إصلاح فحص الرصيد — من Firestore مباشرة بدل Citizen object"""
import re

PATH = "app/src/main/java/com/ummah/app/MurderMysteryManager.java"

with open(PATH, "r", encoding="utf-8") as f:
    c = f.read()

# ═══ 1. نلقاو المكان اللي كيتحقق من الرصيد ═══
old_check = '''int fee = game.entryFee;
                if (citizen.balance < fee) {
                    cb.onError("ما عندكش " + fee + " Đ");
                    return;
                }'''

new_check = '''final int fee = game.entryFee;'''

if old_check in c:
    c = c.replace(old_check, new_check)
    print("✅ حذفنا الفحص القديم")
else:
    # نجربو regex
    pattern = r'int fee = game\.entryFee;\s*\n\s*if \(citizen\.balance < fee\) \{\s*\n\s*cb\.onError\([^)]+\);\s*\n\s*return;\s*\n\s*\}'
    if re.search(pattern, c):
        c = re.sub(pattern, 'final int fee = game.entryFee;', c)
        print("✅ حذفنا الفحص (regex)")

# ═══ 2. نستبدلوها بفحص Firestore مباشر ═══
old_deduct = '''// خصم الرسوم من رصيد المواطن مباشرة
                db.collection("citizens").document(citizen.nationalId)
                    .update("balance", com.google.firebase.firestore.FieldValue.increment(-fee));'''

new_deduct = '''// ═══ نجيبو الرصيد من Firestore مباشرة ═══
                db.collection("citizens").document(citizen.nationalId).get()
                    .addOnSuccessListener(citDoc -> {
                        if (!citDoc.exists()) {
                            cb.onError("حسابك ماكانش");
                            return;
                        }
                        
                        Long balanceObj = citDoc.getLong("balance");
                        long balance = balanceObj != null ? balanceObj : 0;
                        
                        android.util.Log.d("MurderMystery", 
                            "💰 رصيدك: " + balance + " | مطلوب: " + fee);
                        
                        if (balance < fee) {
                            cb.onError("ما عندكش " + fee + " Đ (عندك " + balance + " Đ)");
                            return;
                        }
                        
                        // ═══ خصم الرسوم ═══
                        db.collection("citizens").document(citizen.nationalId)
                            .update("balance", com.google.firebase.firestore.FieldValue.increment(-fee))
                            .addOnSuccessListener(v -> {
                                // نكملو التسجيل
                                continueRegistration(gameId, citizen, cb);
                            })
                            .addOnFailureListener(e -> cb.onError("خطأ: " + e.getMessage()));
                    })
                    .addOnFailureListener(e -> cb.onError("خطأ: " + e.getMessage()));'''

if old_deduct in c:
    c = c.replace(old_deduct, new_deduct)
    print("✅ أضفنا فحص Firestore + استدعاء continueRegistration")
else:
    print("⚠️ ما لقيناش مكان الخصم")

# ═══ 3. نضيفو دالة continueRegistration منفصلة ═══
# نلقاو آخر register ونضيفو دالة جديدة قبل آخر }

# لكن أول، نشيلو الكود المتبقي بعد deduct (نقلوه لـ continueRegistration)
old_rest = '''// إضافة اللاعب
                Map<String, Object> player = new HashMap<>();
                player.put("userId", citizen.nationalId);
                player.put("userName", citizen.name);
                player.put("role", "investigator"); // مؤقتاً
                player.put("character", "مواطن");
                player.put("characterDesc", "في انتظار القصة...");
                player.put("avatarEmoji", "👤");
                player.put("joinedAt", System.currentTimeMillis());
                player.put("status", "active");
                player.put("suspicion", 0);

                db.collection(COLLECTION).document(gameId)
                    .collection("players").document(citizen.nationalId)
                    .set(player)
                    .addOnSuccessListener(v -> {
                        // زيادة العداد
                        db.collection(COLLECTION).document(gameId)
                            .update("currentPlayers",
                                com.google.firebase.firestore.FieldValue.increment(1))
                            .addOnSuccessListener(x -> cb.onSuccess())
                            .addOnFailureListener(e -> cb.onError(e.getMessage()));
                    })
                    .addOnFailureListener(e -> cb.onError(e.getMessage()));
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }'''

new_rest = '''            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }
    
    /** إكمال التسجيل بعد خصم الرسوم */
    private void continueRegistration(String gameId, Citizen citizen, SimpleCallback cb) {
        // إضافة اللاعب
        Map<String, Object> player = new HashMap<>();
        player.put("userId", citizen.nationalId);
        player.put("userName", citizen.name);
        player.put("role", "investigator");
        player.put("character", "مواطن");
        player.put("characterDesc", "في انتظار القصة...");
        player.put("avatarEmoji", "👤");
        player.put("joinedAt", System.currentTimeMillis());
        player.put("status", "active");
        player.put("suspicion", 0);

        db.collection(COLLECTION).document(gameId)
            .collection("players").document(citizen.nationalId)
            .set(player)
            .addOnSuccessListener(v -> {
                // زيادة العداد
                db.collection(COLLECTION).document(gameId)
                    .update("currentPlayers",
                        com.google.firebase.firestore.FieldValue.increment(1))
                    .addOnSuccessListener(x -> cb.onSuccess())
                    .addOnFailureListener(e -> cb.onError(e.getMessage()));
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }'''

if old_rest in c:
    c = c.replace(old_rest, new_rest)
    print("✅ أضفنا دالة continueRegistration")
else:
    print("⚠️ ما لقيناش باقي register — نفحصو")

with open(PATH, "w", encoding="utf-8") as f:
    f.write(c)

# التحقق
print()
print("═══ التحقق ═══")
print(f"  continueRegistration:  {'✅' if 'continueRegistration' in c else '❌'}")
print(f"  فحص balance القديم:    {'❌' if 'citizen.balance < fee' in c else '✅ حُذف'}")
print(f"  getLong(balance):      {'✅' if 'getLong(\"balance\")' in c else '❌'}")
print(f"  الأقواس:               {c.count('{')}/{c.count('}')}")
