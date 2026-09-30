package com.ummah.app;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

/**
 * إخفاء أشرطة النظام (ساعة + بطارية + أزرار)
 * آمن 100% — أي فشل = ما يصير شي
 */
public class ImmersiveHelper {

    public static void enable(Activity activity) {
        try {
            if (activity == null) return;

            // الطريقة الحديثة (Android 11+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity.getWindow().setDecorFitsSystemWindows(false);
                activity.getWindow().setStatusBarColor(0x00000000);
                activity.getWindow().setNavigationBarColor(0x00000000);

                android.view.WindowInsetsController controller =
                        activity.getWindow().getInsetsController();
                if (controller != null) {
                    controller.hide(android.view.WindowInsets.Type.statusBars());
                    controller.hide(android.view.WindowInsets.Type.navigationBars());
                    controller.setSystemBarsBehavior(
                            android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    );
                }
            } else {
                // الطريقة القديمة (Android 10-)
                activity.getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                );
            }

            // إبقاء الشاشة مضاءة (اختياري)
            activity.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            );

        } catch (Exception e) {
            // فشل؟ ما يصير شي
        }
    }
}
