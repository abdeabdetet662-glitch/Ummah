package com.ummah.app;

import java.util.ArrayList;
import java.util.List;

public class GiftCatalog {

    public static List<Gift> getCommonGifts() {
        List<Gift> list = new ArrayList<>();
        list.add(new Gift("🌹", "وردة", 1, "تقدير بسيط", 1));
        list.add(new Gift("❤️", "قلب", 2, "محبة صافية", 1));
        list.add(new Gift("🍫", "شوكولاتة", 5, "حلاوة الكلام", 1));
        list.add(new Gift("⭐", "نجمة", 10, "أنت نجم", 1));
        list.add(new Gift("🎈", "بالون", 3, "فرحة صغيرة", 1));
        list.add(new Gift("☕", "قهوة", 4, "صباح الخير", 1));
        return list;
    }

    public static List<Gift> getRareGifts() {
        List<Gift> list = new ArrayList<>();
        list.add(new Gift("🏆", "كأس", 25, "أنت الأفضل", 2));
        list.add(new Gift("👑", "تاج", 50, "يا ملك", 2));
        list.add(new Gift("💎", "ألماسة", 100, "أنت جوهرة", 2));
        list.add(new Gift("🚀", "صاروخ", 200, "انطلق!", 2));
        list.add(new Gift("🦅", "صقر", 150, "عالٍ كالنسر", 2));
        list.add(new Gift("🎸", "غيتار", 80, "لحن رائع", 2));
        return list;
    }

    public static List<Gift> getLegendaryGifts() {
        List<Gift> list = new ArrayList<>();
        list.add(new Gift("🏰", "قلعة", 500, "ملك البناء", 3));
        list.add(new Gift("🌌", "مجرة", 1000, "أنت عالم كامل", 3));
        list.add(new Gift("🦁", "أسد", 2000, "قوة وشجاعة", 3));
        list.add(new Gift("🐉", "تنين", 5000, "أسطورة حية", 3));
        list.add(new Gift("🌞", "شمس", 10000, "أنت مصدر الحياة", 3));
        list.add(new Gift("🕋", "مكة", 25000, "أعظم هدية", 3));
        return list;
    }

    public static List<Gift> getAllGifts() {
        List<Gift> all = new ArrayList<>();
        all.addAll(getCommonGifts());
        all.addAll(getRareGifts());
        all.addAll(getLegendaryGifts());
        return all;
    }

    public static String getTierColor(int tier) {
        switch (tier) {
            case 1: return "#2E7D32";
            case 2: return "#1565C0";
            case 3: return "#B8860B";
            default: return "#424242";
        }
    }

    public static String getTierName(int tier) {
        switch (tier) {
            case 1: return "🟢 عادي";
            case 2: return "🔵 نادر";
            case 3: return "🟡 أسطوري";
            default: return "";
        }
    }

    public static android.graphics.drawable.GradientDrawable getTierBackground(int tier) {
        android.graphics.drawable.GradientDrawable g = new android.graphics.drawable.GradientDrawable();
        g.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        g.setCornerRadius(30);
        switch (tier) {
            case 1:
                g.setColors(new int[]{android.graphics.Color.parseColor("#1B5E20"), android.graphics.Color.parseColor("#2E7D32")});
                g.setStroke(3, android.graphics.Color.parseColor("#4CAF50"));
                break;
            case 2:
                g.setColors(new int[]{android.graphics.Color.parseColor("#0D47A1"), android.graphics.Color.parseColor("#1565C0")});
                g.setStroke(3, android.graphics.Color.parseColor("#2196F3"));
                break;
            case 3:
                g.setColors(new int[]{android.graphics.Color.parseColor("#B8860B"), android.graphics.Color.parseColor("#FFD700")});
                g.setStroke(4, android.graphics.Color.parseColor("#FFF176"));
                break;
        }
        g.setGradientType(android.graphics.drawable.GradientDrawable.LINEAR_GRADIENT);
        g.setOrientation(android.graphics.drawable.GradientDrawable.Orientation.TL_BR);
        return g;
    }
}
