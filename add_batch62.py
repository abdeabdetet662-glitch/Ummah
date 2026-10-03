#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Batch 6.2 - Final cleanup of 86 hardcoded Arabic strings"""
import os, re

BASE = "app/src/main/res"
JAVA = "app/src/main/java/com/ummah/app"

# ═══════════════════════════════════════════════════════
# نصوص جديدة
# ═══════════════════════════════════════════════════════
NEW = {
    # Common
    "common_loading": {"ar": "جاري التحميل...", "fr": "Chargement...", "en": "Loading...", "ru": "Загрузка..."},
    "common_error_prefix": {"ar": "خطأ: ", "fr": "Erreur: ", "en": "Error: ", "ru": "Ошибка: "},
    "common_unknown": {"ar": "مجهول", "fr": "Inconnu", "en": "Unknown", "ru": "Неизвестно"},

    # Gifts
    "gifts_tab_received": {"ar": "📥 استقبلت", "fr": "📥 Reçus", "en": "📥 Received", "ru": "📥 Получено"},
    "gifts_tab_sent": {"ar": "📤 أرسلت", "fr": "📤 Envoyés", "en": "📤 Sent", "ru": "📤 Отправлено"},
    "gifts_price_prefix": {"ar": "السعر: ", "fr": "Prix: ", "en": "Price: ", "ru": "Цена: "},
    "gifts_sent_format": {"ar": "✅ تم إرسال %1$s!", "fr": "✅ %1$s envoyé!", "en": "✅ %1$s sent!", "ru": "✅ %1$s отправлен!"},

    # Parliament
    "parl_by": {"ar": "بقلم: ", "fr": "Par: ", "en": "By: ", "ru": "Автор: "},

    # Private Chat
    "pchat_empty_full": {"ar": "لا توجد رسائل بعد.\\nابدأ المحادثة!", "fr": "Aucun message.\\nCommencez la conversation!", "en": "No messages yet.\\nStart the conversation!", "ru": "Сообщений нет.\\nНачните разговор!"},

    # Transfer
    "transfer_title": {"ar": "💸 التحويلات", "fr": "💸 Transferts", "en": "💸 Transfers", "ru": "💸 Переводы"},
    "transfer_sub": {"ar": "أرسل ديناراً لمواطن آخر في أي مكان في العالم", "fr": "Envoyez un dinar à un autre citoyen partout dans le monde", "en": "Send dinar to another citizen anywhere in the world", "ru": "Отправьте динар другому гражданину в любую точку мира"},
    "transfer_send_btn": {"ar": "📤  إرسال دينار", "fr": "📤  Envoyer", "en": "📤  Send Dinar", "ru": "📤  Отправить"},
    "transfer_my_id": {"ar": "📥  رقمي للاستقبال", "fr": "📥  Mon ID pour recevoir", "en": "📥  My ID to receive", "ru": "📥  Мой ID для получения"},
    "transfer_empty": {"ar": "لا توجد تحويلات بعد.", "fr": "Aucun transfert pour le moment.", "en": "No transfers yet.", "ru": "Переводов пока нет."},

    # Treasury
    "treasury_info": {"ar": "\\n\\nالخزينة العامة ممولة من تبرعات المواطنين.\\nتُستخدم لتمويل المشاريع العامة، ومنح المحتاجين، ومكافآت المبدعين.", "fr": "\\n\\nLe trésor public est financé par les dons des citoyens.\\nIl finance les projets publics, aide les nécessiteux et récompense les créatifs.", "en": "\\n\\nThe public treasury is funded by citizen donations.\\nIt funds public projects, helps the needy, and rewards creators.", "ru": "\\n\\nГосударственная казна финансируется за счёт пожертвований граждан.\\nСредства идут на общественные проекты, помощь нуждающимся и награды творцам."},

    # Wallet
    "wallet_bal_label": {"ar": "✦  رصيدك الحالي  ✦", "fr": "✦  Votre solde  ✦", "en": "✦  Your Balance  ✦", "ru": "✦  Ваш баланс  ✦"},
    "wallet_id_label": {"ar": "📥  رقمك للاستقبال", "fr": "📥  Votre ID pour recevoir", "en": "📥  Your receiving ID", "ru": "📥  Ваш ID для получения"},
    "wallet_earn_section": {"ar": "  💎  طرق كسب الدينار  ", "fr": "  💎  Façons de gagner des dinars  ", "en": "  💎  Ways to earn dinars  ", "ru": "  💎  Способы заработать динары  "},

    # Splash
    "splash_sub": {"ar": "أول دولة رقمية عربية", "fr": "Le premier État numérique arabe", "en": "The first Arab digital state", "ru": "Первое арабское цифровое государство"},
    "splash_footer": {"ar": "☆  حوكمة رقمية حقيقية  ☆", "fr": "☆  Une vraie gouvernance numérique  ☆", "en": "☆  Real digital governance  ☆", "ru": "☆  Настоящее цифровое управление  ☆"},

    # Inventory
    "inv_wear": {"ar": "👕 البس", "fr": "👕 Porter", "en": "👕 Wear", "ru": "👕 Надеть"},

    # Jobs
    "jobs_salary_format": {"ar": "💰 %1$d Đ / عملة", "fr": "💰 %1$d Đ / action", "en": "💰 %1$d Đ / action", "ru": "💰 %1$d Đ / действие"},

    # My Job
    "job_level": {"ar": "⭐ المستوى: ", "fr": "⭐ Niveau: ", "en": "⭐ Level: ", "ru": "⭐ Уровень: "},
    "job_cooldown": {"ar": "⏳ انتظر: ", "fr": "⏳ Attendez: ", "en": "⏳ Wait: ", "ru": "⏳ Подождите: "},

    # Avatar
    "avatar_sub": {"ar": "اسحب لتدوير • قرّب بإصبعين • بدّل الألوان", "fr": "Glissez pour tourner • Pincez pour zoomer • Changez les couleurs", "en": "Drag to rotate • Pinch to zoom • Change colors", "ru": "Проведите для вращения • Сведите пальцы для зума • Меняйте цвета"},

    # Life Stats
    "life_balance": {"ar": "💰  رصيدك", "fr": "💰  Votre solde", "en": "💰  Your balance", "ru": "💰  Ваш баланс"},

    # Garage
    "garage_price": {"ar": "💰 سعر الشراء: ", "fr": "💰 Prix d'achat: ", "en": "💰 Purchase price: ", "ru": "💰 Цена покупки: "},

    # City Map
    "city_balance": {"ar": "💰 رصيدك: ", "fr": "💰 Votre solde: ", "en": "💰 Your balance: ", "ru": "💰 Ваш баланс: "},
    "city_choose_building": {"ar": "اختر نوع المبنى 👇", "fr": "Choisissez le type de bâtiment 👇", "en": "Choose building type 👇", "ru": "Выберите тип здания 👇"},

    # President Badge
    "president_badge": {"ar": "رئيس معتمد", "fr": "Président certifié", "en": "Certified President", "ru": "Заверенный президент"},

    # President Pardon
    "pardon_sub": {"ar": "المواطنون المحظورون أو المكتومون", "fr": "Citoyens bloqués ou muets", "en": "Blocked or muted citizens", "ru": "Заблокированные или заглушённые"},
    "pardon_empty": {"ar": "✅ ما فيه حتى مواطن محظور أو مكتوم", "fr": "✅ Aucun citoyen bloqué ou muet", "en": "✅ No blocked or muted citizens", "ru": "✅ Нет заблокированных или заглушённых"},
    "pardon_blocked": {"ar": "🚫 محظور  ", "fr": "🚫 Bloqué  ", "en": "🚫 Blocked  ", "ru": "🚫 Заблокирован  "},
    "pardon_muted": {"ar": "🔇 مكتوم", "fr": "🔇 Muet", "en": "🔇 Muted", "ru": "🔇 Заглушён"},

    "president_unknown": {"ar": "الرئيس", "fr": "Président", "en": "President", "ru": "Президент"},

    # Wheel
    "wheel_spin_cost": {"ar": "🎰  دوّر العجلة (%1$d Đ)", "fr": "🎰  Tourner (%1$d Đ)", "en": "🎰  Spin (%1$d Đ)", "ru": "🎰  Крутить (%1$d Đ)"},
    "wheel_disabled": {"ar": "معطّل", "fr": "Désactivé", "en": "Disabled", "ru": "Отключено"},
    "wheel_free_spin": {"ar": "⏳ الدورة المجانية القادمة بعد: %1$d س %2$d د", "fr": "⏳ Prochain tour gratuit dans: %1$dh %2$dm", "en": "⏳ Next free spin in: %1$dh %2$dm", "ru": "⏳ Следующее бесплатное вращение: %1$dч %2$dм"},

    # Welcome
    "welcome_skip": {"ar": "تخطي", "fr": "Passer", "en": "Skip", "ru": "Пропустить"},
    "welcome_next": {"ar": "التالي ←", "fr": "Suivant →", "en": "Next →", "ru": "Далее →"},
    "welcome_have_account": {"ar": "لدي حساب — استعادة", "fr": "J'ai un compte — Restaurer", "en": "I have an account — Restore", "ru": "У меня есть аккаунт — Восстановить"},
    "welcome_start": {"ar": "🚀 ابدأ الآن", "fr": "🚀 Commencer", "en": "🚀 Start now", "ru": "🚀 Начать"},

    # Identity Creation
    "id_step_of": {"ar": "الخطوة %1$d من %2$d", "fr": "Étape %1$d sur %2$d", "en": "Step %1$d of %2$d", "ru": "Шаг %1$d из %2$d"},
    "id_step1": {"ar": "١ • اختر بلدك", "fr": "١ • Choisissez votre pays", "en": "١ • Choose your country", "ru": "١ • Выберите страну"},
    "id_step2": {"ar": "٢ • اختر اسمك", "fr": "٢ • Choisissez votre nom", "en": "٢ • Choose your name", "ru": "٢ • Выберите имя"},
    "id_step3": {"ar": "٣ • كلماتك السرية", "fr": "٣ • Vos mots secrets", "en": "٣ • Your secret words", "ru": "٣ • Секретные слова"},
    "id_step4": {"ar": "٤ • مبروك!", "fr": "٤ • Félicitations!", "en": "٤ • Congratulations!", "ru": "٤ • Поздравляем!"},
    "id_back": {"ar": "← رجوع", "fr": "← Retour", "en": "← Back", "ru": "← Назад"},
    "id_confirm": {"ar": "تأكيد ✓", "fr": "Confirmer ✓", "en": "Confirm ✓", "ru": "Подтвердить ✓"},
    "id_start_journey": {"ar": "🎁 ابدأ رحلتك", "fr": "🎁 Commencez votre voyage", "en": "🎁 Start your journey", "ru": "🎁 Начните путешествие"},
    "id_country_hint": {"ar": "من وين راك؟ اختر بلدك:", "fr": "D'où venez-vous? Choisissez votre pays:", "en": "Where are you from? Choose your country:", "ru": "Откуда вы? Выберите страну:"},
    "id_name_tip": {"ar": "💡 اختر اسماً يعرفوك بيه، هذا اسمك الرسمي في الدولة", "fr": "💡 Choisissez un nom connu, c'est votre nom officiel dans l'État", "en": "💡 Choose a known name, this is your official name in the state", "ru": "💡 Выберите известное имя — это ваше официальное имя в государстве"},
    "id_seed_warning": {"ar": "⚠️ احفظ هذه الكلمات في مكان آمن — هي هويتك الوحيدة!", "fr": "⚠️ Conservez ces mots en lieu sûr — c'est votre seule identité!", "en": "⚠️ Keep these words safe — it's your only identity!", "ru": "⚠️ Храните слова в безопасности — это ваша единственная личность!"},
    "id_copy_seed": {"ar": "📋 انسخ الكلمات", "fr": "📋 Copier les mots", "en": "📋 Copy words", "ru": "📋 Копировать слова"},
    "id_confirm_saved": {"ar": "✓ حفظت الكلمات في مكان آمن", "fr": "✓ J'ai sauvegardé les mots en lieu sûr", "en": "✓ I saved the words safely", "ru": "✓ Я сохранил слова в безопасности"},
    "id_congrats": {"ar": "مبروك يا %1$s!", "fr": "Félicitations %1$s!", "en": "Congratulations %1$s!", "ru": "Поздравляем, %1$s!"},
    "id_national_id": {"ar": "رقمك الوطني", "fr": "Votre ID national", "en": "Your National ID", "ru": "Ваш национальный ID"},
    "id_welcome_reward": {"ar": "💰 +5 دج مكافأة ترحيب في محفظتك", "fr": "💰 +5 Đ de bonus de bienvenue dans votre portefeuille", "en": "💰 +5 Đ welcome bonus in your wallet", "ru": "💰 +5 Đ приветственный бонус в кошельке"},

    # Redeem Code
    "redeem_title": {"ar": "استبدال كود", "fr": "Échanger un code", "en": "Redeem Code", "ru": "Погасить код"},
    "redeem_sub": {"ar": "عندك كود من الإدارة؟ استبدلو دابا!", "fr": "Vous avez un code de l'administration? Échangez-le maintenant!", "en": "Got a code from admin? Redeem it now!", "ru": "Есть код от администрации? Погасите сейчас!"},
    "redeem_balance": {"ar": "💰 رصيدك الحالي", "fr": "💰 Votre solde actuel", "en": "💰 Your current balance", "ru": "💰 Ваш текущий баланс"},
    "redeem_input": {"ar": "🎫 اكتب الكود هنا", "fr": "🎫 Entrez le code ici", "en": "🎫 Enter code here", "ru": "🎫 Введите код здесь"},
    "redeem_paste": {"ar": "📋 لصق من الحافظة", "fr": "📋 Coller du presse-papiers", "en": "📋 Paste from clipboard", "ru": "📋 Вставить из буфера"},
    "redeem_btn": {"ar": "✨ استبدل الكود", "fr": "✨ Échanger le code", "en": "✨ Redeem code", "ru": "✨ Погасить код"},
    "redeem_footer": {"ar": "⚠️ كل كود يُستعمل مرة واحدة فقط", "fr": "⚠️ Chaque code ne peut être utilisé qu'une seule fois", "en": "⚠️ Each code can only be used once", "ru": "⚠️ Каждый код используется только один раз"},
    "redeem_verifying": {"ar": "⏳ جاري التحقق...", "fr": "⏳ Vérification...", "en": "⏳ Verifying...", "ru": "⏳ Проверка..."},

    # Notifications
    "notif_title": {"ar": "مركز الإشعارات", "fr": "Centre de notifications", "en": "Notification Center", "ru": "Центр уведомлений"},
    "notif_loading": {"ar": "⏳ جاري التحميل...", "fr": "⏳ Chargement...", "en": "⏳ Loading...", "ru": "⏳ Загрузка..."},
    "notif_empty": {"ar": "📭\\n\\nماكانش إشعارات حالياً", "fr": "📭\\n\\nAucune notification pour le moment", "en": "📭\\n\\nNo notifications currently", "ru": "📭\\n\\nУведомлений пока нет"},
    "notif_not_registered": {"ar": "❌ ما راكش مسجل", "fr": "❌ Non enregistré", "en": "❌ Not registered", "ru": "❌ Не зарегистрирован"},
    "notif_no_notifs": {"ar": "📭 ماكانش إشعارات", "fr": "📭 Aucune notification", "en": "📭 No notifications", "ru": "📭 Нет уведомлений"},
    "notif_count": {"ar": "📊 %1$d إشعار", "fr": "📊 %1$d notification(s)", "en": "📊 %1$d notification(s)", "ru": "📊 %1$d уведомление"},

    # SellItem format
    "sell_original_price_fmt": {"ar": "💰 سعر الشراء الأصلي: %1$d Đ", "fr": "💰 Prix d'achat original: %1$d Đ", "en": "💰 Original purchase price: %1$d Đ", "ru": "💰 Исходная цена покупки: %1$d Đ"},
    "sell_commission_fmt": {"ar": "⚠️ عمولة التطبيق: 5%\\nمن %1$d Đ → تستلم %2$d Đ", "fr": "⚠️ Commission: 5%\\nde %1$d Đ → Vous recevez %2$d Đ", "en": "⚠️ App commission: 5%\\nof %1$d Đ → You receive %2$d Đ", "ru": "⚠️ Комиссия: 5%\\nот %1$d Đ → Вы получите %2$d Đ"},
}

