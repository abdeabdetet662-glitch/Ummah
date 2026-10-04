package com.ummah.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * BattleRoyaleActivity — معركة أُمّة
 * لعبة Battle Royale أونلاين داخل WebView
 */
public class BattleRoyaleActivity extends Activity {

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

        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        FrameLayout container = new FrameLayout(this);
        container.setBackgroundColor(Color.parseColor("#0a0510"));

        webView = new WebView(this);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.setBackgroundColor(Color.parseColor("#0a0510"));

        webView.addJavascriptInterface(new GameBridge(), "Android");

        webView.loadUrl("file:///android_asset/battle_royale/index.html");

        container.addView(webView);
        setContentView(container);
    }

    private class GameBridge {

        @JavascriptInterface
        public String getPlayerName() {
            return me != null && me.name != null ? me.name : "عميل";
        }

        @JavascriptInterface
        public String getPlayerId() {
            return me != null ? me.nationalId : "";
        }

        @JavascriptInterface
        public int getBalance() {
            if (me == null) return 0;
            // نقراو الرصيد مباشرة من Firestore
            try {
                com.google.firebase.firestore.FirebaseFirestore
                    .getInstance()
                    .collection("citizens").document(me.nationalId).get()
                    .addOnSuccessListener(doc -> {
                        Long bal = doc.getLong("balance");
                        if (bal != null) {
                            webView.post(() -> webView.evaluateJavascript(
                                "document.getElementById('my-balance').textContent='" + bal + " Đ';", null));
                        }
                    });
            } catch (Exception e) {}
            return 0;
        }

        @JavascriptInterface
        public void onGameEnd(final boolean won, final int kills, final int rank) {
            runOnUiThread(() -> {
                int reward;
                if (won) {
                    reward = 5000;
                } else if (rank <= 3) {
                    reward = 1000;
                } else {
                    reward = 50 + kills * 20;
                }

                if (me != null) {
                    PaymentManager.addBalance(
                        me.nationalId, reward,
                        "battle_reward",
                        won ? "فوز في معركة أُمّة" : "مشاركة في معركة أُمّة",
                        new PaymentManager.PayCallback() {
                            @Override public void onSuccess(long nb) {
                                Toast.makeText(BattleRoyaleActivity.this,
                                    "💰 +" + reward + " Đ" + (won ? "\n🏆 فوز!" : ""),
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
            runOnUiThread(BattleRoyaleActivity.this::finish);
        }

        @JavascriptInterface
        public void showToast(final String msg) {
            runOnUiThread(() -> Toast.makeText(BattleRoyaleActivity.this, msg, Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
            .setTitle("🚪 الخروج")
            .setMessage("هل تريد الخروج من المعركة؟")
            .setPositiveButton("نعم", (d, w) -> finish())
            .setNegativeButton("لا", null)
            .show();
    }

    @Override protected void onPause() { super.onPause(); if (webView != null) webView.onPause(); }
    @Override protected void onResume() { super.onResume(); if (webView != null) webView.onResume(); }

    @Override
    protected void onDestroy() {
        if (webView != null) { webView.destroy(); webView = null; }
        super.onDestroy();
    }
}
