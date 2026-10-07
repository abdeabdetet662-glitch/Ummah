package com.ummah.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * SupportActivity — ديوان أُمّة (الدعم)
 */
public class SupportActivity extends Activity {

    private IdentityManager im;
    private Citizen me;
    private SupportManager manager;

    private LinearLayout ticketsContainer;
    private ListenerRegistration ticketsReg;

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
        loadTickets();
    }

    private void buildUI() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(50), dp(20), dp(50));
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        scroll.addView(root);

        // Back
        TextView back = new TextView(this);
        back.setText("← عودة");
        back.setTextColor(Color.parseColor("#D4AF37"));
        back.setTextSize(14);
        back.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        blp.gravity = Gravity.START;
        back.setLayoutParams(blp);
        back.setOnClickListener(v -> finish());
        root.addView(back);

        // Icon
        TextView icon = new TextView(this);
        icon.setText("🏛️");
        icon.setTextSize(70);
        icon.setGravity(Gravity.CENTER);
        icon.setPadding(0, dp(20), 0, dp(8));
        root.addView(icon);

        // Title
        TextView title = new TextView(this);
        title.setText("ديوان أُمّة");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(28);
        title.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        // Subtitle
        TextView sub = new TextView(this);
        sub.setText("صوتك مسموع — نحن هنا لأجلك");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, dp(6), 0, dp(24));
        root.addView(sub);

        // New Ticket Button
        Button newBtn = new Button(this);
        newBtn.setText("➕ إرسال طلب جديد");
        newBtn.setTextSize(15);
        newBtn.setTextColor(Color.parseColor("#0a0510"));
        newBtn.setAllCaps(false);
        newBtn.setTypeface(null, Typeface.BOLD);
        newBtn.setBackgroundResource(R.drawable.bg_btn_gold);
        LinearLayout.LayoutParams nl = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        nl.setMargins(0, 0, 0, dp(24));
        newBtn.setLayoutParams(nl);
        newBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, NewTicketActivity.class));
        });
        root.addView(newBtn);

        // Tickets Title
        TextView tl = new TextView(this);
        tl.setText("═══ تذاكري ═══");
        tl.setTextColor(Color.parseColor("#D4AF37"));
        tl.setTextSize(14);
        tl.setTypeface(null, Typeface.BOLD);
        tl.setGravity(Gravity.CENTER);
        tl.setPadding(0, dp(8), 0, dp(16));
        root.addView(tl);

        // Container
        ticketsContainer = new LinearLayout(this);
        ticketsContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(ticketsContainer);

        setContentView(scroll);
    }

    private void loadTickets() {
        ticketsReg = manager.listenMyTickets(me.nationalId, tickets -> {
            runOnUiThread(() -> renderTickets(tickets));
        });
    }

    private void renderTickets(List<SupportTicket> list) {
        ticketsContainer.removeAllViews();

        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("📭 لا توجد تذاكر\nاضغط 'إرسال طلب جديد' للتواصل");
            empty.setTextColor(Color.parseColor("#666666"));
            empty.setTextSize(13);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(30), 0, dp(30));
            ticketsContainer.addView(empty);
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.US);

        for (SupportTicket t : list) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.bg_card);
            card.setPadding(dp(16), dp(14), dp(16), dp(14));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, dp(10));
            card.setLayoutParams(lp);

            // Row 1: Subject + Status
            LinearLayout row1 = new LinearLayout(this);
            row1.setOrientation(LinearLayout.HORIZONTAL);
            row1.setGravity(Gravity.CENTER_VERTICAL);

            TextView subject = new TextView(this);
            subject.setText("📌 " + t.subject);
            subject.setTextColor(Color.WHITE);
            subject.setTextSize(14);
            subject.setTypeface(null, Typeface.BOLD);
            subject.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            row1.addView(subject);

            TextView status = new TextView(this);
            status.setText(t.getStatusAr());
            status.setTextColor(Color.parseColor(t.getStatusColor()));
            status.setTextSize(10);
            status.setTypeface(null, Typeface.BOLD);
            status.setPadding(dp(8), dp(4), dp(8), dp(4));
            status.setBackgroundResource(R.drawable.bg_card);
            row1.addView(status);

            card.addView(row1);

            // Row 2: Category + Priority
            TextView info = new TextView(this);
            info.setText(t.getCategoryAr() + " • " + t.getPriorityAr());
            info.setTextColor(Color.parseColor("#888888"));
            info.setTextSize(11);
            info.setPadding(0, dp(6), 0, dp(4));
            card.addView(info);

            // Row 3: Last message
            if (t.lastMessage != null && !t.lastMessage.isEmpty()) {
                TextView msg = new TextView(this);
                String preview = t.lastMessage;
                if (preview.length() > 60) preview = preview.substring(0, 60) + "...";
                msg.setText(preview);
                msg.setTextColor(Color.parseColor("#CCCCCC"));
                msg.setTextSize(12);
                msg.setPadding(0, dp(4), 0, dp(4));
                card.addView(msg);
            }

            // Row 4: Date
            TextView date = new TextView(this);
            date.setText("🕐 " + sdf.format(new Date(t.lastMessageAt > 0 ? t.lastMessageAt : t.createdAt)));
            date.setTextColor(Color.parseColor("#666666"));
            date.setTextSize(10);
            card.addView(date);

            final String tid = t.id;
            card.setOnClickListener(v -> {
                startActivity(new Intent(this, SupportChatActivity.class)
                        .putExtra("ticket_id", tid)
                        .putExtra("is_admin", false));
            });
            card.setClickable(true);
            card.setFocusable(true);

            ticketsContainer.addView(card);
        }
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }

    @Override
    protected void onDestroy() {
        if (ticketsReg != null) ticketsReg.remove();
        super.onDestroy();
    }
}
