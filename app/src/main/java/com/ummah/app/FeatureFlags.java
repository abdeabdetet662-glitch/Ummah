package com.ummah.app;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.HashMap;
import java.util.Map;

/**
 * نظام التحكم في الميزات (Feature Flags)
 * 
 * يقرأ من Firestore: app_config/features
 * يحفظ cache محلي للسرعة
 * يعطي إشارات فورية عند تغيير أي ميزة
 * 
 * استخدام:
 *   if (!FeatureFlags.isEnabled("wheel")) { ... }
 */
public class FeatureFlags {

    private static final String PREFS = "ummah_feature_flags";
    private static final String FIRESTORE_COLLECTION = "app_config";
    private static final String FIRESTORE_DOC = "features";

    // ═══════════════════════════════════════════
    //  قائمة كل الميزات (default = true)
    // ═══════════════════════════════════════════
    public static final String WHEEL = "wheel";                       // عجلة الحظ
    public static final String MARKET = "market";                     // السوق العام
    public static final String CHAT = "chat";                         // الدردشة العامة
    public static final String PRIVATE_CHAT = "private_chat";         // الدردشة الخاصة
    public static final String PARLIAMENT = "parliament";             // البرلمان
    public static final String ELECTIONS = "elections";               // الانتخابات
    public static final String COURT = "court";                       // المحكمة
    public static final String JOBS = "jobs";                         // الوظائف
    public static final String CITY = "city";                         // مدينة أُمّة
    public static final String CITIZEN_MARKET = "citizen_market";    // سوق المواطنين
    public static final String GIFTS = "gifts";                       // الهدايا
    public static final String DAILY_REWARD = "daily_reward";        // المكافأة اليومية
    public static final String TRANSFERS = "transfers";              // التحويلات
    public static final String REGISTRATION = "registration";        // تسجيل مواطنين
    public static final String TREASURY = "treasury";                // الخزينة
    public static final String NEWS = "news";                         // الأخبار
    public static final String LEADERBOARD = "leaderboard";          // المتصدرون
    public static final String PRESIDENT = "president";              // نظام الرئيس
    public static final String STATS = "stats";                       // الإحصائيات

    private static final Map<String, Boolean> cache = new HashMap<>();
    private static FirebaseFirestore db;
    private static ListenerRegistration listener;
    private static SharedPreferences prefs;
    private static boolean initialized = false;

    // ═══════════════════════════════════════════
    //  التهيئة (تُستدعى مرة واحدة في MainActivity)
    // ═══════════════════════════════════════════
    public static void init(Context context) {
        if (initialized) return;
        try {
            prefs = context.getApplicationContext()
                    .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            db = FirebaseFirestore.getInstance();

            // 1. تحميل القيم المحفوظة محلياً
            loadFromCache();

            // 2. الاستماع للتغييرات من Firestore (فوري)
            listenToFirestore();

            initialized = true;
        } catch (Exception e) {
            // فشل التهيئة — كل الميزات مفعلة تلقائياً
            initialized = true;
        }
    }

    // ═══════════════════════════════════════════
    //  الفحص الأساسي
    // ═══════════════════════════════════════════
    public static boolean isEnabled(String feature) {
        try {
            // 1. إذا موجود في الـ cache
            if (cache.containsKey(feature)) {
                return cache.get(feature);
            }

            // 2. إذا في SharedPreferences
            if (prefs != null) {
                return prefs.getBoolean(feature, true);
            }

            // 3. الافتراضي: مفعل
            return true;
        } catch (Exception e) {
            return true; // آمن
        }
    }

    // ═══════════════════════════════════════════
    //  تحميل من Cache
    // ═══════════════════════════════════════════
    private static void loadFromCache() {
        try {
            String[] allFlags = {
                    WHEEL, MARKET, CHAT, PRIVATE_CHAT, PARLIAMENT,
                    ELECTIONS, COURT, JOBS, CITY, CITIZEN_MARKET,
                    GIFTS, DAILY_REWARD, TRANSFERS, REGISTRATION,
                    TREASURY, NEWS, LEADERBOARD, PRESIDENT, STATS
            };

            for (String flag : allFlags) {
                boolean value = prefs.getBoolean(flag, true);
                cache.put(flag, value);
            }
        } catch (Exception ignored) {}
    }

