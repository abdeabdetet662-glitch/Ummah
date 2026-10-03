#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Fix: NavDrawerHelper getString → context.getString"""
import re

PATH = "app/src/main/java/com/ummah/app/NavDrawerHelper.java"

with open(PATH, "r", encoding="utf-8") as f:
    c = f.read()

print("═══ السطر المشكل (قبل) ═══")
for i, line in enumerate(c.split("\n"), 1):
    if "citizen_ummah" in line or (240 <= i <= 250):
        print(f"{i:4d}: {line}")

# نشوفو اسم الـ context variable المستعمل في نفس الميثود
# نشوفو السطر 244 وش كاين قبلو
lines = c.split("\n")
for i in range(230, 250):
    if i < len(lines):
        print(f"{i+1:4d}: {lines[i]}")

