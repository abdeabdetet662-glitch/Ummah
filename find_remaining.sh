#!/bin/bash
echo "═══════════════════════════════════════════"
echo "🔍 البحث عن النصوص العربية الباقية"
echo "═══════════════════════════════════════════"
echo ""

echo "📁 1. نصوص عربية في XML layouts:"
grep -rl 'android:text="[^@]' app/src/main/res/layout/ 2>/dev/null | head -20
echo ""
echo "التفاصيل (أول 30):"
grep -rn 'android:text="[^@]' app/src/main/res/layout/ 2>/dev/null \
  | grep -P '[\x{0600}-\x{06FF}]' | head -30
echo ""

echo "📁 2. Toast مباشرة في كل Java:"
grep -rn 'Toast.makeText.*"' app/src/main/java/com/ummah/app/ 2>/dev/null \
  | grep -P '[\x{0600}-\x{06FF}]' | grep -v 'getString' | head -20
echo ""

echo "📁 3. SetText في Helpers (NavDrawer, HomeTab, etc):"
grep -rn 'setText("' app/src/main/java/com/ummah/app/ 2>/dev/null \
  | grep -P '[\x{0600}-\x{06FF}]' | grep -v 'getString' | grep -v 'أُمّة' | head -30
echo ""

echo "📁 4. setTitle(\"...\") مباشرة:"
grep -rn 'setTitle("' app/src/main/java/com/ummah/app/ 2>/dev/null \
  | grep -P '[\x{0600}-\x{06FF}]' | head -20
echo ""

echo "📁 5. Helper classes:"
ls app/src/main/java/com/ummah/app/*Helper.java 2>/dev/null
echo ""

echo "📁 6. Adapters:"
ls app/src/main/java/com/ummah/app/*Adapter*.java 2>/dev/null
echo ""

echo "📁 7. Bottom tab labels في MainActivity:"
grep -n 'setText\|Tab\|BottomNav\|📊\|💰\|👤' app/src/main/java/com/ummah/app/MainActivity.java 2>/dev/null | head -20
echo ""

echo "📁 8. كل ملفات Java غير Activity وغير Helper فيها نصوص عربية:"
grep -rn 'setText("' app/src/main/java/com/ummah/app/*.java 2>/dev/null \
  | grep -P '[\x{0600}-\x{06FF}]' | grep -v 'getString' | grep -v 'أُمّة' | \
  awk -F: '{print $1}' | sort -u | head -30
echo ""

echo "═══════════════════════════════════════════"
echo "✅ خلص — انسخ النتيجة كاملة"
echo "═══════════════════════════════════════════"
