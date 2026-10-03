#!/bin/bash
echo "═══════════════════════════════════════════"
echo "🔍 تشخيص شامل لمشروع أُمّة"
echo "═══════════════════════════════════════════"
echo ""

echo "📁 1. حالة git:"
git status --short
echo ""
echo "📝 آخر 3 commits:"
git log --oneline -3
echo ""

echo "📊 2. عدد النصوص في كل لغة:"
for loc in values values-fr values-en values-ru; do
    if [ -f "app/src/main/res/$loc/strings.xml" ]; then
        count=$(grep -c '<string name=' "app/src/main/res/$loc/strings.xml")
        echo "  $loc: $count"
    fi
done
echo ""

echo "🔧 3. LocaleHelper:"
ls -la app/src/main/java/com/ummah/app/LocaleHelper.java 2>/dev/null || echo "  ❌ ما لقيتوش"
echo ""

echo "🔧 4. locales_config.xml:"
cat app/src/main/res/xml/locales_config.xml 2>/dev/null || echo "  ❌ ما لقيتوش"
echo ""

echo "🔧 5. attachBaseContext في Activities:"
grep -rl "attachBaseContext" app/src/main/java/com/ummah/app/ | wc -l
echo "  (من أصل 44 Activity)"
echo ""

echo "🔧 6. MainActivity — كيفاش كيتعامل مع attachBaseContext:"
grep -A2 "attachBaseContext" app/src/main/java/com/ummah/app/MainActivity.java 2>/dev/null
echo ""

echo "🔧 7. Settings — كيفاش كيبدل اللغة:"
grep -n -A2 "LocaleHelper.setLocale\|setLocale" app/src/main/java/com/ummah/app/SettingsActivity.java 2>/dev/null | head -20
echo ""

echo "🔧 8. AndroidManifest — localeConfig:"
grep -n "localeConfig" app/src/main/AndroidManifest.xml
echo ""

echo "🔧 9. build.gradle — androidResources:"
grep -n "androidResources\|generateLocaleConfig\|resConfigs" app/build.gradle
echo ""

echo "═══════════════════════════════════════════"
echo "✅ التشخيص كمل — انسخ النتيجة كاملة"
echo "═══════════════════════════════════════════"
