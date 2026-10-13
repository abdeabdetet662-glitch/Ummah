package com.ummah.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class OfficeActivity extends Activity {

    private IdentityManager im;
    private WebView webView;
    private LinearLayout root;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        // Feature Check
        if (!FeatureFlags.checkOrToast(this, "city")) {
            finish();
            return;
        }

        im = new IdentityManager(this);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#C8E0F0"));

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER);
        header.setPadding(20, 40, 20, 20);
        header.setBackgroundColor(Color.parseColor("#FFFFFF"));

        TextView title = new TextView(this);
        title.setText("🏢  مكتب أُمّة");
        title.setTextColor(Color.parseColor("#1A1A1A"));
        title.setTextSize(22);
        title.setTypeface(null, Typeface.BOLD);
        header.addView(title);

        root.addView(header);

        // WebView
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.setBackgroundColor(Color.parseColor("#C8E0F0"));
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");

        LinearLayout.LayoutParams wvLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        webView.setLayoutParams(wvLp);
        webView.loadUrl("file:///android_asset/office/index.html");
        root.addView(webView);

        setContentView(root);

        // نبعثو بيانات المستخدم بعد التحميل
        webView.postDelayed(() -> {
            Citizen me = im.getCitizen();
            if (me != null && webView != null) {
                webView.evaluateJavascript(
                        "setCurrentUser(\"" + me.nationalId + "\");", null);
            }
        }, 1500);
    }

    public class AndroidBridge {

        @JavascriptInterface
        public void onReady() {
            runOnUiThread(() -> {
                Citizen me = im.getCitizen();
                if (me != null && webView != null) {
                    webView.evaluateJavascript(
                            "setCurrentUser(\"" + me.nationalId + "\");", null);
                }
            });
        }

        @JavascriptInterface
        public void openActivity(String name) {
            runOnUiThread(() -> {
                try {
                    Class<?> clazz = Class.forName("com.ummah.app." + name);
                    android.content.Intent intent = new android.content.Intent(OfficeActivity.this, clazz);
                    startActivity(intent);
                } catch (ClassNotFoundException e) {
                    Toast.makeText(OfficeActivity.this,
                            "⚠️ " + name + " غير متاح", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(OfficeActivity.this,
                            "❌ " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        @JavascriptInterface
        public void showToast(String message) {
            runOnUiThread(() -> Toast.makeText(OfficeActivity.this,
                    message, Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) webView.destroy();
    }

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }
}
