package com.ummah.app;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
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

    public ListenerRegistration listenByCategory(String category, final ItemsListener l) {
        return db.collection("market_items")
                .whereEqualTo("category", category)
                .orderBy("price", Query.Direction.ASCENDING)
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
                    l.onItems(items);
                });
    }

    public ListenerRegistration listenAll(final ItemsListener l) {
        return db.collection("market_items")
                .orderBy("price", Query.Direction.ASCENDING)
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

            com.google.firebase.firestore.DocumentReference invRef =
                    db.collection("users_inventory").document(buyerId)
                      .collection("items").document();

            transaction.set(invRef, inventory);

            return null;
        }).addOnSuccessListener(a -> cb.onSuccess())
          .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public ListenerRegistration listenMyInventory(String userId, final ItemsListener l) {
        return db.collection("users_inventory").document(userId)
                .collection("items")
                .orderBy("boughtAt", Query.Direction.DESCENDING)
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
}
