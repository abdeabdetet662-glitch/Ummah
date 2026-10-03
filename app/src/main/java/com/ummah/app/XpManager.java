package com.ummah.app;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * XpManager — إدارة XP والمستوى
 * 
 * المعادلة:
 * - Level 1-2: 100 XP
 * - Level 2-3: 200 XP
 * - Level N-N+1: N * 100 XP
 * - Total XP for Level N: N * (N+1) / 2 * 100
 */
public class XpManager {

    private static final String PREF = "ummah_xp";
    private static final String KEY_XP = "xp";
    private static final String KEY_LEVEL = "level";
    private static final String KEY_TOTAL_XP = "total_xp";

    private static final int MAX_LEVEL = 100;

    private final SharedPreferences prefs;
    private final FirebaseFirestore db;
    private final Context appContext;

    public XpManager(Context context) {
        this.appContext = context.getApplicationContext();
        prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();
    }

    // ═══════════════════════════════════════════
    //  الحصول على XP الحالي
    // ═══════════════════════════════════════════
    public int getXp() {
        return prefs.getInt(KEY_XP, 0);
    }

    public int getLevel() {
        return prefs.getInt(KEY_LEVEL, 1);
    }

    public int getTotalXp() {
        return prefs.getInt(KEY_TOTAL_XP, 0);
    }

    // ═══════════════════════════════════════════
    //  XP المطلوب للمستوى القادم
    // ═══════════════════════════════════════════
    public int getXpForNextLevel() {
        int level = getLevel();
        if (level >= MAX_LEVEL) return 0;
        return level * 100;
    }

    // ═══════════════════════════════════════════
    //  النسبة المئوية للتقدم
    // ═══════════════════════════════════════════
    public int getProgressPercent() {
        int needed = getXpForNextLevel();
        if (needed <= 0) return 100;
        int current = getXp();
        return Math.min(100, (current * 100) / needed);
    }

    // ═══════════════════════════════════════════
    //  XP المتبقي للمستوى القادم
    // ═══════════════════════════════════════════
    public int getXpRemaining() {
        int needed = getXpForNextLevel();
        return Math.max(0, needed - getXp());
    }

    // ═══════════════════════════════════════════
    //  إضافة XP
    // ═══════════════════════════════════════════
    public void addXp(int amount) {
        if (amount <= 0) return;

        int currentXp = getXp() + amount;
        int currentLevel = getLevel();
        int totalXp = getTotalXp() + amount;

        // نتحققو من الترقية
        boolean leveledUp = false;
        while (currentXp >= getXpForLevel(currentLevel) && currentLevel < MAX_LEVEL) {
            currentXp -= getXpForLevel(currentLevel);
            currentLevel++;
            leveledUp = true;
        }

        prefs.edit()
            .putInt(KEY_XP, currentXp)
            .putInt(KEY_LEVEL, currentLevel)
            .putInt(KEY_TOTAL_XP, totalXp)
            .apply();

        // نحفظ في Firebase
        try {
            IdentityManager im = new IdentityManager(appContext);
            if (im != null && im.isCitizen()) {
                String id = im.getCitizen().nationalId;
                db.collection("citizens").document(id)
                    .update("xp", currentXp, "level", currentLevel, "totalXp", totalXp);
            }
        } catch (Exception ignored) {}

        // إذا ترقّى
        if (leveledUp) {
            onLevelUp(currentLevel);
        }
    }

    private void onLevelUp(int newLevel) {
        // نخزنو مؤقتاً للإشعار
        prefs.edit()
            .putInt("last_level_up", newLevel)
            .putLong("last_level_up_time", System.currentTimeMillis())
            .apply();
    }

    // ═══════════════════════════════════════════
    //  حساب XP لمستوى معين
    // ═══════════════════════════════════════════
    private int getXpForLevel(int level) {
        return level * 100;
    }

    // ═══════════════════════════════════════════
    //  لقب المستوى
    // ═══════════════════════════════════════════
    public String getTitle() {
        int level = getLevel();
        if (level >= 90) return "👑 أسطورة";
        if (level >= 75) return "💎 ماسي";
        if (level >= 60) return "🥇 ذهبي";
        if (level >= 45) return "🥈 فضي";
        if (level >= 30) return "🥉 برونزي";
        if (level >= 20) return "⭐ متميز";
        if (level >= 10) return "🌟 نشيط";
        if (level >= 5)  return "✨ مبتدئ";
        return "🌱 جديد";
    }

    // ═══════════════════════════════════════════
    //  إعادة تعيين (للاختبار)
    // ═══════════════════════════════════════════
    public void reset() {
        prefs.edit().clear().apply();
    }

    // ═══════════════════════════════════════════
    //  نقاط XP للأحداث
    // ═══════════════════════════════════════════
    public static final int XP_DAILY_LOGIN = 10;
    public static final int XP_CHAT_MESSAGE = 1;
    public static final int XP_TRANSFER = 5;
    public static final int XP_BUY_PLOT = 30;
    public static final int XP_BUILD = 50;
    public static final int XP_UPGRADE = 75;
    public static final int XP_SPIN_WHEEL = 20;
    public static final int XP_SELL_PLOT = 10;
    public static final int XP_VOTE = 15;
    public static final int XP_LEVEL_UP = 100;
}
