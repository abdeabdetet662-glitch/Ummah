#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Batch 6.1 - Fix remaining hardcoded Arabic strings"""
import os, re

BASE = "app/src/main/res"
JAVA = "app/src/main/java/com/ummah/app"

# ═══════════════════════════════════════════════════════
# نصوص جديدة (15 نص)
# ═══════════════════════════════════════════════════════
NEW = {
    # MainActivity
    "main_subtitle": {
        "ar": "أول دولة رقمية في العالم العربي",
        "fr": "Le premier État numérique du monde arabe",
        "en": "The first digital state in the Arab world",
        "ru": "Первое цифровое государство арабского мира",
    },
    "main_citizens": {
        "ar": "المواطنون",
        "fr": "Citoyens",
        "en": "Citizens",
        "ru": "Граждане",
    },
    "main_join_hint": {
        "ar": "انضم إلى آلاف المواطنين في أول دولة رقمية عربية",
        "fr": "Rejoignez des milliers de citoyens dans le premier État numérique arabe",
        "en": "Join thousands of citizens in the first Arab digital state",
        "ru": "Присоединяйтесь к тысячам граждан в первом арабском цифровом государстве",
    },
    # ChatActivity
    "chat_title": {
        "ar": "💬  دردشة أُمّة",
        "fr": "💬  Chat Ummah",
        "en": "💬  Ummah Chat",
        "ru": "💬  Чат Уммы",
    },
    "chat_subtitle": {
        "ar": "✦  كل مواطني العالم هنا  ✦",
        "fr": "✦  Tous les citoyens du monde sont ici  ✦",
        "en": "✦  All world citizens are here  ✦",
        "ru": "✦  Все граждане мира здесь  ✦",
    },
    "chat_empty": {
        "ar": "لا توجد رسائل بعد",
        "fr": "Aucun message pour l'instant",
        "en": "No messages yet",
        "ru": "Сообщений пока нет",
    },
    "chat_first_hint": {
        "ar": "كن أول من يتكلم!",
        "fr": "Soyez le premier à parler!",
        "en": "Be the first to speak!",
        "ru": "Будьте первым!",
    },
    # ConstitutionActivity
    "const_yes": {
        "ar": "✅ موافق",
        "fr": "✅ Pour",
        "en": "✅ Yes",
        "ru": "✅ За",
    },
    "const_no": {
        "ar": "❌ رافض",
        "fr": "❌ Contre",
        "en": "❌ No",
        "ru": "❌ Против",
    },
    # CourtActivity
    "court_case_log": {
        "ar": "\n━━━ سجل القضايا ━━━\n",
        "fr": "\n━━━ Registre des affaires ━━━\n",
        "en": "\n━━━ Case Log ━━━\n",
        "ru": "\n━━━ Журнал дел ━━━\n",
    },
    "court_plaintiff": {
        "ar": "📢 الشاكي: ",
        "fr": "📢 Plaignant: ",
        "en": "📢 Plaintiff: ",
        "ru": "📢 Истец: ",
    },
    "court_defendant": {
        "ar": "👤 المدعى عليه: ",
        "fr": "👤 Défendeur: ",
        "en": "👤 Defendant: ",
        "ru": "👤 Ответчик: ",
    },
    # ElectionActivity
    "election_voted_for": {
        "ar": "✅ صوّتت لـ: ",
        "fr": "✅ Vous avez voté pour: ",
        "en": "✅ You voted for: ",
        "ru": "✅ Вы проголосовали за: ",
    },
    "election_total_votes": {
        "ar": "إجمالي الأصوات: ",
        "fr": "Total des votes: ",
        "en": "Total votes: ",
        "ru": "Всего голосов: ",
    },
    # DailyRewardActivity
    "daily_next_format": {
        "ar": "⏳ المكافأة القادمة بعد: %1$sس %2$sد",
        "fr": "⏳ Prochaine récompense dans: %1$sh %2$sm",
        "en": "⏳ Next reward in: %1$sh %2$sm",
        "ru": "⏳ Следующая награда через: %1$sч %2$sм",
    },
}

def escape_xml(t):
    t = t.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    t = t.replace("&amp;amp;", "&amp;").replace("&amp;lt;", "&lt;").replace("&amp;gt;", "&gt;")
    return t

# ═══ أضف النصوص ═══
LOCALES = {
    "ar": f"{BASE}/values/strings.xml",
    "fr": f"{BASE}/values-fr/strings.xml",
    "en": f"{BASE}/values-en/strings.xml",
    "ru": f"{BASE}/values-ru/strings.xml",
}

