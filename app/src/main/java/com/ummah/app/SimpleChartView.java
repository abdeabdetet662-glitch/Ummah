package com.ummah.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * SimpleChartView — رسم بياني بسيط للرصيد
 */
public class SimpleChartView extends View {

    private List<StatsManager.ChartPoint> points = new ArrayList<>();

    private final Paint pLine = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pDot = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pGrid = new Paint(Paint.ANTI_ALIAS_FLAG);

    public SimpleChartView(Context c) { super(c); init(); }
    public SimpleChartView(Context c, AttributeSet a) { super(c, a); init(); }

    private void init() {
        pLine.setStyle(Paint.Style.STROKE);
        pLine.setStrokeWidth(4f);
        pLine.setColor(Color.parseColor("#D4AF37"));

        pFill.setStyle(Paint.Style.FILL);
        pFill.setColor(Color.parseColor("#30D4AF37"));

        pDot.setColor(Color.parseColor("#F4D97A"));
        pDot.setStyle(Paint.Style.FILL);

        pText.setColor(Color.parseColor("#888888"));
        pText.setTextSize(24f);
        pText.setTextAlign(Paint.Align.CENTER);

        pGrid.setColor(Color.parseColor("#20D4AF37"));
        pGrid.setStrokeWidth(1f);
    }

    public void setData(List<StatsManager.ChartPoint> pts) {
        this.points = pts != null ? pts : new ArrayList<>();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        int padL = 30, padR = 30, padT = 30, padB = 60;

        int chartW = w - padL - padR;
        int chartH = h - padT - padB;

        // Background
        canvas.drawColor(Color.parseColor("#0a0510"));

        if (points.isEmpty()) {
            pText.setColor(Color.parseColor("#666666"));
            pText.setTextSize(28f);
            canvas.drawText("لا توجد بيانات بعد", w / 2f, h / 2f, pText);
            return;
        }

        // Find min/max
        long minV = Long.MAX_VALUE, maxV = Long.MIN_VALUE;
        for (StatsManager.ChartPoint p : points) {
            if (p.balance < minV) minV = p.balance;
            if (p.balance > maxV) maxV = p.balance;
        }
        if (maxV == minV) maxV = minV + 1;

        // Grid lines
        for (int i = 0; i <= 4; i++) {
            float y = padT + chartH * i / 4f;
            canvas.drawLine(padL, y, padL + chartW, y, pGrid);
        }

        // Calculate points
        int n = points.size();
        float[] xs = new float[n];
        float[] ys = new float[n];

        for (int i = 0; i < n; i++) {
            xs[i] = padL + chartW * i / Math.max(1, n - 1);
            float pct = (points.get(i).balance - minV) / (float)(maxV - minV);
            ys[i] = padT + chartH * (1f - pct);
        }

        // Fill path
        Path fillPath = new Path();
        fillPath.moveTo(xs[0], padT + chartH);
        for (int i = 0; i < n; i++) {
            fillPath.lineTo(xs[i], ys[i]);
        }
        fillPath.lineTo(xs[n-1], padT + chartH);
        fillPath.close();

        // Gradient fill
        LinearGradient lg = new LinearGradient(0, padT, 0, padT + chartH,
                Color.parseColor("#60D4AF37"), Color.parseColor("#00D4AF37"),
                Shader.TileMode.CLAMP);
        pFill.setShader(lg);
        canvas.drawPath(fillPath, pFill);
        pFill.setShader(null);

        // Line path
        Path linePath = new Path();
        linePath.moveTo(xs[0], ys[0]);
        for (int i = 1; i < n; i++) {
            // Bezier curve
            float cx = (xs[i-1] + xs[i]) / 2f;
            float cy = (ys[i-1] + ys[i]) / 2f;
            linePath.quadTo(xs[i-1], ys[i-1], cx, cy);
            if (i == n - 1) linePath.lineTo(xs[i], ys[i]);
        }
        canvas.drawPath(linePath, pLine);

        // Dots + labels
        for (int i = 0; i < n; i++) {
            canvas.drawCircle(xs[i], ys[i], 8f, pDot);

            // Date label
            String label = points.get(i).date;
            if (label.length() >= 10) label = label.substring(5); // MM-DD
            pText.setColor(Color.parseColor("#888888"));
            pText.setTextSize(22f);
            canvas.drawText(label, xs[i], padT + chartH + 35, pText);
        }

        // Max value label
        pText.setColor(Color.parseColor("#D4AF37"));
        pText.setTextSize(24f);
        pText.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(formatAmount(maxV), padL, padT - 8, pText);

        pText.setTextAlign(Paint.Align.CENTER);
    }

    private String formatAmount(long v) {
        if (v >= 1_000_000) return (v / 1_000_000) + "M";
        if (v >= 1_000) return (v / 1_000) + "K";
        return String.valueOf(v);
    }
}
