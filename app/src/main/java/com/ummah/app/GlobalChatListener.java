package com.ummah.app;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

public class GlobalChatListener {

    public interface OnNewMessage {
        void onNew(String senderName, String content, long timestamp);
    }

    private final Context ctx;
    private final FirebaseFirestore db;
    private ListenerRegistration reg;

    public GlobalChatListener(Context ctx) {
        this.ctx = ctx;
        this.db = FirebaseFirestore.getInstance();
    }

    public void start(final String myNationalId, final OnNewMessage cb) {
        stop();

        SharedPreferences prefs = ctx.getSharedPreferences("ummah_prefs", Context.MODE_PRIVATE);
        final long lastSeen = prefs.getLong("last_global_chat_ts", 0);

        reg = db.collection("global_chat")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null || snap.isEmpty()) return;
                    DocumentSnapshot doc = snap.getDocuments().get(0);
                    Long ts = doc.getLong("timestamp");
                    if (ts == null) return;
                    if (ts <= lastSeen) return;

                    String senderId = doc.getString("nationalId");
                    if (senderId != null && senderId.equals(myNationalId)) return;

                    String name = doc.getString("name");
                    String content = doc.getString("content");
                    if (name == null) name = "مواطن";

                    cb.onNew(name, content != null ? content : "", ts);
                });
    }

    public void stop() {
        if (reg != null) {
            reg.remove();
            reg = null;
        }
    }
}
