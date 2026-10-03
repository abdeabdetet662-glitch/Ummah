package com.ummah.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * RedeemCodeActivity — استبدال أكواد الهدايا
 * 
 * المستخدم يكتب كود → النظام يتحقق → يضيف الرصيد
 */
public class RedeemCodeActivity extends Activity {

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

    private IdentityManager im;
    private WalletManager wm;
    private FirebaseFirestore db;

    private EditText etCode;
    private TextView tvBalance;
    private Button btnRedeem;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        im = new IdentityManager(this);
        wm = new WalletManager(this);
        db = FirebaseFirestore.getInstance();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(30, 60, 30, 60);
        scroll.addView(root);

        // ═══ Header ═══
        TextView icon = new TextView(this);
        icon.setText("🎁");
        icon.setTextSize(80);
        icon.setGravity(Gravity.CENTER);
        root.addView(icon);

        TextView title = new TextView(this);
        title.setText("استبدال كود");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 20, 0, 10);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("عندك كود من الإدارة؟ استبدلو دابا!");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(14);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 30);
        root.addView(sub);

        // ═══ بطاقة الرصيد ═══
        LinearLayout balanceCard = new LinearLayout(this);
        balanceCard.setOrientation(LinearLayout.VERTICAL);
        balanceCard.setBackgroundResource(R.drawable.bg_card_gold);
        balanceCard.setPadding(40, 30, 40, 30);
        balanceCard.setGravity(Gravity.CENTER);

        TextView balLbl = new TextView(this);
        balLbl.setText("💰 رصيدك الحالي");
        balLbl.setTextColor(Color.parseColor("#D4AF37"));
        balLbl.setTextSize(14);
        balLbl.setGravity(Gravity.CENTER);
        balanceCard.addView(balLbl);

        tvBalance = new TextView(this);
        tvBalance.setText(wm.getBalance() + " Đ");
        tvBalance.setTextColor(Color.WHITE);
        tvBalance.setTextSize(28);
        tvBalance.setTypeface(null, Typeface.BOLD);
        tvBalance.setGravity(Gravity.CENTER);
        tvBalance.setPadding(0, 10, 0, 0);
        balanceCard.addView(tvBalance);

        LinearLayout.LayoutParams balLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        balLp.setMargins(0, 0, 0, 40);
        balanceCard.setLayoutParams(balLp);
        root.addView(balanceCard);

        // ═══ بطاقة الإدخال ═══
        LinearLayout inputCard = new LinearLayout(this);
        inputCard.setOrientation(LinearLayout.VERTICAL);
        inputCard.setBackgroundResource(R.drawable.bg_card);
        inputCard.setPadding(40, 40, 40, 40);

        TextView inputLbl = new TextView(this);
        inputLbl.setText("🎫 اكتب الكود هنا");
        inputLbl.setTextColor(Color.parseColor("#D4AF37"));
        inputLbl.setTextSize(16);
        inputLbl.setTypeface(null, Typeface.BOLD);
        inputLbl.setGravity(Gravity.CENTER);
        inputCard.addView(inputLbl);

        etCode = new EditText(this);
        etCode.setHint("UMM-XXXX-XXXX");
        etCode.setTextColor(Color.WHITE);
        etCode.setHintTextColor(Color.parseColor("#666666"));
        etCode.setTextSize(22);
        etCode.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        etCode.setBackgroundResource(R.drawable.bg_input);
        etCode.setPadding(30, 40, 30, 40);
        etCode.setGravity(Gravity.CENTER);
        etCode.setSingleLine(true);
        etCode.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

        LinearLayout.LayoutParams etLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        etLp.setMargins(0, 20, 0, 20);
        etCode.setLayoutParams(etLp);
        inputCard.addView(etCode);

        // ═══ زر اللصق ═══
        Button btnPaste = new Button(this);
        btnPaste.setText("📋 لصق من الحافظة");
        btnPaste.setTextSize(13);
        btnPaste.setAllCaps(false);
        btnPaste.setTextColor(Color.parseColor("#D4AF37"));
        btnPaste.setBackgroundResource(R.drawable.bg_btn_outline);
        LinearLayout.LayoutParams pasteLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        btnPaste.setLayoutParams(pasteLp);
        btnPaste.setOnClickListener(v -> {
            try {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (cm.hasPrimaryClip()) {
                    CharSequence text = cm.getPrimaryClip().getItemAt(0).getText();
                    if (text != null) {
                        etCode.setText(text.toString().trim().toUpperCase());
                    }
                }
            } catch (Exception ignored) {}
        });
        inputCard.addView(btnPaste);

        root.addView(inputCard);

        // ═══ زر الاستبدال ═══
        btnRedeem = new Button(this);
        btnRedeem.setText("✨ استبدل الكود");
        btnRedeem.setTextSize(20);
        btnRedeem.setTypeface(null, Typeface.BOLD);
        btnRedeem.setTextColor(Color.BLACK);
        btnRedeem.setBackgroundResource(R.drawable.bg_btn_gold_hero);
        btnRedeem.setMinHeight(160);

        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                160);
        btnLp.setMargins(0, 40, 0, 0);
        btnRedeem.setLayoutParams(btnLp);
        btnRedeem.setOnClickListener(v -> redeemCode());
        root.addView(btnRedeem);

        // ═══ Footer ═══
        TextView footer = new TextView(this);
        footer.setText("⚠️ كل كود يُستعمل مرة واحدة فقط");
        footer.setTextColor(Color.parseColor("#9E9E9E"));
        footer.setTextSize(11);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, 30, 0, 0);
        root.addView(footer);

        setContentView(scroll);
    }

    // ═══════════════════════════════════════════
    //  استبدال الكود
    // ═══════════════════════════════════════════
    private void redeemCode() {
        String code = etCode.getText().toString().trim().toUpperCase();

        if (code.isEmpty()) {
            toast("⚠️ اكتب الكود");
            return;
        }

        Citizen me = im.getCitizen();
        if (me == null) {
            toast("❌ ما راكش مسجل");
            return;
        }

        btnRedeem.setEnabled(false);
        btnRedeem.setText("⏳ جاري التحقق...");

        final String finalCode = code;
        final String myId = me.nationalId;

        db.collection("redeem_codes").document(code).get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) {
                    fail("❌ كود غير صحيح");
                    return;
                }

                Boolean used = doc.getBoolean("used");
                if (used != null && used) {
                    fail("❌ الكود مستعمل من قبل");
                    return;
                }

                Long exp = doc.getLong("expiresAt");
                if (exp != null && exp > 0 && System.currentTimeMillis() > exp) {
                    fail("⏰ الكود منتهي الصلاحية");
                    return;
                }

                // ═══ نجحو → نستبدلو ═══
                Long amountL = doc.getLong("amount");
                int amount = amountL != null ? amountL.intValue() : 0;
                String type = doc.getString("type");
                String note = doc.getString("note");

                if (amount <= 0) {
                    fail("⚠️ مبلغ غير صالح في الكود");
                    return;
                }

                // نحدّثو الرصيد
                db.collection("citizens").document(myId).get()
                    .addOnSuccessListener(userDoc -> {
                        Long balL = userDoc.getLong("balance");
                        int currentBal = balL != null ? balL.intValue() : 0;
                        int newBal = currentBal + amount;

                        // ═══ Transaction ═══
                        db.runTransaction(tx -> {
                            // نحدّثو الكود
                            Map<String, Object> codeUpdate = new HashMap<>();
                            codeUpdate.put("used", true);
                            codeUpdate.put("usedBy", myId);
                            codeUpdate.put("usedAt", System.currentTimeMillis());
                            tx.update(db.collection("redeem_codes").document(finalCode), 
                                    "used", true, 
                                    "usedBy", myId, 
                                    "usedAt", System.currentTimeMillis());

                            // نحدّثو الرصيد
                            tx.update(db.collection("citizens").document(myId), "balance", newBal);

                            return null;
                        }).addOnSuccessListener(v -> {
                            success(amount, note);
                        }).addOnFailureListener(e -> {
                            fail("❌ " + e.getMessage());
                        });
                    })
                    .addOnFailureListener(e -> fail("❌ " + e.getMessage()));
            })
            .addOnFailureListener(e -> fail("❌ " + e.getMessage()));
    }

    private void success(int amount, String note) {
        btnRedeem.setEnabled(true);
        btnRedeem.setText("✨ استبدل الكود");
        etCode.setText("");

        // نحدّثو الرصيد في WalletManager (local cache)
        wm.add(amount);
        tvBalance.setText(wm.getBalance() + " Đ");

        // Dialog النجاح
        new AlertDialog.Builder(this)
            .setTitle("🎉 مبروك!")
            .setMessage("✅ تم استبدال الكود بنجاح!\n\n" +
                    "💰 +" + amount + " Đ\n" +
                    (note != null && !note.isEmpty() ? "📝 " + note : ""))
            .setPositiveButton("🎉 شكراً", null)
            .setCancelable(false)
            .show();
    }

    private void fail(String msg) {
        btnRedeem.setEnabled(true);
        btnRedeem.setText("✨ استبدل الكود");
        toast(msg);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