def escape_xml(t):
    t = t.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    t = t.replace("&amp;amp;", "&amp;").replace("&amp;lt;", "&lt;").replace("&amp;gt;", "&gt;")
    return t

# ═══ إضافة النصوص ═══
print("═══ إضافة النصوص ═══")
for loc, path in {"ar": f"{BASE}/values/strings.xml", "fr": f"{BASE}/values-fr/strings.xml",
                  "en": f"{BASE}/values-en/strings.xml", "ru": f"{BASE}/values-ru/strings.xml"}.items():
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()
    added = 0
    lines = ["\n    <!-- Batch 6.2 -->"]
    for sid, t in NEW.items():
        if f'name="{sid}"' in content: continue
        lines.append(f'    <string name="{sid}">{escape_xml(t[loc])}</string>')
        added += 1
    lines.append("")
    content = content.replace("</resources>", "\n".join(lines) + "\n</resources>")
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"✅ {loc}: +{added}")

# ═══ استبدال في Activities ═══
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

# ElectionActivity
n = rep("ElectionActivity", [
    ('nameView.setText("👤 " + (name != null ? name : "مجهول"))',
     'nameView.setText("👤 " + (name != null ? name : getString(R.string.common_unknown)))'),
    ('myVoteView.setText("✅ صوّتت لـ: " + candidateName)',
     'myVoteView.setText(getString(R.string.election_voted_for) + candidateName)'),
])
print(f"✅ ElectionActivity: {n}")