    // ═══════════════════════════════════════════
    //  الاستماع لـ Firestore
    // ═══════════════════════════════════════════
    private static void listenToFirestore() {
        try {
            if (listener != null) listener.remove();

            listener = db.collection(FIRESTORE_COLLECTION)
                    .document(FIRESTORE_DOC)
                    .addSnapshotListener((snapshot, error) -> {
                        if (error != null || snapshot == null) return;
                        if (!snapshot.exists()) {
                            // إذا ماكانش، ننشئه بالقيم الافتراضية
                            createDefaultConfig();
                            return;
                        }

                        // تحديث كل الميزات
                        for (Map.Entry<String, Object> entry : snapshot.getData().entrySet()) {
                            try {
                                Object val = entry.getValue();
                                boolean boolVal = (val instanceof Boolean) ? (Boolean) val : true;
                                cache.put(entry.getKey(), boolVal);
                                if (prefs != null) {
                                    prefs.edit().putBoolean(entry.getKey(), boolVal).apply();
                                }
                            } catch (Exception ignored) {}
                        }
                    });
        } catch (Exception ignored) {}
    }

    // ═══════════════════════════════════════════
    //  إنشاء الـ Config الافتراضي
    // ═══════════════════════════════════════════
    private static void createDefaultConfig() {
        try {
            Map<String, Object> defaults = new HashMap<>();
            defaults.put(WHEEL, true);
            defaults.put(MARKET, true);
            defaults.put(CHAT, true);
            defaults.put(PRIVATE_CHAT, true);
            defaults.put(PARLIAMENT, true);
            defaults.put(ELECTIONS, true);
            defaults.put(COURT, true);
            defaults.put(JOBS, true);
            defaults.put(CITY, true);
            defaults.put(CITIZEN_MARKET, true);
            defaults.put(GIFTS, true);
            defaults.put(DAILY_REWARD, true);
            defaults.put(TRANSFERS, true);
            defaults.put(REGISTRATION, true);
            defaults.put(TREASURY, true);
            defaults.put(NEWS, true);
            defaults.put(LEADERBOARD, true);
            defaults.put(PRESIDENT, true);
            defaults.put(STATS, true);

            db.collection(FIRESTORE_COLLECTION)
                    .document(FIRESTORE_DOC)
                    .set(defaults);
        } catch (Exception ignored) {}
    }

    // ═══════════════════════════════════════════
    //  تحديث قيمة (من Admin)
    // ═══════════════════════════════════════════
    public static void setEnabled(String feature, boolean enabled) {
        try {
            cache.put(feature, enabled);
            if (prefs != null) {
                prefs.edit().putBoolean(feature, enabled).apply();
            }
            if (db != null) {
                db.collection(FIRESTORE_COLLECTION)
                        .document(FIRESTORE_DOC)
                        .update(feature, enabled);
            }
        } catch (Exception ignored) {}
    }

    // ═══════════════════════════════════════════
    //  إيقاف الاستماع (في onDestroy)
    // ═══════════════════════════════════════════
    public static void cleanup() {
        try {
            if (listener != null) {
                listener.remove();
                listener = null;
            }
        } catch (Exception ignored) {}
    }

    // ═══════════════════════════════════════════
    //  فحص سريع مع رسالة
    // ═══════════════════════════════════════════
    public static boolean checkOrToast(android.app.Activity activity, String feature) {
        if (!isEnabled(feature)) {
            try {
                android.widget.Toast.makeText(activity,
                        "⚠️ هذه الميزة متوقفة مؤقتاً من الإدارة",
                        android.widget.Toast.LENGTH_LONG).show();
            } catch (Exception ignored) {}
            return false;
        }
        return true;
    }
}
