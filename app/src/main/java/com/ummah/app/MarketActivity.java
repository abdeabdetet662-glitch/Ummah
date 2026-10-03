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

import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class MarketActivity extends Activity {

    private static final String PREFS = "market_prefs";
    private static final String KEY_CAT = "current_category";

    private IdentityManager im;
    private MarketManager mm;
    private LinearLayout itemsContainer;
    private LinearLayout tabsContainer;
    private ListenerRegistration reg;
    private String currentCategory = "vehicle";
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        // Feature Check
        if (!FeatureFlags.checkOrToast(this, "market")) {
            finish();
            return;
        }

        im = new IdentityManager(this);
        mm = new MarketManager();
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        // نقراو الفئة المحفوظة
        currentCategory = prefs.getString(KEY_CAT, "vehicle");

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
        icon.setText("🛒");
        icon.setTextSize(36);
        header.addView(icon);

        TextView title = new TextView(this);
        title.setText(getString(R.string.market_title));
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(26);
        title.setTypeface(null, Typeface.BOLD);
        header.addView(title);

        root.addView(header);

        TextView sub = new TextView(this);
        sub.setText(getString(R.string.market_subtitle));
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

        // Container للمنتجات
        itemsContainer = new LinearLayout(this);
        itemsContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(itemsContainer);

        setContentView(scroll);

        loadItems(currentCategory);
    }

    private void buildTabs() {
        tabsContainer.removeAllViews();
        addTab("🚗", getString(R.string.market_category_vehicles), "vehicle");
        addTab("🏠", getString(R.string.market_category_realestate), "property");
        addTab("📱", getString(R.string.market_category_electronics), "electronics");
        addTab("👕", getString(R.string.market_category_clothes), "clothing");
    }

    private void addTab(String emoji, String label, final String category) {
        boolean active = category.equals(currentCategory);

        LinearLayout tab = new LinearLayout(this);
        tab.setOrientation(LinearLayout.VERTICAL);
        tab.setGravity(Gravity.CENTER);
        tab.setPadding(20, 14, 20, 14);
        tab.setBackgroundResource(active ? R.drawable.bg_btn_gold_hero : R.drawable.bg_btn_outline);
        tab.setElevation(active ? 10f : 0f);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(4, 0, 4, 0);
        tab.setLayoutParams(lp);

        TextView e = new TextView(this);
        e.setText(emoji);
        e.setTextSize(22);
        e.setGravity(Gravity.CENTER);
        tab.addView(e);

        TextView t = new TextView(this);
        t.setText(label);
        t.setTextColor(active ? Color.parseColor("#0A0A0A") : Color.parseColor("#D4AF37"));
        t.setTextSize(11);
        t.setTypeface(null, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        tab.addView(t);

        tab.setOnClickListener(v -> {
            if (category.equals(currentCategory)) return;
            currentCategory = category;
            prefs.edit().putString(KEY_CAT, category).apply();
            buildTabs();
            loadItems(category);
        });

        tabsContainer.addView(tab);
    }

    private void loadItems(String category) {
        if (reg != null) reg.remove();
        itemsContainer.removeAllViews();

        // Loading
        TextView loading = new TextView(this);
        loading.setText(R.string.common_loading);
        loading.setTextColor(Color.parseColor("#9E9E9E"));
        loading.setGravity(Gravity.CENTER);
        loading.setPadding(0, 40, 0, 40);
        itemsContainer.addView(loading);

        reg = mm.listenByCategory(category, new MarketManager.ItemsListener() {
            @Override public void onItems(final List<MarketItem> items) {
                runOnUiThread(() -> renderItems(items));
            }
            @Override public void onError(String msg) {
                runOnUiThread(() -> {
                    itemsContainer.removeAllViews();
                    TextView err = new TextView(MarketActivity.this);
                    err.setText(getString(R.string.common_error_prefix) + msg);
                    err.setTextColor(Color.parseColor("#CF6679"));
                    err.setGravity(Gravity.CENTER);
                    err.setPadding(0, 40, 0, 40);
                    itemsContainer.addView(err);
                });
            }
        });
    }

    private void renderItems(List<MarketItem> items) {
        itemsContainer.removeAllViews();

        if (items.isEmpty()) {
            LinearLayout emptyBox = new LinearLayout(this);
            emptyBox.setOrientation(LinearLayout.VERTICAL);
            emptyBox.setGravity(Gravity.CENTER);
            emptyBox.setPadding(0, 80, 0, 0);

            TextView icon = new TextView(this);
            icon.setText("🛒");
            icon.setTextSize(60);
            icon.setGravity(Gravity.CENTER);
            emptyBox.addView(icon);

            TextView empty = new TextView(this);
            empty.setText(getString(R.string.market_no_products));
            empty.setTextColor(Color.parseColor("#D4AF37"));
            empty.setTextSize(16);
            empty.setTypeface(null, Typeface.BOLD);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 20, 0, 0);
            emptyBox.addView(empty);

            itemsContainer.addView(emptyBox);
            return;
        }

        for (MarketItem item : items) {
            itemsContainer.addView(buildItemCard(item));
        }
    }

    private View buildItemCard(final MarketItem item) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card_premium);
        card.setPadding(30, 30, 30, 30);
        card.setElevation(8f);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 10, 0, 10);
        card.setLayoutParams(lp);

        // صورة
        ImageView image = new ImageView(this);
        LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 500);
        image.setLayoutParams(imgLp);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setBackgroundColor(Color.parseColor("#1A1A1A"));

        com.bumptech.glide.Glide.with(this)
                .load(item.imageUrl)
                .placeholder(R.drawable.ic_ummah)
                .into(image);

        card.addView(image);

        // اسم
        TextView name = new TextView(this);
        name.setText(item.name);
        name.setTextColor(Color.WHITE);
        name.setTextSize(20);
        name.setTypeface(null, Typeface.BOLD);
        name.setPadding(0, 20, 0, 6);
        card.addView(name);

        // العلامة + الندرة
        TextView brand = new TextView(this);
        brand.setText(item.brand + "  •  " + getRarityLabel(item.rarity));
        brand.setTextColor(Color.parseColor("#9E9E9E"));
        brand.setTextSize(12);
        card.addView(brand);

        // الوصف
        TextView desc = new TextView(this);
        desc.setText(item.description);
        desc.setTextColor(Color.parseColor("#CCCCCC"));
        desc.setTextSize(13);
        desc.setPadding(0, 10, 0, 16);
        card.addView(desc);

        // السعر + زر شراء
        LinearLayout bottomRow = new LinearLayout(this);
        bottomRow.setOrientation(LinearLayout.HORIZONTAL);
        bottomRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView price = new TextView(this);
        price.setText("💰 " + item.price + " Đ");
        price.setTextColor(Color.parseColor("#D4AF37"));
        price.setTextSize(22);
        price.setTypeface(null, Typeface.BOLD);
        price.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        bottomRow.addView(price);

        Button buyBtn = UiHelper.primaryButton(this, "شراء");
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                300, LinearLayout.LayoutParams.WRAP_CONTENT);
        buyBtn.setLayoutParams(btnLp);
        buyBtn.setOnClickListener(v -> showBuyDialog(item));

        bottomRow.addView(buyBtn);
        card.addView(bottomRow);

        return card;
    }

    private String getRarityLabel(String r) {
        if (r == null) return "";
        switch (r) {
            case "common": return getString(R.string.market_quality_common);
            case "rare": return getString(R.string.market_quality_rare);
            case "epic": return getString(R.string.market_quality_epic);
            case "legendary": return getString(R.string.market_quality_legendary);
            default: return "";
        }
    }

    private void showBuyDialog(final MarketItem item) {
        final Citizen me = im.getCitizen();
        if (me == null) return;

        new android.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.market_confirm_purchase))
                .setMessage("هل تريد شراء:\n\n" + item.name + "\n\nبسعر: " + item.price + " Đ")
                .setPositiveButton("شراء", (d, w) -> mm.buyItem(me.nationalId, item, new MarketManager.OnDone() {
                    @Override public void onSuccess() {
                        Toast.makeText(MarketActivity.this, getString(R.string.market_purchased), Toast.LENGTH_LONG).show();
                    }
                    @Override public void onError(String msg) {
                        Toast.makeText(MarketActivity.this, "❌ " + msg, Toast.LENGTH_LONG).show();
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

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

}
