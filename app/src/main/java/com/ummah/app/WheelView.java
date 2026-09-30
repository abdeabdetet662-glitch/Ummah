package com.ummah.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WheelView extends View {

    public interface SpinListener {
        void onSpinFinished(WheelSegment winner);
    }

    private List<WheelSegment> segments = new ArrayList<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint emojiPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float rotation = 0f;
    private float targetRotation = 0f;
    private float velocity = 0f;
    private boolean spinning = false;
    private SpinListener spinListener;
    private final Random random = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private long spinStartTime = 0;
    private long spinDuration = 4500;
    private float spinFromRotation = 0f;
    private float spinDeltaRotation = 0f;

    public WheelView(Context ctx) {
        super(ctx);
        init();
    }

    public WheelView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        init();
    }

    private void init() {
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        emojiPaint.setTextAlign(Paint.Align.CENTER);

        centerPaint.setColor(Color.parseColor("#0A0A0A"));
    }

    public void setSegments(List<WheelSegment> list) {
        this.segments = list != null ? list : new ArrayList<>();
        invalidate();
    }

    public void setSpinListener(SpinListener l) {
        this.spinListener = l;
    }

    // ═══════════════════════════════════════
    //  Spin
    // ═══════════════════════════════════════
    public void spinToSegment(WheelSegment winner) {
        if (segments.isEmpty() || winner == null) return;

        int winnerIndex = segments.indexOf(winner);
        if (winnerIndex < 0) winnerIndex = 0;

        int n = segments.size();
        float segmentAngle = 360f / n;

        // الزاوية الوسطية للقطاع الفائز (بالنسبة للمؤشر فوق)
        // القطاع 0 يبدأ من -90 درجة (فوق) - نستعمل دوران بحيث يكون المؤشر فوق
        float winnerCenterAngle = winnerIndex * segmentAngle + segmentAngle / 2f;

        // نختارو دوران عشوائي (3-5 لفات) + زاوية الفائز
        int fullSpins = 3 + random.nextInt(3);
        float currentRotationMod = ((rotation % 360f) + 360f) % 360f;

        // نريدو المؤشر (زاوية 0 بالراديان) يقع على winnerCenterAngle
        // المؤشر في الأعلى (زاوية -90 = -π/2). لذا الزاوية المطلوبة:
        float targetAngle = 360f - winnerCenterAngle;
        float delta = (targetAngle - currentRotationMod + 360f) % 360f;
        float totalDelta = fullSpins * 360f + delta;

        spinFromRotation = rotation;
        spinDeltaRotation = totalDelta;
        spinStartTime = System.currentTimeMillis();
        spinning = true;

        AnimHelper.lightHaptic(getContext());
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        float cx = w / 2f;
        float cy = h / 2f;
        float radius = Math.min(w, h) / 2f - 20f;

        if (segments.isEmpty()) {
            // رسم دائرة فاضية
            paint.setColor(Color.parseColor("#1A1A1A"));
            canvas.drawCircle(cx, cy, radius, paint);
            paint.setColor(Color.parseColor("#D4AF37"));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(6f);
            canvas.drawCircle(cx, cy, radius, paint);
            paint.setStyle(Paint.Style.FILL);

            textPaint.setTextSize(radius * 0.15f);
            canvas.drawText("جاري التحميل...", cx, cy, textPaint);
            return;
        }

        // ═══ الحساب ═══
        float currentRotation = rotation;
        if (spinning) {
            long elapsed = System.currentTimeMillis() - spinStartTime;
            float t = Math.min(1f, elapsed / (float) spinDuration);
            // easing: ease-out cubic
            float easeOut = 1f - (float) Math.pow(1f - t, 3);
            currentRotation = spinFromRotation + spinDeltaRotation * easeOut;
            rotation = currentRotation;

            if (t >= 1f) {
                spinning = false;
                if (spinListener != null) {
                    WheelSegment winner = pickWinnerFromRotation(currentRotation);
                    if (winner != null) spinListener.onSpinFinished(winner);
                }
            }
        }

        int n = segments.size();
        float segmentAngle = 360f / n;

        // ═══ رسم القطاعات ═══
        for (int i = 0; i < n; i++) {
            WheelSegment s = segments.get(i);
            float startAngle = -90f + i * segmentAngle + currentRotation;

            paint.setColor(Color.parseColor(s.color != null ? s.color : "#333333"));
            RectF oval = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
            canvas.drawArc(oval, startAngle, segmentAngle, true, paint);

            // خط فاصل
            paint.setColor(0x33000000);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2f);
            canvas.drawArc(oval, startAngle, segmentAngle, true, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        // ═══ النصوص (emoji + label) ═══
        float emojiSize = radius * 0.16f;
        float labelSize = radius * 0.075f;
        emojiPaint.setTextSize(emojiSize);
        textPaint.setTextSize(labelSize);

        for (int i = 0; i < n; i++) {
            WheelSegment s = segments.get(i);
            float midAngle = -90f + i * segmentAngle + segmentAngle / 2f + currentRotation;
            double rad = Math.toRadians(midAngle);

            float emojiRadius = radius * 0.62f;
            float labelRadius = radius * 0.82f;

            float ex = cx + (float) (Math.cos(rad) * emojiRadius);
            float ey = cy + (float) (Math.sin(rad) * emojiRadius);
            float lx = cx + (float) (Math.cos(rad) * labelRadius);
            float ly = cy + (float) (Math.sin(rad) * labelRadius);

            // emoji
            canvas.drawText(s.emoji != null ? s.emoji : "•",
                    ex, ey + emojiSize * 0.35f, emojiPaint);

            // label
            canvas.drawText(s.label != null ? s.label : "",
                    lx, ly + labelSize * 0.35f, textPaint);
        }

        // ═══ الحدود الذهبية ═══
        paint.setColor(Color.parseColor("#D4AF37"));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8f);
        canvas.drawCircle(cx, cy, radius, paint);
        paint.setStyle(Paint.Style.FILL);

        // ═══ المركز ═══
        paint.setColor(Color.parseColor("#0A0A0A"));
        canvas.drawCircle(cx, cy, radius * 0.18f, paint);
        paint.setColor(Color.parseColor("#D4AF37"));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6f);
        canvas.drawCircle(cx, cy, radius * 0.18f, paint);
        paint.setStyle(Paint.Style.FILL);

        // emoji في المركز
        Paint centerEmoji = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerEmoji.setTextAlign(Paint.Align.CENTER);
        centerEmoji.setTextSize(radius * 0.22f);
        canvas.drawText("🎰", cx, cy + radius * 0.075f, centerEmoji);

        // ═══ المؤشر (فوق) ═══
        android.graphics.Path pointer = new android.graphics.Path();
        float pw = radius * 0.08f;
        float ph = radius * 0.18f;
        pointer.moveTo(cx, cy - radius + 4);
        pointer.lineTo(cx - pw, cy - radius - ph);
        pointer.lineTo(cx + pw, cy - radius - ph);
        pointer.close();
        paint.setColor(Color.parseColor("#D4AF37"));
        canvas.drawPath(pointer, paint);
        paint.setColor(Color.parseColor("#FFFFFF"));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        canvas.drawPath(pointer, paint);
        paint.setStyle(Paint.Style.FILL);

        // ═══ Animation ═══
        if (spinning) {
            postInvalidateOnAnimation();
        }
    }

    private WheelSegment pickWinnerFromRotation(float rot) {
        if (segments.isEmpty()) return null;
        int n = segments.size();
        float segmentAngle = 360f / n;

        // المؤشر فوق = زاوية -90 (أي 270)
        float pointerAngle = 270f;
        float normalized = ((pointerAngle - rot) % 360f + 360f) % 360f;
        int idx = (int) (normalized / segmentAngle) % n;
        if (idx < 0) idx = 0;
        if (idx >= n) idx = n - 1;
        return segments.get(idx);
    }
}
