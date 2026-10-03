package com.ummah.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

/**
 * LocaleHelper — إدارة اللغة
 * 
 * يستعمل في كل Activity:
 *   @Override
 *   protected void attachBaseContext(Context base) {
 *       super.attachBaseContext(LocaleHelper.wrap(base));
 *   }
 */
public class LocaleHelper {

    private static final String PREF = "ummah_settings";
    private static final String KEY_LANG = "language_code";

    // ═══════════════════════════════════════════
    //  حفظ اللغة
    // ═══════════════════════════════════════════
    public static void setLocale(Context ctx, String langCode) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANG, langCode).apply();
    }

    // ═══════════════════════════════════════════
    //  الحصول على اللغة الحالية
    // ═══════════════════════════════════════════
    public static String getLocale(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANG, "");
    }

    // ═══════════════════════════════════════════
    //  تغليف Context
    // ═══════════════════════════════════════════
    public static Context wrap(Context ctx) {
        String lang = getLocale(ctx);
        if (lang == null || lang.isEmpty()) return ctx;

        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Configuration config = new Configuration(ctx.getResources().getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            config.setLocales(localeList);
        } else {
            config.setLocale(locale);
        }

        return ctx.createConfigurationContext(config);
    }

    // ═══════════════════════════════════════════
    //  إعادة تشغيل الـ Activity
    // ═══════════════════════════════════════════
    public static void applyLocale(Activity activity, String langCode) {
        setLocale(activity, langCode);
        activity.recreate();
    }

    // ═══════════════════════════════════════════
    //  اسم اللغة الكامل
    // ═══════════════════════════════════════════
    public static String getLanguageName(String code) {
        switch (code) {
            case "ar": return "🇩🇿 العربية";
            case "fr": return "🇫🇷 Français";
            case "en": return "🇬🇧 English";
            case "ru": return "🇷🇺 Русский";
        }
        return "🌍 Default";
    }
}
