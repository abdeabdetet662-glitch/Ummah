package com.ummah.app.heist;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ummah.app.LocaleHelper;
import com.ummah.app.R;

/**
 * HeistGameActivity — المحرك الرئيسي
 * (Placeholder — سنبنيه في المرحلة 3)
 */
public class HeistGameActivity extends Activity {

    private String gameId;
    private HeistRole myRole;

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        gameId = getIntent().getStringExtra("game_id");
        myRole = HeistRole.fromId(getIntent().getStringExtra("my_role"));

        FrameLayout container = new FrameLayout(this);
        container.setBackgroundResource(R.drawable.bg_heist);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(30), dp(30), dp(30), dp(30));

        TextView icon = new TextView(this);
        icon.setText("🚧");
        icon.setTextSize(100);
        icon.setGravity(Gravity.CENTER);
        root.addView(icon);

        TextView title = new TextView(this);
        title.setText("المحرك قادم قريباً");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(20), 0, dp(10));
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("المرحلة 3: Top-Down 2D Gameplay\n\nالآن خلصنا:\n✅ Lobby\n✅ Story\n✅ Planning\n\nالباقي:\n⏳ الحركة والإطلاق\n⏳ الشرطة AI\n⏳ المال والخزنة\n⏳ الهروب");
        sub.setTextColor(Color.parseColor("#CCCCCC"));
        sub.setTextSize(14);
        sub.setGravity(Gravity.CENTER);
        sub.setLineSpacing(0, 1.5f);
        root.addView(sub);

        Button back = new Button(this);
        back.setText("← رجوع للبداية");
        back.setTextColor(Color.parseColor("#0a0510"));
        back.setAllCaps(false);
        back.setTypeface(null, Typeface.BOLD);
        back.setBackgroundResource(R.drawable.bg_heist_btn_gold);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        blp.setMargins(0, dp(40), 0, 0);
        back.setLayoutParams(blp);
        back.setOnClickListener(v -> {
            Toast.makeText(this, "🎬 المحرك في المرحلة 3", Toast.LENGTH_SHORT).show();
            finish();
        });
        root.addView(back);

        container.addView(root);
        setContentView(container);
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }
}
