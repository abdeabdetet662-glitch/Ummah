#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Batch 7 FINAL: All remaining hardcoded strings"""
import os

BASE = "app/src/main/res"
JAVA = "app/src/main/java/com/ummah/app"

NEW = {
    # Common
    "common_error_connection": {"ar": "خطأ اتصال: ", "fr": "Erreur de connexion: ", "en": "Connection error: ", "ru": "Ошибка соединения: "},
    "congrats": {"ar": "🎉 مبروك!", "fr": "🎉 Félicitations!", "en": "🎉 Congratulations!", "ru": "🎉 Поздравляем!"},
    "no_candidates": {"ar": "لا يوجد مرشحون", "fr": "Aucun candidat", "en": "No candidates", "ru": "Нет кандидатов"},
    "citizen_ummah": {"ar": "مواطن أُمّة", "fr": "Citoyen d'Ummah", "en": "Ummah Citizen", "ru": "Гражданин Уммы"},

    # MainActivity
    "main_settings_soon": {"ar": "الإعدادات — قريباً", "fr": "Paramètres — bientôt", "en": "Settings — coming soon", "ru": "Настройки — скоро"},
    "main_notif_error": {"ar": "تعذر فتح الإشعارات", "fr": "Impossible d'ouvrir les notifications", "en": "Cannot open notifications", "ru": "Не удаётся открыть уведомления"},
    "main_page_error": {"ar": "تعذر فتح الصفحة", "fr": "Impossible d'ouvrir la page", "en": "Cannot open page", "ru": "Не удаётся открыть страницу"},
    "dialog_logout": {"ar": "🚪 تسجيل الخروج", "fr": "🚪 Déconnexion", "en": "🚪 Logout", "ru": "🚪 Выход"},

    # Chat
    "chat_blocked": {"ar": "🚫 أنت محظور من الإرسال", "fr": "🚫 Vous êtes bloqué de l'envoi", "en": "🚫 You are blocked from sending", "ru": "🚫 Вам запрещено отправлять"},
    "chat_muted": {"ar": "🔇 أنت مكتوم مؤقتاً", "fr": "🔇 Vous êtes temporairement muet", "en": "🔇 You are temporarily muted", "ru": "🔇 Вы временно заглушены"},
    "dialog_report": {"ar": "🚩 إبلاغ عن الرسالة", "fr": "🚩 Signaler le message", "en": "🚩 Report message", "ru": "🚩 Пожаловаться на сообщение"},

    # Election
    "election_voted_short": {"ar": "صوّتت لـ ", "fr": "Vous avez voté pour ", "en": "You voted for ", "ru": "Вы проголосовали за "},

    # Gifts
    "gifts_send_prefix": {"ar": "🎁 إرسال ", "fr": "🎁 Envoyer ", "en": "🎁 Send ", "ru": "🎁 Отправить "},

    # Transfer / Wallet
    "transfer_dialog_title": {"ar": "تحويل دينار", "fr": "Transfert de dinar", "en": "Transfer Dinar", "ru": "Перевод динара"},

    # Citizen Market
    "cmarket_confirm_dialog": {"ar": "تأكيد الشراء", "fr": "Confirmer l'achat", "en": "Confirm Purchase", "ru": "Подтвердить покупку"},

    # President Pardon
    "pardon_dialog_title": {"ar": "⚖️  عفو رئاسي", "fr": "⚖️  Grâce présidentielle", "en": "⚖️  Presidential Pardon", "ru": "⚖️  Президентское помилование"},

    # Bottom Nav tabs
    "tab_home": {"ar": "🏠", "fr": "🏠", "en": "🏠", "ru": "🏠"},
    "tab_market": {"ar": "🛒", "fr": "🛒", "en": "🛒", "ru": "🛒"},
    "tab_chat": {"ar": "💬", "fr": "💬", "en": "💬", "ru": "💬"},
    "tab_profile": {"ar": "👤", "fr": "👤", "en": "👤", "ru": "👤"},
    "tab_home_label": {"ar": "الرئيسية", "fr": "Accueil", "en": "Home", "ru": "Главная"},
    "tab_market_label": {"ar": "السوق", "fr": "Marché", "en": "Market", "ru": "Рынок"},
    "tab_chat_label": {"ar": "الدردشة", "fr": "Chat", "en": "Chat", "ru": "Чат"},
    "tab_profile_label": {"ar": "حسابي", "fr": "Profil", "en": "Profile", "ru": "Профиль"},
}

def escape_xml(t):
    t = t.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    t = t.replace("'", "\\u2019")
    t = t.replace("&amp;amp;", "&amp;").replace("&amp;lt;", "&lt;").replace("&amp;gt;", "&gt;")
    return t

