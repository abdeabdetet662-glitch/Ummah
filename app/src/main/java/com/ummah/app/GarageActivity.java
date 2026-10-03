package com.ummah.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GarageActivity extends Activity {

    private IdentityManager im;
    private MarketManager mm;
    private FirebaseFirestore db;
    private LinearLayout container;
    private ListenerRegistration reg;
    private List<MarketItem> vehicles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        im = new IdentityManager(this);
        mm = new MarketManager();
        db = FirebaseFirestore.getInstance();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 50, 30, 60);
        scroll.addView(root);

        TextView title = UiHelper.goldTitle(this, "🚗  مرآبي", 28);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText(getString(R.string.garage_subtitle));
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
        Citizen c = im.getCitizen();
        if (c == null) { finish(); return; }

        if (reg != null) reg.remove();
        reg = mm.listenMyInventory(c.nationalId, new MarketManager.ItemsListener() {
            @Override public void onItems(List<MarketItem> items) {
                // نصفيو المركبات فقط
                vehicles.clear();
                for (MarketItem item : items) {
                    if ("vehicle".equals(item.category)) {
                        vehicles.add(item);
                    }
                }
                runOnUiThread(() -> renderVehicles());
            }
            @Override public void onError(String msg) {
                runOnUiThread(() -> Toast.makeText(GarageActivity.this, "خطأ: " + msg, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void renderVehicles() {
        container.removeAllViews();

        if (vehicles.isEmpty()) {
            LinearLayout box = new LinearLayout(this);
            box.setOrientation(LinearLayout.VERTICAL);
            box.setGravity(Gravity.CENTER);
            box.setPadding(0, 80, 0, 0);

            TextView ic = new TextView(this);
            ic.setText("🚗");
            ic.setTextSize(80);
            ic.setGravity(Gravity.CENTER);
            box.addView(ic);

            TextView t = new TextView(this);
            t.setText(getString(R.string.garage_empty));
            t.setTextColor(Color.parseColor("#D4AF37"));
            t.setTextSize(18);
            t.setTypeface(null, Typeface.BOLD);
            t.setGravity(Gravity.CENTER);
            t.setPadding(0, 20, 0, 6);
            box.addView(t);

            TextView hint = new TextView(this);
            hint.setText(getString(R.string.garage_go_market));
            hint.setTextColor(Color.parseColor("#9E9E9E"));
            hint.setTextSize(13);
            hint.setGravity(Gravity.CENTER);
            box.addView(hint);

            Button goBtn = UiHelper.primaryButton(this, getString(R.string.garage_open_market));
            goBtn.setOnClickListener(v -> {
                startActivity(new android.content.Intent(this, MarketActivity.class));
                finish();
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 30, 0, 0);
            goBtn.setLayoutParams(lp);
            box.addView(goBtn);

            container.addView(box);
            return;
        }

        // عنوان القسم
        TextView section = new TextView(this);
        section.setText("🚗  " + vehicles.size() + getString(R.string.garage_vehicle));
        section.setTextColor(Color.parseColor("#D4AF37"));
        section.setTextSize(15);
        section.setTypeface(null, Typeface.BOLD);
        section.setGravity(Gravity.CENTER);
        section.setPadding(0, 0, 0, 16);
        container.addView(section);

        for (MarketItem item : vehicles) {
            container.addView(buildVehicleCard(item));
        }
    }

    private View buildVehicleCard(final MarketItem item) {
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
                LinearLayout.LayoutParams.MATCH_PARENT, 450);
        image.setLayoutParams(imgLp);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setBackgroundColor(Color.parseColor("#1A1A1A"));

        com.bumptech.glide.Glide.with(this)
                .load(item.imageUrl)
                .placeholder(R.drawable.ic_ummah)
                .into(image);
        card.addView(image);

        // الاسم
        TextView name = new TextView(this);
        name.setText(item.name);
        name.setTextColor(Color.WHITE);
        name.setTextSize(20);
        name.setTypeface(null, Typeface.BOLD);
        name.setPadding(0, 20, 0, 4);
        card.addView(name);

        // العلامة
        TextView brand = new TextView(this);
        brand.setText(item.brand + "  •  " + ("car".equals(item.type) ? getString(R.string.garage_car) : getString(R.string.garage_bicycle)));
        brand.setTextColor(Color.parseColor("#9E9E9E"));
        brand.setTextSize(12);
        card.addView(brand);

        // السعر
        TextView price = new TextView(this);
        price.setText(getString(R.string.garage_price) + item.price + " Đ");
        price.setTextColor(Color.parseColor("#D4AF37"));
        price.setTextSize(13);
        price.setPadding(0, 12, 0, 16);
        card.addView(price);

        // أزرار
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);

        Button customizeBtn = UiHelper.primaryButton(this, "🎨 عدّل");
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lp1.setMargins(0, 0, 8, 0);
        customizeBtn.setLayoutParams(lp1);
        customizeBtn.setOnClickListener(v -> showCustomizeDialog(item));

        btnRow.addView(customizeBtn);

        Button sellBtn = UiHelper.actionButton(this, "💰 بِع", "#C62828");
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lp2.setMargins(8, 0, 0, 0);
        sellBtn.setLayoutParams(lp2);
        sellBtn.setOnClickListener(v -> showSellDialog(item));

        btnRow.addView(sellBtn);
        card.addView(btnRow);

        return card;
    }

    private void showCustomizeDialog(final MarketItem item) {
        // نعرضو حوار بسيط لتخصيص الاسم
        final EditText input = UiHelper.input(this, "اسم مخصص للمركبة");
        input.setText(item.name);

        LinearLayout box = new LinearLayout(this);
        box.setPadding(40, 20, 40, 20);
        box.addView(input);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.garage_customize))
                .setMessage(getString(R.string.garage_name_hint))
                .setView(box)
                .setPositiveButton(getString(R.string.garage_save), (d, w) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty()) return;
                    saveCustomName(item, newName);
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void saveCustomName(MarketItem item, String newName) {
        Citizen c = im.getCitizen();
        if (c == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("customName", newName);

        db.collection("users_inventory").document(c.nationalId)
                .collection("items").document(item.id)
                .update(data)
                .addOnSuccessListener(a -> {
                    Toast.makeText(GarageActivity.this, getString(R.string.garage_customized), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(GarageActivity.this, "❌ " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showSellDialog(final MarketItem item) {
        final Citizen c = im.getCitizen();
        if (c == null) return;

        int refund = (int) (item.price * 0.7);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.garage_sell_vehicle))
                .setMessage("هل تريد بيع:\n\n" + item.name +
                        "\n\nسعر الشراء: " + item.price + " Đ" +
                        "\nستستلم: " + refund + " Đ (70%)")
                .setPositiveButton("بِع", (d, w) -> mm.sellItem(c.nationalId, item.id, item.price,
                        new MarketManager.OnDone() {
                    @Override public void onSuccess() {
                        Toast.makeText(GarageActivity.this,
                                getString(R.string.garage_sold_plus) + refund + " Đ", Toast.LENGTH_LONG).show();
                    }
                    @Override public void onError(String msg) {
                        Toast.makeText(GarageActivity.this, "❌ " + msg, Toast.LENGTH_LONG).show();
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