# GiftsActivity
n = rep("GiftsActivity", [
    ('recTab.setText("📥 استقبلت")', 'recTab.setText(R.string.gifts_tab_received)'),
    ('sentTab.setText("📤 أرسلت")', 'sentTab.setText(R.string.gifts_tab_sent)'),
    ('priceView.setText("السعر: " + gift.price + " Đ")',
     'priceView.setText(getString(R.string.gifts_price_prefix) + gift.price + " Đ")'),
    ('msg.setText("✅ تم إرسال " + gift.name + "!")',
     'msg.setText(getString(R.string.gifts_sent_format, gift.name))'),
])
print(f"✅ GiftsActivity: {n}")

# NewsActivity
n = rep("NewsActivity", [
    ('author.setText("👤 " + (n.author != null ? n.author : "مجهول"))',
     'author.setText("👤 " + (n.author != null ? n.author : getString(R.string.common_unknown)))'),
])
print(f"✅ NewsActivity: {n}")

# ParliamentActivity
n = rep("ParliamentActivity", [
    ('author.setText("بقلم: " + p.author)',
     'author.setText(getString(R.string.parl_by) + p.author)'),
])
print(f"✅ ParliamentActivity: {n}")

# PrivateChatActivity
n = rep("PrivateChatActivity", [
    ('sub.setText("مع: " + otherName)',
     'sub.setText(getString(R.string.pchat_with) + otherName)'),
    ('empty.setText("لا توجد رسائل بعد.\\nابدأ المحادثة!")',
     'empty.setText(R.string.pchat_empty_full)'),
])
print(f"✅ PrivateChatActivity: {n}")

