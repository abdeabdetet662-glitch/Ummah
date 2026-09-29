package com.ummah.app;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarketManager {

    private final FirebaseFirestore db;

    public MarketManager() {
        db = FirebaseFirestore.getInstance();
    }

    public interface ItemsListener {
        void onItems(List<MarketItem> items);
        void onError(String msg);
    }

    public interface OnDone {
        void onSuccess();
        void onError(String msg);
    }

    // بدون orderBy (باش ما يحتاجش index)
    public ListenerRegistration listenByCategory(String category, final ItemsListener l) {
        return db.collection("market_items")
                .whereEqualTo("category", category)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) { l.onError(e.getMessage()); return; }
                    if (snap == null) { l.onItems(new ArrayList<>()); return; }
                    List<MarketItem> items = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        MarketItem item = d.toObject(MarketItem.class);
                        if (item != null) {
                            item.id = d.getId();
                            items.add(item);
                        }
                    }
                    // الترتيب في Java
                    Collections.sort(items, new Comparator<MarketItem>() {
                        @Override public int compare(MarketItem a, MarketItem b) {
                            return Integer.compare(a.price, b.price);
                        }
                    });
                    l.onItems(items);
                });
    }

    public ListenerRegistration listenAll(final ItemsListener l) {
        return db.collection("market_items")
                .addSnapshotListener((snap, e) -> {
                    if (e != null) { l.onError(e.getMessage()); return; }
                    if (snap == null) { l.onItems(new ArrayList<>()); return; }
                    List<MarketItem> items = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        MarketItem item = d.toObject(MarketItem.class);
                        if (item != null) {
                            item.id = d.getId();
                            items.add(item);
                        }
                    }
                    Collections.sort(items, new Comparator<MarketItem>() {
                        @Override public int compare(MarketItem a, MarketItem b) {
                            return Integer.compare(a.price, b.price);
                        }
                    });
                    l.onItems(items);
                });
    }

    public void buyItem(final String buyerId, final MarketItem item, final OnDone cb) {
        db.runTransaction(transaction -> {
            com.google.firebase.firestore.DocumentReference userRef =
                    db.collection("citizens").document(buyerId);
            DocumentSnapshot user = transaction.get(userRef);
            if (!user.exists()) throw new RuntimeException("المستخدم غير موجود");

            Long balance = user.getLong("balance");
            int bal = balance != null ? balance.intValue() : 0;

            if (bal < item.price) throw new RuntimeException("الرصيد غير كافٍ");

            transaction.update(userRef, "balance", bal - item.price);

            Map<String, Object> inventory = new HashMap<>();
            inventory.put("itemId", item.id);
            inventory.put("name", item.name);
            inventory.put("brand", item.brand);
            inventory.put("category", item.category);
            inventory.put("type", item.type);
            inventory.put("price", item.price);
            inventory.put("imageUrl", item.imageUrl);
            inventory.put("boughtAt", System.currentTimeMillis());

            // metadata للملابس
            com.google.firebase.firestore.DocumentSnapshot itemDoc =
                    transaction.get(db.collection("market_items").document(item.id));
            if (itemDoc.exists()) {
                String wearType = itemDoc.getString("wearType");
                String color = itemDoc.getString("color");
                if (wearType != null) inventory.put("wearType", wearType);
                if (color != null) inventory.put("color", color);
            }

            com.google.firebase.firestore.DocumentReference invRef =
                    db.collection("users_inventory").document(buyerId)
                      .collection("items").document();

            transaction.set(invRef, inventory);

            return null;
        }).addOnSuccessListener(a -> cb.onSuccess())
          .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // بدون orderBy
    public ListenerRegistration listenMyInventory(String userId, final ItemsListener l) {
        return db.collection("users_inventory").document(userId)
                .collection("items")
                .addSnapshotListener((snap, e) -> {
                    if (e != null) { l.onError(e.getMessage()); return; }
                    if (snap == null) { l.onItems(new ArrayList<>()); return; }
                    List<MarketItem> items = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        MarketItem item = new MarketItem();
                        item.id = d.getId();
                        item.name = d.getString("name");
                        item.brand = d.getString("brand");
                        item.category = d.getString("category");
                        item.type = d.getString("type");
                        Long p = d.getLong("price");
                        item.price = p != null ? p.intValue() : 0;
                        item.imageUrl = d.getString("imageUrl");
                        items.add(item);
                    }
                    l.onItems(items);
                });
    }


    public void sellItem(final String sellerId, final String inventoryDocId,
                          final int originalPrice, final OnDone cb) {
        db.runTransaction(transaction -> {
            com.google.firebase.firestore.DocumentReference userRef =
                    db.collection("citizens").document(sellerId);
            com.google.firebase.firestore.DocumentReference invRef =
                    db.collection("users_inventory").document(sellerId)
                      .collection("items").document(inventoryDocId);

            DocumentSnapshot user = transaction.get(userRef);
            DocumentSnapshot inv = transaction.get(invRef);

            if (!user.exists()) throw new RuntimeException("المستخدم غير موجود");
            if (!inv.exists()) throw new RuntimeException("المنتج غير موجود");

            Long balance = user.getLong("balance");
            int bal = balance != null ? balance.intValue() : 0;

            // نرجعو 70% من السعر
            int refund = (int) (originalPrice * 0.7);

            transaction.update(userRef, "balance", bal + refund);
            transaction.delete(invRef);

            return refund;
        }).addOnSuccessListener(r -> cb.onSuccess())
          .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }
}
