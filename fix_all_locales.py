#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Add attachBaseContext to ALL activities in Ummah app"""
import os, re

JAVA = "app/src/main/java/com/ummah/app"

# الكود اللي نضيفوه
ATTACH_METHOD = """
    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }
"""

# نلقاو كل ملفات Activity
activities = []
for f in sorted(os.listdir(JAVA)):
    if f.endswith("Activity.java"):
        activities.append(os.path.join(JAVA, f))

print(f"📁 لقيت {len(activities)} Activity")
print()

fixed = 0
skipped = 0
already = 0

for path in activities:
    name = os.path.basename(path)
    with open(path, "r", encoding="utf-8") as f:
        c = f.read()
    
    # إذا فيه أصلاً attachBaseContext
    if "attachBaseContext" in c:
        already += 1
        print(f"⏭️  {name}: عندو")
        continue
    
    # نلقاو آخر } في الكلاس ونضيفو قبلو
    # نستعملو regex: آخر } في الملف
    last_brace = c.rfind("}")
    if last_brace == -1:
        skipped += 1
        print(f"⚠️  {name}: ما لقيتش }}")
        continue
    
    # نضيفو الدالة قبل الـ } الأخيرة
    new_c = c[:last_brace] + ATTACH_METHOD + "\n" + c[last_brace:]
    
    with open(path, "w", encoding="utf-8") as f:
        f.write(new_c)
    
    fixed += 1
    print(f"✅ {name}")

print()
print("═══════════════════════════════════════════")
print(f"📊 النتيجة:")
print(f"   ✅ مضاف جديد: {fixed}")
print(f"   ⏭️  كان موجود: {already}")
print(f"   ⚠️  تخطى: {skipped}")
print(f"   📁 الإجمالي: {len(activities)}")
print("═══════════════════════════════════════════")
