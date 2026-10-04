#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Lobby: الزر يعرف إذا المستخدم مسجل"""
import re

PATH = "app/src/main/java/com/ummah/app/MurderMysteryLobbyActivity.java"

with open(PATH, "r", encoding="utf-8") as f:
    c = f.read()

# ═══ 1. زيد field ═══
if "boolean iAmRegistered" not in c:
    c = c.replace(
        "private ListenerRegistration playersReg;",
        "private ListenerRegistration playersReg;\n    private boolean iAmRegistered = false;"
    )
    print("✅ أضفنا field")

# ═══ 2. عدّل renderPlayers — يتحقق إذا أنا مسجل ═══
old_render = '''private void renderPlayers(List<MMPlayer> players) {
        playersContainer.removeAllViews();'''

new_render = '''private void renderPlayers(List<MMPlayer> players) {
        // ═══ نتحققو إذا أنا مسجل ═══
        boolean found = false;
        if (me != null) {
            for (MMPlayer p : players) {
                if (p.userId != null && p.userId.equals(me.nationalId)) {
                    found = true;
                    break;
                }
            }
        }
        
        if (found != iAmRegistered) {
            iAmRegistered = found;
            updateUI();
        }
        
        playersContainer.removeAllViews();'''

if old_render in c:
    c = c.replace(old_render, new_render)
    print("✅ عدّلنا renderPlayers")
else:
    print("⚠️ ما لقيناش renderPlayers")

# ═══ 3. زيد me field ═══
if "private Citizen me" not in c:
    c = c.replace(
        "private IdentityManager im;",
        "private IdentityManager im;\n    private Citizen me;"
    )
    print("✅ أضفنا me field")

# ═══ 4. عيّن me في onCreate ═══
if "me = im.getCitizen();" not in c:
    c = c.replace(
        "im = new IdentityManager(this);",
        "im = new IdentityManager(this);\n        me = im.getCitizen();"
    )
    print("✅ عيّنّ me في onCreate")

# ═══ 5. عدّل updateUI — الزر ذكي ═══
old_ui = '''if (currentGame.isRegistration()) {
            actionBtn.setText("🎫 انضم للتحقيق · " + currentGame.entryFee + " Đ");
            actionBtn.setEnabled(true);
            actionBtn.setOnClickListener(v -> registerNow());
        }'''

new_ui = '''if (currentGame.isRegistration()) {
            if (iAmRegistered) {
                actionBtn.setText("✅ أنت مسجل في التحقيق");
                actionBtn.setEnabled(false);
            } else {
                actionBtn.setText("🎫 انضم للتحقيق · " + currentGame.entryFee + " Đ");
                actionBtn.setEnabled(true);
                actionBtn.setOnClickListener(v -> registerNow());
            }
        }'''

if old_ui in c:
    c = c.replace(old_ui, new_ui)
    print("✅ عدّلنا updateUI — زر ذكي")
else:
    print("⚠️ ما لقيناش updateUI")

# ═══ 6. بعد التسجيل، بدّل الزر فوراً ═══
old_success = '''Toast.makeText(MurderMysteryLobbyActivity.this,
                            "✅ انضممت للتحقيق! استعد...", Toast.LENGTH_LONG).show();'''

new_success = '''Toast.makeText(MurderMysteryLobbyActivity.this,
                            "✅ انضممت للتحقيق! استعد...", Toast.LENGTH_LONG).show();
                    
                    iAmRegistered = true;
                    actionBtn.setText("✅ أنت مسجل في التحقيق");
                    actionBtn.setEnabled(false);'''

if old_success in c:
    c = c.replace(old_success, new_success)
    print("✅ عدّلنا بعد التسجيل")

with open(PATH, "w", encoding="utf-8") as f:
    f.write(c)

print()
print(f"📊 الأقواس: {c.count('{')}/{c.count('}')}")
