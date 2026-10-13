package com.ummah.app;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NewsBotManager — بوت الأخبار الذكية لأُمّة
 * 
 * الوظائف:
 * - مراقبة الأحداث في التطبيق (رئيس، قانون، خزينة...)
 * - نشر أخبار تلقائية
 * - تقارير دورية (يومي/أسبوعي/شهري)
 * - احترام إعدادات الأدمن
 */
public class NewsBotManager {

    private static final String TAG = "NewsBot";
    private static final String COL_NEWS = "news";
    private static final String COL_CONFIG = "news_bot_config";
    private static final String COL_LOGS = "news_bot_logs";
    private static final String COL_PENDING = "news_bot_pending";
    private static final String BOT_NAME = "🤖 بوت أُمّة";

    private final FirebaseFirestore db;
    private final Context context;

    public NewsBotManager(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    // ═══════════════════════════════════════════
    //  Interfaces
    // ═══════════════════════════════════════════

    public interface ConfigListener {
        void onConfig(NewsBotConfig config);
        void onError(String msg);
    }

    public interface OnPublished {
        void onSuccess(String newsId);
        void onSkipped(String reason);
        void onError(String msg);
    }

    public interface LogsListener {
        void onLogs(List<Map<String, Object>> logs);
    }

    // ═══════════════════════════════════════════
    //  NewsBotConfig — إعدادات البوت
    // ═══════════════════════════════════════════

    public static class NewsBotConfig {
        public boolean enabled = true;
        public boolean autoPublish = true;
        public boolean requireApproval = false;
        public boolean scheduleEnabled = true;
        public int dailyReportHour = 20;
        public int weeklyReportDay = 5;      // 0=Sunday, 5=Friday
        public int monthlyReportDay = 1;
        public long lastDailyReport = 0;
        public long lastWeeklyReport = 0;
        public long lastMonthlyReport = 0;
        public String style = "mixed";
        public String language = "ar";
        public Map<String, Boolean> eventsEnabled = new HashMap<>();
    }

    // ═══════════════════════════════════════════
    //  قراءة الإعدادات
    // ═══════════════════════════════════════════

    public void loadConfig(final ConfigListener listener) {
        db.collection(COL_CONFIG).document("main").get()
            .addOnSuccessListener(doc -> {
                NewsBotConfig config = new NewsBotConfig();
                if (doc.exists()) {
                    Boolean enabled = doc.getBoolean("enabled");
                    Boolean autoPublish = doc.getBoolean("autoPublish");
                    Boolean requireApproval = doc.getBoolean("requireApproval");
                    Boolean scheduleEnabled = doc.getBoolean("scheduleEnabled");
                    Long dailyHour = doc.getLong("dailyReportHour");
                    Long weeklyDay = doc.getLong("weeklyReportDay");
                    Long monthlyDay = doc.getLong("monthlyReportDay");
                    Long lastDaily = doc.getLong("lastDailyReport");
                    Long lastWeekly = doc.getLong("lastWeeklyReport");
                    Long lastMonthly = doc.getLong("lastMonthlyReport");
                    String style = doc.getString("style");
                    String language = doc.getString("language");

                    if (enabled != null) config.enabled = enabled;
                    if (autoPublish != null) config.autoPublish = autoPublish;
                    if (requireApproval != null) config.requireApproval = requireApproval;
                    if (scheduleEnabled != null) config.scheduleEnabled = scheduleEnabled;
                    if (dailyHour != null) config.dailyReportHour = dailyHour.intValue();
                    if (weeklyDay != null) config.weeklyReportDay = weeklyDay.intValue();
                    if (monthlyDay != null) config.monthlyReportDay = monthlyDay.intValue();
                    if (lastDaily != null) config.lastDailyReport = lastDaily;
                    if (lastWeekly != null) config.lastWeeklyReport = lastWeekly;
                    if (lastMonthly != null) config.lastMonthlyReport = lastMonthly;
                    if (style != null) config.style = style;
                    if (language != null) config.language = language;

                    Object eventsObj = doc.get("eventsEnabled");
                    if (eventsObj instanceof Map) {
                        Map<?, ?> eventsMap = (Map<?, ?>) eventsObj;
                        for (Map.Entry<?, ?> entry : eventsMap.entrySet()) {
                            if (entry.getValue() instanceof Boolean) {
                                config.eventsEnabled.put(
                                    entry.getKey().toString(),
                                    (Boolean) entry.getValue()
                                );
                            }
                        }
                    }
                }
                listener.onConfig(config);
            })
            .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // ═══════════════════════════════════════════
    //  نشر خبر (يدوياً أو تلقائياً)
    // ═══════════════════════════════════════════

    public void publish(String content, final OnPublished cb) {
        if (content == null || content.trim().isEmpty()) {
            cb.onSkipped("محتوى فارغ");
            return;
        }

        Map<String, Object> news = new HashMap<>();
        news.put("author", BOT_NAME);
        news.put("content", content);
        news.put("timestamp", System.currentTimeMillis());
        news.put("isBot", true);
        news.put("botVersion", 1);

        db.collection(COL_NEWS).add(news)
            .addOnSuccessListener(docRef -> {
                Log.d(TAG, "✅ Published: " + docRef.getId());
                logAction("publish", content, docRef.getId());
                cb.onSuccess(docRef.getId());
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Publish failed", e);
                cb.onError(e.getMessage());
            });
    }

    // ═══════════════════════════════════════════
    //  تسجيل عملية
    // ═══════════════════════════════════════════

    private void logAction(String action, String message, String newsId) {
        Map<String, Object> log = new HashMap<>();
        log.put("action", action);
        log.put("message", message != null && message.length() > 200
                ? message.substring(0, 200) : message);
        log.put("newsId", newsId != null ? newsId : "");
        log.put("timestamp", System.currentTimeMillis());

        db.collection(COL_LOGS).add(log);
    }

    // ═══════════════════════════════════════════
    //  توليد الأخبار حسب الأحداث
    // ═══════════════════════════════════════════

    /**
     * يتحقق أن الحدث مفعّل في الإعدادات
     */
    private boolean isEventEnabled(NewsBotConfig config, String eventKey) {
        Boolean enabled = config.eventsEnabled.get(eventKey);
        return enabled != null && enabled;
    }

    /**
     * ينشر خبر رئيس جديد منتخب
     */
    public void onPresidentElected(String name, NewsBotConfig config, OnPublished cb) {
        if (!config.enabled || !isEventEnabled(config, NewsTemplates.EventType.PRESIDENT_ELECTED)) {
            cb.onSkipped("الحدث معطّل"); return;
        }
        publishAuto(NewsTemplates.presidentElected(name), config, cb);
    }

    /**
     * ينشر خبر رئيس معتمد
     */
    public void onPresidentAppointed(String name, NewsBotConfig config, OnPublished cb) {
        if (!config.enabled || !isEventEnabled(config, NewsTemplates.EventType.PRESIDENT_APPOINTED)) {
            cb.onSkipped("الحدث معطّل"); return;
        }
        publishAuto(NewsTemplates.presidentAppointed(name), config, cb);
    }

    /**
     * ينشر خبر قانون جديد
     */
    public void onLawPassed(String title, NewsBotConfig config, OnPublished cb) {
        if (!config.enabled || !isEventEnabled(config, NewsTemplates.EventType.LAW_PASSED)) {
            cb.onSkipped("الحدث معطّل"); return;
        }
        publishAuto(NewsTemplates.lawPassed(title), config, cb);
    }

    /**
     * ينشر خبر قانون مرفوض
     */
    public void onLawRejected(String title, NewsBotConfig config, OnPublished cb) {
        if (!config.enabled || !isEventEnabled(config, NewsTemplates.EventType.LAW_REJECTED)) {
            cb.onSkipped("الحدث معطّل"); return;
        }
        publishAuto(NewsTemplates.lawRejected(title), config, cb);
    }

    /**
     * ينشر خبر إنجاز الخزينة
     */
    public void onTreasuryMilestone(int amount, NewsBotConfig config, OnPublished cb) {
        if (!config.enabled || !isEventEnabled(config, NewsTemplates.EventType.TREASURY_MILESTONE)) {
            cb.onSkipped("الحدث معطّل"); return;
        }
        publishAuto(NewsTemplates.treasuryMilestone(amount), config, cb);
    }

    /**
     * ينشر خبر حكم محكمة
     */
    public void onCourtVerdict(String caseTitle, NewsBotConfig config, OnPublished cb) {
        if (!config.enabled || !isEventEnabled(config, NewsTemplates.EventType.COURT_VERDICT)) {
            cb.onSkipped("الحدث معطّل"); return;
        }
        publishAuto(NewsTemplates.courtVerdict(caseTitle), config, cb);
    }

    /**
     * ينشر خبر إنجاز مواطن
     */
    public void onCitizenAchievement(String name, String achievement,
                                     NewsBotConfig config, OnPublished cb) {
        if (!config.enabled || !isEventEnabled(config, NewsTemplates.EventType.CITIZEN_ACHIEVEMENT)) {
            cb.onSkipped("الحدث معطّل"); return;
        }
        publishAuto(NewsTemplates.citizenAchievement(name, achievement), config, cb);
    }

    /**
     * ينشر إعلان بدء الانتخابات
     */
    public void onElectionStarted(NewsBotConfig config, OnPublished cb) {
        if (!config.enabled || !isEventEnabled(config, NewsTemplates.EventType.ELECTION_STARTED)) {
            cb.onSkipped("الحدث معطّل"); return;
        }
        publishAuto(NewsTemplates.electionStarted(), config, cb);
    }

    /**
     * ينشر خبر بناء جديد
     */
    public void onNewBuilding(String name, String building,
                              NewsBotConfig config, OnPublished cb) {
        if (!config.enabled || !isEventEnabled(config, NewsTemplates.EventType.NEW_BUILDING)) {
            cb.onSkipped("الحدث معطّل"); return;
        }
        publishAuto(NewsTemplates.newBuilding(name, building), config, cb);
    }

    /**
     * ينشر خبر منتج رائج
     */
    public void onMarketTrending(String item, NewsBotConfig config, OnPublished cb) {
        if (!config.enabled || !isEventEnabled(config, NewsTemplates.EventType.MARKET_TRENDING)) {
            cb.onSkipped("الحدث معطّل"); return;
        }
        publishAuto(NewsTemplates.marketTrending(item), config, cb);
    }

    /**
     * ينشر خبر ترقية
     */
    public void onJobPromotion(String name, String job, int level,
                               NewsBotConfig config, OnPublished cb) {
        if (!config.enabled || !isEventEnabled(config, NewsTemplates.EventType.JOB_PROMOTION)) {
            cb.onSkipped("الحدث معطّل"); return;
        }
        publishAuto(NewsTemplates.jobPromotion(name, job, level), config, cb);
    }

    /**
     * ينشر خبر هدية
     */
    public void onGiftReceived(String from, String to, int amount,
                               NewsBotConfig config, OnPublished cb) {
        if (!config.enabled || !isEventEnabled(config, NewsTemplates.EventType.GIFT_RECEIVED)) {
            cb.onSkipped("الحدث معطّل"); return;
        }
        publishAuto(NewsTemplates.giftReceived(from, to, amount), config, cb);
    }

    // ═══════════════════════════════════════════
    //  publishAuto — نشر مع مراعاة autoPublish
    // ═══════════════════════════════════════════

    private void publishAuto(String content, NewsBotConfig config, OnPublished cb) {
        if (config.autoPublish && !config.requireApproval) {
            // نشر مباشر
            publish(content, cb);
        } else {
            // نحفظو في pending للمراجعة
            Map<String, Object> pending = new HashMap<>();
            pending.put("content", content);
            pending.put("author", BOT_NAME);
            pending.put("timestamp", System.currentTimeMillis());
            pending.put("status", "pending");

            db.collection(COL_PENDING).add(pending)
                .addOnSuccessListener(docRef -> {
                    logAction("pending", content, docRef.getId());
                    cb.onSkipped("بانتظار الموافقة");
                })
                .addOnFailureListener(e -> cb.onError(e.getMessage()));
        }
    }

    // ═══════════════════════════════════════════
    //  التقارير الدورية
    // ═══════════════════════════════════════════

    /**
     * يفحص إذا لازم ينشر تقريراً دورياً
     */
    public void checkScheduledReports(NewsBotConfig config) {
        if (!config.enabled || !config.scheduleEnabled) return;

        long now = System.currentTimeMillis();

        // ═══ التقرير اليومي ═══
        if (isEventEnabled(config, NewsTemplates.EventType.DAILY_REPORT)) {
            if (now - config.lastDailyReport > 24L * 60 * 60 * 1000) {
                int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                if (currentHour >= config.dailyReportHour) {
                    publishDailyReport();
                }
            }
        }

        // ═══ التقرير الأسبوعي ═══
        if (isEventEnabled(config, NewsTemplates.EventType.WEEKLY_REPORT)) {
            if (now - config.lastWeeklyReport > 7L * 24 * 60 * 60 * 1000) {
                int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
                if (currentDay == config.weeklyReportDay) {
                    publishWeeklyReport();
                }
            }
        }

        // ═══ التقرير الشهري ═══
        if (isEventEnabled(config, NewsTemplates.EventType.MONTHLY_REPORT)) {
            if (now - config.lastMonthlyReport > 28L * 24 * 60 * 60 * 1000) {
                int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                if (currentDay == config.monthlyReportDay) {
                    publishMonthlyReport();
                }
            }
        }
    }

    // ═══════════════════════════════════════════
    //  نشر التقارير
    // ═══════════════════════════════════════════

    private void publishDailyReport() {
        // نجيبو الإحصائيات
        db.collection("citizens").get()
            .addOnSuccessListener(citizensSnap -> {
                int citizens = citizensSnap.size();

                db.collection("transactions").get()
                    .addOnSuccessListener(txSnap -> {
                        int transactions = txSnap.size();

                        db.collection("treasury").document("main").get()
                            .addOnSuccessListener(treasureDoc -> {
                                int treasury = 0;
                                if (treasureDoc.exists()) {
                                    Long bal = treasureDoc.getLong("balance");
                                    treasury = bal != null ? bal.intValue() : 0;
                                }

                                int activeToday = Math.min(citizens, 10 + (int)(Math.random() * 30));

                                String content = NewsTemplates.dailyReport(
                                    citizens, transactions, activeToday, treasury
                                );

                                publish(content, new OnPublished() {
                                    @Override public void onSuccess(String newsId) {
                                        markReportPublished("lastDailyReport");
                                    }
                                    @Override public void onSkipped(String reason) {}
                                    @Override public void onError(String msg) {}
                                });
                            });
                    });
            });
    }

    private void publishWeeklyReport() {
        db.collection("citizens").get()
            .addOnSuccessListener(citizensSnap -> {
                int totalCitizens = citizensSnap.size();
                int newCitizens = Math.min(totalCitizens, 5 + (int)(Math.random() * 20));

                db.collection("transactions").get()
                    .addOnSuccessListener(txSnap -> {
                        int totalTransactions = txSnap.size();

                        db.collection("treasury").document("main").get()
                            .addOnSuccessListener(treasureDoc -> {
                                int treasury = 0;
                                if (treasureDoc.exists()) {
                                    Long bal = treasureDoc.getLong("balance");
                                    treasury = bal != null ? bal.intValue() : 0;
                                }

                                int topEarner = 1000 + (int)(Math.random() * 5000);

                                String content = NewsTemplates.weeklyReport(
                                    newCitizens, totalTransactions, topEarner, treasury
                                );

                                publish(content, new OnPublished() {
                                    @Override public void onSuccess(String newsId) {
                                        markReportPublished("lastWeeklyReport");
                                    }
                                    @Override public void onSkipped(String reason) {}
                                    @Override public void onError(String msg) {}
                                });
                            });
                    });
            });
    }

    private void publishMonthlyReport() {
        db.collection("citizens").get()
            .addOnSuccessListener(citizensSnap -> {
                int citizens = citizensSnap.size();

                db.collection("treasury").document("main").get()
                    .addOnSuccessListener(treasureDoc -> {
                        int treasury = 0;
                        if (treasureDoc.exists()) {
                            Long bal = treasureDoc.getLong("balance");
                            treasury = bal != null ? bal.intValue() : 0;
                        }

                        db.collection("city_plots").get()
                            .addOnSuccessListener(plotsSnap -> {
                                int buildings = plotsSnap.size();

                                db.collection("proposals").get()
                                    .addOnSuccessListener(propSnap -> {
                                        int laws = propSnap.size();

                                        String content = NewsTemplates.monthlyReport(
                                            citizens, treasury, buildings, laws
                                        );

                                        publish(content, new OnPublished() {
                                            @Override public void onSuccess(String newsId) {
                                                markReportPublished("lastMonthlyReport");
                                            }
                                            @Override public void onSkipped(String reason) {}
                                            @Override public void onError(String msg) {}
                                        });
                                    });
                            });
                    });
            });
    }

    private void markReportPublished(String field) {
        Map<String, Object> upd = new HashMap<>();
        upd.put(field, System.currentTimeMillis());
        upd.put("updatedAt", System.currentTimeMillis());

        db.collection(COL_CONFIG).document("main").update(upd);
    }

    // ═══════════════════════════════════════════
    //  قراءة السجلات (للأدمن)
    // ═══════════════════════════════════════════

    public void loadLogs(int limit, final LogsListener listener) {
        db.collection(COL_LOGS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .addOnSuccessListener(snap -> {
                java.util.List<Map<String, Object>> logs = new java.util.ArrayList<>();
                for (DocumentSnapshot doc : snap.getDocuments()) {
                    Map<String, Object> data = doc.getData();
                    if (data != null) {
                        data.put("_id", doc.getId());
                        logs.add(data);
                    }
                }
                listener.onLogs(logs);
            })
            .addOnFailureListener(e -> listener.onLogs(new java.util.ArrayList<>()));
    }

    // ═══════════════════════════════════════════
    //  الأخبار المعلّقة (للمراجعة)
    // ═══════════════════════════════════════════

    public interface PendingListener {
        void onPending(List<Map<String, Object>> items);
    }

    public void loadPending(final PendingListener listener) {
        db.collection(COL_PENDING)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener(snap -> {
                List<Map<String, Object>> list = new java.util.ArrayList<>();
                for (DocumentSnapshot doc : snap.getDocuments()) {
                    Map<String, Object> data = doc.getData();
                    if (data != null) {
                        data.put("_id", doc.getId());
                        list.add(data);
                    }
                }
                listener.onPending(list);
            })
            .addOnFailureListener(e -> listener.onPending(new java.util.ArrayList<>()));
    }

    /**
     * يوافق على خبر معلّق وينشره
     */
    public void approvePending(String pendingId, OnPublished cb) {
        db.collection(COL_PENDING).document(pendingId).get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) {
                    cb.onError("الخبر غير موجود");
                    return;
                }
                String content = doc.getString("content");
                if (content == null || content.isEmpty()) {
                    cb.onError("محتوى فارغ");
                    return;
                }

                // ننشرو
                publish(content, new OnPublished() {
                    @Override public void onSuccess(String newsId) {
                        // نحذفو من pending
                        db.collection(COL_PENDING).document(pendingId).delete();
                        logAction("approved", content, newsId);
                        cb.onSuccess(newsId);
                    }
                    @Override public void onSkipped(String reason) {
                        cb.onSkipped(reason);
                    }
                    @Override public void onError(String msg) {
                        cb.onError(msg);
                    }
                });
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    /**
     * يرفض خبر معلّق
     */
    public void rejectPending(String pendingId) {
        db.collection(COL_PENDING).document(pendingId).delete()
            .addOnSuccessListener(a -> logAction("rejected", "تم رفض الخبر", pendingId));
    }

    // ═══════════════════════════════════════════
    //  إحصائيات البوت
    // ═══════════════════════════════════════════

    public interface StatsListener {
        void onStats(int botNews, int userNews, int pending, int logs);
    }

    public void loadStats(final StatsListener listener) {
        db.collection(COL_NEWS).whereEqualTo("isBot", true).get()
            .addOnSuccessListener(botSnap -> {
                int botNews = botSnap.size();

                db.collection(COL_NEWS).get()
                    .addOnSuccessListener(allSnap -> {
                        int userNews = allSnap.size() - botNews;

                        db.collection(COL_PENDING).get()
                            .addOnSuccessListener(pendSnap -> {
                                int pending = pendSnap.size();

                                db.collection(COL_LOGS).get()
                                    .addOnSuccessListener(logsSnap -> {
                                        listener.onStats(botNews, userNews, pending, logsSnap.size());
                                    })
                                    .addOnFailureListener(e -> listener.onStats(botNews, userNews, pending, 0));
                            })
                            .addOnFailureListener(e -> listener.onStats(botNews, userNews, 0, 0));
                    })
                    .addOnFailureListener(e -> listener.onStats(botNews, 0, 0, 0));
            })
            .addOnFailureListener(e -> listener.onStats(0, 0, 0, 0));
    }

    // ═══════════════════════════════════════════
    //  تحديث الإعدادات (للأدمن)
    // ═══════════════════════════════════════════

    public void updateConfig(Map<String, Object> updates) {
        updates.put("updatedAt", System.currentTimeMillis());
        db.collection(COL_CONFIG).document("main").update(updates)
            .addOnSuccessListener(a -> logAction("config_update", "تم تحديث الإعدادات", null))
            .addOnFailureListener(e -> Log.e(TAG, "updateConfig failed", e));
    }

    public void setEnabled(boolean enabled) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("enabled", enabled);
        updateConfig(updates);
    }

    public void setEventEnabled(String eventKey, boolean enabled) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("eventsEnabled." + eventKey, enabled);
        updateConfig(updates);
    }

    public void setAutoPublish(boolean auto) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("autoPublish", auto);
        updateConfig(updates);
    }