# TransferActivity
n = rep("TransferActivity", [
    ('title.setText("💸 التحويلات")', 'title.setText(R.string.transfer_title)'),
    ('sub.setText("أرسل ديناراً لمواطن آخر في أي مكان في العالم")',
     'sub.setText(R.string.transfer_sub)'),
    ('sendBtn.setText("📤  إرسال دينار")', 'sendBtn.setText(R.string.transfer_send_btn)'),
    ('myIdBtn.setText("📥  رقمي للاستقبال")', 'myIdBtn.setText(R.string.transfer_my_id)'),
    ('empty.setText("لا توجد تحويلات بعد.")', 'empty.setText(R.string.transfer_empty)'),
])
print(f"✅ TransferActivity: {n}")

# TreasuryActivity
n = rep("TreasuryActivity", [
    ('info.setText("\\n\\nالخزينة العامة ممولة من تبرعات المواطنين.\\nتُستخدم لتمويل المشاريع العامة، ومنح المحتاجين، ومكافآت المبدعين.")',
     'info.setText(R.string.treasury_info)'),
])
print(f"✅ TreasuryActivity: {n}")

# WalletActivity
n = rep("WalletActivity", [
    ('balLabel.setText("✦  رصيدك الحالي  ✦")', 'balLabel.setText(R.string.wallet_bal_label)'),
    ('idLabel.setText("📥  رقمك للاستقبال")', 'idLabel.setText(R.string.wallet_id_label)'),
    ('secText.setText("  💎  طرق كسب الدينار  ")', 'secText.setText(R.string.wallet_earn_section)'),
])
print(f"✅ WalletActivity: {n}")

