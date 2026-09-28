package com.ummah.app;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;

public class UnreadManager {

    private final FirebaseFirestore db;

    public UnreadManager() {
        db = FirebaseFirestore.getInstance();
    }

    public interface UnreadListener {
        void onUnread(int chat, int gifts, int election);
    }

    // يزيد الرقم بـ 1
    public void increment(String userId, String field) {
        if (userId == null || userId.isEmpty()) return;
        db.collection("unread").document(userId)
            .update(field, FieldValue.increment(1))
            .addOnFailureListener(e -> {
                // إذا الوثيقة ما كانتش، ننشئها
                Map<String, Object> data = new HashMap<>();
                data.put(field, 1);
                db.collection("unread").document(userId).set(data);
            });
    }

    // يرجع الرقم لـ 0
    public void reset(String userId, String field) {
        if (userId == null || userId.isEmpty()) return;
        db.collection("unread").document(userId)
            .update(field, 0)
            .addOnFailureListener(e -> {
                Map<String, Object> data = new HashMap<>();
                data.put(field, 0);
                db.collection("unread").document(userId).set(data);
            });
    }

    // يسمع للتغييرات
    public ListenerRegistration listen(String userId, final UnreadListener l) {
        if (userId == null || userId.isEmpty()) return null;
        return db.collection("unread").document(userId)
            .addSnapshotListener((doc, e) -> {
                if (e != null || doc == null) {
                    l.onUnread(0, 0, 0);
                    return;
                }
                int chat = intOf(doc.getLong("chat"));
                int gifts = intOf(doc.getLong("gifts"));
                int election = intOf(doc.getLong("election"));
                l.onUnread(chat, gifts, election);
            });
    }

    private int intOf(Long v) {
        return v != null ? v.intValue() : 0;
    }
}
