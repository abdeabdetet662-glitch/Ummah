package com.ummah.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * NewTicketActivity — إنشاء تذكرة جديدة
 */
public class NewTicketActivity extends Activity {

    private IdentityManager im;
    private Citizen me;
    private SupportManager manager;

    private EditText etSubject, etMessage;
    private String selectedCategory = SupportTicket.CAT_OTHER;
    private String selectedPriority = SupportTicket.PRIORITY_MEDIUM;

    private LinearLayout categoriesContainer;
    private LinearLayout priorityContainer;

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        im = new IdentityManager(this);
        me = im.getCitizen();
        manager = new SupportManager();

        if (me == null) { finish(); return; }
        buildUI();
    }

    private void buildUI() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(50), dp(20), dp(50));
        scroll.addView(root);

        // Back
        TextView back = new TextView(this);
        back.setText("← عودة");
        back.setTextColor(Color.parseColor("#D4AF37"));
        back.setTextSize(14);
        back.setTypeface(null, Typeface.BOLD);
        back.setOnClickListener(v -> finish());
        root.addView(back);

        // Icon
        TextView icon = new TextView(this);
        icon.setText("📝");
        icon.setTextSize(60);
        icon.setGravity(Gravity.CENTER);
        icon.setPadding(0, dp(16), 0, dp(8));
        root.addView(icon);

        // Title
        TextView title = new TextView(this);
        title.setText("طلب جديد");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(26);
        title.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("سيرد عليك فريق ديوان أُمّة في أقرب وقت");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, dp(6), 0, dp(24));
        root.addView(sub);

        // Category
        addLabel("📂 الفئة");
        categoriesContainer = new LinearLayout(this);
        categoriesContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(categoriesContainer);
        renderCategories();

        // Priority
        addLabel("⚡ الأولوية");
        priorityContainer = new LinearLayout(this);
        priorityContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(priorityContainer);
        renderPriorities();

        // Subject
        addLabel("📌 الموضوع");
        etSubject = new EditText(this);
        etSubject.setHint("اكتب موضوع مختصر...");
        etSubject.setHintTextColor(Color.parseColor("#666666"));
        etSubject.setTextColor(Color.WHITE);
        etSubject.setTextSize(14);
        etSubject.setBackgroundResource(R.drawable.bg_input);
        etSubject.setPadding(dp(14), dp(12), dp(14), dp(12));
        root.addView(etSubject);

        // Message
        addLabel("💬 التفاصيل");
        etMessage = new EditText(this);
        etMessage.setHint("اشرح مشكلتك بالتفصيل...");
        etMessage.setHintTextColor(Color.parseColor("#666666"));
        etMessage.setTextColor(Color.WHITE);
        etMessage.setTextSize(14);
        etMessage.setBackgroundResource(R.drawable.bg_input);
        etMessage.setPadding(dp(14), dp(12), dp(14), dp(12));
        etMessage.setMinLines(4);
        etMessage.setGravity(Gravity.TOP);
        LinearLayout.LayoutParams ml = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(120));
        etMessage.setLayoutParams(ml);
        root.addView(etMessage);

        // Submit
        Button submit = new Button(this);
        submit.setText("📤 إرسال الطلب");
        submit.setTextSize(15);
        submit.setTextColor(Color.parseColor("#0a0510"));
        submit.setAllCaps(false);
        submit.setTypeface(null, Typeface.BOLD);
        submit.setBackgroundResource(R.drawable.bg_btn_gold);
        LinearLayout.LayoutParams sl = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        sl.setMargins(0, dp(24), 0, 0);
        submit.setLayoutParams(sl);
        submit.setOnClickListener(v -> submit());
        root.addView(submit);

        setContentView(scroll);
    }

    private void addLabel(String text) {
        LinearLayout parent = (LinearLayout) ((ScrollView)
                ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0)).getChildAt(0);
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextColor(Color.parseColor("#D4AF37"));
        label.setTextSize(13);
        label.setTypeface(null, Typeface.BOLD);
        label.setPadding(0, dp(16), 0, dp(8));
        parent.addView(label);
    }

    private void renderCategories() {
        categoriesContainer.removeAllViews();

        String[][] cats = {
                {SupportTicket.CAT_TRANSFER, "💰 التحويلات"},
                {SupportTicket.CAT_MARKET, "🛒 السوق"},
                {SupportTicket.CAT_ACCOUNT, "👤 الحساب"},
                {SupportTicket.CAT_TECHNICAL, "🔧 مشكلة تقنية"},
                {SupportTicket.CAT_OTHER, "📌 أخرى"}
        };

        for (String[] c : cats) {
            addChip(categoriesContainer, c[0], c[1], c[0].equals(selectedCategory),
                    () -> { selectedCategory = c[0]; renderCategories(); });
        }
    }

    private void renderPriorities() {
        priorityContainer.removeAllViews();

        String[][] pris = {
                {SupportTicket.PRIORITY_LOW, "🟢 منخفضة"},
                {SupportTicket.PRIORITY_MEDIUM, "🟡 عادية"},
                {SupportTicket.PRIORITY_HIGH, "🔴 عاجلة"}
        };

        for (String[] p : pris) {
            addChip(priorityContainer, p[0], p[1], p[0].equals(selectedPriority),
                    () -> { selectedPriority = p[0]; renderPriorities(); });
        }
    }

    private void addChip(LinearLayout parent, String id, String label,
                          boolean selected, Runnable onClick) {
        TextView chip = new TextView(this);
        chip.setText((selected ? "✅ " : "○ ") + label);
        chip.setTextColor(selected ? Color.parseColor("#0a0510") : Color.WHITE);
        chip.setTextSize(13);
        chip.setTypeface(null, Typeface.BOLD);
        chip.setGravity(Gravity.CENTER_VERTICAL);
        chip.setPadding(dp(16), dp(12), dp(16), dp(12));

        if (selected) {
            chip.setBackgroundResource(R.drawable.bg_btn_gold);
        } else {
            chip.setBackgroundResource(R.drawable.bg_card);
        }

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(8));
        chip.setLayoutParams(lp);

        chip.setOnClickListener(v -> onClick.run());
        parent.addView(chip);
    }

    private void submit() {
        String subject = etSubject.getText().toString().trim();
        String message = etMessage.getText().toString().trim();

        if (subject.isEmpty()) {
            Toast.makeText(this, "⚠️ اكتب الموضوع", Toast.LENGTH_SHORT).show();
            return;
        }
        if (message.length() < 10) {
            Toast.makeText(this, "⚠️ اشرح المشكلة (10 أحرف على الأقل)", Toast.LENGTH_SHORT).show();
            return;
        }

        manager.createTicket(me, subject, selectedCategory, selectedPriority, message,
            new SupportManager.SimpleCallback() {
                @Override
                public void onSuccess(String ticketId) {
                    runOnUiThread(() -> {
                        Toast.makeText(NewTicketActivity.this,
                                "✅ تم إرسال طلبك\nسيرد عليك الديوان قريباً",
                                Toast.LENGTH_LONG).show();
                        // ننتقل للشات مباشرة
                        startActivity(new Intent(NewTicketActivity.this, SupportChatActivity.class)
                                .putExtra("ticket_id", ticketId)
                                .putExtra("is_admin", false));
                        finish();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> Toast.makeText(NewTicketActivity.this,
                            "❌ " + error, Toast.LENGTH_LONG).show());
                }
            });
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }
}
