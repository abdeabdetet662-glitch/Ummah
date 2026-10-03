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
        title.setText(getString(R.string.citizens_title));
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(26);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText(getString(R.string.citizens_richest));
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 8, 0, 30);
        root.addView(sub);

        Button searchBtn = new Button(this);
        searchBtn.setText(getString(R.string.citizens_search_id));
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
            empty.setText(getString(R.string.citizens_none));
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
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackgroundColor(isMe ? Color.parseColor("#0B4F2C") : Color.parseColor("#141414"));
        card.setPadding(20, 18, 20, 18);
        card.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 12);
        card.setLayoutParams(lp);

        // الصورة
        android.widget.ImageView avatar = new android.widget.ImageView(this);
        LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(120, 120);
        alp.setMargins(0, 0, 20, 0);
        avatar.setLayoutParams(alp);
        avatar.setBackgroundColor(Color.parseColor("#1E1E1E"));
        avatar.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);

        if (c.photoUrl != null && !c.photoUrl.isEmpty()) {
            com.bumptech.glide.Glide.with(this).load(c.photoUrl)
                .placeholder(android.R.drawable.ic_menu_myplaces)
                .circleCrop().into(avatar);
        } else {
            avatar.setImageResource(android.R.drawable.ic_menu_myplaces);
        }
        card.addView(avatar);

        // المعلومات
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView rankView = new TextView(this);
        String rankText;
        if (rank == 1) rankText = "🥇 الأول";
        else if (rank == 2) rankText = getString(R.string.citizens_second);
        else if (rank == 3) rankText = getString(R.string.citizens_third);
        else rankText = "#" + rank;
        rankView.setText(rankText + (isMe ? getString(R.string.citizens_you) : ""));
        rankView.setTextColor(Color.parseColor("#D4AF37"));
        rankView.setTextSize(11);
        rankView.setTypeface(null, Typeface.BOLD);
        info.addView(rankView);

        TextView name = new TextView(this);
        name.setText(c.name != null ? c.name : "مجهول");
        name.setTextColor(Color.WHITE);
        name.setTextSize(16);
        name.setTypeface(null, Typeface.BOLD);
        name.setPadding(0, 4, 0, 4);
        info.addView(name);

        TextView balance = new TextView(this);
        balance.setText("💰 " + c.balance + " Đ");
        balance.setTextColor(Color.parseColor("#D4AF37"));
        balance.setTextSize(14);
        info.addView(balance);

        card.addView(info);

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
            .setTitle(getString(R.string.citizens_search_hint))
            .setView(c)
            .setPositiveButton(getString(R.string.citizens_search), (d, w) -> {
                String id = input.getText().toString().trim();
                if (id.isEmpty()) return;
                fm.searchCitizenByExactId(id, new FirebaseManager.CitizenLookup() {
                    @Override public void onFound(FirebaseManager.CitizenItem item) {
                        new AlertDialog.Builder(CitizensActivity.this)
                            .setTitle(getString(R.string.citizens_found))
                            .setMessage("الاسم: " + item.name + "\n" +
                                    "الرصيد: " + item.balance + " Đ\n" +
                                    "الانضمام: " + item.joinDate)
                            .setPositiveButton(getString(R.string.citizens_send_dinar), (d2, w2) -> {
                                Intent i = new Intent(CitizensActivity.this, TransferActivity.class);
                                i.putExtra("prefill_id", item.nationalId);
                                startActivity(i);
                            })
                            .setNegativeButton("إغلاق", null)
                            .show();
                    }
                    @Override public void onNotFound() {
                        Toast.makeText(CitizensActivity.this, getString(R.string.citizens_not_found), Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }
}
