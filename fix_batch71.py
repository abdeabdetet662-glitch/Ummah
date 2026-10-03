#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Batch 7.1 - Last 24 hardcoded Arabic strings"""
import os

BASE = "app/src/main/res"
JAVA = "app/src/main/java/com/ummah/app"

NEW = {
    # Toasts
    "toast_fill_fields": {"ar": "املأ الحقول", "fr": "Remplissez les champs", "en": "Fill in the fields", "ru": "Заполните поля"},
    "toast_invalid_amount": {"ar": "مبلغ غير صالح", "fr": "Montant invalide", "en": "Invalid amount", "ru": "Неверная сумма"},
    "toast_transfer_done": {"ar": "✅ تم التحويل", "fr": "✅ Transfert effectué", "en": "✅ Transfer complete", "ru": "✅ Перевод выполнен"},
    "toast_copied": {"ar": "تم النسخ", "fr": "Copié", "en": "Copied", "ru": "Скопировано"},
    "toast_copied_check": {"ar": "✓ تم النسخ", "fr": "✓ Copié", "en": "✓ Copied", "ru": "✓ Скопировано"},
    "toast_updating": {"ar": "جاري التحديث...", "fr": "Mise à jour...", "en": "Updating...", "ru": "Обновление..."},
    "toast_avatar_saved": {"ar": "✅ تم حفظ شخصيتك!", "fr": "✅ Votre avatar est sauvegardé!", "en": "✅ Your avatar is saved!", "ru": "✅ Ваш аватар сохранён!"},

    # Identity creation validation
    "id_error_country": {"ar": "⚠️ اختر بلدك أولاً", "fr": "⚠️ Choisissez d'abord votre pays", "en": "⚠️ Choose your country first", "ru": "⚠️ Сначала выберите страну"},
    "id_error_name_empty": {"ar": "⚠️ لازم تكتب اسم", "fr": "⚠️ Vous devez écrire un nom", "en": "⚠️ You must enter a name", "ru": "⚠️ Введите имя"},
    "id_error_name_short": {"ar": "⚠️ الاسم قصير برشا", "fr": "⚠️ Nom trop court", "en": "⚠️ Name is too short", "ru": "⚠️ Имя слишком короткое"},
    "id_error_confirm": {"ar": "⚠️ أكد أنك حفظت الكلمات", "fr": "⚠️ Confirmez avoir sauvegardé les mots", "en": "⚠️ Confirm you saved the words", "ru": "⚠️ Подтвердите сохранение слов"},

    # President
    "pres_invalid_amount": {"ar": "مبلغ غير صحيح", "fr": "Montant incorrect", "en": "Incorrect amount", "ru": "Неверная сумма"},
}

def escape_xml(t):
    t = t.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    t = t.replace("'", "\\u2019")
    t = t.replace("&amp;amp;", "&amp;").replace("&amp;lt;", "&lt;").replace("&amp;gt;", "&gt;")
    return t

# إضافة
print("═══ إضافة النصوص ═══")
for loc, path in {"ar": f"{BASE}/values/strings.xml", "fr": f"{BASE}/values-fr/strings.xml",
                  "en": f"{BASE}/values-en/strings.xml", "ru": f"{BASE}/values-ru/strings.xml"}.items():
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()
    added = 0
    lines = ["\n    <!-- Batch 7.1 -->"]
    for sid, t in NEW.items():
        if f'name="{sid}"' in content: continue
        lines.append(f'    <string name="{sid}">{escape_xml(t[loc])}</string>')
        added += 1
    lines.append("")
    content = content.replace("</resources>", "\n".join(lines) + "\n</resources>")
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"✅ {loc}: +{added}")

# استبدال
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

n = rep("TransferActivity", [
    ('Toast.makeText(this, "املأ الحقول", Toast.LENGTH_SHORT)',
     'Toast.makeText(this, R.string.toast_fill_fields, Toast.LENGTH_SHORT)'),
    ('Toast.makeText(this, "مبلغ غير صالح", Toast.LENGTH_SHORT)',
     'Toast.makeText(this, R.string.toast_invalid_amount, Toast.LENGTH_SHORT)'),
    ('Toast.makeText(TransferActivity.this, "✅ تم التحويل", Toast.LENGTH_LONG)',
     'Toast.makeText(TransferActivity.this, R.string.toast_transfer_done, Toast.LENGTH_LONG)'),
    ('Toast.makeText(this, "تم النسخ", Toast.LENGTH_SHORT)',
     'Toast.makeText(this, R.string.toast_copied, Toast.LENGTH_SHORT)'),
])
print(f"✅ TransferActivity: {n}")

n = rep("TreasuryActivity", [
    ('Toast.makeText(TreasuryActivity.this, "خطأ: " + msg, Toast.LENGTH_SHORT)',
     'Toast.makeText(TreasuryActivity.this, getString(R.string.common_error_prefix) + msg, Toast.LENGTH_SHORT)'),
])
print(f"✅ TreasuryActivity: {n}")

n = rep("WalletActivity", [
    ('Toast.makeText(WalletActivity.this, "جاري التحديث...", Toast.LENGTH_SHORT)',
     'Toast.makeText(WalletActivity.this, R.string.toast_updating, Toast.LENGTH_SHORT)'),
])
print(f"✅ WalletActivity: {n}")

n = rep("JobsActivity", [
    ('Toast.makeText(JobsActivity.this, "خطأ: " + msg, Toast.LENGTH_SHORT)',
     'Toast.makeText(JobsActivity.this, getString(R.string.common_error_prefix) + msg, Toast.LENGTH_SHORT)'),
])
print(f"✅ JobsActivity: {n}")

