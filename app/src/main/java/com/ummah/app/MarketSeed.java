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
                "شقة صغيرة للبداية", "common");
        addProperty(db, "apartment_2br", "شقة غرفتين", "apartment", 50000,
                "https://images.pexels.com/photos/1918291/pexels-photo-1918291.jpeg?auto=compress&cs=tinysrgb&w=800",
                "شقة عصرية، غرفتين + صالون", "rare");
        addProperty(db, "villa_small", "فيلا صغيرة", "villa", 150000,
                "https://images.pexels.com/photos/1396122/pexels-photo-1396122.jpeg?auto=compress&cs=tinysrgb&w=800",
                "فيلا مستقلة بحديقة", "epic");
        addProperty(db, "villa_luxury", "فيلا فاخرة", "villa", 500000,
                "https://images.pexels.com/photos/1029599/pexels-photo-1029599.jpeg?auto=compress&cs=tinysrgb&w=800",
                "فيلا فخمة مع مسبح", "legendary");
        addProperty(db, "palace", "قصر أُمّة", "palace", 2000000,
                "https://images.pexels.com/photos/32870/pexels-photo.jpg?auto=compress&cs=tinysrgb&w=800",
                "قصر ملكي فاخر", "legendary");
    }

    private static void addVehicle(FirebaseFirestore db, String id, String name, String brand,
                                    String type, int price, String img, String desc, String rarity) {
        addItem(db, id, name, brand, "vehicle", type, price, img, desc, rarity);
    }

    private static void addProperty(FirebaseFirestore db, String id, String name, String type,
                                     int price, String img, String desc, String rarity) {
        addItem(db, id, name, "أُمّة العقارية", "property", type, price, img, desc, rarity);
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

        db.collection("market_items").document(id).set(data);
    }
}
