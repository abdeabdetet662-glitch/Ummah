#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Fix: static array using getString() → move to instance field"""
import re

PATH = "app/src/main/java/com/ummah/app/PresidentTitlesActivity.java"

with open(PATH, "r", encoding="utf-8") as f:
    c = f.read()

# 1. شوف السياق
print("═══ قبل الإصلاح (أسطر 15-35) ═══")
lines = c.split("\n")
for i in range(14, min(35, len(lines))):
    print(f"{i+1:4d}: {lines[i]}")

# 2. إزالة "static" من الإعلان
c = re.sub(
    r'(private|public|protected)\s+static\s+final\s+String\[\]\s+(\w+)',
    r'\1 final String[] \2',
    c
)

# 3. إذا كان فيه static method يستعملها، نحولو
# الأسهل: نخليها عادية + نستعملها من onCreate

with open(PATH, "w", encoding="utf-8") as f:
    f.write(c)

print("\n═══ بعد الإصلاح (أسطر 15-35) ═══")
lines = c.split("\n")
for i in range(14, min(35, len(lines))):
    print(f"{i+1:4d}: {lines[i]}")

print("\n✅ تم إزالة static من المصفوفة")
