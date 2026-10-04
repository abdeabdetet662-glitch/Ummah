package com.ummah.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * SpyMissionActivity — عملية سرية
 * يشغّل اللعبة داخل WebView مع Canvas
 */
public class SpyMissionActivity extends Activity {

    private WebView webView;
    private IdentityManager im;
    private Citizen me;

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        im = new IdentityManager(this);
        me = im.getCitizen();

        // شاشة كاملة
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        FrameLayout container = new FrameLayout(this);
        container.setBackgroundColor(Color.parseColor("#0a0510"));

        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.setBackgroundColor(Color.parseColor("#0a0510"));

        // JavaScript Bridge
        webView.addJavascriptInterface(new GameBridge(), "Android");

        webView.loadUrl("file:///android_asset/spy_mission/index.html");

        container.addView(webView);
        setContentView(container);
    }

    // ═══ Bridge بين JS و Android ═══
    private class GameBridge {

        @JavascriptInterface
        public String getPlayerName() {
            return me != null ? me.name : "عميل";
        }

        @JavascriptInterface
        public String getPlayerId() {
            return me != null ? me.nationalId : "";
        }

        @JavascriptInterface
        public void onMissionEnd(final boolean caught, final int kills, final String moleName) {
            runOnUiThread(() -> {
                // مكافآت
                int reward = caught ? 5000 : 500;
                if (me != null) {
                    PaymentManager.addBalance(
                        me.nationalId,
                        reward,
                        "spy_mission_reward",
                        caught ? "اكتشاف الخائن في عملية سرية" : "مكافأة مشاركة",
                        new PaymentManager.PayCallback() {
                            @Override public void onSuccess(long newBalance) {
                                Toast.makeText(SpyMissionActivity.this,
                                    "💰 +" + reward + " Đ\n" + 
                                    (caught ? "🎉 اكتشفت الخائن!" : "الخائن كان: " + moleName),
                                    Toast.LENGTH_LONG).show();
                            }
                            @Override public void onInsufficient(long a, long r) {}
                            @Override public void onError(String e) {}
                        }
                    );
                }
            });
        }

        @JavascriptInterface
        public void onExit() {
            runOnUiThread(() -> {
                finish();
            });
        }

        @JavascriptInterface
        public void showToast(final String msg) {
            runOnUiThread(() ->
                Toast.makeText(SpyMissionActivity.this, msg, Toast.LENGTH_SHORT).show()
            );
        }
    }

    @Override
    public void onBackPressed() {
        // نعرض سؤال الخروج
        new android.app.AlertDialog.Builder(this)
            .setTitle("🚪 الخروج")
            .setMessage("هل تريد الخروج من المهمة؟")
            .setPositiveButton("نعم", (d, w) -> finish())
            .setNegativeButton("لا", null)
            .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) webView.onResume();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
