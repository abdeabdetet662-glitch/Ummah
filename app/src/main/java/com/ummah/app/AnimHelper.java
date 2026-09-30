package com.ummah.app;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;

public class AnimHelper {

    // Fade-in + Slide-up للبطاقات
    public static void fadeInUp(View v, long delayMs) {
        AnimationSet set = new AnimationSet(true);
        set.setInterpolator(new DecelerateInterpolator());

        AlphaAnimation alpha = new AlphaAnimation(0f, 1f);
        alpha.setDuration(400);

        android.view.animation.TranslateAnimation slide =
                new android.view.animation.TranslateAnimation(0, 0, 60, 0);
        slide.setDuration(400);

        set.addAnimation(alpha);
        set.addAnimation(slide);
        set.setStartOffset(delayMs);

        v.startAnimation(set);
    }

    // Scale on press
    public static void pressEffect(View v) {
        ScaleAnimation scale = new ScaleAnimation(
                1f, 0.95f, 1f, 0.95f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(80);
        scale.setRepeatCount(1);
        scale.setRepeatMode(Animation.REVERSE);
        v.startAnimation(scale);
    }

    // Haptic feedback
    public static void vibrate(Context ctx, long ms) {
        try {
            Vibrator vib = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
            if (vib == null || !vib.hasVibrator()) return;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vib.vibrate(ms);
            }
        } catch (Exception ignored) {}
    }

    public static void lightHaptic(Context ctx) {
        vibrate(ctx, 15);
    }

    public static void mediumHaptic(Context ctx) {
        vibrate(ctx, 30);
    }
}
