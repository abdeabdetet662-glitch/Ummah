package com.ummah.app;

import android.app.Activity;
import android.app.AlertDialog;
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

public class CitizenMarketActivity extends Activity {

    private IdentityManager im;
    private CitizenListingManager clm;
    private LinearLayout container;
    private ListenerRegistration reg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        // Feature Check
        if (!FeatureFlags.checkOrToast(this, "citizen_market")) {
            finish();
            return;
        }

        im = new IdentityManager(this);
        clm = new CitizenListingManager();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 50, 30, 60);
        scroll.addView(root);

        TextView title = UiHelper.goldTitle(this, getString(R.string.cmarket_title), 28);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText(getString(R.string.cmarket_subtitle));
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 24);
        root.addView(sub);

        container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        root.addView(container);

        setContentView(scroll);
        startListener();
    }

    private void startListener() {
        if (reg != null) reg.remove();
        reg = clm.listenActiveListings(new CitizenListingManager.ListingsListener() {
            @Override public void onListings(List<CitizenListing> listings) {
                runOnUiThread(() -> render(listings));
            }
            @Override public void onError(String msg) {
                runOnUiThread(() -> Toast.makeText(CitizenMarketActivity.this, getString(R.string.common_error_prefix) + msg, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void render(List<CitizenListing> listings) {
        container.removeAllViews();

        if (listings.isEmpty()) {
            LinearLayout box = new LinearLayout(this);
            box.setOrientation(LinearLayout.VERTICAL);
            box.setGravity(Gravity.CENTER);
            box.setPadding(0, 80, 0, 0);

            TextView ic = new TextView(this);
            ic.setText("🤝");
            ic.setTextSize(80);
            ic.setGravity(Gravity.CENTER);
            box.addView(ic);

            TextView t = new TextView(this);
            t.setText(getString(R.string.cmarket_empty));
            t.setTextColor(Color.parseColor("#D4AF37"));
            t.setTextSize(16);
            t.setTypeface(null, Typeface.BOLD);
            t.setGravity(Gravity.CENTER);
            t.setPadding(0, 20, 0, 6);
            box.addView(t);

            container.addView(box);
            return;
        }

        for (CitizenListing cl : listings) {
            container.addView(buildCard(cl));
        }
    }

    private View buildCard(final CitizenListing cl) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card_premium);
        card.setPadding(30, 30, 30, 30);
        card.setElevation(8f);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 10, 0, 10);
        card.setLayoutParams(lp);

        TextView badge = new TextView(this);
        badge.setText(getString(R.string.cmarket_citizen_offer));
        badge.setTextColor(Color.parseColor("#D4AF37"));
        badge.setTextSize(11);
        badge.setTypeface(null, Typeface.BOLD);
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(30, 12, 30, 12);
        LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        badgeLp.gravity = Gravity.CENTER;
        badgeLp.setMargins(0, 0, 0, 12);
        badge.setLayoutParams(badgeLp);
        card.addView(badge);

        if (cl.imageUrl != null && !cl.imageUrl.isEmpty()) {
            ImageView img = new ImageView(this);
            LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 500);
            img.setLayoutParams(imgLp);
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            img.setBackgroundColor(Color.parseColor("#1A1A1A"));
            com.bumptech.glide.Glide.with(this)
                    .load(cl.imageUrl)
                    .placeholder(R.drawable.ic_ummah)
                    .into(img);
            card.addView(img);
        }

        TextView name = new TextView(this);
        name.setText(cl.itemName);
        name.setTextColor(Color.WHITE);
        name.setTextSize(20);
        name.setTypeface(null, Typeface.BOLD);
        name.setPadding(0, 20, 0, 6);
        card.addView(name);

        TextView seller = new TextView(this);
        seller.setText("👤 " + cl.sellerName);
        seller.setTextColor(Color.parseColor("#D4AF37"));
        seller.setTextSize(12);
        card.addView(seller);

        if (cl.description != null && !cl.description.isEmpty()) {
            TextView desc = new TextView(this);
            desc.setText(cl.description);
            desc.setTextColor(Color.parseColor("#CCCCCC"));
            desc.setTextSize(13);
            desc.setPadding(0, 10, 0, 6);
            card.addView(desc);
        }

        LinearLayout bottomRow = new LinearLayout(this);
        bottomRow.setOrientation(LinearLayout.HORIZONTAL);
        bottomRow.setGravity(Gravity.CENTER_VERTICAL);
        bottomRow.setPadding(0, 14, 0, 0);

        TextView price = new TextView(this);
        price.setText("💰 " + cl.price + " Đ");
        price.setTextColor(Color.parseColor("#D4AF37"));
        price.setTextSize(22);
        price.setTypeface(null, Typeface.BOLD);
        price.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        bottomRow.addView(price);

        Button buyBtn = UiHelper.primaryButton(this, "شراء");
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(300, LinearLayout.LayoutParams.WRAP_CONTENT);
        buyBtn.setLayoutParams(btnLp);
        buyBtn.setOnClickListener(v -> showBuyDialog(cl));
        bottomRow.addView(buyBtn);

        card.addView(bottomRow);
        return card;
    }

    private void showBuyDialog(final CitizenListing cl) {
        final Citizen me = im.getCitizen();
        if (me == null) return;

        if (me.nationalId.equals(cl.sellerId)) {
            Toast.makeText(this, getString(R.string.cmarket_your_product), Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.cmarket_confirm_dialog)
                .setMessage("هل تريد شراء:\\n\\n" + cl.itemName +
                        "\\n\\nمن: " + cl.sellerName +
                        "\\n\\nبسعر: " + cl.price + " Đ")
                .setPositiveButton("شراء", (d, w) -> clm.buyFromCitizen(me.nationalId, cl, new CitizenListingManager.BuyDone() {
                    @Override public void onSuccess(int paid) {
                        Toast.makeText(CitizenMarketActivity.this,
                                "✅ تم الشراء! -" + paid + " Đ", Toast.LENGTH_LONG).show();
                    }
                    @Override public void onError(String msg) {
                        Toast.makeText(CitizenMarketActivity.this, "❌ " + msg, Toast.LENGTH_LONG).show();
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