# SplashActivity
n = rep("SplashActivity", [
    ('sub.setText("أول دولة رقمية عربية")', 'sub.setText(R.string.splash_sub)'),
    ('footer.setText("☆  حوكمة رقمية حقيقية  ☆")', 'footer.setText(R.string.splash_footer)'),
])
print(f"✅ SplashActivity: {n}")

# MarketActivity
n = rep("MarketActivity", [
    ('loading.setText("جاري التحميل...")', 'loading.setText(R.string.common_loading)'),
    ('err.setText("خطأ: " + msg)', 'err.setText(getString(R.string.common_error_prefix) + msg)'),
])
print(f"✅ MarketActivity: {n}")

# MyInventoryActivity
n = rep("MyInventoryActivity", [
    ('err.setText("خطأ: " + msg)', 'err.setText(getString(R.string.common_error_prefix) + msg)'),
    ('wearBtn.setText("👕 البس")', 'wearBtn.setText(R.string.inv_wear)'),
])
print(f"✅ MyInventoryActivity: {n}")

# JobsActivity
n = rep("JobsActivity", [
    ('salary.setText("💰 " + job.salary + " Đ / عملة")',
     'salary.setText(getString(R.string.jobs_salary_format, job.salary))'),
])
print(f"✅ JobsActivity: {n}")