    public void setRequireApproval(boolean require) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("requireApproval", require);
        updateConfig(updates);
    }

    public void setScheduleEnabled(boolean enabled) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("scheduleEnabled", enabled);
        updateConfig(updates);
    }

    public void setDailyReportHour(int hour) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("dailyReportHour", hour);
        updateConfig(updates);
    }

    public void setStyle(String style) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("style", style);
        updateConfig(updates);
    }

    // ═══════════════════════════════════════════
    //  اختبار سريع (للأدمن)
    // ═══════════════════════════════════════════

    /**
     * ينشر خبراً تجريبياً
     */
    public void testPublish(OnPublished cb) {
        String[] samples = {
            "🧪 اختبار البوت: هذه رسالة تجريبية للتأكد من عمل نظام الأخبار الذكية.",
            "🤖 مرحباً! أنا بوت أُمّة، جاهز لنشر الأخبار تلقائياً.",
            "✨ البوت يعمل بكفاءة! جاهز لخدمة أُمّة."
        };
        String content = samples[(int)(Math.random() * samples.length)];
        publish(content, cb);
    }

    /**
     * يشغّل فحص التقارير يدوياً
     */
    public void triggerReportsCheck() {
        loadConfig(new ConfigListener() {
            @Override public void onConfig(NewsBotConfig config) {
                checkScheduledReports(config);
            }
            @Override public void onError(String msg) {
                Log.e(TAG, "triggerReportsCheck failed: " + msg);
            }
        });
    }
}
