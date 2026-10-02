package com.ummah.app;

import java.util.ArrayList;
import java.util.List;

/**
 * BuildingCatalog — كتالوج المبانى
 * 
 * 16 نوع مبنى في 5 فئات:
 * - سكني (5)
 * - فاخر (4)
 * - تجاري (2)
 * - صناعي (1)
 * - خدمي (4)
 */
public class BuildingCatalog {

    // ═══════════════════════════════════════════
    //  نموذج المبنى
    // ═══════════════════════════════════════════
    public static class Building {
        public String id;
        public String name;
        public String emoji;
        public int price;
        public String category;   // residential, luxury, commercial, industrial, public
        public String categoryName;
        public String desc;
        public int level;         // المستوى (1-15)

        public Building(String id, String emoji, String name, int price,
                        String category, String categoryName, String desc, int level) {
            this.id = id;
            this.emoji = emoji;
            this.name = name;
            this.price = price;
            this.category = category;
            this.categoryName = categoryName;
            this.desc = desc;
            this.level = level;
        }
    }

    // ═══════════════════════════════════════════
    //  قائمة كل المبانى
    // ═══════════════════════════════════════════
    public static List<Building> getAll() {
        List<Building> list = new ArrayList<>();

        // ─── سكني ───
        list.add(new Building("hut", "🏚️", "كوخ خشبي", 50,
                "residential", "🏠 سكني", "بداية بسيطة", 1));
        list.add(new Building("house", "🏠", "منزل بسيط", 200,
                "residential", "🏠 سكني", "منزل عائلي", 2));
        list.add(new Building("duplex", "🏘️", "دوبلكس", 500,
                "residential", "🏠 سكني", "طابقين مستقلين", 3));
        list.add(new Building("garden_house", "🏡", "منزل بحديقة", 1000,
                "residential", "🏠 سكني", "مع مساحة خضراء", 4));
        list.add(new Building("apartment", "🏢", "شقة حديثة", 2000,
                "residential", "🏠 سكني", "مبنى متعدد الطوابق", 5));

        // ─── فاخر ───
        list.add(new Building("villa", "🏰", "فيلا", 5000,
                "luxury", "💎 فاخر", "مع مسبح وحديقة", 6));
        list.add(new Building("luxury_villa", "🏰", "فيلا فاخرة", 10000,
                "luxury", "💎 فاخر", "تصميم عصري + سيارة", 7));
        list.add(new Building("palace", "🏛️", "قصر", 15000,
                "luxury", "💎 فاخر", "مع 4 أبراج", 8));
        list.add(new Building("royal_palace", "🏛️", "قصر ملكي", 30000,
                "luxury", "💎 فاخر", "قبة ذهبية ملكية", 9));

        // ─── تجاري ───
        list.add(new Building("shop", "🏪", "متجر", 1000,
                "commercial", "🛒 تجاري", "لبيع المنتجات", 4));
        list.add(new Building("restaurant", "🍽️", "مطعم", 3000,
                "commercial", "🛒 تجاري", "مع جلسات خارجية", 5));

        // ─── صناعي ───
        list.add(new Building("factory", "🏭", "مصنع", 5000,
                "industrial", "⚙️ صناعي", "مع 2 مداخن", 6));

        // ─── خدمي ───
        list.add(new Building("school", "🏫", "مدرسة", 10000,
                "public", "🏛️ خدمي", "تعليم للأطفال", 7));
        list.add(new Building("hospital", "🏥", "مستشفى", 20000,
                "public", "🏛️ خدمي", "خدمات طبية", 8));
        list.add(new Building("bank", "🏦", "بنك", 30000,
                "public", "🏛️ خدمي", "خدمات مالية", 9));
        list.add(new Building("mosque", "🕌", "مسجد", 50000,
                "public", "🏛️ خدمي", "مع مأذنة + قبة", 10));

        return list;
    }

    // ═══════════════════════════════════════════
    //  البحث بالـ id
    // ═══════════════════════════════════════════
    public static Building getById(String id) {
        if (id == null) return null;
        for (Building b : getAll()) {
            if (b.id.equals(id)) return b;
        }
        return null;
    }

    // ═══════════════════════════════════════════
    //  الفئات
    // ═══════════════════════════════════════════
    public static List<String> getCategories() {
        List<String> cats = new ArrayList<>();
        cats.add("residential");
        cats.add("luxury");
        cats.add("commercial");
        cats.add("industrial");
        cats.add("public");
        return cats;
    }

    public static String getCategoryName(String cat) {
        switch (cat) {
            case "residential": return "🏠 سكني";
            case "luxury":      return "💎 فاخر";
            case "commercial":  return "🛒 تجاري";
            case "industrial":  return "⚙️ صناعي";
            case "public":      return "🏛️ خدمي";
        }
        return cat;
    }

    // ═══════════════════════════════════════════
    //  المبانى حسب الفئة
    // ═══════════════════════════════════════════
    public static List<Building> getByCategory(String cat) {
        List<Building> list = new ArrayList<>();
        for (Building b : getAll()) {
            if (b.category.equals(cat)) list.add(b);
        }
        return list;
    }

    // ═══════════════════════════════════════════
    //  التسمية
    // ═══════════════════════════════════════════
    public static String getLabel(String id) {
        Building b = getById(id);
        if (b == null) return "لا يوجد";
        return b.emoji + " " + b.name;
    }

    // ═══════════════════════════════════════════
    //  السعر
    // ═══════════════════════════════════════════
    public static int getPrice(String id) {
        Building b = getById(id);
        return b != null ? b.price : 0;
    }
}