# MyJobActivity
n = rep("MyJobActivity", [
    ('levelView.setText("⭐ المستوى: " + level)',
     'levelView.setText(getString(R.string.job_level) + level)'),
    ('cooldownView.setText("⏳ انتظر: " + min + ":" + String.format("%02d", sec))',
     'cooldownView.setText(getString(R.string.job_cooldown) + min + ":" + String.format("%02d", sec))'),
])
print(f"✅ MyJobActivity: {n}")

# AvatarActivity
n = rep("AvatarActivity", [
    ('sub.setText("اسحب لتدوير • قرّب بإصبعين • بدّل الألوان")',
     'sub.setText(R.string.avatar_sub)'),
])
print(f"✅ AvatarActivity: {n}")

# LifeStatsActivity
n = rep("LifeStatsActivity", [
    ('balLabel.setText("💰  رصيدك")', 'balLabel.setText(R.string.life_balance)'),
])
print(f"✅ LifeStatsActivity: {n}")

# GarageActivity
n = rep("GarageActivity", [
    ('price.setText("💰 سعر الشراء: " + item.price + " Đ")',
     'price.setText(getString(R.string.garage_price) + item.price + " Đ")'),
])
print(f"✅ GarageActivity: {n}")

# SellItemActivity — استعمل الـ format strings
n = rep("SellItemActivity", [
    ('orig.setText("💰 سعر الشراء الأصلي: " + originalPrice + " Đ")',
     'orig.setText(getString(R.string.sell_original_price_fmt, originalPrice))'),
    ('hint.setText("⚠️ عمولة التطبيق: 5%\\\\nمن " + originalPrice + " Đ → تستلم " + (originalPrice - commission) + " Đ")',
     'hint.setText(getString(R.string.sell_commission_fmt, originalPrice, originalPrice - commission))'),
])
print(f"✅ SellItemActivity: {n}")