n = rep("MyJobActivity", [
    ('Toast.makeText(MyJobActivity.this, "خطأ: " + msg, Toast.LENGTH_SHORT)',
     'Toast.makeText(MyJobActivity.this, getString(R.string.common_error_prefix) + msg, Toast.LENGTH_SHORT)'),
])
print(f"✅ MyJobActivity: {n}")

n = rep("AvatarActivity", [
    ('Toast.makeText(AvatarActivity.this, "✅ تم حفظ شخصيتك!", Toast.LENGTH_LONG)',
     'Toast.makeText(AvatarActivity.this, R.string.toast_avatar_saved, Toast.LENGTH_LONG)'),
])
print(f"✅ AvatarActivity: {n}")

n = rep("GarageActivity", [
    ('Toast.makeText(GarageActivity.this, "خطأ: " + msg, Toast.LENGTH_SHORT)',
     'Toast.makeText(GarageActivity.this, getString(R.string.common_error_prefix) + msg, Toast.LENGTH_SHORT)'),
])
print(f"✅ GarageActivity: {n}")

n = rep("CitizenMarketActivity", [
    ('Toast.makeText(CitizenMarketActivity.this, "خطأ: " + msg, Toast.LENGTH_SHORT)',
     'Toast.makeText(CitizenMarketActivity.this, getString(R.string.common_error_prefix) + msg, Toast.LENGTH_SHORT)'),
])
print(f"✅ CitizenMarketActivity: {n}")

n = rep("CityMapActivity", [
    ('Toast.makeText(CityMapActivity.this, "خطأ: " + msg, Toast.LENGTH_SHORT)',
     'Toast.makeText(CityMapActivity.this, getString(R.string.common_error_prefix) + msg, Toast.LENGTH_SHORT)'),
    ('Toast.makeText(this, "خطأ: " + e.getMessage(), Toast.LENGTH_SHORT)',
     'Toast.makeText(this, getString(R.string.common_error_prefix) + e.getMessage(), Toast.LENGTH_SHORT)'),
])
print(f"✅ CityMapActivity: {n}")

n = rep("PresidentAnnounceActivity", [
    ('Toast.makeText(this, "أكمل الحقول", Toast.LENGTH_SHORT)',
     'Toast.makeText(this, R.string.toast_fill_fields, Toast.LENGTH_SHORT)'),
])
print(f"✅ PresidentAnnounceActivity: {n}")

n = rep("PresidentGiftActivity", [
    ('Toast.makeText(this, "مبلغ غير صحيح", Toast.LENGTH_SHORT)',
     'Toast.makeText(this, R.string.pres_invalid_amount, Toast.LENGTH_SHORT)'),
])
print(f"✅ PresidentGiftActivity: {n}")

n = rep("PresidentMinistersActivity", [
    ('Toast.makeText(this, "أكمل الحقول", Toast.LENGTH_SHORT)',
     'Toast.makeText(this, R.string.toast_fill_fields, Toast.LENGTH_SHORT)'),
])
print(f"✅ PresidentMinistersActivity: {n}")

n = rep("PresidentDecreesActivity", [
    ('Toast.makeText(this, "أكمل الحقول", Toast.LENGTH_SHORT)',
     'Toast.makeText(this, R.string.toast_fill_fields, Toast.LENGTH_SHORT)'),
])
print(f"✅ PresidentDecreesActivity: {n}")

n = rep("WheelActivity", [
    ('Toast.makeText(WheelActivity.this, "خطأ: " + msg, Toast.LENGTH_SHORT)',
     'Toast.makeText(WheelActivity.this, getString(R.string.common_error_prefix) + msg, Toast.LENGTH_SHORT)'),
])
print(f"✅ WheelActivity: {n}")

n = rep("IdentityCreationActivity", [
    ('Toast.makeText(this, "✓ تم النسخ", Toast.LENGTH_SHORT)',
     'Toast.makeText(this, R.string.toast_copied_check, Toast.LENGTH_SHORT)'),
    ('Toast.makeText(this, "⚠️ اختر بلدك أولاً", Toast.LENGTH_SHORT)',
     'Toast.makeText(this, R.string.id_error_country, Toast.LENGTH_SHORT)'),
    ('Toast.makeText(this, "⚠️ لازم تكتب اسم", Toast.LENGTH_SHORT)',
     'Toast.makeText(this, R.string.id_error_name_empty, Toast.LENGTH_SHORT)'),
    ('Toast.makeText(this, "⚠️ الاسم قصير برشا", Toast.LENGTH_SHORT)',
     'Toast.makeText(this, R.string.id_error_name_short, Toast.LENGTH_SHORT)'),
    ('Toast.makeText(this, "⚠️ أكد أنك حفظت الكلمات", Toast.LENGTH_SHORT)',
     'Toast.makeText(this, R.string.id_error_confirm, Toast.LENGTH_SHORT)'),
])
print(f"✅ IdentityCreationActivity: {n}")

n = rep("NotificationCenterActivity", [
    ('Toast.makeText(this, "تعذر فتح الصفحة", Toast.LENGTH_SHORT)',
     'Toast.makeText(this, R.string.main_page_error, Toast.LENGTH_SHORT)'),
])
print(f"✅ NotificationCenterActivity: {n}")

print("\n═══════════════════════════════════════════")
print("📊 الدفعة 7.1 خلصت!")
print("═══════════════════════════════════════════")
