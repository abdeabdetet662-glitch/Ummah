package com.ummah.app;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MarketSeed {

    public static void seed(FirebaseFirestore db) {
        // ═══ 🚗 مركبات ═══
        addVehicle(db, "toyota_corolla", "تويوتا كورولا", "Toyota", "car", 5000,
                "https://images.pexels.com/photos/116675/pexels-photo-116675.jpeg?auto=compress&cs=tinysrgb&w=800",
                "سيارة اقتصادية موثوقة", "common");
        addVehicle(db, "bmw_3series", "BMW الفئة الثالثة", "BMW", "car", 35000,
                "https://images.pexels.com/photos/170811/pexels-photo-170811.jpeg?auto=compress&cs=tinysrgb&w=800",
                "سيارة فاخرة رياضية", "rare");
        addVehicle(db, "mercedes_sclass", "مرسيدس S-Class", "Mercedes", "car", 85000,
                "https://images.pexels.com/photos/120049/pexels-photo-120049.jpeg?auto=compress&cs=tinysrgb&w=800",
                "سيارة رجال الأعمال", "epic");
        addVehicle(db, "ferrari_f8", "فيراري F8", "Ferrari", "car", 250000,
                "https://images.pexels.com/photos/337909/pexels-photo-337909.jpeg?auto=compress&cs=tinysrgb&w=800",
                "أسطورية", "legendary");
        addVehicle(db, "lambo_aventador", "لامبورجيني", "Lamborghini", "car", 350000,
                "https://images.pexels.com/photos/2127733/pexels-photo-2127733.jpeg?auto=compress&cs=tinysrgb&w=800",
                "أسطورة الطرقات", "legendary");
        addVehicle(db, "yamaha_sport", "دراجة ياماها", "Yamaha", "motorcycle", 8000,
                "https://images.pexels.com/photos/1413412/pexels-photo-1413412.jpeg?auto=compress&cs=tinysrgb&w=800",
                "سريعة خفيفة", "common");
        addVehicle(db, "harley_classic", "دراجة هارلي", "Harley", "motorcycle", 25000,
                "https://images.pexels.com/photos/2116475/pexels-photo-2116475.jpeg?auto=compress&cs=tinysrgb&w=800",
                "دراجة كلاسيكية", "rare");

        // ═══ 🏠 عقارات ═══
        addProperty(db, "studio_small", "شقة استوديو", "apartment", 20000,
                "https://images.pexels.com/photos/1571460/pexels-photo-1571460.jpeg?auto=compress&cs=tinysrgb&w=800",
                "شقة صغيرة", "common");
        addProperty(db, "apartment_2br", "شقة غرفتين", "apartment", 50000,
                "https://images.pexels.com/photos/1918291/pexels-photo-1918291.jpeg?auto=compress&cs=tinysrgb&w=800",
                "شقة عصرية", "rare");
        addProperty(db, "villa_small", "فيلا صغيرة", "villa", 150000,
                "https://images.pexels.com/photos/1396122/pexels-photo-1396122.jpeg?auto=compress&cs=tinysrgb&w=800",
                "فيلا مستقلة", "epic");
        addProperty(db, "villa_luxury", "فيلا فاخرة", "villa", 500000,
                "https://images.pexels.com/photos/1029599/pexels-photo-1029599.jpeg?auto=compress&cs=tinysrgb&w=800",
                "فيلا فخمة", "legendary");
        addProperty(db, "palace", "قصر أُمّة", "palace", 2000000,
                "https://images.pexels.com/photos/32870/pexels-photo.jpg?auto=compress&cs=tinysrgb&w=800",
                "قصر ملكي", "legendary");

        // ═══ 📱 إلكترونيات ═══
        addPhone(db, "phone_basic", "هاتف أساسي", "Nokia", 3000,
                "https://images.pexels.com/photos/788946/pexels-photo-788946.jpeg?auto=compress&cs=tinysrgb&w=800",
                "#424242", "هاتف اقتصادي", "common");
        addPhone(db, "phone_mid", "هاتف متوسط", "Samsung", 10000,
                "https://images.pexels.com/photos/699122/pexels-photo-699122.jpeg?auto=compress&cs=tinysrgb&w=800",
                "#1565C0", "هاتف عصري", "rare");
        addPhone(db, "phone_premium", "هاتف فاخر", "Apple", 30000,
                "https://images.pexels.com/photos/404280/pexels-photo-404280.jpeg?auto=compress&cs=tinysrgb&w=800",
                "#1A1A1A", "أحدث هاتف", "epic");
        addElectronics(db, "laptop", "لابتوب احترافي", "laptop", 25000,
                "https://images.pexels.com/photos/18105/pexels-photo.jpg?auto=compress&cs=tinysrgb&w=800",
                "لابتوب قوي", "epic");
        addElectronics(db, "headphones", "سماعات لاسلكية", "headphones", 2000,
                "https://images.pexels.com/photos/3394650/pexels-photo-3394650.jpeg?auto=compress&cs=tinysrgb&w=800",
                "سماعات بلوتوث", "common");

        // ═══ 👕 ملابس - قمصان ═══
        addClothing(db, "shirt_blue", "قميص أزرق", "shirt", "#1565C0", 500,
                "https://images.pexels.com/photos/996329/pexels-photo-996329.jpeg?auto=compress&cs=tinysrgb&w=800",
                "قميص قطني أزرق", "common");
        addClothing(db, "shirt_red", "قميص أحمر", "shirt", "#C62828", 500,
                "https://images.pexels.com/photos/996329/pexels-photo-996329.jpeg?auto=compress&cs=tinysrgb&w=800",
                "قميص أحمر ناري", "common");
        addClothing(db, "shirt_green", "قميص أخضر", "shirt", "#2E7D32", 500,
                "https://images.pexels.com/photos/996329/pexels-photo-996329.jpeg?auto=compress&cs=tinysrgb&w=800",
                "قميص أخضر", "common");
        addClothing(db, "shirt_purple", "قميص بنفسجي", "shirt", "#6A1B9A", 700,
                "https://images.pexels.com/photos/996329/pexels-photo-996329.jpeg?auto=compress&cs=tinysrgb&w=800",
                "قميص بنفسجي فاخر", "rare");
        addClothing(db, "shirt_white", "قميص أبيض", "shirt", "#FFFFFF", 600,
                "https://images.pexels.com/photos/996329/pexels-photo-996329.jpeg?auto=compress&cs=tinysrgb&w=800",
                "قميص أبيض", "common");

        // ═══ 👖 بناطيل ═══
        addClothing(db, "pants_jeans", "بنطال جينز", "pants", "#1565C0", 800,
                "https://images.pexels.com/photos/1598507/pexels-photo-1598507.jpeg?auto=compress&cs=tinysrgb&w=800",
                "بنطال جينز كلاسيكي", "common");
        addClothing(db, "pants_black", "بنطال أسود", "pants", "#212121", 700,
                "https://images.pexels.com/photos/1598507/pexels-photo-1598507.jpeg?auto=compress&cs=tinysrgb&w=800",
                "بنطال أسود أنيق", "common");
        addClothing(db, "pants_khaki", "بنطال كاكي", "pants", "#A1887F", 700,
                "https://images.pexels.com/photos/1598507/pexels-photo-1598507.jpeg?auto=compress&cs=tinysrgb&w=800",
                "بنطال كاكي", "common");
        addClothing(db, "pants_green", "بنطال أخضر", "pants", "#1B5E20", 900,
                "https://images.pexels.com/photos/1598507/pexels-photo-1598507.jpeg?auto=compress&cs=tinysrgb&w=800",
                "بنطال أخضر", "common");

        // ═══ 👟 أحذية ═══
        addClothing(db, "shoes_sneakers", "حذاء رياضي", "shoes", "#FFFFFF", 1500,
                "https://images.pexels.com/photos/2529148/pexels-photo-2529148.jpeg?auto=compress&cs=tinysrgb&w=800",
                "حذاء رياضي", "common");
        addClothing(db, "shoes_boots", "بوت", "shoes", "#4E342E", 2500,
                "https://images.pexels.com/photos/2529148/pexels-photo-2529148.jpeg?auto=compress&cs=tinysrgb&w=800",
                "بوت جلدي", "rare");
        addClothing(db, "shoes_classic", "حذاء كلاسيك", "shoes", "#1A1A1A", 2000,
                "https://images.pexels.com/photos/2529148/pexels-photo-2529148.jpeg?auto=compress&cs=tinysrgb&w=800",
                "حذاء رسمي", "rare");

        // ═══ 🎩 قبعات ═══
        addClothing(db, "hat_cap", "قبعة رياضية", "hat", "#C62828", 800,
                "https://images.pexels.com/photos/1124468/pexels-photo-1124468.jpeg?auto=compress&cs=tinysrgb&w=800",
                "قبعة رياضية", "common");
        addClothing(db, "hat_top", "قبعة رسمية", "hat", "#1A1A1A", 2500,
                "https://images.pexels.com/photos/1124468/pexels-photo-1124468.jpeg?auto=compress&cs=tinysrgb&w=800",
                "قبعة فاخرة", "epic");

        // ═══ 🕶️ نظارات ═══
        addClothing(db, "glasses_sun", "نظارات شمسية", "glasses", "#1A1A1A", 1200,
                "https://images.pexels.com/photos/46710/pexels-photo-46710.jpeg?auto=compress&cs=tinysrgb&w=800",
                "نظارات شمسية", "common");
        addClothing(db, "glasses_blue", "نظارات زرقاء", "glasses", "#1565C0", 1500,
                "https://images.pexels.com/photos/46710/pexels-photo-46710.jpeg?auto=compress&cs=tinysrgb&w=800",
                "نظارات زرقاء", "rare");
    }

    private static void addVehicle(FirebaseFirestore db, String id, String name, String brand,
                                    String type, int price, String img, String desc, String rarity) {
        addItem(db, id, name, brand, "vehicle", type, price, img, desc, rarity, null, null);
    }

    private static void addProperty(FirebaseFirestore db, String id, String name, String type,
                                     int price, String img, String desc, String rarity) {
        addItem(db, id, name, "أُمّة العقارية", "property", type, price, img, desc, rarity, null, null);
    }

    private static void addElectronics(FirebaseFirestore db, String id, String name, String type,
                                        int price, String img, String desc, String rarity) {
        addItem(db, id, name, "إلكترونيات", "electronics", type, price, img, desc, rarity, null, null);
    }

    private static void addPhone(FirebaseFirestore db, String id, String name, String brand,
                                  int price, String img, String color, String desc, String rarity) {
        addItem(db, id, name, brand, "electronics", "phone", price, img, desc, rarity, "phone", color);
    }

    private static void addClothing(FirebaseFirestore db, String id, String name, String wearType,
                                     String color, int price, String img, String desc, String rarity) {
        addItem(db, id, name, "أزياء أُمّة", "clothing", wearType, price, img, desc, rarity, wearType, color);
    }

    private static void addItem(FirebaseFirestore db, String id, String name, String brand,
                                 String category, String type, int price, String img,
                                 String desc, String rarity, String wearType, String color) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("brand", brand);
        data.put("category", category);
        data.put("type", type);
        data.put("price", price);
        data.put("imageUrl", img);
        data.put("description", desc);
        data.put("rarity", rarity);
        data.put("stock", 99);
        data.put("createdAt", System.currentTimeMillis());
        if (wearType != null) data.put("wearType", wearType);
        if (color != null) data.put("color", color);

        db.collection("market_items").document(id).set(data);
    }
}
