package com.ummah.app;

/**
 * SupportTicket — تذكرة دعم في ديوان أُمّة
 */
public class SupportTicket {

    public static final String CAT_TRANSFER = "transfer";
    public static final String CAT_MARKET = "market";
    public static final String CAT_ACCOUNT = "account";
    public static final String CAT_TECHNICAL = "technical";
    public static final String CAT_OTHER = "other";

    public static final String STATUS_OPEN = "open";
    public static final String STATUS_PROGRESS = "in_progress";
    public static final String STATUS_RESOLVED = "resolved";

    public static final String PRIORITY_LOW = "low";
    public static final String PRIORITY_MEDIUM = "medium";
    public static final String PRIORITY_HIGH = "high";

    public String id;
    public String userId;
    public String userName;
    public String subject;
    public String category = CAT_OTHER;
    public String priority = PRIORITY_MEDIUM;
    public String status = STATUS_OPEN;
    public String lastMessage;
    public long lastMessageAt;
    public long createdAt;
    public long updatedAt;

    public SupportTicket() {}

    public String getCategoryAr() {
        if (category == null) return "أخرى";
        switch (category) {
            case CAT_TRANSFER: return "💰 التحويلات";
            case CAT_MARKET: return "🛒 السوق";
            case CAT_ACCOUNT: return "👤 الحساب";
            case CAT_TECHNICAL: return "🔧 مشكلة تقنية";
            default: return "📌 أخرى";
        }
    }

    public String getPriorityAr() {
        if (priority == null) return "عادي";
        switch (priority) {
            case PRIORITY_HIGH: return "🔴 عاجل";
            case PRIORITY_LOW: return "🟢 منخفض";
            default: return "🟡 عادي";
        }
    }

    public String getStatusAr() {
        if (status == null) return "مفتوحة";
        switch (status) {
            case STATUS_PROGRESS: return "⏳ قيد المعالجة";
            case STATUS_RESOLVED: return "✅ تم الحل";
            default: return "📬 مفتوحة";
        }
    }

    public String getStatusColor() {
        if (status == null) return "#D4AF37";
        switch (status) {
            case STATUS_PROGRESS: return "#F59E0B";
            case STATUS_RESOLVED: return "#10B981";
            default: return "#3B82F6";
        }
    }
}