# CityMapActivity
n = rep("CityMapActivity", [
    ('header.setText("💰 رصيدك: " + myBalance + " Đ")',
     'header.setText(getString(R.string.city_balance) + myBalance + " Đ")'),
    ('hint.setText("اختر نوع المبنى 👇")', 'hint.setText(R.string.city_choose_building)'),
])
print(f"✅ CityMapActivity: {n}")

# PresidentBadgeHelper
n = rep("PresidentBadgeHelper", [
    ('text.setText("رئيس معتمد")', 'text.setText(R.string.president_badge)'),
])
print(f"✅ PresidentBadgeHelper: {n}")

# PresidentPardonActivity
n = rep("PresidentPardonActivity", [
    ('sub.setText("المواطنون المحظورون أو المكتومون")', 'sub.setText(R.string.pardon_sub)'),
    ('empty.setText("✅ ما فيه حتى مواطن محظور أو مكتوم")', 'empty.setText(R.string.pardon_empty)'),
    ('b.setText("🚫 محظور  ")', 'b.setText(R.string.pardon_blocked)'),
    ('m.setText("🔇 مكتوم")', 'm.setText(R.string.pardon_muted)'),
])
print(f"✅ PresidentPardonActivity: {n}")

# PresidentAnnouncementsListActivity
n = rep("PresidentAnnouncementsListActivity", [
    ('footer.setText("👑 " + (president != null ? president : "الرئيس")',
     'footer.setText("👑 " + (president != null ? president : getString(R.string.president_unknown))'),
])
print(f"✅ PresidentAnnouncementsListActivity: {n}")

# WheelActivity
n = rep("WheelActivity", [
    ('spinBtn.setText("🎰  دوّر العجلة (" + c.spinCost + " Đ)")',
     'spinBtn.setText(getString(R.string.wheel_spin_cost, c.spinCost))'),
    ('jackpotView.setText("معطّل")', 'jackpotView.setText(R.string.wheel_disabled)'),
    ('freeSpinView.setText("⏳ الدورة المجانية القادمة بعد: " + hours + " س " + minutes + " د")',
     'freeSpinView.setText(getString(R.string.wheel_free_spin, hours, minutes))'),
])
print(f"✅ WheelActivity: {n}")

# WelcomeActivity
n = rep("WelcomeActivity", [
    ('btnSkip.setText("تخطي")', 'btnSkip.setText(R.string.welcome_skip)'),
    ('btnNext.setText("التالي ←")', 'btnNext.setText(R.string.welcome_next)'),
    ('btnHaveAccount.setText("لدي حساب — استعادة")', 'btnHaveAccount.setText(R.string.welcome_have_account)'),
    ('btnNext.setText("🚀 ابدأ الآن")', 'btnNext.setText(R.string.welcome_start)'),
])
print(f"✅ WelcomeActivity: {n}")

