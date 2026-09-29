package com.ummah.app;

import android.app.Activity;
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

public class AvatarActivity extends Activity {

    private IdentityManager im;
    private AvatarManager am;
    private AvatarGLSurfaceView glView;
    private Avatar current;
    private ListenerRegistration reg;

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
        sub.setText("اسحب لتدوير الشخصية • قرّب بإصبعين");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 20);
        root.addView(sub);

        // GLSurfaceView
        glView = new AvatarGLSurfaceView(this);
        LinearLayout.LayoutParams glLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 900);
        glView.setLayoutParams(glLp);
        root.addView(glView);

        // ألوان البشرة
        addColorSection(root, "🎨  لون البشرة", new String[]{
                "#F5D0A9", "#E8B98A", "#C68B59", "#8D5524"
        }, "skin");

        // ألوان القميص
        addColorSection(root, "👕  لون القميص", new String[]{
                "#1565C0", "#C62828", "#2E7D32", "#6A1B9A", "#F9A825", "#FFFFFF"
        }, "shirt");

        // ألوان البنطال
        addColorSection(root, "👖  لون البنطال", new String[]{
                "#212121", "#0D47A1", "#4E342E", "#616161", "#1B5E20"
        }, "pants");

        // ألوان الشعر
        addColorSection(root, "💇  لون الشعر", new String[]{
                "#1A1A1A", "#4E342E", "#F9A825", "#C62828", "#E0E0E0"
        }, "hair");

        // أزرار
        Button rotateBtn = UiHelper.actionButton(this, "🔄  تدوير تلقائي", "#0D47A1");
        rotateBtn.setOnClickListener(v -> {
            if (glView != null) glView.renderer.autoRotate = !glView.renderer.autoRotate;
        });
        root.addView(rotateBtn);

        Button saveBtn = UiHelper.primaryButton(this, "💾  حفظ شخصيتي");
        saveBtn.setOnClickListener(v -> save());
        root.addView(saveBtn);

        setContentView(scroll);
        startListener();
    }

    private void addColorSection(LinearLayout root, String title, final String[] colors, final String field) {
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
            android.graphics.drawable.GradientDrawable g = new android.graphics.drawable.GradientDrawable();
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
        int c = Color.parseColor(hex);
        float r = Color.red(c) / 255f;
        float g = Color.green(c) / 255f;
        float bl = Color.blue(c) / 255f;

        float[] rgba = {r, g, bl};

        if ("skin".equals(field)) {
            current.skinColor = hex;
            if (glView != null) glView.renderer.skinColor = rgba;
        } else if ("shirt".equals(field)) {
            current.shirtColor = hex;
            if (glView != null) glView.renderer.shirtColor = rgba;
        } else if ("pants".equals(field)) {
            current.pantsColor = hex;
            if (glView != null) glView.renderer.pantsColor = rgba;
        } else if ("hair".equals(field)) {
            current.hairColor = hex;
            if (glView != null) glView.renderer.hairColor = rgba;
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
        if (current.skinColor != null && current.skinColor.startsWith("#")) {
            applyColorToGL("skin", current.skinColor);
        }
        if (current.shirtColor != null && current.shirtColor.startsWith("#")) {
            applyColorToGL("shirt", current.shirtColor);
        }
        if (current.pantsColor != null && current.pantsColor.startsWith("#")) {
            applyColorToGL("pants", current.pantsColor);
        }
        if (current.hairColor != null && current.hairColor.startsWith("#")) {
            applyColorToGL("hair", current.hairColor);
        }
    }

    private void applyColorToGL(String field, String hex) {
        if (glView == null || glView.renderer == null) return;
        int c = Color.parseColor(hex);
        float[] rgba = {
                Color.red(c) / 255f,
                Color.green(c) / 255f,
                Color.blue(c) / 255f
        };
        if ("skin".equals(field)) glView.renderer.skinColor = rgba;
        else if ("shirt".equals(field)) glView.renderer.shirtColor = rgba;
        else if ("pants".equals(field)) glView.renderer.pantsColor = rgba;
        else if ("hair".equals(field)) glView.renderer.hairColor = rgba;
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
    protected void onResume() {
        super.onResume();
        if (glView != null) glView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (glView != null) glView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) reg.remove();
    }
}
