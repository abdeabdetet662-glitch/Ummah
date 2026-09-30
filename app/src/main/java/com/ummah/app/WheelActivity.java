package com.ummah.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class WheelActivity extends Activity {

    private IdentityManager im;
    private WheelManager wm;
    private WheelView wheelView;
    private TextView balanceView;
    private TextView jackpotView;
    private TextView freeSpinView;
    private Button spinBtn;
    private Button freeSpinBtn;
    private LinearLayout historyContainer;

    private WheelManager.WheelConfig config = new WheelManager.WheelConfig();
    private List<WheelSegment> segments;
    private ListenerRegistration configReg, segmentsReg, balReg, historyReg;
    private boolean spinning = false;
    private boolean canFreeSpin = false;
    private long nextFreeAt = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        im = new IdentityManager(this);
        wm = new WheelManager();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(20, 30, 20, 40);
        scroll.addView(root);

        // Header
        TextView title = UiHelper.goldTitle(this, "🎡  عجلة الحظ", 28);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("جرّب حظك واربح جوائز مميزة");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 16);
        root.addView(sub);

        // Jackpot card
        LinearLayout jackpotCard = new LinearLayout(this);
        jackpotCard.setOrientation(LinearLayout.VERTICAL);
        jackpotCard.setGravity(Gravity.CENTER);
        jackpotCard.setBackgroundResource(R.drawable.bg_president_card);
        jackpotCard.setPadding(30, 20, 30, 20);
        LinearLayout.LayoutParams jlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        jlp.setMargins(0, 0, 0, 16);
        jackpotCard.setLayoutParams(jlp);

        TextView jlbl = new TextView(this);
        jlbl.setText("💎  الجاكبوت التراكمي");
        jlbl.setTextColor(Color.parseColor("#FFD700"));
        jlbl.setTextSize(12);
        jlbl.setGravity(Gravity.CENTER);
        jackpotCard.addView(jlbl);

        jackpotView = new TextView(this);
        jackpotView.setText("...");
        jackpotView.setTextColor(Color.parseColor("#FFFFFF"));
        jackpotView.setTextSize(32);
        jackpotView.setTypeface(null, Typeface.BOLD);
        jackpotView.setGravity(Gravity.CENTER);
        jackpotView.setPadding(0, 6, 0, 0);
        jackpotCard.addView(jackpotView);

        root.addView(jackpotCard);

        // Wheel View
        wheelView = new WheelView(this);
        LinearLayout.LayoutParams wlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 700);
        wlp.setMargins(0, 8, 0, 16);
        wheelView.setLayoutParams(wlp);
        wheelView.setSpinListener(winner -> onSpinFinished(winner));
        root.addView(wheelView);

        // Balance
        LinearLayout balCard = UiHelper.card(this);
        balCard.setGravity(Gravity.CENTER);

        TextView blbl = new TextView(this);
        blbl.setText("💰  رصيدك");
        blbl.setTextColor(Color.parseColor("#9E9E9E"));
        blbl.setTextSize(12);
        blbl.setGravity(Gravity.CENTER);
        balCard.addView(blbl);

        balanceView = new TextView(this);
        balanceView.setText("...");
        balanceView.setTextColor(Color.parseColor("#D4AF37"));
        balanceView.setTextSize(28);
        balanceView.setTypeface(null, Typeface.BOLD);
        balanceView.setGravity(Gravity.CENTER);
        balanceView.setPadding(0, 6, 0, 0);
        balCard.addView(balanceView);

        root.addView(balCard);

        // Free spin timer
        freeSpinView = new TextView(this);
        freeSpinView.setText("");
        freeSpinView.setTextColor(Color.parseColor("#4CAF50"));
        freeSpinView.setTextSize(13);
        freeSpinView.setGravity(Gravity.CENTER);
        freeSpinView.setPadding(0, 12, 0, 12);
        root.addView(freeSpinView);

        // Spin Button
        spinBtn = UiHelper.primaryButton(this, "🎰  دوّر العجلة (500 Đ)");
        spinBtn.setMinHeight(160);
        spinBtn.setTextSize(19);
        spinBtn.setOnClickListener(v -> doSpin(false));
        root.addView(spinBtn);

        // Free Spin Button
        freeSpinBtn = UiHelper.actionButton(this, "🎁  دورة مجانية", "#2E7D32");
        freeSpinBtn.setOnClickListener(v -> doSpin(true));
        root.addView(freeSpinBtn);

        // History
        TextView histTitle = new TextView(this);
        histTitle.setText("📋  آخر الدورات");
        histTitle.setTextColor(Color.parseColor("#D4AF37"));
        histTitle.setTextSize(15);
        histTitle.setTypeface(null, Typeface.BOLD);
        histTitle.setGravity(Gravity.CENTER);
        histTitle.setPadding(0, 32, 0, 12);
        root.addView(histTitle);

        historyContainer = new LinearLayout(this);
        historyContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(historyContainer);

        setContentView(scroll);

        // ═══ Listeners ═══
        startListeners();
    }

    private void startListeners() {
        // Config
        configReg = wm.listenConfig(new WheelManager.ConfigListener() {
            @Override public void onConfig(WheelManager.WheelConfig c) {
                config = c;
                runOnUiThread(() -> {
                    spinBtn.setText("🎰  دوّر العجلة (" + c.spinCost + " Đ)");
                    if (!c.jackpotEnabled) jackpotView.setText("معطّل");
                    if (!c.active) {
                        spinBtn.setEnabled(false);
                        spinBtn.setAlpha(0.5f);
                        freeSpinBtn.setEnabled(false);
                        freeSpinBtn.setAlpha(0.5f);
                    }
                });
            }
            @Override public void onError(String msg) {}
        });

        // Segments
        segmentsReg = wm.listenSegments(new WheelManager.SegmentsListener() {
            @Override public void onSegments(List<WheelSegment> list) {
                segments = list;
                runOnUiThread(() -> wheelView.setSegments(list));
            }
            @Override public void onError(String msg) {
                runOnUiThread(() -> Toast.makeText(WheelActivity.this, "خطأ: " + msg, Toast.LENGTH_SHORT).show());
            }
        });

        // Balance
        Citizen c = im.getCitizen();
        if (c != null) {
            balReg = FirebaseManager.get().listenBalance(c.nationalId,
                    new FirebaseManager.BalanceListener() {
                @Override public void onBalance(int balance) {
                    runOnUiThread(() -> balanceView.setText(balance + " Đ"));
                }
                @Override public void onError(String m) {}
            });

            // History
            historyReg = wm.listenMySpins(c.nationalId, list -> {
                runOnUiThread(() -> renderHistory(list));
            });

            // Free spin check
            checkFreeSpin(c.nationalId);
        }
    }

    private void checkFreeSpin(String nationalId) {
        wm.checkFreeSpin(nationalId, config.freeSpinCooldownMs,
                new WheelManager.FreeSpinListener() {
            @Override public void onResult(boolean can, long nextAt) {
                canFreeSpin = can;
                nextFreeAt = nextAt;
                runOnUiThread(() -> updateFreeSpinUI());
            }
        });
    }

    private void updateFreeSpinUI() {
        if (canFreeSpin) {
            freeSpinView.setText("🎁 عندك دورة مجانية متاحة!");
            freeSpinView.setTextColor(Color.parseColor("#4CAF50"));
            freeSpinBtn.setEnabled(true);
            freeSpinBtn.setAlpha(1f);
        } else {
            long remaining = nextFreeAt - System.currentTimeMillis();
            long hours = remaining / (60 * 60 * 1000);
            long minutes = (remaining % (60 * 60 * 1000)) / (60 * 1000);
            freeSpinView.setText("⏳ الدورة المجانية القادمة بعد: " + hours + " س " + minutes + " د");
            freeSpinView.setTextColor(Color.parseColor("#FFA500"));
            freeSpinBtn.setEnabled(false);
            freeSpinBtn.setAlpha(0.5f);
        }
    }

    private void doSpin(boolean isFree) {
        if (spinning) {
            Toast.makeText(this, "العجلة تدور...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (segments == null || segments.isEmpty()) {
            Toast.makeText(this, "العجلة ما تحملتش", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!config.active) {
            Toast.makeText(this, "العجلة معطلة", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isFree && !canFreeSpin) {
            Toast.makeText(this, "الدورة المجانية ماشي متاحة", Toast.LENGTH_SHORT).show();
            return;
        }

        Citizen c = im.getCitizen();
        if (c == null) return;

        spinning = true;
        spinBtn.setEnabled(false);
        freeSpinBtn.setEnabled(false);
        AnimHelper.mediumHaptic(this);

        wm.spin(c.nationalId, isFree, segments, config, new WheelManager.SpinListener() {
            @Override public void onResult(WheelSegment winner, int jackpotAmount) {
                runOnUiThread(() -> {
                    wheelView.spinToSegment(winner);
                });
            }
            @Override public void onError(String msg) {
                runOnUiThread(() -> {
                    spinning = false;
                    spinBtn.setEnabled(true);
                    freeSpinBtn.setEnabled(canFreeSpin);
                    Toast.makeText(WheelActivity.this, "❌ " + msg, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void onSpinFinished(WheelSegment winner) {
        spinning = false;
        spinBtn.setEnabled(true);
        freeSpinBtn.setEnabled(canFreeSpin);

        AnimHelper.mediumHaptic(this);

        // حوار الفوز
        String title = winner.emoji + "  " + winner.label;
        String message;
        if ("money".equals(winner.type)) {
            message = "🎉 ربحت " + winner.value + " Đ!";
        } else if ("item".equals(winner.type)) {
            message = "🎉 ربحت: " + (winner.itemName != null ? winner.itemName : winner.label) + "!";
        } else if ("jackpot".equals(winner.type)) {
            message = "💎💎💎 ربحت الجاكبوت!";
        } else if ("nothing".equals(winner.type)) {
            message = "😢 للأسف، ما ربحتش هذي المرة.\nجرّب مرة أخرى!";
        } else if ("privilege".equals(winner.type)) {
            message = "✨ ربحت: " + winner.label;
        } else {
            message = "🎉 ربحت: " + winner.label;
        }

        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("حسناً", null)
            .show();

        // نحدثو الـ free spin
        Citizen c = im.getCitizen();
        if (c != null) {
            checkFreeSpin(c.nationalId);
        }
    }

    private void renderHistory(List<java.util.Map<String, Object>> list) {
        historyContainer.removeAllViews();

        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("ما فيه دورات بعد");
            empty.setTextColor(Color.parseColor("#9E9E9E"));
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 20, 0, 0);
            historyContainer.addView(empty);
            return;
        }

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.US);

        for (java.util.Map<String, Object> m : list) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setBackgroundResource(R.drawable.bg_card_premium);
            card.setPadding(20, 14, 20, 14);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 4, 0, 4);
            card.setLayoutParams(lp);

            String label = (String) m.get("segmentLabel");
            String type = (String) m.get("type");
            Long ts = (Long) m.get("timestamp");

            TextView tv = new TextView(this);
            tv.setText(label != null ? label : "—");
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(13);
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            card.addView(tv);

            TextView time = new TextView(this);
            time.setText(ts != null ? sdf.format(new java.util.Date(ts)) : "");
            time.setTextColor(Color.parseColor("#9E9E9E"));
            time.setTextSize(10);
            card.addView(time);

            historyContainer.addView(card);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (configReg != null) configReg.remove();
        if (segmentsReg != null) segmentsReg.remove();
        if (balReg != null) balReg.remove();
        if (historyReg != null) historyReg.remove();
    }
}
