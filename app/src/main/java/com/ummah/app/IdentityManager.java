package com.ummah.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class IdentityManager {

    private static final String PREFS = "ummah_prefs";
    private static final String KEY_ID = "national_id";
    private static final String KEY_NAME = "citizen_name";
    private static final String KEY_DATE = "join_date";
    private static final String KEY_SEED = "seed_phrase";
    private static final String KEY_COUNTRY = "country";

    private static final String[] WORDS = {
        "حرية", "عدالة", "كرامة", "أمل", "سلام", "نور", "حق",
        "خير", "علم", "أمان", "إرادة", "شجاعة", "صدق", "وفاء",
        "صبر", "حكمة", "رحمة", "عزة", "نصر", "فجر", "قلب",
        "روح", "عقل", "يد", "أرض", "سماء", "بحر", "جبل",
        "نهر", "شمس", "قمر", "نجم", "زهر", "شجر", "طير",
        "نحل", "فرس", "أسد", "نسر", "كوكب", "أفق", "بيت",
        "وطن", "أمة", "شعب", "جيل", "مستقبل", "بداية"
    };

    private final SharedPreferences prefs;
    private final SecureRandom random = new SecureRandom();

    public IdentityManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean isCitizen() { return prefs.contains(KEY_ID); }

    public Citizen getCitizen() {
        if (!isCitizen()) return null;
        return new Citizen(
                prefs.getString(KEY_ID, ""),
                prefs.getString(KEY_NAME, ""),
                prefs.getString(KEY_DATE, ""),
                prefs.getString(KEY_SEED, ""),
                prefs.getString(KEY_COUNTRY, "DZ")
        );
    }

    public Citizen registerCitizen(String name, String country) {
        String seedPhrase = generateSeedPhrase();
        String nationalId = deriveNationalId(seedPhrase);
        String joinDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());

        prefs.edit()
                .putString(KEY_ID, nationalId)
                .putString(KEY_NAME, name)
                .putString(KEY_DATE, joinDate)
                .putString(KEY_SEED, seedPhrase)
                .putString(KEY_COUNTRY, country)
                .apply();

        return new Citizen(nationalId, name, joinDate, seedPhrase, country);
    }

    public Citizen restoreCitizen(String id, String name, String date, String seed, String country) {
        prefs.edit()
                .putString(KEY_ID, id)
                .putString(KEY_NAME, name)
                .putString(KEY_DATE, date)
                .putString(KEY_SEED, seed)
                .putString(KEY_COUNTRY, country)
                .apply();
        return new Citizen(id, name, date, seed, country);
    }

    public String hashSeed(String seed) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] h = md.digest(seed.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : h) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return seed; }
    }

    // === الكشف التلقائي ===
    public String detectCountry(Context ctx) {
        // 1. حاول من شريحة SIM
        try {
            TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null && tm.getSimCountryIso() != null && !tm.getSimCountryIso().isEmpty()) {
                return tm.getSimCountryIso().toUpperCase();
            }
        } catch (Exception e) { /* تجاهل */ }

        // 2. من الشبكة
        try {
            TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null && tm.getNetworkCountryIso() != null && !tm.getNetworkCountryIso().isEmpty()) {
                return tm.getNetworkCountryIso().toUpperCase();
            }
        } catch (Exception e) { /* تجاهل */ }

        // 3. من اللغة
        String langCountry = Locale.getDefault().getCountry();
        if (langCountry != null && !langCountry.isEmpty()) return langCountry.toUpperCase();

        return "DZ"; // افتراضي
    }

    private String generateSeedPhrase() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            if (i > 0) sb.append(" ");
            sb.append(WORDS[random.nextInt(WORDS.length)]);
        }
        return sb.toString();
    }

    private String deriveNationalId(String seedPhrase) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(seedPhrase.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 12; i++) hex.append(String.format("%02X", hash[i]));
            String h = hex.toString();
            return "UMM-" + h.substring(0, 4) + "-" + h.substring(4, 8) + "-" + h.substring(8, 12);
        } catch (Exception e) {
            return "UMM-0000-0000-0000";
        }
    }
}
