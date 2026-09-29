package com.ummah.app;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MarketSeed {

    public static void seed(FirebaseFirestore db) {
        // ═══ 🚗 مركبات ═══
        addVehicle(db, "toyota_corolla", "تويوتا كورولا", "Toyota", "car", 5000,
                "https://images.pexels.com/photos/116675/pexels-photo-116675.jpeg?auto=compress&cs=tinysrgb&w=800",
                "سيارة اقتصادية موثوقة، مثالية للبداية", "common");

        addVehicle(db, "bmw_3series", "BMW الفئة الثالثة", "BMW", "car", 35000,
                "https://images.pexels.com/photos/170811/pexels-photo-170811.jpeg?auto=compress&cs=tinysrgb&w=800",
                "سيارة فاخرة رياضية أنيقة", "rare");

        addVehicle(db, "mercedes_sclass", "مرسيدس S-Class", "Mercedes", "car", 85000,
                "https://images.pexels.com/photos/120049/pexels-photo-120049.jpeg?auto=compress&cs=tinysrgb&w=800",
                "سيارة رجال الأعمال، راحة تامة", "epic");

        addVehicle(db, "ferrari_f8", "فيراري F8", "Ferrari", "car", 250000,
                "https://images.pexels.com/photos/337909/pexels-photo-337909.jpeg?auto=compress&cs=tinysrgb&w=800",
                "سيارة رياضية أسطورية", "legendary");

        addVehicle(db, "lambo_aventador", "لامبورجيني أفنتادور", "Lamborghini", "car", 350000,
                "https://images.pexels.com/photos/2127733/pexels-photo-2127733.jpeg?auto=compress&cs=tinysrgb&w=800",
                "أسطورة الطرقات، قوة لا توصف", "legendary");

        addVehicle(db, "yamaha_sport", "دراجة ياماها رياضية", "Yamaha", "motorcycle", 8000,
                "https://images.pexels.com/photos/1413412/pexels-photo-1413412.jpeg?auto=compress&cs=tinysrgb&w=800",
                "سريعة خفيفة للشوارع", "common");

        addVehicle(db, "harley_classic", "دراجة هارلي ديفيدسون", "Harley", "motorcycle", 25000,
                "https://images.pexels.com/photos/2116475/pexels-photo-2116475.jpeg?auto=compress&cs=tinysrgb&w=800",
                "دراجة كلاسيكية فخمة", "rare");

        // ═══ 🏠 عقارات ═══
        addProperty(db, "studio_small", "شقة استوديو", "apartment", 20000,
                "https://images.pexels.com/photos/1571460/pexels-photo-1571460.jpeg?auto=compress&cs=tinysrgb&w=800",
                "شقة صغيرة للبداية، غرفة واحدة + حمام", "common");

        addProperty(db, "apartment_2br", "شقة غرفتين", "apartment", 50000,
                "https://images.pexels.com/photos/1918291/pexels-photo-1918291.jpeg?auto=compress&cs=tinysrgb&w=800",
                "شقة عصرية، غرفتين + صالون + مطبخ", "rare");

        addProperty(db, "villa_small", "فيلا صغيرة", "villa", 150000,
                "https://images.pexels.com/photos/1396122/pexels-photo-1396122.jpeg?auto=compress&cs=tinysrgb&w=800",
                "فيلا مستقلة، 3 غرف + حديقة صغيرة", "epic");

        addProperty(db, "villa_luxury", "فيلا فاخرة", "villa", 500000,
                "https://images.pexels.com/photos/1029599/pexels-photo-1029599.jpeg?auto=compress&cs=tinysrgb&w=800",
                "فيلا فخمة، مسبح + حديقة واسعة + 5 غرف", "legendary");

        addProperty(db, "palace", "قصر أُمّة", "palace", 2000000,
                "https://images.pexels.com/photos/32870/pexels-photo.jpg?auto=compress&cs=tinysrgb&w=800",
                "قصر ملكي، 10 غرف + مسبح + ملعب + حراس", "legendary");

        // ═══ 📱 إلكترونيات ═══
        addElectronics(db, "phone_basic", "هاتف أساسي", "phone", 3000,
                "https://images.pexels.com/photos/788946/pexels-photo-788946.jpeg?auto=compress&cs=tinysrgb&w=800",
                "هاتف اقتصادي، للاتصال والتواصل", "common");

        addElectronics(db, "phone_mid", "هاتف متوسط", "phone", 10000,
                "https://images.pexels.com/photos/699122/pexels-photo-699122.jpeg?auto=compress&cs=tinysrgb&w=800",
                "هاتف عصري بكاميرا جيدة", "rare");

        addElectronics(db, "phone_premium", "هاتف فاخر", "phone", 30000,
                "https://images.pexels.com/photos/404280/pexels-photo-404280.jpeg?auto=compress&cs=tinysrgb&w=800",
                "أحدث هاتف، كاميرا احترافية", "epic");

        addElectronics(db, "laptop", "لابتوب احترافي", "laptop", 25000,
                "https://images.pexels.com/photos/18105/pexels-photo.jpg?auto=compress&cs=tinysrgb&w=800",
                "لابتوب قوي للعمل والدراسة", "epic");

        addElectronics(db, "headphones", "سماعات لاسلكية", "headphones", 2000,
                "https://images.pexels.com/photos/3394650/pexels-photo-3394650.jpeg?auto=compress&cs=tinysrgb&w=800",
                "سماعات بلوتوث عالية الجودة", "common");

        // ═══ 👕 ملابس ═══
        addClothing(db, "tshirt_basic", "قميص قطني", "shirt", 500,
                "https://images.pexels.com/photos/996329/pexels-photo-996329.jpeg?auto=compress&cs=tinysrgb&w=800",
                "قميص قطني مريح، كل الألوان", "common");

        addClothing(db, "jeans", "بنطال جينز", "pants", 800,
                "https://images.pexels.com/photos/1598507/pexels-photo-1598507.jpeg?auto=compress&cs=tinysrgb&w=800",
                "بنطال جينز كلاسيكي", "common");

        addClothing(db, "sneakers", "حذاء رياضي", "shoes", 1500,
                "https://images.pexels.com/photos/2529148/pexels-photo-2529148.jpeg?auto=compress&cs=tinysrgb&w=800",
                "حذاء رياضي مريح وأنيق", "common");

        addClothing(db, "jacket", "جاكيت شتوي", "jacket", 2000,
                "https://images.pexels.com/photos/1124468/pexels-photo-1124468.jpeg?auto=compress&cs=tinysrgb&w=800",
                "جاكيت شتوي دافئ", "rare");

        addClothing(db, "glasses", "نظارات شمسية", "accessory", 1000,
                "https://images.pexels.com/photos/46710/pexels-photo-46710.jpeg?auto=compress&cs=tinysrgb&w=800",
                "نظارات شمسية أنيقة", "common");
    }

    // ═══ Helpers ═══

    private static void addVehicle(FirebaseFirestore db, String id, String name, String brand,
                                    String type, int price, String img, String desc, String rarity) {
        addItem(db, id, name, brand, "vehicle", type, price, img, desc, rarity);
    }

    private static void addProperty(FirebaseFirestore db, String id, String name, String type,
                                     int price, String img, String desc, String rarity) {
        addItem(db, id, name, "أُمّة العقارية", "property", type, price, img, desc, rarity);
    }

    private static void addElectronics(FirebaseFirestore db, String id, String name, String type,
                                        int price, String img, String desc, String rarity) {
        addItem(db, id, name, "إلكترونيات", "electronics", type, price, img, desc, rarity);
    }

    private static void addClothing(FirebaseFirestore db, String id, String name, String type,
                                     int price, String img, String desc, String rarity) {
        addItem(db, id, name, "أزياء أُمّة", "clothing", type, price, img, desc, rarity);
    }

    private static void addItem(FirebaseFirestore db, String id, String name, String brand,
                                 String category, String type, int price, String img,
                                 String desc, String rarity) {
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

        // نستعملو document(id) → ما يتكررش
        db.collection("market_items").document(id).set(data);
    }
}
