package com.ummah.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CelebrationView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean running = true;
    private boolean initialized = false;

    private final int[] colors = {
        Color.parseColor("#D4AF37"), Color.parseColor("#FFD700"),
        Color.parseColor("#FF1744"), Color.parseColor("#00E676"),
        Color.parseColor("#00E5FF"), Color.parseColor("#D500F9"),
        Color.WHITE
    };

    private final String[] emojis = {"🎉", "🎊", "⭐", "💫", "✨", "🌟", "👑", "🏆", "🎆"};

    public CelebrationView(Context c) { super(c); }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        if (!initialized && w > 0 && h > 0) {
            initialized = true;
            for (int i = 0; i < 120; i++) particles.add(createParticle(w, h, true));
            startLoop();
        }
    }

    private Particle createParticle(int w, int h, boolean randomY) {
        Particle p = new Particle();
        p.x = random.nextFloat() * w;
        p.y = randomY ? random.nextFloat() * h : -50;
        p.vx = (random.nextFloat() - 0.5f) * 4;
        p.vy = 2 + random.nextFloat() * 5;
        p.color = colors[random.nextInt(colors.length)];
        p.size = 15 + random.nextFloat() * 30;
        p.rotation = random.nextFloat() * 360;
        p.rotSpeed = (random.nextFloat() - 0.5f) * 12;
        p.emoji = emojis[random.nextInt(emojis.length)];
        p.isEmoji = random.nextFloat() < 0.4f;
        return p;
    }

    private void startLoop() {
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                if (!running) return;
                int w = getWidth(), h = getHeight();
                for (Particle p : particles) {
                    p.x += p.vx; p.y += p.vy; p.rotation += p.rotSpeed; p.vy += 0.06f;
                    if (p.y > h + 50) {
                        Particle np = createParticle(w, h, false);
                        p.x = np.x; p.y = np.y; p.vx = np.vx; p.vy = np.vy;
                        p.color = np.color; p.size = np.size;
                        p.rotation = np.rotation; p.rotSpeed = np.rotSpeed;
                        p.emoji = np.emoji; p.isEmoji = np.isEmoji;
                    }
                }
                invalidate();
                handler.postDelayed(this, 16);
            }
        }, 16);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Particle p : particles) {
            canvas.save();
            canvas.rotate(p.rotation, p.x, p.y);
            if (p.isEmoji) {
                paint.setTextSize(p.size);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawText(p.emoji, p.x, p.y, paint);
            } else {
                paint.setColor(p.color);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(p.x - p.size/2, p.y - p.size/2, p.x + p.size/2, p.y + p.size/2, paint);
            }
            canvas.restore();
        }
    }

    public void stop() { running = false; handler.removeCallbacksAndMessages(null); }

    private static class Particle {
        float x, y, vx, vy, size, rotation, rotSpeed;
        int color;
        String emoji;
        boolean isEmoji;
    }
}
