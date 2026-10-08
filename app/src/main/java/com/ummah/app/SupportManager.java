package com.ummah.app;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SupportManager — إدارة الدعم (ديوان أُمّة)
 */
public class SupportManager {

    private static final String TAG = "Support";
    private static final String COL = "support_tickets";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface TicketCallback {
        void onResult(SupportTicket ticket);
        void onError(String e);
    }

    public interface TicketsCallback {
        void onResult(List<SupportTicket> list);
    }

    public interface MessagesCallback {
        void onResult(List<SupportMessage> messages);
    }

    public interface SimpleCallback {
        void onSuccess(String ticketId);
        void onError(String error);
    }

    // ═══ 1. إنشاء تذكرة جديدة ═══
    public void createTicket(Citizen citizen, String subject, String category,
                              String priority, String message, SimpleCallback cb) {

        if (citizen == null) { cb.onError("غير مسجل"); return; }

        long now = System.currentTimeMillis();

        Map<String, Object> ticket = new HashMap<>();
        ticket.put("userId", citizen.nationalId);
        ticket.put("userName", citizen.name);
        ticket.put("subject", subject);
        ticket.put("category", category != null ? category : SupportTicket.CAT_OTHER);
        ticket.put("priority", priority != null ? priority : SupportTicket.PRIORITY_MEDIUM);
        ticket.put("status", SupportTicket.STATUS_OPEN);
        ticket.put("lastMessage", message);
        ticket.put("lastMessageAt", now);
        ticket.put("createdAt", now);
        ticket.put("updatedAt", now);

        db.collection(COL).add(ticket)
            .addOnSuccessListener(doc -> {
                String ticketId = doc.getId();
                Log.d(TAG, "✅ Ticket created: " + ticketId);

                // نضيفو أول رسالة
                Map<String, Object> msg = new HashMap<>();
                msg.put("senderId", citizen.nationalId);
                msg.put("senderName", citizen.name);
                msg.put("senderType", "user");
                msg.put("text", message);
                msg.put("createdAt", now);

                db.collection(COL).document(ticketId)
                    .collection("messages").add(msg)
                    .addOnSuccessListener(m -> cb.onSuccess(ticketId))
                    .addOnFailureListener(e -> cb.onError(e.getMessage()));
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ═══ 2. جلب تذاكر المستخدم ═══
    public ListenerRegistration listenMyTickets(String userId, TicketsCallback cb) {
        return db.collection(COL)
            .whereEqualTo("userId", userId)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener((snap, e) -> {
                if (e != null || snap == null) return;
                List<SupportTicket> list = new ArrayList<>();
                for (DocumentSnapshot d : snap.getDocuments()) {
                    SupportTicket t = d.toObject(SupportTicket.class);
                    if (t != null) { t.id = d.getId(); list.add(t); }
                }
                cb.onResult(list);
            });
    }

    // ═══ 3. جلب كل التذاكر (Admin) ═══
    public ListenerRegistration listenAllTickets(TicketsCallback cb) {
        return db.collection(COL)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener((snap, e) -> {
                if (e != null || snap == null) return;
                List<SupportTicket> list = new ArrayList<>();
                for (DocumentSnapshot d : snap.getDocuments()) {
                    SupportTicket t = d.toObject(SupportTicket.class);
                    if (t != null) { t.id = d.getId(); list.add(t); }
                }
                cb.onResult(list);
            });
    }

    // ═══ 4. جلب الرسائل ═══
    public ListenerRegistration listenMessages(String ticketId, MessagesCallback cb) {
        return db.collection(COL).document(ticketId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener((snap, e) -> {
                if (e != null || snap == null) return;
                List<SupportMessage> list = new ArrayList<>();
                for (DocumentSnapshot d : snap.getDocuments()) {
                    SupportMessage m = d.toObject(SupportMessage.class);
                    if (m != null) { m.id = d.getId(); list.add(m); }
                }
                cb.onResult(list);
            });
    }

    // ═══ 5. إرسال رسالة ═══
    public void sendMessage(String ticketId, String senderId, String senderName,
                             String senderType, String text, SimpleCallback cb) {

        long now = System.currentTimeMillis();

        Map<String, Object> msg = new HashMap<>();
        msg.put("senderId", senderId);
        msg.put("senderName", senderName);
        msg.put("senderType", senderType);
        msg.put("text", text);
        msg.put("createdAt", now);

        db.collection(COL).document(ticketId)
            .collection("messages").add(msg)
            .addOnSuccessListener(doc -> {
                // نحدّث التذكرة
                Map<String, Object> upd = new HashMap<>();
                upd.put("lastMessage", text);
                upd.put("lastMessageAt", now);
                upd.put("updatedAt", now);
                if ("admin".equals(senderType)) {
                    upd.put("status", SupportTicket.STATUS_PROGRESS);
                }
                db.collection(COL).document(ticketId).update(upd);
                cb.onSuccess(ticketId);
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ═══ 6. تغيير الحالة (Admin) ═══
    public void setStatus(String ticketId, String status, SimpleCallback cb) {
        db.collection(COL).document(ticketId)
            .update("status", status, "updatedAt", System.currentTimeMillis())
            .addOnSuccessListener(v -> cb.onSuccess(ticketId))
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ═══ 7. جلب تذكرة واحدة ═══
    public void getTicket(String ticketId, TicketCallback cb) {
        db.collection(COL).document(ticketId).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    SupportTicket t = doc.toObject(SupportTicket.class);
                    if (t != null) t.id = doc.getId();
                    cb.onResult(t);
                } else cb.onResult(null);
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ═══ 8. فحص إذا الأدمن ردّ (للـ Bot) ═══
    public interface BoolCallback {
        void onResult(boolean value);
    }

    public void hasAdminReplied(String ticketId, BoolCallback cb) {
        db.collection(COL).document(ticketId)
          .collection("messages")
          .whereEqualTo("senderType", "admin")
          .limit(1)
          .get()
          .addOnSuccessListener(snap -> cb.onResult(snap != null && !snap.isEmpty()))
          .addOnFailureListener(e -> cb.onResult(false));
    }

    // ═══ 9. فحص إذا البوت ردّ سابقاً (باش ما يعاودش) ═══
    public void hasBotReplied(String ticketId, BoolCallback cb) {
        db.collection(COL).document(ticketId)
          .collection("messages")
          .whereEqualTo("senderType", "bot")
          .limit(1)
          .get()
          .addOnSuccessListener(snap -> cb.onResult(snap != null && !snap.isEmpty()))
          .addOnFailureListener(e -> cb.onResult(false));
    }
}
