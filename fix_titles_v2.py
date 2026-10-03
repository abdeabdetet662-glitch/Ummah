#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Fix: move TITLES array initialization from field to onCreate"""
import re

PATH = "app/src/main/java/com/ummah/app/PresidentTitlesActivity.java"

with open(PATH, "r", encoding="utf-8") as f:
    c = f.read()

# 1. إزالة الإعلان القديم بالكامل (من "private final String[] TITLES = {" لـ "};")
old_block = re.search(
    r'\n\s*private final String\[\] TITLES = \{.*?\n\s*\};',
    c, re.DOTALL
)

if not old_block:
    print("❌ ما لقيتش المصفوفة")
    exit(1)

# 2. نخليو فقط إعلان فارغ
c = c.replace(old_block.group(0), "\n    private String[] TITLES;")

# 3. نضيفو التعبئة داخل onCreate بعد super.onCreate()
oncreate_match = re.search(
    r'(protected void onCreate\(Bundle b\) \{\s*\n\s*super\.onCreate\(b\);)',
    c
)

if not oncreate_match:
    print("❌ ما لقيتش onCreate")
    exit(1)

titles_init = '''

        TITLES = new String[]{
                getString(R.string.ptitles_knight),
                getString(R.string.ptitles_star),
                getString(R.string.ptitles_hero),
                getString(R.string.ptitles_noble),
                getString(R.string.ptitles_role_model),
                getString(R.string.ptitles_jewel),
                getString(R.string.ptitles_falcon),
                getString(R.string.ptitles_lion),
        };'''

c = c.replace(
    oncreate_match.group(1),
    oncreate_match.group(1) + titles_init
)

with open(PATH, "w", encoding="utf-8") as f:
    f.write(c)

# 4. عرض النتيجة
print("═══ بعد الإصلاح (أسطر 15-45) ═══")
lines = c.split("\n")
for i in range(14, min(45, len(lines))):
    print(f"{i+1:4d}: {lines[i]}")

print("\n✅ تم نقل TITLES إلى onCreate")
