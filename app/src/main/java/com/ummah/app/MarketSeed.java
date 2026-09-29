package com.ummah.app;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MarketSeed {

    private static final String IMG_BMW = "https://images.pexels.com/photos/170811/pexels-photo-170811.jpeg?auto=compress&cs=tinysrgb&w=800";
    private static final String IMG_MERCEDES = "https://images.pexels.com/photos/120049/pexels-photo-120049.jpeg?auto=compress&cs=tinysrgb&w=800";
    private static final String IMG_TOYOTA = "https://images.pexels.com/photos/116675/pexels-photo-116675.jpeg?auto=compress&cs=tinysrgb&w=800";
    private static final String IMG_FERRARI = "https://images.pexels.com/photos/337909/pexels-photo-337909.jpeg?auto=compress&cs=tinysrgb&w=800";
    private static final String IMG_LAMBO = "https://images.pexels.com/photos/2127733/pexels-photo-2127733.jpeg?auto=compress&cs=tinysrgb&w=800";
    private static final String IMG_MOTO_1 = "https://images.pexels.com/photos/1413412/pexels-photo-1413412.jpeg?auto=compress&cs=tinysrgb&w=800";
    private static final String IMG_MOTO_2 = "https://images.pexels.com/photos/2116475/pexels-photo-2116475.jpeg?auto=compress&cs=tinysrgb&w=800";

    public static void seed(FirebaseFirestore db) {
        addItem(db, "تويوتا كورولا", "Toyota", "car", 5000,
                IMG_TOYOTA, "سيارة اقتصادية موثوقة، مثالية للبداية", "common");

        addItem(db, "BMW الفئة الثالثة", "BMW", "car", 35000,
                IMG_BMW, "سيارة فاخرة رياضية أنيقة", "rare");

        addItem(db, "مرسيدس S-Class", "Mercedes", "car", 85000,
                IMG_MERCEDES, "سيارة رجال الأعمال، راحة تامة", "epic");

        addItem(db, "فيراري F8", "Ferrari", "car", 250000,
                IMG_FERRARI, "سيارة رياضية أسطورية", "legendary");

        addItem(db, "لامبورجيني أفنتادور", "Lamborghini", "car", 350000,
                IMG_LAMBO, "أسطورة الطرقات، قوة لا توصف", "legendary");

        addItem(db, "دراجة نارية رياضية", "Yamaha", "motorcycle", 8000,
                IMG_MOTO_1, "سريعة خفيفة للشوارع", "common");

        addItem(db, "دراجة هارلي ديفيدسون", "Harley", "motorcycle", 25000,
                IMG_MOTO_2, "دراجة كلاسيكية فخمة", "rare");
    }

    private static void addItem(FirebaseFirestore db, String name, String brand,
                                 String type, int price, String img, String desc, String rarity) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("brand", brand);
        data.put("category", "vehicle");
        data.put("type", type);
        data.put("price", price);
        data.put("imageUrl", img);
        data.put("description", desc);
        data.put("rarity", rarity);
        data.put("stock", 99);
        data.put("createdAt", System.currentTimeMillis());

        db.collection("market_items").add(data);
    }
}
