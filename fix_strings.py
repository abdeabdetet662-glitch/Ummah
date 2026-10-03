#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Fix: escape ' → \u2019 + add formatted=false to sell_commission_fmt"""
import re

FILES = [
    "app/src/main/res/values/strings.xml",
    "app/src/main/res/values-fr/strings.xml",
    "app/src/main/res/values-en/strings.xml",
    "app/src/main/res/values-ru/strings.xml",
]

for path in FILES:
    with open(path, "r", encoding="utf-8") as f:
        c = f.read()
    
    before = c.count("'")
    
    # 1. استبدال ' بـ \u2019 (escape للأبوستروف)
    c = c.replace("'", "\\u2019")
    
    # 2. sell_commission_fmt: formatted="false"
    c = re.sub(
        r'<string name="sell_commission_fmt">',
        '<string name="sell_commission_fmt" formatted="false">',
        c
    )
    
    # 3. sell_original_price_fmt + sell_suggested_price + DailyReward — قد تحتاج أيضاً
    # (فقط sell_commission_fmt عنده مشكل % غير positional)
    
    with open(path, "w", encoding="utf-8") as f:
        f.write(c)
    
    print(f"✅ {path}: {before} apostrophes escaped")

print("\n═══════════════════════════════════════════")
print("📊 تم إصلاح كل الملفات!")
print("═══════════════════════════════════════════")
