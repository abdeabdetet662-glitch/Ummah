package com.ummah.app;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

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
}
