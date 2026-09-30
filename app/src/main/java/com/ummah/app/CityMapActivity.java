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
        title.setText("🏙️  مدينة أُمّة");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(22);
        title.setTypeface(null, Typeface.BOLD);
        header.addView(title);

        root.addView(header);

        // WebView
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
        hint.setText("👆 اضغط على قطعة لشرائها أو البناء عليها");
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
                        .setTitle("قطعة مملوكة")
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
                .setTitle("شراء قطعة أرض")
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
                                    "✅ تم الشراء!", Toast.LENGTH_LONG).show();
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
        final String[] buildings = {"house", "villa", "shop", "factory", "palace"};
        final String[] labels = {"🏠 منزل (5,000 Đ)", "🏡 فيلا (15,000 Đ)",
                "🏪 متجر (10,000 Đ)", "🏭 مصنع (20,000 Đ)", "🏰 قصر (50,000 Đ)"};
        final int[] costs = {5000, 15000, 10000, 20000, 50000};

        new AlertDialog.Builder(this)
                .setTitle("بناء مبنى")
                .setItems(labels, (d, which) -> {
                    Citizen me = im.getCitizen();
                    if (me == null) return;
                    CityPlot plot = findPlot(plotId);
                    if (plot == null) return;

                    cm.buildOnPlot(me.nationalId, plot, buildings[which], costs[which],
                            new CityManager.OnDone() {
                        @Override public void onSuccess() {
                            Toast.makeText(CityMapActivity.this,
                                    "✅ تم البناء!", Toast.LENGTH_LONG).show();
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

    private void showMyPlotDialog(final String plotId, String districtId,
                                    String buildingType, int level, int basePrice) {
        int upgradeCost = (level + 1) * 5000;
        new AlertDialog.Builder(this)
                .setTitle("🏢 " + buildingLabel(buildingType) + " (مستوى " + level + ")")
                .setMessage("هذي القطعة ديالك.\n\nهل تريد ترقيتها للمستوى " + (level + 1) +
                        "؟\n💰 التكلفة: " + upgradeCost + " Đ")
                .setPositiveButton("ترقية", (d, w) -> {
                    Citizen me = im.getCitizen();
                    if (me == null) return;
                    CityPlot plot = findPlot(plotId);
                    if (plot == null) return;

                    cm.upgradeBuilding(me.nationalId, plot, upgradeCost,
                            new CityManager.OnDone() {
                        @Override public void onSuccess() {
                            Toast.makeText(CityMapActivity.this,
                                    "✅ تمت الترقية!", Toast.LENGTH_LONG).show();
                        }
                        @Override public void onError(String msg) {
                            Toast.makeText(CityMapActivity.this,
                                    "❌ " + msg, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNeutralButton("بيع للتطبيق (60%)", (d, w) -> {
                    Citizen me = im.getCitizen();
                    if (me == null) return;
                    CityPlot plot = findPlot(plotId);
                    if (plot == null) return;

                    int refund = (int) (basePrice * 0.6);
                    new AlertDialog.Builder(this)
                            .setTitle("تأكيد البيع")
                            .setMessage("ستستلم: " + refund + " Đ")
                            .setPositiveButton("بِع", (dd, ww) ->
                                    cm.sellPlotBackToSystem(me.nationalId, plot, refund,
                                            new CityManager.OnDone() {
                                        @Override public void onSuccess() {
                                            Toast.makeText(CityMapActivity.this,
                                                    "✅ تم البيع!", Toast.LENGTH_LONG).show();
                                        }
                                        @Override public void onError(String msg) {
                                            Toast.makeText(CityMapActivity.this,
                                                    "❌ " + msg, Toast.LENGTH_LONG).show();
                                        }
                                    }))
                            .setNegativeButton("إلغاء", null)
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
            case "center": return "🏛️ الوسط التجاري";
            case "luxury": return "🏙️ الحي الراقي";
            case "garden": return "🌳 الحدائق";
            case "mid": return "🏘️ الحي المتوسط";
            case "suburb": return "🏚️ الضواحي";
            case "industrial": return "🏭 المنطقة الصناعية";
        }
        return id;
    }

    private String buildingLabel(String t) {
        switch (t) {
            case "house": return "🏠 منزل";
            case "villa": return "🏡 فيلا";
            case "palace": return "🏰 قصر";
            case "shop": return "🏪 متجر";
            case "factory": return "🏭 مصنع";
        }
        return "لا يوجد";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (plotsReg != null) plotsReg.remove();
        if (webView != null) webView.destroy();
    }
}
