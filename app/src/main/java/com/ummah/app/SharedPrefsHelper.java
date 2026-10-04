package com.ummah.app;

/**
 * SharedPrefsHelper — إدارة الـ balance cache محلياً
 */
public class SharedPrefsHelper {

    private static final String PREFS = "balance_cache";
    private static final String KEY = "balance";

    public static void updateBalance(long balance) {
        try {
            android.content.Context ctx =
                    com.ummah.app.UmmahApp.getContext();
            if (ctx != null) {
                ctx.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
                    .edit().putLong(KEY, balance).apply();
            }
        } catch (Exception ignored) {}
    }

    public static long getBalance() {
        try {
            android.content.Context ctx =
                    com.ummah.app.UmmahApp.getContext();
            if (ctx != null) {
                return ctx.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
                    .getLong(KEY, 0);
            }
        } catch (Exception ignored) {}
        return 0;
    }
}