print("═══ إضافة النصوص ═══")
for loc, path in LOCALES.items():
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()
    added = 0
    lines = ["\n    <!-- Batch 6.1 -->"]
    for sid, t in NEW.items():
        if f'name="{sid}"' in content:
            continue
        lines.append(f'    <string name="{sid}">{escape_xml(t[loc])}</string>')
        added += 1
    lines.append("")
    content = content.replace("</resources>", "\n".join(lines) + "\n</resources>")
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"✅ {loc}: +{added}")

# ═══ الاستبدال في Activities ═══
print("\n═══ استبدال في Activities ═══")
def rep(filename, repl):
    path = f"{JAVA}/{filename}.java"
    if not os.path.exists(path):
        return 0
    with open(path, "r", encoding="utf-8") as f:
        c = f.read()
    n = 0
    for old, new in repl:
        if old in c:
            c = c.replace(old, new)
            n += 1
    with open(path, "w", encoding="utf-8") as f:
        f.write(c)
    return n

# AccountRecoveryActivity — استبدال النص الموجود
n = rep("AccountRecoveryActivity", [
    ('desc.setText("أدخل كلماتك السرية الـ 12 لاستعادة\\nرقمك الوطني ورصيدك على أي هاتف")',
     'desc.setText(R.string.recovery_subtitle)'),
    ('warn.setText("\\n⚠️ لا يمكن التراجع عن الاستعادة.\\nقد تفقد حسابك الحالي.")',
     'warn.setText(R.string.recovery_warning)'),
])
print(f"✅ AccountRecoveryActivity: {n}")

# MainActivity
n = rep("MainActivity", [
    ('sub.setText("أول دولة رقمية في العالم العربي")',
     'sub.setText(R.string.main_subtitle)'),
    ('cLabel.setText("المواطنون")',
     'cLabel.setText(R.string.main_citizens)'),
    ('hint.setText("انضم إلى آلاف المواطنين في أول دولة رقمية عربية")',
     'hint.setText(R.string.main_join_hint)'),
])
print(f"✅ MainActivity: {n}")

# ChatActivity
n = rep("ChatActivity", [
    ('title.setText("💬  دردشة أُمّة")',
     'title.setText(R.string.chat_title)'),
    ('sub.setText("✦  كل مواطني العالم هنا  ✦")',
     'sub.setText(R.string.chat_subtitle)'),
    ('empty.setText("لا توجد رسائل بعد")',
     'empty.setText(R.string.chat_empty)'),
    ('hint.setText("كن أول من يتكلم!")',
     'hint.setText(R.string.chat_first_hint)'),
])
print(f"✅ ChatActivity: {n}")

# ConstitutionActivity
n = rep("ConstitutionActivity", [
    ('num.setText("المادة " + a.number)',
     'num.setText(getString(R.string.const_article) + " " + a.number)'),
    ('y.setText("✅ موافق")',
     'y.setText(R.string.const_yes)'),
    ('n.setText("❌ رافض")',
     'n.setText(R.string.const_no)'),
])
print(f"✅ ConstitutionActivity: {n}")

# CourtActivity
n = rep("CourtActivity", [
    ('sep.setText("\\n━━━ سجل القضايا ━━━\\n")',
     'sep.setText(R.string.court_case_log)'),
    ('plaintiff.setText("📢 الشاكي: " + c.plaintiffName)',
     'plaintiff.setText(getString(R.string.court_plaintiff) + c.plaintiffName)'),
    ('defendant.setText("👤 المدعى عليه: " + c.defendantName)',
     'defendant.setText(getString(R.string.court_defendant) + c.defendantName)'),
])
print(f"✅ CourtActivity: {n}")

# DailyRewardActivity
n = rep("DailyRewardActivity", [
    ('info.setText("\\n\\n⏰ المكافأة تُجدّد كل 24 ساعة.\\nلا تُفوّت أي يوم!")',
     'info.setText(R.string.daily_reset_info)'),
    ('statusView.setText("⏳ المكافأة القادمة بعد: " + h + "س " + m + "د")',
     'statusView.setText(getString(R.string.daily_next_format, h, m))'),
])
print(f"✅ DailyRewardActivity: {n}")

# ElectionActivity
n = rep("ElectionActivity", [
    ('myVoteView.setText("✅ صوّتت لـ: " + (candidate != null ? candidate : ""))',
     'myVoteView.setText(getString(R.string.election_voted_for) + (candidate != null ? candidate : ""))'),
    ('totalVotesView.setText("إجمالي الأصوات: " + fTotal)',
     'totalVotesView.setText(getString(R.string.election_total_votes) + fTotal)'),
])
print(f"✅ ElectionActivity: {n}")

print("\n═══════════════════════════════════════════")
print("📊 الدفعة 6.1 خلصت!")
print("═══════════════════════════════════════════")
