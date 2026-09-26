package com.ummah.app;

import java.util.LinkedHashMap;
import java.util.Map;

public class CountryList {

    public static Map<String, String> getCountries() {
        Map<String, String> c = new LinkedHashMap<>();
        c.put("DZ", "🇩🇿 الجزائر");
        c.put("EG", "🇪🇬 مصر");
        c.put("MA", "🇲🇦 المغرب");
        c.put("TN", "🇹🇳 تونس");
        c.put("LY", "🇱🇾 ليبيا");
        c.put("SD", "🇸🇩 السودان");
        c.put("MR", "🇲🇷 موريتانيا");
        c.put("SA", "🇸🇦 السعودية");
        c.put("AE", "🇦🇪 الإمارات");
        c.put("KW", "🇰🇼 الكويت");
        c.put("QA", "🇶🇦 قطر");
        c.put("BH", "🇧🇭 البحرين");
        c.put("OM", "🇴🇲 عُمان");
        c.put("YE", "🇾🇪 اليمن");
        c.put("JO", "🇯🇴 الأردن");
        c.put("LB", "🇱🇧 لبنان");
        c.put("SY", "🇸🇾 سوريا");
        c.put("IQ", "🇮🇶 العراق");
        c.put("PS", "🇵🇸 فلسطين");
        c.put("SO", "🇸🇴 الصومال");
        c.put("DJ", "🇩🇯 جيبوتي");
        c.put("KM", "🇰🇲 جزر القمر");
        c.put("TR", "🇹🇷 تركيا");
        c.put("FR", "🇫🇷 فرنسا");
        c.put("US", "🇺🇸 أمريكا");
        c.put("CA", "🇨🇦 كندا");
        c.put("GB", "🇬🇧 بريطانيا");
        c.put("DE", "🇩🇪 ألمانيا");
        c.put("ES", "🇪🇸 إسبانيا");
        c.put("IT", "🇮🇹 إيطاليا");
        c.put("BE", "🇧🇪 بلجيكا");
        c.put("NL", "🇳🇱 هولندا");
        c.put("SE", "🇸🇪 السويد");
        c.put("CH", "🇨🇭 سويسرا");
        c.put("OTHER", "🌍 أخرى");
        return c;
    }

    public static String getFlagEmoji(String code) {
        if (code == null || code.length() != 2) return "🌍";
        try {
            int first = 0x1F1E6 + (code.charAt(0) - 'A');
            int second = 0x1F1E6 + (code.charAt(1) - 'A');
            return new String(Character.toChars(first)) + new String(Character.toChars(second));
        } catch (Exception e) {
            return "🌍";
        }
    }

    public static String getName(String code) {
        String entry = getCountries().get(code);
        if (entry == null) return "🌍";
        return entry;
    }
}
