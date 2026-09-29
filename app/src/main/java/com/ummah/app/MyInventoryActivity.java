package com.ummah.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class MyInventoryActivity extends Activity {

    private static final String PREFS = "inventory_prefs";
    private static final String KEY_CAT = "cat";

    private IdentityManager im;
    private MarketManager mm;
    private FirebaseFirestore db;
    private LinearLayout itemsContainer;
    private LinearLayout tabsContainer;
    private ListenerRegistration reg;
    private String currentCategory = "all";
    private SharedPreferences prefs;
    private List<MarketItem> allItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        im = new IdentityManager(this);
        mm = new MarketManager();
        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        currentCategory = prefs.getString(KEY_CAT, "all");

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 50, 30, 60);
        scroll.addView(root);

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER);
        header.setPadding(0, 0, 0, 20);

        TextView icon = new TextView(this);
        icon.setText("🎒");
        icon.setTextSize(36);
        header.addView(icon);

        TextView title = new TextView(this);
        title.setText("  ممتلكاتي");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(26);
        title.setTypeface(null, Typeface.BOLD);
        header.addView(title);

        root.addView(header);

        TextView sub = new TextView(this);
        sub.setText("كل ما اشتريته من السوق");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 30);
        root.addView(sub);

        // Tabs
        tabsContainer = new LinearLayout(this);
        tabsContainer.setOrientation(LinearLayout.HORIZONTAL);
        tabsContainer.setGravity(Gravity.CENTER);
        tabsContainer.setPadding(0, 0, 0, 24);
        root.addView(tabsContainer);

        buildTabs();

        // Container
        itemsContainer = new LinearLayout(this);
        itemsContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(itemsContainer);

        setContentView(scroll);

        startListener();
    }

    private void buildTabs() {
        tabsContainer.removeAllViews();
        addTab("الكل", "all");
        addTab("🚗", "vehicle");
        addTab("🏠", "property");
        addTab("📱", "electronics");
        addTab("👕", "clothing");
    }

    private void addTab(String label, final String category) {
        boolean active = category.equals(currentCategory);

        TextView tab = new TextView(this);
        tab.setText(label);
        tab.setTextSize(13);
        tab.setTypeface(null, Typeface.BOLD);
        tab.setGravity(Gravity.CENTER);
        tab.setPadding(30, 20, 30, 20);
        tab.setBackgroundResource(active ? R.drawable.bg_btn_gold_hero : R.drawable.bg_btn_outline);
        tab.setTextColor(active ? Color.parseColor("#0A0A0A") : Color.parseColor("#D4AF37"));
        tab.setElevation(active ? 8f : 0f);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(4, 0, 4, 0);
        tab.setLayoutParams(lp);

        tab.setOnClickListener(v -> {
            if (category.equals(currentCategory)) return;
            currentCategory = category;
            prefs.edit().putString(KEY_CAT, category).apply();
            buildTabs();
            renderItems();
        });

        tabsContainer.addView(tab);
    }

    private void startListener() {
        Citizen c = im.getCitizen();
        if (c == null) { finish(); return; }

        if (reg != null) reg.remove();
        reg = mm.listenMyInventory(c.nationalId, new MarketManager.ItemsListener() {
            @Override public void onItems(List<MarketItem> items) {
                allItems = items;
                runOnUiThread(() -> renderItems());
            }
            @Override public void onError(String msg) {
                runOnUiThread(() -> {
                    itemsContainer.removeAllViews();
                    TextView err = new TextView(MyInventoryActivity.this);
                    err.setText("خطأ: " + msg);
                    err.setTextColor(Color.parseColor("#CF6679"));
                    err.setGravity(Gravity.CENTER);
                    itemsContainer.addView(err);
                });
            }
        });
    }

    private void renderItems() {
        itemsContainer.removeAllViews();

        // فلترة
        List<MarketItem> filtered = new ArrayList<>();
        for (MarketItem item : allItems) {
            if ("all".equals(currentCategory) ||
                    (item.category != null && item.category.equals(currentCategory))) {
                filtered.add(item);
            }
        }

        if (filtered.isEmpty()) {
            LinearLayout emptyBox = new LinearLayout(this);
            emptyBox.setOrientation(LinearLayout.VERTICAL);
            emptyBox.setGravity(Gravity.CENTER);
            emptyBox.setPadding(0, 80, 0, 0);

            TextView icon = new TextView(this);
            icon.setText("🎒");
            icon.setTextSize(60);
            icon.setGravity(Gravity.CENTER);
            emptyBox.addView(icon);

            TextView empty = new TextView(this);
            empty.setText("ما عندك حتى منتج بعد");
            empty.setTextColor(Color.parseColor("#D4AF37"));
            empty.setTextSize(16);
            empty.setTypeface(null, Typeface.BOLD);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 20, 0, 6);
            emptyBox.addView(empty);

            TextView hint = new TextView(this);
            hint.setText("روح للسوق واشتري!");
            hint.setTextColor(Color.parseColor("#9E9E9E"));
            hint.setTextSize(13);
            hint.setGravity(Gravity.CENTER);
            emptyBox.addView(hint);

            itemsContainer.addView(emptyBox);
            return;
        }

        for (MarketItem item : filtered) {
            itemsContainer.addView(buildItemCard(item));
        }
    }

    private View buildItemCard(final MarketItem item) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackgroundResource(R.drawable.bg_card_premium);
        card.setPadding(24, 24, 24, 24);
        card.setElevation(6f);
        card.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 8, 0, 8);
        card.setLayoutParams(lp);

        // صورة
        ImageView image = new ImageView(this);
        LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(220, 220);
        image.setLayoutParams(imgLp);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setBackgroundColor(Color.parseColor("#1A1A1A"));

        com.bumptech.glide.Glide.with(this)
                .load(item.imageUrl)
                .placeholder(R.drawable.ic_ummah)
                .into(image);

        card.addView(image);

        // معلومات
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(24, 0, 0, 0);
        LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        info.setLayoutParams(infoLp);

        TextView name = new TextView(this);
        name.setText(item.name);
        name.setTextColor(Color.WHITE);
        name.setTextSize(17);
        name.setTypeface(null, Typeface.BOLD);
        info.addView(name);

        TextView brand = new TextView(this);
        brand.setText(item.brand);
        brand.setTextColor(Color.parseColor("#9E9E9E"));
        brand.setTextSize(11);
        brand.setPadding(0, 4, 0, 8);
        info.addView(brand);

        TextView price = new TextView(this);
        price.setText("💰 " + item.price + " Đ");
        price.setTextColor(Color.parseColor("#D4AF37"));
        price.setTextSize(16);
        price.setTypeface(null, Typeface.BOLD);
        info.addView(price);

        card.addView(info);

        // زر ارتداء (للملابس والهواتف)
        String wearType = getWearType(item);
        if (wearType != null && !wearType.isEmpty()) {
            Button wearBtn = new Button(this);
            wearBtn.setText("👕 البس");
            wearBtn.setTextSize(12);
            wearBtn.setTextColor(Color.parseColor("#0A0A0A"));
            wearBtn.setTypeface(null, Typeface.BOLD);
            wearBtn.setBackgroundResource(R.drawable.bg_btn_gold_hero);
            LinearLayout.LayoutParams wearLp = new LinearLayout.LayoutParams(180, 180);
            wearLp.setMargins(0, 0, 8, 0);
            wearBtn.setLayoutParams(wearLp);
            wearBtn.setOnClickListener(v -> wearItem(item, wearType));
            card.addView(wearBtn);
        }

        // زر بيع
        Button sellBtn = new Button(this);
        sellBtn.setText("بِع");
        sellBtn.setTextSize(13);
        sellBtn.setTextColor(Color.WHITE);
        sellBtn.setTypeface(null, Typeface.BOLD);
        sellBtn.setBackgroundResource(R.drawable.bg_button_danger);
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(180, 180);
        sellBtn.setLayoutParams(btnLp);
        sellBtn.setOnClickListener(v -> showSellDialog(item));

        card.addView(sellBtn);

        return card;
    }

    private void showSellDialog(final MarketItem item) {
        final Citizen me = im.getCitizen();
        if (me == null) return;

        int refund = (int) (item.price * 0.7);

        new android.app.AlertDialog.Builder(this)
                .setTitle("بيع المنتج")
                .setMessage("هل تريد بيع:\n\n" + item.name +
                        "\n\nسعر الشراء: " + item.price + " Đ" +
                        "\nستستلم: " + refund + " Đ (70%)")
                .setPositiveButton("بِع", (d, w) -> mm.sellItem(me.nationalId, item.id, item.price,
                        new MarketManager.OnDone() {
                    @Override public void onSuccess() {
                        Toast.makeText(MyInventoryActivity.this,
                                "✅ تم البيع! +" + refund + " Đ", Toast.LENGTH_LONG).show();
                    }
                    @Override public void onError(String msg) {
                        Toast.makeText(MyInventoryActivity.this, "❌ " + msg, Toast.LENGTH_LONG).show();
                    }
                }))
                .setNegativeButton("إلغاء", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) reg.remove();
    }


    private String getWearType(MarketItem item) {
        if (item.category == null) return null;
        if (!"clothing".equals(item.category) && !"electronics".equals(item.category)) {
            return null;
        }
        if ("electronics".equals(item.category) && !"phone".equals(item.type)) {
            return null;
        }
        // نحاولو نستخرجو النوع من الاسم
        String name = item.name != null ? item.name : "";
        if (name.contains("قميص")) return "shirt";
        if (name.contains("بنطال")) return "pants";
        if (name.contains("حذاء") || name.contains("بوت")) return "shoes";
        if (name.contains("قبعة")) return "hat";
        if (name.contains("نظارات")) return "glasses";
        if ("phone".equals(item.type)) return "phone";
        return null;
    }

    private void wearItem(MarketItem item, String slot) {
        Citizen c = im.getCitizen();
        if (c == null) return;

        // نجيبو اللون من الـ inventory
        db.collection("users_inventory").document(c.nationalId)
                .collection("items").document(item.id)
                .get()
                .addOnSuccessListener(doc -> {
                    String color = doc.getString("color");
                    if (color == null) color = "#1565C0";

                    AvatarManager am = new AvatarManager();
                    final String finalColor = color;
                    am.equipItem(c.nationalId, slot, item.id, item.name, color,
                            new AvatarManager.OnDone() {
                        @Override public void onSuccess() {
                            Toast.makeText(MyInventoryActivity.this,
                                    "✅ تم ارتداء " + item.name, Toast.LENGTH_LONG).show();
                        }
                        @Override public void onError(String msg) {
                            Toast.makeText(MyInventoryActivity.this, "❌ " + msg, Toast.LENGTH_LONG).show();
                        }
                    });
                });
    }
}