# ═══ إضافة النصوص ═══
print("═══ إضافة النصوص ═══")
for loc, path in {"ar": f"{BASE}/values/strings.xml", "fr": f"{BASE}/values-fr/strings.xml",
                  "en": f"{BASE}/values-en/strings.xml", "ru": f"{BASE}/values-ru/strings.xml"}.items():
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()
    added = 0
    lines = ["\n    <!-- Batch 7 -->"]
    for sid, t in NEW.items():
        if f'name="{sid}"' in content: continue
        lines.append(f'    <string name="{sid}">{escape_xml(t[loc])}</string>')
        added += 1
    lines.append("")
    content = content.replace("</resources>", "\n".join(lines) + "\n</resources>")
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"✅ {loc}: +{added}")

# ═══ استبدال ═══
print("\n═══ استبدال في Activities ═══")
def rep(filename, repl):
    path = f"{JAVA}/{filename}.java"
    if not os.path.exists(path): return 0
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

n = rep("MainActivity", [
    ('Toast.makeText(MainActivity.this, "خطأ اتصال: " + msg, Toast.LENGTH_LONG)',
     'Toast.makeText(MainActivity.this, getString(R.string.common_error_connection) + msg, Toast.LENGTH_LONG)'),
    ('Toast.makeText(MainActivity.this, "خطأ: " + msg, Toast.LENGTH_LONG)',
     'Toast.makeText(MainActivity.this, getString(R.string.common_error_prefix) + msg, Toast.LENGTH_LONG)'),
    ('Toast.makeText(this, "الإعدادات — قريباً", Toast.LENGTH_SHORT)',
     'Toast.makeText(this, R.string.main_settings_soon, Toast.LENGTH_SHORT)'),
    ('Toast.makeText(this, "تعذر فتح الإشعارات", Toast.LENGTH_SHORT)',
     'Toast.makeText(this, R.string.main_notif_error, Toast.LENGTH_SHORT)'),
    ('Toast.makeText(this, "تعذر فتح الصفحة", Toast.LENGTH_SHORT)',
     'Toast.makeText(this, R.string.main_page_error, Toast.LENGTH_SHORT)'),
    ('.setTitle("🚪 تسجيل الخروج")', '.setTitle(R.string.dialog_logout)'),
])
print(f"✅ MainActivity: {n}")

n = rep("ChatActivity", [
    ('Toast.makeText(this, "🚫 أنت محظور من الإرسال", Toast.LENGTH_LONG)',
     'Toast.makeText(this, R.string.chat_blocked, Toast.LENGTH_LONG)'),
    ('Toast.makeText(this, "🔇 أنت مكتوم مؤقتاً", Toast.LENGTH_LONG)',
     'Toast.makeText(this, R.string.chat_muted, Toast.LENGTH_LONG)'),
    ('.setTitle("🚩 إبلاغ عن الرسالة")', '.setTitle(R.string.dialog_report)'),
])
print(f"✅ ChatActivity: {n}")

n = rep("ConstitutionActivity", [
    ('Toast.makeText(ConstitutionActivity.this, "خطأ: " + msg, Toast.LENGTH_SHORT)',
     'Toast.makeText(ConstitutionActivity.this, getString(R.string.common_error_prefix) + msg, Toast.LENGTH_SHORT)'),
])
print(f"✅ ConstitutionActivity: {n}")

n = rep("CourtActivity", [
    ('Toast.makeText(CourtActivity.this, "خطأ: " + msg, Toast.LENGTH_SHORT)',
     'Toast.makeText(CourtActivity.this, getString(R.string.common_error_prefix) + msg, Toast.LENGTH_SHORT)'),
])
print(f"✅ CourtActivity: {n}")

n = rep("DailyRewardActivity", [
    ('Toast.makeText(DailyRewardActivity.this, "خطأ: " + msg, Toast.LENGTH_SHORT)',
     'Toast.makeText(DailyRewardActivity.this, getString(R.string.common_error_prefix) + msg, Toast.LENGTH_SHORT)'),
])
print(f"✅ DailyRewardActivity: {n}")

n = rep("ElectionActivity", [
    ('Toast.makeText(this, "صوّتت لـ " + candidateName, Toast.LENGTH_SHORT)',
     'Toast.makeText(this, getString(R.string.election_voted_short) + candidateName, Toast.LENGTH_SHORT)'),
    ('Toast.makeText(this, "لا يوجد مرشحون", Toast.LENGTH_SHORT)',
     'Toast.makeText(this, R.string.no_candidates, Toast.LENGTH_SHORT)'),
    ('Toast.makeText(this, "خطأ: " + e.getMessage(), Toast.LENGTH_SHORT)',
     'Toast.makeText(this, getString(R.string.common_error_prefix) + e.getMessage(), Toast.LENGTH_SHORT)'),
])
print(f"✅ ElectionActivity: {n}")

