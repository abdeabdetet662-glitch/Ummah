package com.ummah.app;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class WheelSeed {

    public static void seed(FirebaseFirestore db) {
        // ═══ 12 قطاع ═══
        // شائعة (خضراء)
        add(db, 0, "💚", "100 Đ", 100, "money", "#2E7D32", 20);
        add(db, 1, "💚", "250 Đ", 250, "money", "#388E3C", 15);
        add(db, 2, "💚", "500 Đ", 500, "money", "#43A047", 12);
        add(db, 3, "💚", "1000 Đ", 1000, "money", "#4CAF50", 8);

        // نادرة (زرقاء)
        add(db, 4, "💙", "2000 Đ", 2000, "money", "#1565C0", 6);
        add(db, 5, "💙", "هاتف", 3000, "item", "#1976D2", 5);
        add(db, 6, "💙", "سماعات", 2000, "item", "#1E88E5", 5);
        add(db, 7, "💙", "5000 Đ", 5000, "money", "#2196F3", 3);

        // أسطورية (بنفسجية/ذهبية)
        add(db, 8, "💜", "سيارة", 35000, "item", "#6A1B9A", 2);
        add(db, 9, "💜", "منزل صغير", 20000, "item", "#7B1FA2", 1);
        add(db, 10, "💜", "قصر", 2000000, "item", "#8E24AA", 1);
        add(db, 11, "💎", "جاكبوت", 0, "jackpot", "#FFD700", 1);

        // ═══ Config ═══
        Map<String, Object> cfg = new HashMap<>();
        cfg.put("spinCost", 500);
        cfg.put("freeSpinEnabled", true);
        cfg.put("freeSpinCooldownMs", 24L * 60 * 60 * 1000);
        cfg.put("jackpotEnabled", true);
        cfg.put("jackpotAmount", 0);
        cfg.put("jackpotMin", 100000);
        cfg.put("jackpotPercent", 1);
        cfg.put("active", true);
        db.collection("wheel_config").document("main").set(cfg);

        // ═══ Jackpot init ═══
        Map<String, Object> jm = new HashMap<>();
        jm.put("amount", 0);
        db.collection("wheel_jackpot").document("main").set(jm);
    }

    private static void add(FirebaseFirestore db, int order, String emoji,
                             String label, int value, String type,
                             String color, int weight) {
        Map<String, Object> data = new HashMap<>();
        data.put("emoji", emoji);
        data.put("label", label);
        data.put("value", value);
        data.put("type", type);
        data.put("color", color);
        data.put("weight", weight);
        data.put("order", order);
        data.put("active", true);
        if ("item".equals(type)) data.put("itemName", label);

        db.collection("wheel_segments").document("seg_" + order).set(data);
    }
}
