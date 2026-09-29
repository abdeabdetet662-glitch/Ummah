package com.ummah.app;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MarketCleanup {

    public static void cleanupOldItems(FirebaseFirestore db) {
        // نمسحو كل المنتجات اللي IDs عشوائية (20 حرف)
        // IDs الثابتة اللي درناها قصيرة (مثلاً "bmw_3series")
        db.collection("market_items").get().addOnSuccessListener(snap -> {
            for (DocumentSnapshot d : snap.getDocuments()) {
                String id = d.getId();
                // إذا الـ ID فيه شرطة سفلية (_) → هذا جديد
                // إذا ما فيهاش → عشوائي → نمسحو
                if (!id.contains("_")) {
                    d.getReference().delete();
                }
            }
        });
    }
}