n = rep("GiftsActivity", [
    ('Toast.makeText(GiftsActivity.this, "خطأ: " + msg, Toast.LENGTH_LONG)',
     'Toast.makeText(GiftsActivity.this, getString(R.string.common_error_prefix) + msg, Toast.LENGTH_LONG)'),
    ('.setTitle("🎁 إرسال " + gift.name)', '.setTitle(getString(R.string.gifts_send_prefix) + gift.name)'),
])
print(f"✅ GiftsActivity: {n}")

n = rep("NewsActivity", [
    ('Toast.makeText(NewsActivity.this, "خطأ: " + msg, Toast.LENGTH_SHORT)',
     'Toast.makeText(NewsActivity.this, getString(R.string.common_error_prefix) + msg, Toast.LENGTH_SHORT)'),
])
print(f"✅ NewsActivity: {n}")

n = rep("ParliamentActivity", [
    ('Toast.makeText(ParliamentActivity.this, "خطأ: " + msg, Toast.LENGTH_SHORT)',
     'Toast.makeText(ParliamentActivity.this, getString(R.string.common_error_prefix) + msg, Toast.LENGTH_SHORT)'),
])
print(f"✅ ParliamentActivity: {n}")

n = rep("PrivateChatActivity", [
    ('Toast.makeText(PrivateChatActivity.this, "خطأ: " + msg, Toast.LENGTH_SHORT)',
     'Toast.makeText(PrivateChatActivity.this, getString(R.string.common_error_prefix) + msg, Toast.LENGTH_SHORT)'),
])
print(f"✅ PrivateChatActivity: {n}")

n = rep("TransferActivity", [
    ('.setTitle("تحويل دينار")', '.setTitle(R.string.transfer_dialog_title)'),
    ('.setTitle("📥 رقمك للاستقبال")', '.setTitle(R.string.wallet_id_label)'),
])
print(f"✅ TransferActivity: {n}")

n = rep("WalletActivity", [
    ('.setTitle("رقمك للاستقبال")', '.setTitle(R.string.wallet_id_label)'),
])
print(f"✅ WalletActivity: {n}")

n = rep("CitizenMarketActivity", [
    ('.setTitle("تأكيد الشراء")', '.setTitle(R.string.cmarket_confirm_dialog)'),
])
print(f"✅ CitizenMarketActivity: {n}")

n = rep("PresidentPardonActivity", [
    ('.setTitle("⚖️  عفو رئاسي")', '.setTitle(R.string.pardon_dialog_title)'),
])
print(f"✅ PresidentPardonActivity: {n}")

n = rep("IdentityCreationActivity", [
    ('.setTitle("🎉 مبروك!")', '.setTitle(R.string.congrats)'),
])
print(f"✅ IdentityCreationActivity: {n}")

n = rep("RedeemCodeActivity", [
    ('.setTitle("🎉 مبروك!")', '.setTitle(R.string.congrats)'),
])
print(f"✅ RedeemCodeActivity: {n}")

n = rep("NavDrawerHelper", [
    (': "مواطن أُمّة";', ': getString(R.string.citizen_ummah);'),
])
print(f"✅ NavDrawerHelper: {n}")

# ═══ BottomNavHelper — الأهم ═══
n = rep("BottomNavHelper", [
    ('addTab(act, nav, "🏠", "الرئيسية", TAB_HOME, activeTab == TAB_HOME, listener);',
     'addTab(act, nav, act.getString(R.string.tab_home), act.getString(R.string.tab_home_label), TAB_HOME, activeTab == TAB_HOME, listener);'),
    ('addTab(act, nav, "🛒", "السوق", TAB_MARKET, activeTab == TAB_MARKET, listener);',
     'addTab(act, nav, act.getString(R.string.tab_market), act.getString(R.string.tab_market_label), TAB_MARKET, activeTab == TAB_MARKET, listener);'),
    ('addTab(act, nav, "💬", "الدردشة", TAB_CHAT, activeTab == TAB_CHAT, listener);',
     'addTab(act, nav, act.getString(R.string.tab_chat), act.getString(R.string.tab_chat_label), TAB_CHAT, activeTab == TAB_CHAT, listener);'),
    ('addTab(act, nav, "👤", "حسابي", TAB_PROFILE, activeTab == TAB_PROFILE, listener);',
     'addTab(act, nav, act.getString(R.string.tab_profile), act.getString(R.string.tab_profile_label), TAB_PROFILE, activeTab == TAB_PROFILE, listener);'),
])
print(f"✅ BottomNavHelper: {n}")

print("\n═══════════════════════════════════════════")
print("📊 الدفعة 7 (النهائية) خلصت!")
print("═══════════════════════════════════════════")
