package com.ummah.app;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class JobSeed {

    public static void seed(FirebaseFirestore db) {
        addJob(db, "driver", "سائق توصيل", "🚚",
                "وصّل الطلبات في كل أنحاء أُمّة",
                50, 30, 1, "#0D47A1");

        addJob(db, "chef", "طباخ", "🍔",
                "اطبخ أطباق لذيذة للمواطنين",
                100, 30, 1, "#E65100");

        addJob(db, "journalist", "صحفي", "📰",
                "اكتب الأخبار والمقالات",
                150, 30, 2, "#4A148C");

        addJob(db, "builder", "عامل بناء", "🏗️",
                "ابنِ المنازل والعمارات",
                80, 30, 1, "#5D4037");

        addJob(db, "doctor", "طبيب", "🩺",
                "عالج المواطنين المرضى",
                200, 30, 3, "#1B5E20");

        addJob(db, "programmer", "مبرمج", "💻",
                "ابنِ التطبيقات والمواقع",
                250, 30, 3, "#00695C");
    }

    private static void addJob(FirebaseFirestore db, String id, String title,
                                String emoji, String desc, int salary,
                                int cooldown, int reqLevel, String color) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("emoji", emoji);
        data.put("description", desc);
        data.put("salary", salary);
        data.put("cooldownMin", cooldown);
        data.put("requiredLevel", reqLevel);
        data.put("color", color);

        db.collection("jobs").document(id).set(data);
    }
}
