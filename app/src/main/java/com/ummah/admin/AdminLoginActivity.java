package com.ummah.admin;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AdminLoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        // إذا مسجّل الدخول مسبقاً
        SharedPreferences prefs = getSharedPreferences("admin", MODE_PRIVATE);
        if (prefs.getBoolean("logged_in", false)) {
            startActivity(new Intent(this, AdminMainActivity.class));
            finish();
            return;
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.parseColor("#0A0A0A"));
        root.setPadding(60, 80, 60, 80);

        TextView icon = new TextView(this);
        icon.setText("👑");
        icon.setTextSize(80);
        icon.setGravity(Gravity.CENTER);
        root.addView(icon);

        TextView title = new TextView(this);
        title.setText("لوحة تحكم أُمّة");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 20, 0, 10);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("أدخل كلمة السر للدخول");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(14);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 40);
        root.addView(sub);

        final EditText input = new EditText(this);
        input.setHint("كلمة السر");
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.GRAY);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setGravity(Gravity.CENTER);
        input.setBackgroundColor(Color.parseColor("#141414"));
        input.setPadding(30, 30, 30, 30);
        root.addView(input);

        Button login = new Button(this);
        login.setText("🔓 دخول");
        login.setTextSize(16);
        login.setPadding(40, 30, 40, 30);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 30, 0, 0);
        login.setLayoutParams(lp);
        login.setOnClickListener(v -> {
            String pass = input.getText().toString().trim();
            if (pass.equals(AdminManager.ADMIN_PASSWORD)) {
                getSharedPreferences("admin", MODE_PRIVATE)
                    .edit().putBoolean("logged_in", true).apply();
                startActivity(new Intent(this, AdminMainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "❌ كلمة السر خطأ", Toast.LENGTH_SHORT).show();
            }
        });
        root.addView(login);

        TextView warn = new TextView(this);
        warn.setText("\n⚠️ هذه اللوحة سرية\nلا تشاركها مع أحد");
        warn.setTextColor(Color.parseColor("#F44336"));
        warn.setTextSize(11);
        warn.setGravity(Gravity.CENTER);
        warn.setPadding(0, 40, 0, 0);
        root.addView(warn);

        setContentView(root);
    }
}
