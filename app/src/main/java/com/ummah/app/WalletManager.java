package com.ummah.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WalletManager {
    private static final String PREFS = "ummah_wallet";
    private static final int INITIAL = 100;
    private final SharedPreferences prefs;

    public WalletManager(Context c) {
        prefs = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (!prefs.contains("balance")) prefs.edit().putInt("balance", INITIAL).apply();
    }
    public int getBalance() { return prefs.getInt("balance", INITIAL); }

    /** تحديث الرصيد من Firestore */
    public void setBalance(int amount) {
        prefs.edit().putInt("balance", amount).apply();
    }
    public void add(int amount) { prefs.edit().putInt("balance", getBalance() + amount).apply(); }
    public boolean spend(int amount) {
        if (getBalance() < amount) return false;
        prefs.edit().putInt("balance", getBalance() - amount).apply();
        return true;
    }
    public boolean claimDaily() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        if (today.equals(prefs.getString("last_daily", ""))) return false;
        prefs.edit().putString("last_daily", today).apply();
        return true;
    }
    public boolean canClaimDaily() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        return !today.equals(prefs.getString("last_daily", ""));
    }
    public long millisUntilNextDaily() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date today = sdf.parse(sdf.format(new Date()));
            Date tomorrow = new Date(today.getTime() + 24L * 60 * 60 * 1000);
            return tomorrow.getTime() - System.currentTimeMillis();
        } catch (Exception e) { return 0; }
    }
}
