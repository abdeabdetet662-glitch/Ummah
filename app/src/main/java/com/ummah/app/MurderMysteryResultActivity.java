package com.ummah.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * MurderMysteryResultActivity — كشف الحقيقة
 */
public class MurderMysteryResultActivity extends Activity {

    private FirebaseFirestore db;
    private String gameId;

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        db = FirebaseFirestore.getInstance();
        gameId = getIntent().getStringExtra("gameId");

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_mystery_noir);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(24), dp(80), dp(24), dp(40));
        scroll.addView(root);

        TextView icon = new TextView(this);
        icon.setText("🎭");
        icon.setTextSize(100);
        icon.setGravity(Gravity.CENTER);
        root.addView(icon);
        icon.startAnimation(AnimationUtils.loadAnimation(this, R.anim.mystery_zoom_in));

        TextView title = new TextView(this);
        title.setText("انكشفت الحقيقة!");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(30);
        title.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(24), 0, dp(16));
        root.addView(title);

        TextView details = new TextView(this);
        details.setTextColor(Color.WHITE);
        details.setTextSize(16);
        details.setGravity(Gravity.CENTER);
        details.setLineSpacing(0, 1.5f);
        details.setPadding(dp(20), dp(24), dp(20), dp(24));
        details.setBackgroundResource(R.drawable.bg_mystery_card);
        root.addView(details);

        // جلب النتائج
        db.collection("murder_mysteries").document(gameId).get()
            .addOnSuccessListener(doc -> {
                MurderMystery m = doc.toObject(MurderMystery.class);
                if (m == null) return;

                StringBuilder sb = new StringBuilder();
                sb.append("🎭 القاتل كان:\n");
                sb.append("👤 ").append(m.killerId != null ? m.killerId : "مجهول").append("\n\n");

                if (m.solved) {
                    sb.append("✅ المحققون نجحوا في كشفه!\n");
                    sb.append("💰 الجائزة ذهبت للفائز");
                } else {
                    sb.append("❌ المحققون فشلوا!\n");
                    sb.append("💰 القاتل أخذ الجائزة");
                }

                details.setText(sb.toString());
            });

        TextView closeBtn = new TextView(this);
        closeBtn.setText("← إغلاق");
        closeBtn.setTextColor(Color.parseColor("#D4AF37"));
        closeBtn.setTextSize(16);
        closeBtn.setTypeface(null, Typeface.BOLD);
        closeBtn.setGravity(Gravity.CENTER);
        closeBtn.setPadding(0, dp(40), 0, 0);
        closeBtn.setOnClickListener(v -> finish());
        root.addView(closeBtn);

        setContentView(scroll);
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }
}