# IdentityCreationActivity
n = rep("IdentityCreationActivity", [
    ('tvProgress.setText("الخطوة 1 من 4")', 'tvProgress.setText(getString(R.string.id_step_of, 1, 4))'),
    ('tvStep.setText("١ • اختر بلدك")', 'tvStep.setText(R.string.id_step1)'),
    ('btnBack.setText("← رجوع")', 'btnBack.setText(R.string.id_back)'),
    ('btnNext.setText("التالي ←")', 'btnNext.setText(R.string.welcome_next)'),
    ('tvProgress.setText("الخطوة " + step + " من " + TOTAL_STEPS)',
     'tvProgress.setText(getString(R.string.id_step_of, step, TOTAL_STEPS))'),
    ('tvStep.setText("٢ • اختر اسمك")', 'tvStep.setText(R.string.id_step2)'),
    ('tvStep.setText("٣ • كلماتك السرية")', 'tvStep.setText(R.string.id_step3)'),
    ('btnNext.setText("تأكيد ✓")', 'btnNext.setText(R.string.id_confirm)'),
    ('tvStep.setText("٤ • مبروك!")', 'tvStep.setText(R.string.id_step4)'),
    ('btnNext.setText("🎁 ابدأ رحلتك")', 'btnNext.setText(R.string.id_start_journey)'),
    ('hint.setText("من وين راك؟ اختر بلدك:")', 'hint.setText(R.string.id_country_hint)'),
    ('tip.setText("💡 اختر اسماً يعرفوك بيه، هذا اسمك الرسمي في الدولة")',
     'tip.setText(R.string.id_name_tip)'),
    ('warning.setText("⚠️ احفظ هذه الكلمات في مكان آمن — هي هويتك الوحيدة!")',
     'warning.setText(R.string.id_seed_warning)'),
    ('btnCopy.setText("📋 انسخ الكلمات")', 'btnCopy.setText(R.string.id_copy_seed)'),
    ('cbConfirm.setText("✓ حفظت الكلمات في مكان آمن")', 'cbConfirm.setText(R.string.id_confirm_saved)'),
    ('congrats.setText("مبروك يا " + userName + "!")',
     'congrats.setText(getString(R.string.id_congrats, userName))'),
    ('idLabel.setText("رقمك الوطني")', 'idLabel.setText(R.string.id_national_id)'),
    ('reward.setText("💰 +5 دج مكافأة ترحيب في محفظتك")',
     'reward.setText(R.string.id_welcome_reward)'),
])
print(f"✅ IdentityCreationActivity: {n}")

# RedeemCodeActivity
n = rep("RedeemCodeActivity", [
    ('title.setText("استبدال كود")', 'title.setText(R.string.redeem_title)'),
    ('sub.setText("عندك كود من الإدارة؟ استبدلو دابا!")', 'sub.setText(R.string.redeem_sub)'),
    ('balLbl.setText("💰 رصيدك الحالي")', 'balLbl.setText(R.string.redeem_balance)'),
    ('inputLbl.setText("🎫 اكتب الكود هنا")', 'inputLbl.setText(R.string.redeem_input)'),
    ('btnPaste.setText("📋 لصق من الحافظة")', 'btnPaste.setText(R.string.redeem_paste)'),
    ('btnRedeem.setText("✨ استبدل الكود")', 'btnRedeem.setText(R.string.redeem_btn)'),
    ('footer.setText("⚠️ كل كود يُستعمل مرة واحدة فقط")', 'footer.setText(R.string.redeem_footer)'),
    ('btnRedeem.setText("⏳ جاري التحقق...")', 'btnRedeem.setText(R.string.redeem_verifying)'),
])
print(f"✅ RedeemCodeActivity: {n}")

# NotificationCenterActivity
n = rep("NotificationCenterActivity", [
    ('title.setText("مركز الإشعارات")', 'title.setText(R.string.notif_title)'),
    ('statsView.setText("⏳ جاري التحميل...")', 'statsView.setText(R.string.notif_loading)'),
    ('emptyView.setText("📭\\n\\nماكانش إشعارات حالياً")', 'emptyView.setText(R.string.notif_empty)'),
    ('statsView.setText("❌ ما راكش مسجل")', 'statsView.setText(R.string.notif_not_registered)'),
    ('statsView.setText("📭 ماكانش إشعارات")', 'statsView.setText(R.string.notif_no_notifs)'),
    ('statsView.setText("📊 " + list.size() + " إشعار")',
     'statsView.setText(getString(R.string.notif_count, list.size()))'),
])
print(f"✅ NotificationCenterActivity: {n}")

print("\n═══════════════════════════════════════════")
print("📊 الدفعة 6.2 خلصت!")
print("═══════════════════════════════════════════")
