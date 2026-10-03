package com.ummah.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class SellItemActivity extends Activity {

    public static final String EXTRA_ITEM_ID = "item_id";
    public static final String EXTRA_ITEM_NAME = "item_name";
    public static final String EXTRA_ITEM_BRAND = "item_brand";
    public static final String EXTRA_ITEM_CATEGORY = "item_category";
    public static final String EXTRA_ITEM_TYPE = "item_type";
    public static final String EXTRA_ITEM_PRICE = "item_price";
    public static final String EXTRA_ITEM_IMAGE = "item_image";
    public static final String EXTRA_ITEM_RARITY = "item_rarity";
    public static final String EXTRA_ITEM_DESC = "item_desc";
    public static final String EXTRA_INV_DOC_ID = "inv_doc_id";

    private IdentityManager im;
    private CitizenListingManager clm;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        im = new IdentityManager(this);
        clm = new CitizenListingManager();

        final String itemId = getIntent().getStringExtra(EXTRA_ITEM_ID);
        final String itemName = getIntent().getStringExtra(EXTRA_ITEM_NAME);
        final String itemBrand = getIntent().getStringExtra(EXTRA_ITEM_BRAND);
        final String itemCategory = getIntent().getStringExtra(EXTRA_ITEM_CATEGORY);
        final String itemType = getIntent().getStringExtra(EXTRA_ITEM_TYPE);
        final int originalPrice = getIntent().getIntExtra(EXTRA_ITEM_PRICE, 0);
        final String itemImage = getIntent().getStringExtra(EXTRA_ITEM_IMAGE);
        final String itemRarity = getIntent().getStringExtra(EXTRA_ITEM_RARITY);
        final String itemDesc = getIntent().getStringExtra(EXTRA_ITEM_DESC);
        final String invDocId = getIntent().getStringExtra(EXTRA_INV_DOC_ID);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 50, 30, 60);
        scroll.addView(root);

        TextView title = UiHelper.goldTitle(this, getString(R.string.sell_listed), 28);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText(getString(R.string.sell_set_price));
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 20);
        root.addView(sub);

        LinearLayout card = UiHelper.card(this);

        if (itemImage != null && !itemImage.isEmpty()) {
            ImageView img = new ImageView(this);
            LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 500);
            img.setLayoutParams(imgLp);
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            img.setBackgroundColor(Color.parseColor("#1A1A1A"));
            com.bumptech.glide.Glide.with(this)
                    .load(itemImage)
                    .placeholder(R.drawable.ic_ummah)
                    .into(img);
            card.addView(img);
        }

        TextView name = new TextView(this);
        name.setText(itemName);
        name.setTextColor(Color.WHITE);
        name.setTextSize(20);
        name.setTypeface(null, Typeface.BOLD);
        name.setPadding(0, 20, 0, 6);
        card.addView(name);

        TextView brand = new TextView(this);
        brand.setText((itemBrand != null ? itemBrand : "") + " • " + rarityLabel(itemRarity));
        brand.setTextColor(Color.parseColor("#9E9E9E"));
        brand.setTextSize(12);
        card.addView(brand);

        TextView orig = new TextView(this);
        orig.setText(getString(R.string.sell_original_price_fmt, originalPrice));
        orig.setTextColor(Color.parseColor("#D4AF37"));
        orig.setTextSize(14);
        orig.setTypeface(null, Typeface.BOLD);
        orig.setPadding(0, 14, 0, 0);
        card.addView(orig);

        root.addView(card);

        LinearLayout priceCard = UiHelper.card(this);

        TextView priceLabel = new TextView(this);
        priceLabel.setText(getString(R.string.sell_suggested_price));
        priceLabel.setTextColor(Color.parseColor("#D4AF37"));
        priceLabel.setTextSize(14);
        priceLabel.setTypeface(null, Typeface.BOLD);
        priceLabel.setGravity(Gravity.CENTER);
        priceLabel.setPadding(0, 0, 0, 12);
        priceCard.addView(priceLabel);

        final EditText priceInput = UiHelper.input(this, getString(R.string.sell_price_dinar));
        priceInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        priceInput.setText(String.valueOf(originalPrice));
        priceCard.addView(priceInput);

        int commission = (int) (originalPrice * 0.05);
        TextView hint = new TextView(this);
        hint.setText(getString(R.string.sell_commission_fmt, originalPrice, originalPrice - commission));
        hint.setTextColor(Color.parseColor("#9E9E9E"));
        hint.setTextSize(11);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, 8, 0, 0);
        priceCard.addView(hint);

        root.addView(priceCard);

        LinearLayout quick = new LinearLayout(this);
        quick.setOrientation(LinearLayout.HORIZONTAL);
        quick.setPadding(0, 10, 0, 10);

        addQuickPrice(quick, priceInput, originalPrice, 0.7, "70%");
        addQuickPrice(quick, priceInput, originalPrice, 1.0, "100%");
        addQuickPrice(quick, priceInput, originalPrice, 1.3, "130%");
        addQuickPrice(quick, priceInput, originalPrice, 2.0, "200%");

        root.addView(quick);

        Button publishBtn = UiHelper.primaryButton(this, getString(R.string.sell_publish_offer));
        publishBtn.setMinHeight(160);
        publishBtn.setTextSize(20);
        publishBtn.setOnClickListener(v -> {
            String txt = priceInput.getText().toString().trim();
            if (txt.isEmpty()) {
                Toast.makeText(this, getString(R.string.sell_enter_price), Toast.LENGTH_SHORT).show();
                return;
            }
            int price;
            try { price = Integer.parseInt(txt); }
            catch (Exception e) { Toast.makeText(this, getString(R.string.sell_invalid_price), Toast.LENGTH_SHORT).show(); return; }

            if (price < 1) { Toast.makeText(this, getString(R.string.sell_price_positive), Toast.LENGTH_SHORT).show(); return; }
            if (price > 100000000) { Toast.makeText(this, getString(R.string.sell_price_high), Toast.LENGTH_SHORT).show(); return; }

            Citizen c = im.getCitizen();
            if (c == null) return;

            MarketItem mi = new MarketItem();
            mi.id = itemId;
            mi.name = itemName;
            mi.brand = itemBrand;
            mi.category = itemCategory;
            mi.type = itemType;
            mi.price = originalPrice;
            mi.imageUrl = itemImage;
            mi.rarity = itemRarity;
            mi.description = itemDesc;

            clm.createListing(c.nationalId, c.name, mi, price, invDocId, new CitizenListingManager.OnDone() {
                @Override public void onSuccess() {
                    Toast.makeText(SellItemActivity.this, getString(R.string.sell_published), Toast.LENGTH_LONG).show();
                    finish();
                }
                @Override public void onError(String msg) {
                    Toast.makeText(SellItemActivity.this, "❌ " + msg, Toast.LENGTH_LONG).show();
                }
            });
        });
        root.addView(publishBtn);

        setContentView(scroll);
    }

    private void addQuickPrice(LinearLayout parent, final EditText input, int base, double mult, String label) {
        Button btn = new Button(this);
        btn.setText(label);
        btn.setTextSize(11);
        btn.setTextColor(Color.parseColor("#D4AF37"));
        btn.setAllCaps(false);
        btn.setBackgroundResource(R.drawable.bg_btn_outline);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(4, 0, 4, 0);
        btn.setLayoutParams(lp);
        final int val = (int) (base * mult);
        btn.setOnClickListener(v -> input.setText(String.valueOf(val)));
        parent.addView(btn);
    }

    private String rarityLabel(String r) {
        if (r == null) return "";
        switch (r) {
            case "common": return "شائع";
            case "rare": return "نادر ⭐";
            case "epic": return "ملحمي ✨";
            case "legendary": return "أسطوري 👑";
        }
        return "";
    }
}
