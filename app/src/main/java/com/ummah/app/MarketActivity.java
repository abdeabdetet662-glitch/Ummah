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
    private String currentCategory = "boost";
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
        currentCategory = prefs.getString(KEY_CAT, "boost");

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
        tabsContainer.setOrientation(LinearLayout.VERTICAL);

        String[][] cats = {
                {"\u26a1", "\u062a\u0631\u0642\u064a\u0627\u062a", "boost"},
                {"\ud83c\udfc6", "\u0634\u0627\u0631\u0627\u062a", "badge"},
                {"\ud83c\udf96\ufe0f", "\u0623\u0644\u0642\u0627\u0628", "title"},
                {"\ud83c\udfa8", "\u0623\u0641\u0627\u062a\u0627\u0631", "avatar"},
                {"\ud83c\udfe0", "\u0645\u0646\u0627\u0632\u0644", "home"},
                {"\ud83d\ude97", "\u0645\u0631\u0643\u0628\u0627\u062a", "vehicle"},
                {"\ud83c\udf81", "\u0647\u062f\u0627\u064a\u0627", "gift"},
                {"\ud83d\udc3e", "\u062d\u064a\u0648\u0627\u0646\u0627\u062a", "pet"},
                {"\ud83c\udfb0", "\u062d\u0638", "luck"},
                {"\ud83c\udf1f", "\u0645\u0645\u064a\u0632\u0627\u062a", "special"},
                {"\ud83c\udf54", "\u0637\u0639\u0627\u0645", "food"},
                {"\ud83d\udc8e", "\u0645\u062c\u0648\u0647\u0631\u0627\u062a", "jewelry"},
        };

        LinearLayout row = null;
        for (int i = 0; i < cats.length; i++) {
            if (i % 4 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                rlp.setMargins(0, 6, 0, 6);
                row.setLayoutParams(rlp);
                tabsContainer.addView(row);
            }
            addTabToRow(row, cats[i][0], cats[i][1], cats[i][2]);
        }
    }

    private void addTabToRow(LinearLayout parent, String emoji, String label, final String category) {
        boolean active = category.equals(currentCategory);

        LinearLayout tab = new LinearLayout(this);
        tab.setOrientation(LinearLayout.VERTICAL);
        tab.setGravity(Gravity.CENTER);
        tab.setPadding(8, 12, 8, 12);
        tab.setBackgroundResource(active ? R.drawable.bg_btn_gold_hero : R.drawable.bg_btn_outline);
        tab.setElevation(active ? 10f : 0f);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(3, 0, 3, 0);
        tab.setLayoutParams(lp);

        TextView e = new TextView(this);
        e.setText(emoji);
        e.setTextSize(22);
        e.setGravity(Gravity.CENTER);
        tab.addView(e);

        TextView t = new TextView(this);
        t.setText(label);
        t.setTextColor(active ? Color.parseColor("#0A0A0A") : Color.parseColor("#D4AF37"));
        t.setTextSize(10);
        t.setTypeface(null, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, 4, 0, 0);
        tab.addView(t);

        tab.setOnClickListener(v -> {
            if (category.equals(currentCategory)) return;
            currentCategory = category;
            prefs.edit().putString(KEY_CAT, category).apply();
            buildTabs();
            loadItems(category);
        });

        parent.addView(tab);
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

        // ═══ الصورة / الأيقونة ═══
        if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
            // صورة حقيقية
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
        } else {
            // أيقونة كبيرة + خلفية ملونة
            LinearLayout iconBox = new LinearLayout(this);
            iconBox.setOrientation(LinearLayout.VERTICAL);
            iconBox.setGravity(Gravity.CENTER);
            iconBox.setBackgroundColor(Color.parseColor("#1A1A1A"));
            LinearLayout.LayoutParams ibLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 300);
            iconBox.setLayoutParams(ibLp);

            TextView bigIcon = new TextView(this);
            String emoji = (item.icon != null && !item.icon.isEmpty())
                    ? item.icon : getCategoryEmoji(item.category);
            bigIcon.setText(emoji);
            bigIcon.setTextSize(120);
            bigIcon.setGravity(Gravity.CENTER);
            iconBox.addView(bigIcon);

            card.addView(iconBox);
        }

        // ═══ اسم المنتج ═══
        TextView name = new TextView(this);
        name.setText(item.name != null ? item.name : "منتج");
        name.setTextColor(Color.WHITE);
        name.setTextSize(20);
        name.setTypeface(null, Typeface.BOLD);
        name.setPadding(0, 20, 0, 6);
        card.addView(name);

        // ═══ العلامة + الندرة ═══
        String brand = item.brand != null && !item.brand.isEmpty()
                ? item.brand : getCategoryLabel(item.category);
        String rarity = getRarityLabel(item.rarity);
        String subline = brand;
        if (rarity != null && !rarity.isEmpty()) {
            subline = subline + "  •  " + rarity;
        }

        TextView brandView = new TextView(this);
        brandView.setText(subline);
        brandView.setTextColor(Color.parseColor("#9E9E9E"));
        brandView.setTextSize(12);
        card.addView(brandView);

        // ═══ الوصف ═══
        if (item.description != null && !item.description.isEmpty()) {
            TextView desc = new TextView(this);
            desc.setText(item.description);
            desc.setTextColor(Color.parseColor("#CCCCCC"));
            desc.setTextSize(13);
            desc.setPadding(0, 10, 0, 16);
            card.addView(desc);
        }

        // ═══ السعر + زر شراء ═══
        LinearLayout bottomRow = new LinearLayout(this);
        bottomRow.setOrientation(LinearLayout.HORIZONTAL);
        bottomRow.setGravity(Gravity.CENTER_VERTICAL);
        bottomRow.setPadding(0, 16, 0, 0);

        TextView price = new TextView(this);
        price.setText("\u200E" + item.price + " Đ \ud83d\udcb0");
        price.setTextColor(Color.parseColor("#D4AF37"));
        price.setTextSize(22);
        price.setTypeface(null, Typeface.BOLD);
        price.setTextDirection(View.TEXT_DIRECTION_LTR);
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

    private String getCategoryEmoji(String category) {
        if (category == null) return "\ud83d\udce6";
        switch (category) {
            case "boost":   return "\u26a1";
            case "badge":   return "\ud83c\udfc6";
            case "title":   return "\ud83c\udf96\ufe0f";
            case "avatar":  return "\ud83c\udfa8";
            case "home":    return "\ud83c\udfe0";
            case "vehicle": return "\ud83d\ude97";
            case "gift":    return "\ud83c\udf81";
            case "pet":     return "\ud83d\udc3e";
            case "luck":    return "\ud83c\udfb0";
            case "special": return "\ud83c\udf1f";
            case "food":    return "\ud83c\udf54";
            case "jewelry": return "\ud83d\udc8e";
            case "clothes": return "\ud83d\udc55";
            case "electronics": return "\ud83d\udcf1";
            case "realestate":  return "\ud83c\udfe0";
            default:        return "\ud83d\udce6";
        }
    }

    private String getCategoryLabel(String category) {
        if (category == null) return "";
        switch (category) {
            case "boost":   return "\u062a\u0631\u0642\u064a\u0629";
            case "badge":   return "\u0634\u0627\u0631\u0629";
            case "title":   return "\u0644\u0642\u0628";
            case "avatar":  return "\u0623\u0641\u0627\u062a\u0627\u0631";
            case "home":    return "\u0645\u0646\u0632\u0644";
            case "vehicle": return "\u0645\u0631\u0643\u0628\u0629";
            case "gift":    return "\u0647\u062f\u064a\u0629";
            case "pet":     return "\u062d\u064a\u0648\u0627\u0646";
            case "luck":    return "\u062d\u0638";
            case "special": return "\u0645\u0645\u064a\u0632\u0629";
            case "food":    return "\u0637\u0639\u0627\u0645";
            case "jewelry": return "\u0645\u062c\u0648\u0647\u0631";
            default:        return "";
        }
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
