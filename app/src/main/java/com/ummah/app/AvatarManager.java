package com.ummah.app;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;

public class AvatarManager {

    private final FirebaseFirestore db;

    public AvatarManager() {
        db = FirebaseFirestore.getInstance();
    }

    public interface AvatarListener {
        void onAvatar(Avatar avatar);
        void onError(String msg);
    }

    public interface OnDone {
        void onSuccess();
        void onError(String msg);
    }

    public ListenerRegistration listenAvatar(String userId, final AvatarListener l) {
        return db.collection("avatars").document(userId)
                .addSnapshotListener((doc, e) -> {
                    if (e != null) { l.onError(e.getMessage()); return; }
                    if (doc == null || !doc.exists()) {
                        l.onAvatar(new Avatar());
                        return;
                    }
                    Avatar a = doc.toObject(Avatar.class);
                    if (a == null) a = new Avatar();
                    l.onAvatar(a);
                });
    }

    public void saveAvatar(String userId, Avatar a, final OnDone cb) {
        a.updatedAt = System.currentTimeMillis();
        db.collection("avatars").document(userId).set(a)
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ═══ يرتدي قطعة (shirt/pants/shoes/hat/glasses/phone) ═══
    public void equipItem(String userId, String slot, String itemId, String itemName,
                           String colorHex, final OnDone cb) {
        Map<String, Object> data = new HashMap<>();
        data.put("equipped" + capitalize(slot) + "Id", itemId);
        data.put("equipped" + capitalize(slot) + "Name", itemName);
        data.put("equipped" + capitalize(slot) + "Color", colorHex);
        data.put("updatedAt", System.currentTimeMillis());

        db.collection("avatars").document(userId)
                .set(data, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(a -> cb.onSuccess())
                .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ═══ يخلع قطعة ═══
    public void unequipItem(String userId, String slot, final OnDone cb) {
        Map<String, Object> data = new HashMap<>();
        data.put("equipped" + capitalize(slot) + "Id", "");
        data.put("equipped" + capitalize(slot) + "Name", "");
        data.put("updatedAt", System.currentTimeMillis());

        db.collection("avatars").document(userId)
                .set(data, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(a -> cb.onSuccess())
                .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
