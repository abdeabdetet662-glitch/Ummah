package com.ummah.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration;

public class AvatarActivity extends Activity {

    private IdentityManager im;
    private AvatarManager am;
    private WebView webView;
    private Avatar current;
    private ListenerRegistration reg;
    private boolean pageLoaded = false;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        im = new IdentityManager(this);
        am = new AvatarManager();
        current = new Avatar();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 50, 30, 60);
        scroll.addView(root);

        TextView title = UiHelper.goldTitle(this, "🎨 شخصيتي 3D", 28);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("اسحب لتدوير • قرّب بإصبعين • بدّل الألوان");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 20);
        root.addView(sub);

        // WebView
        webView = new WebView(this);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView v, String url) {
                pageLoaded = true;
                // نطبقو الألوان المحفوظة
                webView.postDelayed(() -> applySaved(), 800);
            }
        });

        LinearLayout.LayoutParams wvLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 950);
        webView.setLayoutParams(wvLp);
        webView.loadUrl("file:///android_asset/avatar/index.html");
        root.addView(webView);

        // أقسام الألوان
        addColorSection(root, "🎨  لون البشرة", new String[]{
                "#F5D0A9", "#E8B98A", "#C68B59", "#8D5524"
        }, "skin");

        addColorSection(root, "👕  لون القميص", new String[]{
                "#1565C0", "#C62828", "#2E7D32", "#6A1B9A", "#F9A825", "#FFFFFF"
        }, "shirt");

        addColorSection(root, "👖  لون البنطال", new String[]{
                "#212121", "#0D47A1", "#4E342E", "#616161", "#1B5E20"
        }, "pants");

        // أزرار
        Button rotateBtn = UiHelper.actionButton(this, "🔄  تدوير تلقائي", "#0D47A1");
        rotateBtn.setOnClickListener(v -> {
            if (pageLoaded) {
                webView.evaluateJavascript(
                        "controls.autoRotate = !controls.autoRotate;", null);
            }
        });
        root.addView(rotateBtn);

        Button saveBtn = UiHelper.primaryButton(this, "💾  حفظ شخصيتي");
        saveBtn.setOnClickListener(v -> save());
        root.addView(saveBtn);

        setContentView(scroll);
        startListener();
    }

    private void addColorSection(LinearLayout root, String title,
                                  final String[] colors, final String field) {
        LinearLayout card = UiHelper.card(this);

        TextView t = new TextView(this);
        t.setText(title);
        t.setTextColor(Color.parseColor("#D4AF37"));
        t.setTextSize(14);
        t.setTypeface(null, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, 0, 0, 14);
        card.addView(t);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        for (String hex : colors) {
            final String color = hex;

            View swatch = new View(this);
            android.graphics.drawable.GradientDrawable g =
                    new android.graphics.drawable.GradientDrawable();
            g.setColor(Color.parseColor(hex));
            g.setCornerRadius(50);
            g.setStroke(3, Color.parseColor("#D4AF37"));
            swatch.setBackground(g);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(110, 110);
            lp.setMargins(8, 0, 8, 0);
            swatch.setLayoutParams(lp);

            swatch.setOnClickListener(v -> applyColor(field, color));
            row.addView(swatch);
        }

        card.addView(row);
        root.addView(card);
    }

    private void applyColor(String field, String hex) {
        if ("skin".equals(field)) current.skinColor = hex;
        else if ("shirt".equals(field)) current.equippedShirtColor = hex;
        else if ("pants".equals(field)) current.equippedPantsColor = hex;

        if (pageLoaded && webView != null) {
            String js = "";
            if ("skin".equals(field)) js = "setSkinColor('" + hex + "')";
            else if ("shirt".equals(field)) js = "setShirtColor('" + hex + "')";
            else if ("pants".equals(field)) js = "setPantsColor('" + hex + "')";
            webView.evaluateJavascript(js, null);
        }
    }

    private void startListener() {
        Citizen c = im.getCitizen();
        if (c == null) { finish(); return; }
        if (reg != null) reg.remove();
        reg = am.listenAvatar(c.nationalId, new AvatarManager.AvatarListener() {
            @Override public void onAvatar(Avatar avatar) {
                current = avatar;
                runOnUiThread(() -> applySaved());
            }
            @Override public void onError(String msg) {}
        });
    }

    private void applySaved() {
        if (!pageLoaded || webView == null) return;

        // نطبّقو كل شي: البشرة + الملابس الملبوسة
        StringBuilder js = new StringBuilder();
        js.append("applyFullAvatar({");

        if (current.skinColor != null && current.skinColor.startsWith("#")) {
            js.append("skin:'").append(current.skinColor).append("',");
        }
        if (current.equippedShirtColor != null && current.equippedShirtColor.startsWith("#")) {
            js.append("shirt:'").append(current.equippedShirtColor).append("',");
            js.append("shirtName:'").append(esc(current.equippedShirtName)).append("',");
        }
        if (current.equippedPantsColor != null && current.equippedPantsColor.startsWith("#")) {
            js.append("pants:'").append(current.equippedPantsColor).append("',");
            js.append("pantsName:'").append(esc(current.equippedPantsName)).append("',");
        }
        if (current.equippedShoesColor != null && current.equippedShoesColor.startsWith("#")) {
            js.append("shoes:'").append(current.equippedShoesColor).append("',");
        }
        if (current.equippedHatColor != null && current.equippedHatColor.startsWith("#")
                && current.equippedHatId != null && !current.equippedHatId.isEmpty()) {
            js.append("hat:'").append(current.equippedHatColor).append("',");
            js.append("hatVisible:true,");
        }
        if (current.equippedGlassesColor != null && current.equippedGlassesColor.startsWith("#")
                && current.equippedGlassesId != null && !current.equippedGlassesId.isEmpty()) {
            js.append("glasses:'").append(current.equippedGlassesColor).append("',");
            js.append("glassesVisible:true,");
        }
        if (current.equippedPhoneId != null && !current.equippedPhoneId.isEmpty()) {
            js.append("phoneVisible:true,");
        }

        js.append("});");

        webView.evaluateJavascript(js.toString(), null);
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("'", "\\'");
    }

    private void save() {
        Citizen c = im.getCitizen();
        if (c == null) return;
        am.saveAvatar(c.nationalId, current, new AvatarManager.OnDone() {
            @Override public void onSuccess() {
                Toast.makeText(AvatarActivity.this, "✅ تم حفظ شخصيتك!", Toast.LENGTH_LONG).show();
            }
            @Override public void onError(String msg) {
                Toast.makeText(AvatarActivity.this, "❌ " + msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) reg.remove();
        if (webView != null) webView.destroy();
    }
}
