package com.ummah.app;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CitySeed {

    public static void seed(FirebaseFirestore db) {
        // ═══ 6 مناطق ═══
        addDistrict(db, "center", "الوسط التجاري", "🏛️",
                "قلب المدينة — مكاتب ومتاجر فاخرة",
                50000, "#D4AF37", 0, 0, 6, 6, "premium");

        addDistrict(db, "luxury", "الحي الراقي", "🏙️",
                "فيلات وقصور خاصة",
                20000, "#9C27B0", 6, 0, 6, 6, "high");

        addDistrict(db, "garden", "الحدائق", "🌳",
                "فيلات هادئة وسط المساحات الخضراء",
                30000, "#4CAF50", 12, 0, 6, 6, "high");

        addDistrict(db, "mid", "الحي المتوسط", "🏘️",
                "منازل عائلية متوسطة",
                5000, "#2196F3", 0, 6, 6, 6, "mid");

        addDistrict(db, "suburb", "الضواحي", "🏚️",
                "منازل صغيرة اقتصادية",
                1500, "#757575", 6, 6, 6, 6, "low");

        addDistrict(db, "industrial", "المنطقة الصناعية", "🏭",
                "مصانع ومستودعات",
                8000, "#FF5722", 12, 6, 6, 6, "mid");

        // ═══ 216 قطعة أرض (6 مناطق × 36 قطعة) ═══
        seedDistrictPlots(db, "center", 0, 0, 50000);
        seedDistrictPlots(db, "luxury", 6, 0, 20000);
        seedDistrictPlots(db, "garden", 12, 0, 30000);
        seedDistrictPlots(db, "mid", 0, 6, 5000);
        seedDistrictPlots(db, "suburb", 6, 6, 1500);
        seedDistrictPlots(db, "industrial", 12, 6, 8000);
    }

    private static void addDistrict(FirebaseFirestore db, String id, String name,
                                     String emoji, String desc, int basePrice,
                                     String color, int gridX, int gridZ,
                                     int width, int depth, String tier) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("emoji", emoji);
        data.put("description", desc);
        data.put("basePrice", basePrice);
        data.put("colorHex", color);
        data.put("gridX", gridX);
        data.put("gridZ", gridZ);
        data.put("width", width);
        data.put("depth", depth);
        data.put("tier", tier);
        data.put("createdAt", System.currentTimeMillis());

        db.collection("city_districts").document(id).set(data);
    }

    private static void seedDistrictPlots(FirebaseFirestore db, String districtId,
                                            int startX, int startZ, int basePrice) {
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                int x = startX + i;
                int z = startZ + j;
                String plotId = districtId + "_" + x + "_" + z;

                // السعر يزيد في وسط المنطقة
                int centerBonus = 0;
                if (i >= 2 && i <= 3 && j >= 2 && j <= 3) {
                    centerBonus = (int) (basePrice * 0.5);
                } else if (i >= 1 && i <= 4 && j >= 1 && j <= 4) {
                    centerBonus = (int) (basePrice * 0.2);
                }

                int finalPrice = basePrice + centerBonus;

                Map<String, Object> data = new HashMap<>();
                data.put("districtId", districtId);
                data.put("x", x);
                data.put("z", z);
                data.put("price", finalPrice);
                data.put("ownerId", "");
                data.put("ownerName", "");
                data.put("buildingType", "");
                data.put("buildingLevel", 0);
                data.put("boughtAt", 0);
                data.put("builtAt", 0);

                db.collection("city_plots").document(plotId).set(data);
            }
        }
    }
}
