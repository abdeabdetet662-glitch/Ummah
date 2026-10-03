package com.ummah.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CityMapActivity extends Activity {

    private IdentityManager im;
    private CityManager cm;
    private WalletManager wm;
    private WebView webView;
    private LinearLayout root;
    private ListenerRegistration plotsReg;
    private List<CityPlot> allPlots = new ArrayList<>();
    private boolean webReady = false;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        // Feature Check
        if (!FeatureFlags.checkOrToast(this, "city")) {
            finish();
            return;
        }

        im = new IdentityManager(this);
        cm = new CityManager();
        wm = new WalletManager(this);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#050505"));

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER);
        header.setPadding(20, 40, 20, 20);
        header.setBackgroundColor(Color.parseColor("#0A0A0A"));

        TextView title = new TextView(this);
        title.setText(getString(R.string.city_title));
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(22);
        title.setTypeface(null, Typeface.BOLD);
        header.addView(title);

        root.addView(header);

        // WebView
        // ═══ WebView Debugging (للتشخيص) ═══
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        
        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.setBackgroundColor(Color.parseColor("#050505"));
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");

        LinearLayout.LayoutParams wvLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        webView.setLayoutParams(wvLp);
        webView.loadUrl("file:///android_asset/city/index.html");
        root.addView(webView);

        // Footer
        LinearLayout footer = new LinearLayout(this);
        footer.setOrientation(LinearLayout.HORIZONTAL);
        footer.setPadding(20, 15, 20, 25);
        footer.setBackgroundColor(Color.parseColor("#0A0A0A"));

        TextView hint = new TextView(this);
        hint.setText(getString(R.string.city_hint));
        hint.setTextColor(Color.parseColor("#9E9E9E"));
        hint.setTextSize(12);
        hint.setGravity(Gravity.CENTER);
        hint.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        footer.addView(hint);

        root.addView(footer);

        setContentView(root);

        startListener();
    }

    private void startListener() {
        if (plotsReg != null) plotsReg.remove();
        plotsReg = cm.listenAllPlots(new CityManager.PlotsListener() {
            @Override public void onPlots(List<CityPlot> plots) {
                allPlots = plots;
                runOnUiThread(() -> sendToWeb());
            }
            @Override public void onError(String msg) {
                runOnUiThread(() -> Toast.makeText(CityMapActivity.this, "خطأ: " + msg, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void sendToWeb() {
        if (!webReady || webView == null) return;

        try {
            JSONArray arr = new JSONArray();
            for (CityPlot p : allPlots) {
                JSONObject o = new JSONObject();
                o.put("id", p.id);
                o.put("districtId", p.districtId);
                o.put("x", p.x);
                o.put("z", p.z);
                o.put("price", p.price);
                o.put("ownerId", p.ownerId != null ? p.ownerId : "");
                o.put("ownerName", p.ownerName != null ? p.ownerName : "");
                o.put("buildingType", p.buildingType != null ? p.buildingType : "");
                o.put("buildingLevel", p.buildingLevel);
                arr.put(o);
            }
            final String json = arr.toString();
            webView.post(() -> webView.evaluateJavascript(
                    "setCityData(" + JSONObject.quote(json) + ");", null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class AndroidBridge {
        @JavascriptInterface
        public void onReady() {
            webReady = true;
            runOnUiThread(() -> sendToWeb());
        }

        @JavascriptInterface
        public void onPlotClick(String plotJson) {
            runOnUiThread(() -> handlePlotClick(plotJson));
        }
    }

    private void handlePlotClick(String plotJson) {
        try {
            JSONObject o = new JSONObject(plotJson);
            String ownerId = o.optString("ownerId", "");
            String districtId = o.optString("districtId", "");
            int price = o.optInt("price", 0);
            String buildingType = o.optString("buildingType", "");
            int buildingLevel = o.optInt("buildingLevel", 0);
            String plotId = o.optString("id", "");

            Citizen me = im.getCitizen();
            if (me == null) return;

            boolean isMine = me.nationalId.equals(ownerId);
            boolean isFree = ownerId.isEmpty();

            if (isFree) {
                showBuyDialog(plotId, districtId, price);
            } else if (isMine) {
                if (buildingType.isEmpty()) {
                    showBuildDialog(plotId, districtId);
                } else {
                    showMyPlotDialog(plotId, districtId, buildingType, buildingLevel, price);
                }
            } else {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.city_owned_plot))
                        .setMessage("👤 المالك: " + o.optString("ownerName", "?") +
                                "\n🏢 المبنى: " + buildingLabel(buildingType) +
                                "\n⭐ المستوى: " + buildingLevel)
                        .setPositiveButton("حسناً", null)
                        .show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "خطأ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showBuyDialog(final String plotId, String districtId, int price) {
        String districtName = districtLabel(districtId);
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.city_buy_plot))
                .setMessage("📍 " + districtName +
                        "\n\n💰 السعر: " + price + " Đ" +
                        "\n\nهل تريد شراء هذي القطعة؟")
                .setPositiveButton("شراء", (d, w) -> {
                    Citizen me = im.getCitizen();
                    if (me == null) return;
                    CityPlot plot = findPlot(plotId);
                    if (plot == null) return;

                    cm.buyPlot(me.nationalId, me.name, plot, new CityManager.OnDone() {
                        @Override public void onSuccess() {
                            Toast.makeText(CityMapActivity.this,
                                    getString(R.string.city_purchased), Toast.LENGTH_LONG).show();
                        }
                        @Override public void onError(String msg) {
                            Toast.makeText(CityMapActivity.this,
                                    "❌ " + msg, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void showBuildDialog(final String plotId, String districtId) {
        // ═══ نشوفو رصيد المستخدم ═══
        Citizen me = im.getCitizen();
        if (me == null) return;

        final int myBalance = wm.getBalance();

        // ═══ ScrollView مع Dialog ═══
        android.widget.ScrollView scroll = new android.widget.ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0A"));

        LinearLayout rootBox = new LinearLayout(this);
        rootBox.setOrientation(LinearLayout.VERTICAL);
        rootBox.setPadding(40, 40, 40, 40);
        scroll.addView(rootBox);

        // ═══ Header ═══
        TextView header = new TextView(this);
        header.setText(getString(R.string.city_balance) + myBalance + " Đ");
        header.setTextColor(Color.parseColor("#00FF88"));
        header.setTextSize(16);
        header.setTypeface(null, Typeface.BOLD);
        header.setGravity(Gravity.CENTER);
        header.setPadding(0, 0, 0, 20);
        rootBox.addView(header);

        // ═══ نصيحة ═══
        TextView hint = new TextView(this);
        hint.setText(R.string.city_choose_building);
        hint.setTextColor(Color.parseColor("#9E9E9E"));
        hint.setTextSize(14);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, 0, 0, 30);
        rootBox.addView(hint);

        // ═══ نعرضو المبانى حسب الفئة ═══
        List<String> categories = BuildingCatalog.getCategories();
        for (String cat : categories) {
            List<BuildingCatalog.Building> list = BuildingCatalog.getByCategory(cat);
            if (list.isEmpty()) continue;

            // عنوان الفئة
            TextView catTitle = new TextView(this);
            catTitle.setText("═══ " + BuildingCatalog.getCategoryName(cat) + " ═══");
            catTitle.setTextColor(Color.parseColor("#D4AF37"));
            catTitle.setTextSize(15);
            catTitle.setTypeface(null, Typeface.BOLD);
            catTitle.setGravity(Gravity.CENTER);
            catTitle.setPadding(0, 20, 0, 15);
            rootBox.addView(catTitle);

            // المبانى
            for (final BuildingCatalog.Building b : list) {
                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.HORIZONTAL);
                card.setGravity(Gravity.CENTER_VERTICAL);
                card.setBackgroundResource(R.drawable.bg_card);
                card.setPadding(30, 25, 30, 25);

                LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                cardLp.setMargins(0, 8, 0, 8);
                card.setLayoutParams(cardLp);
                card.setElevation(6f);

                // الإيموجي
                TextView emoji = new TextView(this);
                emoji.setText(b.emoji);
                emoji.setTextSize(32);
                emoji.setPadding(0, 0, 20, 0);
                card.addView(emoji);

                // المعلومات
                LinearLayout info = new LinearLayout(this);
                info.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                info.setLayoutParams(infoLp);

                TextView name = new TextView(this);
                name.setText(b.name);
                name.setTextColor(Color.WHITE);
                name.setTextSize(15);
                name.setTypeface(null, Typeface.BOLD);
                info.addView(name);

                TextView desc = new TextView(this);
                desc.setText(b.desc);
                desc.setTextColor(Color.parseColor("#9E9E9E"));
                desc.setTextSize(11);
                desc.setPadding(0, 4, 0, 0);
                info.addView(desc);

                // السعر
                TextView price = new TextView(this);
                price.setText("💰 " + b.price + " Đ");
                boolean canAfford = myBalance >= b.price;
                price.setTextColor(canAfford 
                        ? Color.parseColor("#4CAF50") 
                        : Color.parseColor("#F44336"));
                price.setTextSize(12);
                price.setTypeface(null, Typeface.BOLD);
                price.setPadding(0, 6, 0, 0);
                info.addView(price);

                card.addView(info);

                // زر البناء
                android.widget.Button btn = new android.widget.Button(this);
                btn.setText(canAfford ? "🏗️" : "🔒");
                btn.setTextSize(18);
                btn.setBackgroundResource(canAfford 
                        ? R.drawable.bg_btn_gold_hero 
                        : R.drawable.bg_btn_outline);
                btn.setTextColor(canAfford 
                        ? Color.BLACK 
                        : Color.parseColor("#666666"));
                btn.setEnabled(canAfford);
                btn.setMinWidth(100);
                btn.setMinHeight(100);

                btn.setOnClickListener(v -> {
                    doBuild(me, plotId, b);
                });

                card.addView(btn);
                rootBox.addView(card);
            }
        }

        // ═══ Dialog ═══
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.city_build_title))
                .setView(scroll)
                .setNegativeButton("❌ إغلاق", null)
                .create();

        dialog.show();
    }

    private void doBuild(Citizen me, String plotId, final BuildingCatalog.Building b) {
        CityPlot plot = findPlot(plotId);
        if (plot == null) {
            Toast.makeText(this, getString(R.string.city_plot_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        cm.buildOnPlot(me.nationalId, plot, b.id, b.price, new CityManager.OnDone() {
            @Override public void onSuccess() {
                Toast.makeText(CityMapActivity.this,
                        "✅ تم بناء " + b.emoji + " " + b.name + "!",
                        Toast.LENGTH_LONG).show();
                // نرسلو للـ WebView يحدّث
                if (webView != null) {
                    webView.evaluateJavascript("location.reload();", null);
                }
            }
            @Override public void onError(String msg) {
                Toast.makeText(CityMapActivity.this,
                        "❌ " + msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showMyPlotDialog(final String plotId, String districtId,
                                    String buildingType, int level, int basePrice) {
        int upgradeCost = (level + 1) * 5000;
        new AlertDialog.Builder(this)
                .setTitle(buildingLabel(buildingType) + " (مستوى " + level + ")")
                .setMessage("🏢 المبنى: " + buildingLabel(buildingType) +
                        "\n⭐ المستوى الحالي: " + level +
                        "\n⭐ المستوى القادم: " + (level + 1) +
                        "\n\n💰 تكلفة الترقية: " + upgradeCost + " Đ" +
                        "\n📈 المبنى رايح يتحسن!")
                .setPositiveButton(getString(R.string.city_upgrade), (d, w) -> {
                    Citizen me = im.getCitizen();
                    if (me == null) return;
                    CityPlot plot = findPlot(plotId);
                    if (plot == null) return;

                    cm.upgradeBuilding(me.nationalId, plot, upgradeCost,
                            new CityManager.OnDone() {
                        @Override public void onSuccess() {
                            Toast.makeText(CityMapActivity.this,
                                    getString(R.string.city_upgraded), Toast.LENGTH_LONG).show();
                        }
                        @Override public void onError(String msg) {
                            Toast.makeText(CityMapActivity.this,
                                    "❌ " + msg, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNeutralButton(getString(R.string.city_sell_to_app), (d, w) -> {
                    Citizen me = im.getCitizen();
                    if (me == null) return;
                    CityPlot plot = findPlot(plotId);
                    if (plot == null) return;

                    // 70% من قيمة المبنى الحالي
                    int buildingValue = 0;
                    if (buildingType != null && !buildingType.isEmpty()) {
                        BuildingCatalog.Building b = BuildingCatalog.getById(buildingType);
                        if (b != null) {
                            // القيمة = السعر × المستوى
                            buildingValue = b.price * Math.max(1, level);
                        }
                    }

                    // 70% من (قيمة الأرض + المبنى)
                    int totalValue = basePrice + buildingValue;
                    int refund = (int) (totalValue * 0.7);

                    String summary = "🏢 المبنى: " + BuildingCatalog.getLabel(buildingType) +
                            "\n⭐ المستوى: " + level +
                            "\n💰 قيمة البناء: " + buildingValue + " Đ" +
                            "\n🏞️ قيمة الأرض: " + basePrice + " Đ" +
                            "\n\n📊 الإجمالي: " + totalValue + " Đ" +
                            "\n💵 ستحصل على (70%): " + refund + " Đ";

                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.city_confirm_sell))
                            .setMessage(summary)
                            .setPositiveButton(getString(R.string.city_sell_now), (dd, ww) ->
                                    cm.sellPlotBackToSystem(me.nationalId, plot, refund,
                                            new CityManager.OnDone() {
                                        @Override public void onSuccess() {
                                            Toast.makeText(CityMapActivity.this,
                                                    "✅ تم البيع! استلمت " + refund + " Đ",
                                                    Toast.LENGTH_LONG).show();
                                            if (webView != null) {
                                                webView.evaluateJavascript("location.reload();", null);
                                            }
                                        }
                                        @Override public void onError(String msg) {
                                            Toast.makeText(CityMapActivity.this,
                                                    "❌ " + msg, Toast.LENGTH_LONG).show();
                                        }
                                    }))
                            .setNegativeButton("❌ إلغاء", null)
                            .show();
                })
                .setNegativeButton("إغلاق", null)
                .show();
    }

    private CityPlot findPlot(String id) {
        for (CityPlot p : allPlots) {
            if (p.id.equals(id)) return p;
        }
        return null;
    }

    private String districtLabel(String id) {
        switch (id) {
            case "center": return getString(R.string.city_center);
            case "luxury": return getString(R.string.city_luxury);
            case "garden": return getString(R.string.city_garden);
            case "mid": return getString(R.string.city_mid);
            case "suburb": return getString(R.string.city_suburb);
            case "industrial": return getString(R.string.city_industrial);
        }
        return id;
    }

    private String buildingLabel(String t) {
        if (t == null || t.isEmpty()) return "لا يوجد";
        String label = BuildingCatalog.getLabel(t);
        return label != null ? label : getString(R.string.city_building_generic);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (plotsReg != null) plotsReg.remove();
        if (webView != null) webView.destroy();
    }

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

}
