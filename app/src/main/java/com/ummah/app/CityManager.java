package com.ummah.app;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CityManager {

    private final FirebaseFirestore db;

    public CityManager() {
        db = FirebaseFirestore.getInstance();
    }

    public interface DistrictsListener {
        void onDistricts(List<CityDistrict> districts);
        void onError(String msg);
    }

    public interface PlotsListener {
        void onPlots(List<CityPlot> plots);
        void onError(String msg);
    }

    public interface OnDone {
        void onSuccess();
        void onError(String msg);
    }

    // ═══ المناطق ═══
    public ListenerRegistration listenDistricts(final DistrictsListener l) {
        return db.collection("city_districts").addSnapshotListener((snap, e) -> {
            if (e != null) { l.onError(e.getMessage()); return; }
            if (snap == null) { l.onDistricts(new ArrayList<>()); return; }
            List<CityDistrict> list = new ArrayList<>();
            for (DocumentSnapshot d : snap.getDocuments()) {
                CityDistrict cd = d.toObject(CityDistrict.class);
                if (cd != null) {
                    cd.id = d.getId();
                    list.add(cd);
                }
            }
            l.onDistricts(list);
        });
    }

    // ═══ كل القطع ═══
    public ListenerRegistration listenAllPlots(final PlotsListener l) {
        return db.collection("city_plots").addSnapshotListener((snap, e) -> {
            if (e != null) { l.onError(e.getMessage()); return; }
            if (snap == null) { l.onPlots(new ArrayList<>()); return; }
            List<CityPlot> list = new ArrayList<>();
            for (DocumentSnapshot d : snap.getDocuments()) {
                CityPlot cp = d.toObject(CityPlot.class);
                if (cp != null) {
                    cp.id = d.getId();
                    list.add(cp);
                }
            }
            l.onPlots(list);
        });
    }

    // ═══ شراء قطعة ═══
    public void buyPlot(final String buyerId, final String buyerName, final CityPlot plot, final OnDone cb) {
        db.runTransaction(transaction -> {
            // كل القراءات
            DocumentReference userRef = db.collection("citizens").document(buyerId);
            DocumentReference plotRef = db.collection("city_plots").document(plot.id);

            DocumentSnapshot user = transaction.get(userRef);
            DocumentSnapshot plotDoc = transaction.get(plotRef);

            if (!user.exists()) throw new RuntimeException("المستخدم غير موجود");
            if (!plotDoc.exists()) throw new RuntimeException("القطعة غير موجودة");

            String owner = plotDoc.getString("ownerId");
            if (owner != null && !owner.isEmpty()) throw new RuntimeException("القطعة مملوكة");

            Long balL = user.getLong("balance");
            int bal = balL != null ? balL.intValue() : 0;
            if (bal < plot.price) throw new RuntimeException("الرصيد غير كافٍ");

            // كل الكتابات
            transaction.update(userRef, "balance", bal - plot.price);
            transaction.update(plotRef, "ownerId", buyerId, "ownerName", buyerName,
                    "boughtAt", System.currentTimeMillis());

            return null;
        }).addOnSuccessListener(a -> cb.onSuccess())
          .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ═══ بناء مبنى على قطعة ═══
    public void buildOnPlot(final String ownerId, final CityPlot plot,
                            final String buildingType, final int cost, final OnDone cb) {
        db.runTransaction(transaction -> {
            // كل القراءات
            DocumentReference userRef = db.collection("citizens").document(ownerId);
            DocumentReference plotRef = db.collection("city_plots").document(plot.id);

            DocumentSnapshot user = transaction.get(userRef);
            DocumentSnapshot plotDoc = transaction.get(plotRef);

            if (!user.exists()) throw new RuntimeException("المستخدم غير موجود");
            if (!plotDoc.exists()) throw new RuntimeException("القطعة غير موجودة");

            String owner = plotDoc.getString("ownerId");
            if (owner == null || !owner.equals(ownerId)) throw new RuntimeException("هذي القطعة ماشي ديالك");

            String existing = plotDoc.getString("buildingType");
            if (existing != null && !existing.isEmpty()) throw new RuntimeException("القطعة فيها مبنى");

            Long balL = user.getLong("balance");
            int bal = balL != null ? balL.intValue() : 0;
            if (bal < cost) throw new RuntimeException("الرصيد غير كافي للبناء");

            // كل الكتابات
            transaction.update(userRef, "balance", bal - cost);
            transaction.update(plotRef,
                    "buildingType", buildingType,
                    "buildingLevel", 1,
                    "builtAt", System.currentTimeMillis());

            return null;
        }).addOnSuccessListener(a -> cb.onSuccess())
          .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ═══ ترقية مبنى ═══
    public void upgradeBuilding(final String ownerId, final CityPlot plot,
                                final int cost, final OnDone cb) {
        db.runTransaction(transaction -> {
            DocumentReference userRef = db.collection("citizens").document(ownerId);
            DocumentReference plotRef = db.collection("city_plots").document(plot.id);

            DocumentSnapshot user = transaction.get(userRef);
            DocumentSnapshot plotDoc = transaction.get(plotRef);

            if (!user.exists()) throw new RuntimeException("المستخدم غير موجود");
            if (!plotDoc.exists()) throw new RuntimeException("القطعة غير موجودة");

            String owner = plotDoc.getString("ownerId");
            if (owner == null || !owner.equals(ownerId)) throw new RuntimeException("ماشي ديالك");

            Long lvlL = plotDoc.getLong("buildingLevel");
            int lvl = lvlL != null ? lvlL.intValue() : 0;
            if (lvl >= 5) throw new RuntimeException("وصلت للمستوى الأقصى");

            Long balL = user.getLong("balance");
            int bal = balL != null ? balL.intValue() : 0;
            if (bal < cost) throw new RuntimeException("الرصيد غير كافٍ");

            transaction.update(userRef, "balance", bal - cost);
            transaction.update(plotRef, "buildingLevel", lvl + 1);

            return null;
        }).addOnSuccessListener(a -> cb.onSuccess())
          .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ═══ بيع القطعة للتطبيق (60%) ═══
    public void sellPlotBackToSystem(final String ownerId, final CityPlot plot,
                                     final int refund, final OnDone cb) {
        db.runTransaction(transaction -> {
            DocumentReference userRef = db.collection("citizens").document(ownerId);
            DocumentReference plotRef = db.collection("city_plots").document(plot.id);

            DocumentSnapshot user = transaction.get(userRef);
            DocumentSnapshot plotDoc = transaction.get(plotRef);

            if (!user.exists()) throw new RuntimeException("المستخدم غير موجود");

            String owner = plotDoc.getString("ownerId");
            if (owner == null || !owner.equals(ownerId)) throw new RuntimeException("ماشي ديالك");

            Long balL = user.getLong("balance");
            int bal = balL != null ? balL.intValue() : 0;

            transaction.update(userRef, "balance", bal + refund);
            transaction.update(plotRef,
                    "ownerId", "",
                    "ownerName", "",
                    "buildingType", "",
                    "buildingLevel", 0);

            return null;
        }).addOnSuccessListener(a -> cb.onSuccess())
          .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ═══ نعرضو قطعة للبيع لمواطن آخر ═══
    public void listPlotForSale(final String sellerId, final CityPlot plot,
                                final int price, final OnDone cb) {
        Map<String, Object> data = new HashMap<>();
        data.put("plotId", plot.id);
        data.put("sellerId", sellerId);
        data.put("price", price);
        data.put("status", "active");
        data.put("listedAt", System.currentTimeMillis());
        data.put("districtId", plot.districtId);
        data.put("x", plot.x);
        data.put("z", plot.z);
        data.put("buildingType", plot.buildingType);
        data.put("buildingLevel", plot.buildingLevel);

        db.collection("plot_listings").add(data)
                .addOnSuccessListener(ref -> {
                    db.collection("city_plots").document(plot.id)
                            .update("listedForSale", true, "listingId", ref.getId())
                            .addOnSuccessListener(v -> cb.onSuccess())
                            .addOnFailureListener(e -> cb.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }
}
