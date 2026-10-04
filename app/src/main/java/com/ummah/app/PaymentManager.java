package com.ummah.app;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * PaymentManager — نظام دفع موحد لكل الميزات
 *
 * الاستعمال:
 *   PaymentManager.pay(uid, 500, "MM_ENTRY", "اشتراك جريمة", callback);
 */
public class PaymentManager {

    private static final String TAG = "Payment";

    // ═══ Reasons (أسباب الدفع) ═══
    public static final String MM_ENTRY       = "mm_entry";        // جريمة أُمّة
    public static final String WHEEL_SPIN     = "wheel_spin";      // عجلة الحظ
    public static final String MARKET_BUY     = "market_buy";      // شراء من السوق
    public static final String LISTING_FEE    = "listing_fee";     // رسوم عرض
    public static final String GIFT_SEND      = "gift_send";       // إرسال هدية
    public static final String PLOT_BUY       = "plot_buy";        // شراء أرض
    public static final String CAR_BUY        = "car_buy";         // شراء سيارة
    public static final String HOUSE_BUY      = "house_buy";       // شراء منزل
    public static final String TRANSFER_FEE   = "transfer_fee";    // رسوم تحويل
    public static final String JOB_APPLY      = "job_apply";       // التقدم لوظيفة
    public static final String COURT_FEE      = "court_fee";       // رسوم محكمة
    public static final String ELECTION_FEE   = "election_fee";    // رسوم ترشح

    // ═══ Callback ═══
    public interface PayCallback {
        void onSuccess(long newBalance);
        void onInsufficient(long balance, long required);
        void onError(String error);
    }

    /**
     * خصم من الرصيد + تسجيل + إضافة للخزينة
     *
     * @param uid          الرقم الوطني للمواطن
     * @param amount       المبلغ (موجب)
     * @param reason       السبب (من الثوابت فوق)
     * @param description  وصف عربي (يظهر للمستخدم)
     * @param cb           رد الفعل
     */
    public static void pay(String uid, final long amount, final String reason,
                            final String description, final PayCallback cb) {

        if (uid == null || uid.isEmpty()) {
            cb.onError("معرف المستخدم فارغ");
            return;
        }
        if (amount <= 0) {
            cb.onError("المبلغ غير صالح");
            return;
        }

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference citizenRef = db.collection("citizens").document(uid);

        Log.d(TAG, "💸 محاولة دفع " + amount + " Đ لـ " + reason);

        // ═══ 1. نجيب الرصيد الحقيقي ═══
        citizenRef.get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) {
                    cb.onError("حسابك ماكانش");
                    return;
                }

                Long balanceObj = doc.getLong("balance");
                long balance = balanceObj != null ? balanceObj : 0;

                Log.d(TAG, "💰 رصيدك: " + balance + " | مطلوب: " + amount);

                // ═══ 2. نتحققو من الرصيد ═══
                if (balance < amount) {
                    cb.onInsufficient(balance, amount);
                    return;
                }

                // ═══ 3. نخصمو ═══
                citizenRef.update("balance", FieldValue.increment(-amount))
                    .addOnSuccessListener(v -> {
                        long newBalance = balance - amount;
                        Log.d(TAG, "✅ خصم ناجح | الرصيد الجديد: " + newBalance);

                        // ═══ 4. نسجّلو في transactions ═══
                        logTransaction(uid, -amount, reason, description, newBalance);

                        // ═══ 5. نضيفو للخزينة ═══
                        addToTreasury(amount, reason, uid);

                        // ═══ 6. نحدّثو SharedPreferences (cache) ═══
                        try {
                            SharedPrefsHelper.updateBalance(newBalance);
                        } catch (Exception ignored) {}

                        cb.onSuccess(newBalance);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ فشل الخصم", e);
                        cb.onError("خطأ في الخصم: " + e.getMessage());
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ فشل جلب الرصيد", e);
                cb.onError("خطأ في الشبكة: " + e.getMessage());
            });
    }

    // ═══ نسخة مختصرة ═══
    public static void pay(String uid, long amount, String reason,
                            final SimpleCallback cb) {
        pay(uid, amount, reason, reason, new PayCallback() {
            @Override public void onSuccess(long b) { cb.onSuccess(); }
            @Override public void onInsufficient(long bal, long req) {
                cb.onError("ما عندكش " + req + " Đ (عندك " + bal + " Đ)");
            }
            @Override public void onError(String e) { cb.onError(e); }
        });
    }

    // ═══ نسخة مبسطة ═══
    public interface SimpleCallback {
        void onSuccess();
        void onError(String error);
    }

    /**
     * إضافة رصيد (للمكافآت والجوائز)
     */
    public static void addBalance(final String uid, final long amount,
                                    final String reason, final String description,
                                    final PayCallback cb) {
        if (uid == null || amount <= 0) {
            if (cb != null) cb.onError("بيانات غير صالحة");
            return;
        }

        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("citizens").document(uid).get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) {
                    if (cb != null) cb.onError("ماكانش");
                    return;
                }

                Long balanceObj = doc.getLong("balance");
                long balance = balanceObj != null ? balanceObj : 0;
                long newBalance = balance + amount;

                db.collection("citizens").document(uid)
                    .update("balance", FieldValue.increment(amount))
                    .addOnSuccessListener(v -> {
                        logTransaction(uid, amount, reason, description, newBalance);
                        if (cb != null) cb.onSuccess(newBalance);
                    })
                    .addOnFailureListener(e -> {
                        if (cb != null) cb.onError(e.getMessage());
                    });
            })
            .addOnFailureListener(e -> {
                if (cb != null) cb.onError(e.getMessage());
            });
    }

    // ═══ تسجيل معاملة في Firestore ═══
    private static void logTransaction(String uid, long amount, String reason,
                                        String description, long afterBalance) {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> tx = new HashMap<>();
            tx.put("userId", uid);
            tx.put("amount", amount);       // موجب = إضافة، سالب = خصم
            tx.put("reason", reason);
            tx.put("description", description);
            tx.put("afterBalance", afterBalance);
            tx.put("timestamp", System.currentTimeMillis());

            db.collection("transactions").add(tx);
        } catch (Exception e) {
            Log.e(TAG, "خطأ في تسجيل المعاملة", e);
        }
    }

    // ═══ إضافة للخزينة ═══
    private static void addToTreasury(long amount, String reason, String uid) {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> entry = new HashMap<>();
            entry.put("amount", amount);
            entry.put("reason", reason);
            entry.put("fromUser", uid);
            entry.put("timestamp", System.currentTimeMillis());

            db.collection("treasury")
                .document("main")
                .collection("deposits")
                .add(entry);

            // نزيدو الرصيد الإجمالي
            Map<String, Object> upd = new HashMap<>();
            upd.put("balance", FieldValue.increment(amount));

            db.collection("treasury").document("main")
                .set(upd, SetOptions.merge());

        } catch (Exception e) {
            Log.e(TAG, "خطأ في إضافة الخزينة", e);
        }
    }
}
