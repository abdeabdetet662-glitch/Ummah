package com.ummah.admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * AdminNewsBotActivity — لوحة تحكم بوت الأخبار
 * 
 * الميزات:
 * - تفعيل/إيقاف البوت
 * - إعدادات النشر
 * - الأحداث المفعلة (15)
 * - الأخبار المعلّقة
 * - السجل
 * - اختبارات
 */
public class AdminNewsBotActivity extends Activity {

    private FirebaseFirestore db;
    private LinearLayout root;
    private LinearLayout eventsContainer;
    private LinearLayout pendingContainer;
    private LinearLayout logsContainer;

    private TextView statusText;
    private TextView statsText;
    private Switch enabledSwitch;
    private Switch autoPublishSwitch;
    private Switch requireApprovalSwitch;
    private Switch scheduleSwitch;

    private boolean loading = false;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        db = FirebaseFirestore.getInstance();
        setContentView(buildUI());
        loadAll();
    }

    private View buildUI() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0510"));
        scroll.setFillViewport(true);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(50), dp(20), dp(40));
        scroll.addView(root);

        // ═══ Header ═══
        TextView title = new TextView(this);
        title.setText("🤖 لوحة تحكم بوت الأخبار");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(24);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("إدارة نشر الأخبار التلقائية");
        sub.setTextColor(Color.parseColor("#9CA3AF"));
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, dp(6), 0, dp(24));
        root.addView(sub);

        // ═══ بطاقة الحالة ═══
        LinearLayout statusCard = makeCard();
        root.addView(statusCard);

        TextView statusTitle = new TextView(this);
        statusTitle.setText("📊 حالة البوت");
        statusTitle.setTextColor(Color.parseColor("#D4AF37"));
        statusTitle.setTextSize(14);
        statusTitle.setTypeface(null, Typeface.BOLD);
        statusCard.addView(statusTitle);

        statusText = new TextView(this);
        statusText.setText("⏳ جاري التحميل...");
        statusText.setTextColor(Color.WHITE);
        statusText.setTextSize(15);
        statusText.setPadding(0, dp(10), 0, 0);
        statusCard.addView(statusText);

        // ═══ بطاقة الإحصائيات ═══
        LinearLayout statsCard = makeCard();
        LinearLayout.LayoutParams scLp = new LinearLayout.LayoutParams(-1, -2);
        scLp.topMargin = dp(12);
        statsCard.setLayoutParams(scLp);
        root.addView(statsCard);

        TextView statsTitle = new TextView(this);
        statsTitle.setText("📈 الإحصائيات");
        statsTitle.setTextColor(Color.parseColor("#D4AF37"));
        statsTitle.setTextSize(14);
        statsTitle.setTypeface(null, Typeface.BOLD);
        statsCard.addView(statsTitle);

        statsText = new TextView(this);
        statsText.setText("⏳ جاري التحميل...");
        statsText.setTextColor(Color.WHITE);
        statsText.setTextSize(13);
        statsText.setPadding(0, dp(10), 0, 0);
        statsText.setLineSpacing(0, 1.5f);
        statsCard.addView(statsText);

        // ═══ قسم التحكم الرئيسي ═══
        TextView ctrlTitle = sectionTitle("🎛️ التحكم الرئيسي");
        root.addView(ctrlTitle);

        enabledSwitch = addSwitch(root, "🟢 تشغيل البوت", "عند الإيقاف، يتوقف نشر الأخبار التلقائية");
        autoPublishSwitch = addSwitch(root, "✅ النشر التلقائي", "نشر مباشر بلا مراجعة");
        requireApprovalSwitch = addSwitch(root, "📋 مراجعة إلزامية", "تحتاج موافقة قبل النشر");
        scheduleSwitch = addSwitch(root, "⏰ التقارير الدورية", "يومي/أسبوعي/شهري");

        // ═══ الأزرار السريعة ═══
        LinearLayout quickBtns = new LinearLayout(this);
        quickBtns.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams qbLp = new LinearLayout.LayoutParams(-1, -2);
        qbLp.topMargin = dp(16);
        quickBtns.setLayoutParams(qbLp);

        Button testBtn = makeButton("🧪 اختبار", "#9333EA");
        testBtn.setOnClickListener(v -> runTest());
        LinearLayout.LayoutParams tbLp = new LinearLayout.LayoutParams(0, dp(56), 1f);
        tbLp.setMargins(0, 0, dp(6), 0);
        testBtn.setLayoutParams(tbLp);
        quickBtns.addView(testBtn);

        Button reportBtn = makeButton("📊 فحص التقارير", "#3B82F6");
        reportBtn.setOnClickListener(v -> triggerReports());
        LinearLayout.LayoutParams rbLp = new LinearLayout.LayoutParams(0, dp(56), 1f);
        rbLp.setMargins(dp(6), 0, 0, 0);
        reportBtn.setLayoutParams(rbLp);
        quickBtns.addView(reportBtn);

        root.addView(quickBtns);

        // ═══ قسم الأحداث ═══
        TextView eventsTitle = sectionTitle("🎯 الأحداث المفعلة");
        root.addView(eventsTitle);

        eventsContainer = new LinearLayout(this);
        eventsContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(eventsContainer);

        // ═══ قسم الأخبار المعلّقة ═══
        TextView pendingTitle = sectionTitle("📋 أخبار بانتظار الموافقة");
        root.addView(pendingTitle);

        pendingContainer = new LinearLayout(this);
        pendingContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(pendingContainer);

        // ═══ قسم السجل ═══
        TextView logsTitle = sectionTitle("📜 سجل العمليات");
        root.addView(logsTitle);

        logsContainer = new LinearLayout(this);
        logsContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(logsContainer);

        return scroll;
    }

    // ═══════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════

    private LinearLayout makeCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.parseColor("#1A0F2E"));
        card.setPadding(dp(18), dp(16), dp(18), dp(16));
        return card;
    }

    private TextView sectionTitle(String text) {
        TextView title = new TextView(this);
        title.setText(text);
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(15);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, dp(24), 0, dp(12));
        return title;
    }

    private Switch addSwitch(LinearLayout parent, String label, String desc) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundColor(Color.parseColor("#1A0F2E"));
        row.setPadding(dp(14), dp(12), dp(14), dp(12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(4), 0, dp(4));
        row.setLayoutParams(lp);

        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));

        TextView lbl = new TextView(this);
        lbl.setText(label);
        lbl.setTextColor(Color.WHITE);
        lbl.setTextSize(14);
        lbl.setTypeface(null, Typeface.BOLD);
        textCol.addView(lbl);

        TextView d = new TextView(this);
        d.setText(desc);
        d.setTextColor(Color.parseColor("#9CA3AF"));
        d.setTextSize(11);
        d.setPadding(0, dp(2), 0, 0);
        textCol.addView(d);

        row.addView(textCol);

        Switch sw = new Switch(this);
        row.addView(sw);

        parent.addView(row);
        return sw;
    }

    private Button makeButton(String text, String color) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(13);
        btn.setTypeface(null, Typeface.BOLD);
        btn.setBackgroundColor(Color.parseColor(color));
        return btn;
    }

    protected int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }

    // ═══════════════════════════════════════════
    //  تحميل كل البيانات
    // ═══════════════════════════════════════════

    private void loadAll() {
        if (loading) return;
        loading = true;

        loadConfig();
        loadStats();
        loadPending();
        loadLogs();
    }

    // ═══════════════════════════════════════════
    //  تحميل الإعدادات
    // ═══════════════════════════════════════════

    private void loadConfig() {
        db.collection("news_bot_config").document("main").get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) {
                    statusText.setText("❌ الإعدادات غير موجودة");
                    return;
                }

                Boolean enabled = doc.getBoolean("enabled");
                Boolean autoPublish = doc.getBoolean("autoPublish");
                Boolean requireApproval = doc.getBoolean("requireApproval");
                Boolean schedule = doc.getBoolean("scheduleEnabled");
                String style = doc.getString("style");

                boolean isEnabled = enabled != null && enabled;
                boolean isAuto = autoPublish != null && autoPublish;
                boolean isReq = requireApproval != null && requireApproval;
                boolean isSched = schedule != null && schedule;

                statusText.setText(isEnabled ? "🟢 البوت مُفعّل" : "🔴 البوت معطّل");
                statusText.setTextColor(isEnabled ?
                        Color.parseColor("#10B981") :
                        Color.parseColor("#EF4444"));

                if (!loading) {
                    // تحديث الـ switches
                    enabledSwitch.setChecked(isEnabled);
                    autoPublishSwitch.setChecked(isAuto);
                    requireApprovalSwitch.setChecked(isReq);
                    scheduleSwitch.setChecked(isSched);
                } else {
                    enabledSwitch.setChecked(isEnabled);
                    autoPublishSwitch.setChecked(isAuto);
                    requireApprovalSwitch.setChecked(isReq);
                    scheduleSwitch.setChecked(isSched);
                    loading = false;

                    // Setup Listeners (مرة وحدة)
                    setupSwitchListeners();
                }

                // الأحداث
                Object eventsObj = doc.get("eventsEnabled");
                renderEvents(eventsObj);
            })
            .addOnFailureListener(e -> {
                statusText.setText("❌ " + e.getMessage());
            });
    }

    // ═══════════════════════════════════════════
    //  تفعيل الـ Switch Listeners
    // ═══════════════════════════════════════════

    private boolean listenersReady = false;

    private void setupSwitchListeners() {
        if (listenersReady) return;
        listenersReady = true;

        enabledSwitch.setOnCheckedChangeListener((btn, checked) -> {
            updateConfig("enabled", checked);
            statusText.setText(checked ? "🟢 البوت مُفعّل" : "🔴 البوت معطّل");
            statusText.setTextColor(checked ?
                    Color.parseColor("#10B981") :
                    Color.parseColor("#EF4444"));
        });

        autoPublishSwitch.setOnCheckedChangeListener((btn, checked) -> {
            updateConfig("autoPublish", checked);
        });

        requireApprovalSwitch.setOnCheckedChangeListener((btn, checked) -> {
            updateConfig("requireApproval", checked);
        });

        scheduleSwitch.setOnCheckedChangeListener((btn, checked) -> {
            updateConfig("scheduleEnabled", checked);
        });
    }

    private void updateConfig(String key, Object value) {
        Map<String, Object> upd = new HashMap<>();
        upd.put(key, value);
        upd.put("updatedAt", System.currentTimeMillis());

        db.collection("news_bot_config").document("main").update(upd)
            .addOnSuccessListener(a -> {
                Toast.makeText(this, "✅ تم التحديث", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "❌ " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    // ═══════════════════════════════════════════
    //  عرض الأحداث الـ 15
    // ═══════════════════════════════════════════

    private static final String[][] EVENTS = {
        {"presidentElected", "👑", "انتخاب رئيس جديد"},
        {"presidentAppointed", "🎖️", "تعيين رئيس معتمد"},
        {"lawPassed", "⚖️", "قانون جديد"},
        {"lawRejected", "❌", "قانون مرفوض"},
        {"treasuryMilestone", "💰", "إنجاز الخزينة"},
        {"courtVerdict", "🏛️", "حكم محكمة"},
        {"citizenAchievement", "🏆", "إنجاز مواطن"},
        {"electionStarted", "🗳️", "بدء الانتخابات"},
        {"newBuilding", "🏗️", "بناء جديد"},
        {"marketTrending", "📈", "منتج رائج"},
        {"jobPromotion", "💼", "ترقية وظيفية"},
        {"giftReceived", "🎁", "هدية"},
        {"dailyReport", "📊", "التقرير اليومي"},
        {"weeklyReport", "📅", "التقرير الأسبوعي"},
        {"monthlyReport", "🗓️", "التقرير الشهري"}
    };

    private void renderEvents(Object eventsObj) {
        eventsContainer.removeAllViews();

        Map<?, ?> eventsMap = null;
        if (eventsObj instanceof Map) {
            eventsMap = (Map<?, ?>) eventsObj;
        }

        for (String[] evt : EVENTS) {
            String key = evt[0];
            String emoji = evt[1];
            String name = evt[2];

            boolean isEnabled = false;
            if (eventsMap != null && eventsMap.get(key) instanceof Boolean) {
                isEnabled = (Boolean) eventsMap.get(key);
            }

            eventsContainer.addView(makeEventRow(key, emoji, name, isEnabled));
        }
    }

    private View makeEventRow(String key, String emoji, String name, boolean isEnabled) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundColor(Color.parseColor(isEnabled ? "#1A3A2E" : "#1A0F2E"));
        row.setPadding(dp(14), dp(10), dp(14), dp(10));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(3), 0, dp(3));
        row.setLayoutParams(lp);

        TextView icon = new TextView(this);
        icon.setText(emoji);
        icon.setTextSize(20);
        icon.setPadding(0, 0, dp(10), 0);
        row.addView(icon);

        TextView lbl = new TextView(this);
        lbl.setText(name);
        lbl.setTextColor(Color.WHITE);
        lbl.setTextSize(13);
        lbl.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        row.addView(lbl);

        Switch sw = new Switch(this);
        sw.setChecked(isEnabled);
        sw.setOnCheckedChangeListener((btn, checked) -> {
            updateEventEnabled(key, checked);
            row.setBackgroundColor(Color.parseColor(checked ? "#1A3A2E" : "#1A0F2E"));
        });
        row.addView(sw);

        return row;
    }

    private void updateEventEnabled(String key, boolean enabled) {
        Map<String, Object> upd = new HashMap<>();
        upd.put("eventsEnabled." + key, enabled);
        upd.put("updatedAt", System.currentTimeMillis());

        db.collection("news_bot_config").document("main").update(upd)
            .addOnSuccessListener(a -> {
                Toast.makeText(this, enabled ? "✅ مُفعّل" : "⏸️ مُعطّل",
                        Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "❌ " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    // ═══════════════════════════════════════════
    //  تحميل الإحصائيات
    // ═══════════════════════════════════════════

    private void loadStats() {
        db.collection("news").whereEqualTo("isBot", true).get()
            .addOnSuccessListener(botSnap -> {
                int botNews = botSnap.size();

                db.collection("news").get()
                    .addOnSuccessListener(allSnap -> {
                        int userNews = allSnap.size() - botNews;

                        db.collection("news_bot_pending").get()
                            .addOnSuccessListener(pendSnap -> {
                                int pending = pendSnap.size();

                                db.collection("news_bot_logs").get()
                                    .addOnSuccessListener(logsSnap -> {
                                        int logs = logsSnap.size();

                                        String txt = "🤖 أخبار البوت: " + botNews + "\n" +
                                                     "👥 أخبار المواطنين: " + userNews + "\n" +
                                                     "📋 بانتظار الموافقة: " + pending + "\n" +
                                                     "📜 السجلات: " + logs;

                                        statsText.setText(txt);
                                    })
                                    .addOnFailureListener(e -> {
                                        statsText.setText("🤖 " + botNews + " | 👥 " + userNews);
                                    });
                            });
                    });
            })
            .addOnFailureListener(e -> {
                statsText.setText("❌ " + e.getMessage());
            });
    }

    // ═══════════════════════════════════════════
    //  تحميل الأخبار المعلّقة
    // ═══════════════════════════════════════════

    private void loadPending() {
        db.collection("news_bot_pending")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(20)
            .get()
            .addOnSuccessListener(snap -> {
                pendingContainer.removeAllViews();

                if (snap.isEmpty()) {
                    TextView empty = new TextView(this);
                    empty.setText("✅ لا توجد أخبار معلّقة");
                    empty.setTextColor(Color.parseColor("#10B981"));
                    empty.setTextSize(13);
                    empty.setGravity(Gravity.CENTER);
                    empty.setPadding(0, dp(20), 0, dp(20));
                    pendingContainer.addView(empty);
                    return;
                }

                for (com.google.firebase.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                    String content = doc.getString("content");
                    Long ts = doc.getLong("timestamp");
                    String id = doc.getId();

                    pendingContainer.addView(makePendingCard(id, content, ts));
                }
            })
            .addOnFailureListener(e -> {
                pendingContainer.removeAllViews();
                TextView err = new TextView(this);
                err.setText("❌ " + e.getMessage());
                err.setTextColor(Color.parseColor("#EF4444"));
                err.setPadding(0, dp(10), 0, dp(10));
                pendingContainer.addView(err);
            });
    }

    private View makePendingCard(String id, String content, Long ts) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.parseColor("#2A1535"));
        card.setPadding(dp(14), dp(14), dp(14), dp(14));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(6), 0, dp(6));
        card.setLayoutParams(lp);

        // الوقت
        if (ts != null) {
            TextView time = new TextView(this);
            time.setText("🕐 " + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date(ts)));
            time.setTextColor(Color.parseColor("#9CA3AF"));
            time.setTextSize(10);
            card.addView(time);
        }

        // المحتوى
        TextView body = new TextView(this);
        body.setText(content != null ? content : "");
        body.setTextColor(Color.WHITE);
        body.setTextSize(13);
        body.setLineSpacing(0, 1.4f);
        body.setPadding(0, dp(8), 0, dp(12));
        card.addView(body);

        // الأزرار
        LinearLayout btns = new LinearLayout(this);
        btns.setOrientation(LinearLayout.HORIZONTAL);

        Button approve = makeButton("✅ موافقة", "#10B981");
        approve.setLayoutParams(new LinearLayout.LayoutParams(0, dp(44), 1f));
        approve.setOnClickListener(v -> approvePending(id));
        btns.addView(approve);

        LinearLayout.LayoutParams spacerLp = new LinearLayout.LayoutParams(dp(6), 0);
        btns.addView(new View(this) {{ setLayoutParams(spacerLp); }});

        Button reject = makeButton("❌ رفض", "#EF4444");
        reject.setLayoutParams(new LinearLayout.LayoutParams(0, dp(44), 1f));
        reject.setOnClickListener(v -> rejectPending(id));
        btns.addView(reject);

        card.addView(btns);
        return card;
    }

    private void approvePending(String id) {
        db.collection("news_bot_pending").document(id).get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) return;
                String content = doc.getString("content");
                if (content == null || content.isEmpty()) return;

                // ننشرو
                Map<String, Object> news = new HashMap<>();
                news.put("author", "🤖 بوت أُمّة");
                news.put("content", content);
                news.put("timestamp", System.currentTimeMillis());
                news.put("isBot", true);
                news.put("approvedBy", "admin");

                db.collection("news").add(news)
                    .addOnSuccessListener(ref -> {
                        db.collection("news_bot_pending").document(id).delete();
                        Toast.makeText(this, "✅ تم النشر", Toast.LENGTH_SHORT).show();
                        loadAll();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "❌ " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            });
    }

    private void rejectPending(String id) {
        new AlertDialog.Builder(this)
            .setTitle("❌ رفض الخبر")
            .setMessage("هل أنت متأكد من رفض هذا الخبر؟")
            .setPositiveButton("نعم", (d, w) -> {
                db.collection("news_bot_pending").document(id).delete()
                    .addOnSuccessListener(a -> {
                        Toast.makeText(this, "✅ تم الرفض", Toast.LENGTH_SHORT).show();
                        loadAll();
                    });
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    // ═══════════════════════════════════════════
    //  تحميل السجل
    // ═══════════════════════════════════════════

    private void loadLogs() {
        db.collection("news_bot_logs")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(15)
            .get()
            .addOnSuccessListener(snap -> {
                logsContainer.removeAllViews();

                if (snap.isEmpty()) {
                    TextView empty = new TextView(this);
                    empty.setText("📭 لا توجد سجلات");
                    empty.setTextColor(Color.parseColor("#9CA3AF"));
                    empty.setTextSize(13);
                    empty.setGravity(Gravity.CENTER);
                    empty.setPadding(0, dp(20), 0, dp(20));
                    logsContainer.addView(empty);
                    return;
                }

                for (com.google.firebase.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                    String action = doc.getString("action");
                    String message = doc.getString("message");
                    Long ts = doc.getLong("timestamp");

                    logsContainer.addView(makeLogRow(action, message, ts));
                }
            })
            .addOnFailureListener(e -> {
                logsContainer.removeAllViews();
                TextView err = new TextView(this);
                err.setText("❌ " + e.getMessage());
                err.setTextColor(Color.parseColor("#EF4444"));
                err.setPadding(0, dp(10), 0, dp(10));
                logsContainer.addView(err);
            });
    }

    private View makeLogRow(String action, String message, Long ts) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setBackgroundColor(Color.parseColor("#1A0F2E"));
        row.setPadding(dp(12), dp(10), dp(12), dp(10));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(3), 0, dp(3));
        row.setLayoutParams(lp);

        // Header: emoji + action + time
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        String emoji = getActionEmoji(action);
        TextView icon = new TextView(this);
        icon.setText(emoji);
        icon.setTextSize(14);
        icon.setPadding(0, 0, dp(6), 0);
        header.addView(icon);

        TextView act = new TextView(this);
        act.setText(getActionLabel(action));
        act.setTextColor(Color.parseColor("#D4AF37"));
        act.setTextSize(12);
        act.setTypeface(null, Typeface.BOLD);
        act.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        header.addView(act);

        if (ts != null) {
            TextView time = new TextView(this);
            time.setText(new SimpleDateFormat("HH:mm", Locale.US).format(new Date(ts)));
            time.setTextColor(Color.parseColor("#6B7280"));
            time.setTextSize(10);
            header.addView(time);
        }

        row.addView(header);

        // Message
        if (message != null && !message.isEmpty()) {
            TextView msg = new TextView(this);
            String preview = message.length() > 80 ? message.substring(0, 80) + "..." : message;
            msg.setText(preview);
            msg.setTextColor(Color.parseColor("#9CA3AF"));
            msg.setTextSize(11);
            msg.setPadding(0, dp(4), 0, 0);
            row.addView(msg);
        }

        return row;
    }

    private String getActionEmoji(String action) {
        if (action == null) return "📝";
        switch (action) {
            case "publish": return "📰";
            case "init": return "🚀";
            case "config_update": return "⚙️";
            case "approved": return "✅";
            case "rejected": return "❌";
            case "pending": return "⏳";
            case "test": return "🧪";
            default: return "📝";
        }
    }

    private String getActionLabel(String action) {
        if (action == null) return "عملية";
        switch (action) {
            case "publish": return "نشر خبر";
            case "init": return "تهيئة";
            case "config_update": return "تحديث الإعدادات";
            case "approved": return "موافقة";
            case "rejected": return "رفض";
            case "pending": return "بانتظار الموافقة";
            case "test": return "اختبار";
            default: return action;
        }
    }

    // ═══════════════════════════════════════════
    //  الأزرار السريعة
    // ═══════════════════════════════════════════

    private void runTest() {
        // نضيف خبر تجريبي
        String[] samples = {
            "🧪 اختبار البوت: رسالة تجريبية للتأكد من عمل النظام.",
            "🤖 مرحباً! أنا بوت أُمّة، جاهز لخدمة المواطنين.",
            "✨ البوت يعمل بكفاءة عالية! جاهز لنشر الأخبار."
        };
        String content = samples[(int)(Math.random() * samples.length)];

        Map<String, Object> news = new HashMap<>();
        news.put("author", "🤖 بوت أُمّة");
        news.put("content", content);
        news.put("timestamp", System.currentTimeMillis());
        news.put("isBot", true);
        news.put("isTest", true);

        db.collection("news").add(news)
            .addOnSuccessListener(ref -> {
                // نسجلو
                Map<String, Object> log = new HashMap<>();
                log.put("action", "test");
                log.put("message", content);
                log.put("newsId", ref.getId());
                log.put("timestamp", System.currentTimeMillis());
                db.collection("news_bot_logs").add(log);

                Toast.makeText(this, "✅ تم نشر خبر اختباري", Toast.LENGTH_LONG).show();
                loadAll();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "❌ " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void triggerReports() {
        new AlertDialog.Builder(this)
            .setTitle("📊 فحص التقارير")
            .setMessage("هل تريد فحص التقارير الدورية الآن؟\n(يومي/أسبوعي/شهري)")
            .setPositiveButton("نعم", (d, w) -> {
                Toast.makeText(this, "⏳ جاري الفحص...", Toast.LENGTH_SHORT).show();

                db.collection("news_bot_config").document("main").get()
                    .addOnSuccessListener(doc -> {
                        if (!doc.exists()) return;

                        long now = System.currentTimeMillis();
                        Long lastDaily = doc.getLong("lastDailyReport");
                        Long lastWeekly = doc.getLong("lastWeeklyReport");
                        Long lastMonthly = doc.getLong("lastMonthlyReport");

                        long lastD = lastDaily != null ? lastDaily : 0;
                        long lastW = lastWeekly != null ? lastWeekly : 0;
                        long lastM = lastMonthly != null ? lastMonthly : 0;

                        int pendingCount = 0;
                        if (now - lastD > 24L * 60 * 60 * 1000) pendingCount++;
                        if (now - lastW > 7L * 24 * 60 * 60 * 1000) pendingCount++;
                        if (now - lastM > 28L * 24 * 60 * 60 * 1000) pendingCount++;

                        if (pendingCount > 0) {
                            Toast.makeText(this,
                                    "✅ " + pendingCount + " تقرير جاهز للنشر",
                                    Toast.LENGTH_LONG).show();
                            // نحدّثو الأوقات يدوياً (البوت الحقيقي يشتغل من التطبيق)
                            markReportChecked();
                        } else {
                            Toast.makeText(this,
                                    "✅ كل التقارير محدّثة",
                                    Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "❌ " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    private void markReportChecked() {
        // نحدّثو وقت آخر فحص
        Map<String, Object> upd = new HashMap<>();
        upd.put("lastChecked", System.currentTimeMillis());
        upd.put("updatedAt", System.currentTimeMillis());

        db.collection("news_bot_config").document("main").update(upd)
            .addOnSuccessListener(a -> {
                Toast.makeText(this, "✅ تم التحديث", Toast.LENGTH_SHORT).show();
                loadAll();
            });
    }

    // ═══════════════════════════════════════════
    //  على Refresh
    // ═══════════════════════════════════════════

    @Override
    protected void onResume() {
        super.onResume();
        // نعيد التحميل كل مرة
        loadStats();
        loadPending();
        loadLogs();
    }

    // ═══════════════════════════════════════════
    //  Publish Manual (نشر يدوي من الأدمن)
    // ═══════════════════════════════════════════

    public void publishManual(String content) {
        if (content == null || content.trim().isEmpty()) {
            Toast.makeText(this, "⚠️ المحتوى فارغ", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> news = new HashMap<>();
        news.put("author", "👑 الإدارة");
        news.put("content", content);
        news.put("timestamp", System.currentTimeMillis());
        news.put("isManual", true);

        db.collection("news").add(news)
            .addOnSuccessListener(ref -> {
                Map<String, Object> log = new HashMap<>();
                log.put("action", "manual_publish");
                log.put("message", content);
                log.put("newsId", ref.getId());
                log.put("timestamp", System.currentTimeMillis());
                db.collection("news_bot_logs").add(log);

                Toast.makeText(this, "✅ تم النشر", Toast.LENGTH_SHORT).show();
                loadAll();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "❌ " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}
