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

public class CitizenListingManager {

    private final FirebaseFirestore db;
    public static final double COMMISSION = 0.05; // 5%

    public CitizenListingManager() {
        db = FirebaseFirestore.getInstance();
    }

    public interface ListingsListener {
        void onListings(List<CitizenListing> listings);
        void onError(String msg);
    }

    public interface OnDone {
        void onSuccess();
        void onError(String msg);
    }

    public interface BuyDone {
        void onSuccess(int paid);
        void onError(String msg);
    }

    // كل العروض النشطة
    public ListenerRegistration listenActiveListings(final ListingsListener l) {
        return db.collection("citizen_listings")
                .whereEqualTo("status", "active")
                .addSnapshotListener((snap, e) -> {
                    if (e != null) { l.onError(e.getMessage()); return; }
                    if (snap == null) { l.onListings(new ArrayList<>()); return; }
                    List<CitizenListing> list = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        CitizenListing cl = d.toObject(CitizenListing.class);
                        if (cl != null) {
                            cl.id = d.getId();
                            list.add(cl);
                        }
                    }
                    // نرتبو حسب الأحدث
                    list.sort((a, b) -> Long.compare(b.listedAt, a.listedAt));
                    l.onListings(list);
                });
    }

    // عروض مواطن معين
    public ListenerRegistration listenMyListings(String userId, final ListingsListener l) {
        return db.collection("citizen_listings")
                .whereEqualTo("sellerId", userId)
                .whereEqualTo("status", "active")
                .addSnapshotListener((snap, e) -> {
                    if (e != null) { l.onError(e.getMessage()); return; }
                    if (snap == null) { l.onListings(new ArrayList<>()); return; }
                    List<CitizenListing> list = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        CitizenListing cl = d.toObject(CitizenListing.class);
                        if (cl != null) {
                            cl.id = d.getId();
                            list.add(cl);
                        }
                    }
                    l.onListings(list);
                });
    }

    // عرض منتج للبيع
    public void createListing(String sellerId, String sellerName, MarketItem item,
                               int price, String inventoryDocId, final OnDone cb) {
        Map<String, Object> data = new HashMap<>();
        data.put("sellerId", sellerId);
        data.put("sellerName", sellerName);
        data.put("itemId", item.id);
        data.put("inventoryDocId", inventoryDocId);
        data.put("itemName", item.name);
        data.put("itemBrand", item.brand);
        data.put("itemCategory", item.category);
        data.put("itemType", item.type);
        data.put("imageUrl", item.imageUrl);
        data.put("rarity", item.rarity);
        data.put("description", item.description);
        data.put("originalPrice", item.price);
        data.put("price", price);
        data.put("listedAt", System.currentTimeMillis());
        data.put("status", "active");

        db.collection("citizen_listings").add(data)
                .addOnSuccessListener(ref -> {
                    // نحدّثو الـ inventory: نعلمو أنه معروض للبيع
                    db.collection("users_inventory").document(sellerId)
                            .collection("items").document(inventoryDocId)
                            .update("listedForSale", true, "listingId", ref.getId())
                            .addOnSuccessListener(v -> cb.onSuccess())
                            .addOnFailureListener(e -> cb.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // إلغاء العرض
    public void cancelListing(String sellerId, String listingId, String inventoryDocId, final OnDone cb) {
        db.collection("citizen_listings").document(listingId)
                .update("status", "cancelled")
                .addOnSuccessListener(v -> {
                    if (inventoryDocId != null && !inventoryDocId.isEmpty()) {
                        db.collection("users_inventory").document(sellerId)
                                .collection("items").document(inventoryDocId)
                                .update("listedForSale", false, "listingId", "")
                                .addOnSuccessListener(v2 -> cb.onSuccess())
                                .addOnFailureListener(e -> cb.onSuccess());
                    } else {
                        cb.onSuccess();
                    }
                })
                .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // شراء منتج من مواطن آخر
    public void buyFromCitizen(final String buyerId, final CitizenListing listing, final BuyDone cb) {
        db.runTransaction(transaction -> {
            DocumentReference buyerRef = db.collection("citizens").document(buyerId);
            DocumentReference sellerRef = db.collection("citizens").document(listing.sellerId);
            DocumentReference listingRef = db.collection("citizen_listings").document(listing.id);

            DocumentSnapshot buyer = transaction.get(buyerRef);
            DocumentSnapshot seller = transaction.get(sellerRef);
            DocumentSnapshot listingDoc = transaction.get(listingRef);

            if (!buyer.exists()) throw new RuntimeException("المشتري غير موجود");
            if (!seller.exists()) throw new RuntimeException("البائع غير موجود");
            if (!listingDoc.exists()) throw new RuntimeException("العرض غير موجود");

            String status = listingDoc.getString("status");
            if (!"active".equals(status)) throw new RuntimeException("العرض لم يعد متوفراً");

            Long balL = buyer.getLong("balance");
            int bal = balL != null ? balL.intValue() : 0;
            if (bal < listing.price) throw new RuntimeException("الرصيد غير كافٍ");

            // عمولة 5% للتطبيق (نخصمو من البائع)
            int commission = (int) (listing.price * COMMISSION);
            int sellerGets = listing.price - commission;

            // خصم من المشتري
            transaction.update(buyerRef, "balance", bal - listing.price);

            // إضافة للبائع
            Long sellerBalL = seller.getLong("balance");
            int sellerBal = sellerBalL != null ? sellerBalL.intValue() : 0;
            transaction.update(sellerRef, "balance", sellerBal + sellerGets);

            // نحدثو حالة العرض
            Map<String, Object> upd = new HashMap<>();
            upd.put("status", "sold");
            upd.put("buyerId", buyerId);
            upd.put("soldAt", System.currentTimeMillis());
            transaction.update(listingRef, upd);

            // نضيف المنتج لمخزون المشتري (نسخة جديدة)
            Map<String, Object> invData = new HashMap<>();
            invData.put("itemId", listing.itemId);
            invData.put("name", listing.itemName);
            invData.put("brand", listing.itemBrand);
            invData.put("category", listing.itemCategory);
            invData.put("type", listing.itemType);
            invData.put("price", listing.originalPrice);
            invData.put("imageUrl", listing.imageUrl);
            invData.put("boughtAt", System.currentTimeMillis());
            invData.put("boughtFrom", listing.sellerName);
            invData.put("listedForSale", false);

            DocumentReference newInv = db.collection("users_inventory")
                    .document(buyerId).collection("items").document();
            transaction.set(newInv, invData);

            // نحذفو من مخزون البائع
            String invDocId = listingDoc.getString("inventoryDocId");
            if (invDocId != null && !invDocId.isEmpty()) {
                DocumentReference oldInv = db.collection("users_inventory")
                        .document(listing.sellerId).collection("items").document(invDocId);
                transaction.delete(oldInv);
            }

            return listing.price;
        }).addOnSuccessListener(price -> cb.onSuccess(price))
          .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }
}
