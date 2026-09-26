package com.ummah.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class CitizensActivity extends Activity {
    private FirebaseManager fm;
    private IdentityManager im;
    private LinearLayout listContainer;
    private ListenerRegistration reg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        fm = FirebaseManager.get();
        im = new IdentityManager(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0A"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(36, 50, 36, 50);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("👥 دليل المواطنين");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(26);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("أغنى 20 مواطناً في الدولة");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 8, 0, 30);
        root.addView(sub);

        Button searchBtn = new Button(this);
        searchBtn.setText("🔍  ابحث برقم وطني محدد");
        searchBtn.setTextSize(14);
        searchBtn.setOnClickListener(v -> showSearchDialog());
        root.addView(searchBtn);

        listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        listContainer.setPadding(0, 24, 0, 0);
        root.addView(listContainer);

        setContentView(scroll);
        startListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) reg.remove();
    }

    private void startListener() {
        reg = fm.listenTopCitizens(20, list ->
            runOnUiThread(() -> refreshList(list)));
    }

    private void refreshList(List<FirebaseManager.CitizenItem> list) {
        listContainer.removeAllViews();
        Citizen me = im.getCitizen();

        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("لا يوجد مواطنون بعد.");
            empty.setTextColor(Color.parseColor("#616161"));
            empty.setTextSize(14);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 40, 0, 0);
            listContainer.addView(empty);
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            FirebaseManager.CitizenItem c = list.get(i);
            boolean isMe = me != null && me.nationalId.equals(c.nationalId);
            listContainer.addView(buildCard(c, i + 1, isMe));
        }
    }

    private LinearLayout buildCard(final FirebaseManager.CitizenItem c, int rank, boolean isMe) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(isMe ? Color.parseColor("#0B4F2C") : Color.parseColor("#141414"));
        card.setPadding(30, 22, 30, 22);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 12);
        card.setLayoutParams(lp);

        TextView rankView = new TextView(this);
        String rankText;
        if (rank == 1) rankText = "🥇 الأول";
        else if (rank == 2) rankText = "🥈 الثاني";
        else if (rank == 3) rankText = "🥉 الثالث";
        else rankText = "#" + rank;
        rankView.setText(rankText + (isMe ? "  (أنت)" : ""));
        rankView.setTextColor(Color.parseColor("#D4AF37"));
        rankView.setTextSize(12);
        rankView.setTypeface(null, Typeface.BOLD);
        card.addView(rankView);

        TextView name = new TextView(this);
        name.setText("👤 " + (c.name != null ? c.name : "مجهول"));
        name.setTextColor(Color.WHITE);
        name.setTextSize(18);
        name.setTypeface(null, Typeface.BOLD);
        name.setPadding(0, 6, 0, 4);
        card.addView(name);

        TextView balance = new TextView(this);
        balance.setText("💰 " + c.balance + " Đ");
        balance.setTextColor(Color.parseColor("#D4AF37"));
        balance.setTextSize(16);
        balance.setPadding(0, 4, 0, 8);
        card.addView(balance);

        TextView idView = new TextView(this);
        idView.setText(c.nationalId);
        idView.setTextColor(Color.parseColor("#616161"));
        idView.setTextSize(10);
        idView.setTypeface(Typeface.MONOSPACE);
        card.addView(idView);

        if (!isMe) {
            Button chatBtn = new Button(this);
            chatBtn.setText("💬  دردشة خاصة");
            chatBtn.setTextSize(13);
            chatBtn.setOnClickListener(v -> {
                Intent i = new Intent(CitizensActivity.this, PrivateChatActivity.class);
                i.putExtra("other_id", c.nationalId);
                i.putExtra("other_name", c.name);
                startActivity(i);
            });
            card.addView(chatBtn);

            Button sendBtn = new Button(this);
            sendBtn.setText("💸  إرسال دينار له");
            sendBtn.setTextSize(13);
            sendBtn.setOnClickListener(v -> {
                Intent i = new Intent(CitizensActivity.this, TransferActivity.class);
                i.putExtra("prefill_id", c.nationalId);
                startActivity(i);
            });
            card.addView(sendBtn);
        }

        return card;
    }

    private void showSearchDialog() {
        final EditText input = new EditText(this);
        input.setHint("الرقم الوطني (UMM-XXXX-XXXX-XXXX)");
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.GRAY);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        LinearLayout c = new LinearLayout(this);
        c.setPadding(40, 20, 40, 20);
        c.addView(input);

        new AlertDialog.Builder(this)
            .setTitle("ابحث عن مواطن")
            .setView(c)
            .setPositiveButton("بحث", (d, w) -> {
                String id = input.getText().toString().trim();
                if (id.isEmpty()) return;
                fm.searchCitizenByExactId(id, new FirebaseManager.CitizenLookup() {
                    @Override public void onFound(FirebaseManager.CitizenItem item) {
                        new AlertDialog.Builder(CitizensActivity.this)
                            .setTitle("تم العثور")
                            .setMessage("الاسم: " + item.name + "\n" +
                                    "الرصيد: " + item.balance + " Đ\n" +
                                    "الانضمام: " + item.joinDate)
                            .setPositiveButton("إرسال دينار", (d2, w2) -> {
                                Intent i = new Intent(CitizensActivity.this, TransferActivity.class);
                                i.putExtra("prefill_id", item.nationalId);
                                startActivity(i);
                            })
                            .setNegativeButton("إغلاق", null)
                            .show();
                    }
                    @Override public void onNotFound() {
                        Toast.makeText(CitizensActivity.this, "❌ غير موجود", Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }
}
