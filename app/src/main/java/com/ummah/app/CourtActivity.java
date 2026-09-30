package com.ummah.app;

import android.app.Activity;
import android.app.AlertDialog;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CourtActivity extends Activity {
    private FirebaseManager fm;
    private IdentityManager im;
    private LinearLayout listContainer;
    private ListenerRegistration reg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        // Feature Check
        if (!FeatureFlags.checkOrToast(this, "court")) {
            finish();
            return;
        }

        fm = FirebaseManager.get();
        im = new IdentityManager(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0A"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(36, 50, 36, 50);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("⚖️ محكمة أُمّة");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("العدالة للجميع، بلا محاباة");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 8, 0, 30);
        root.addView(sub);

        Button fileBtn = new Button(this);
        fileBtn.setText("📜  رفع دعوى جديدة");
        fileBtn.setTextSize(15);
        fileBtn.setOnClickListener(v -> showFileDialog());
        root.addView(fileBtn);

        TextView sep = new TextView(this);
        sep.setText("\n━━━ سجل القضايا ━━━\n");
        sep.setTextColor(Color.parseColor("#D4AF37"));
        sep.setTextSize(14);
        sep.setTypeface(null, Typeface.BOLD);
        sep.setGravity(Gravity.CENTER);
        sep.setPadding(0, 30, 0, 16);
        root.addView(sep);

        listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
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
        reg = fm.listenCases(list -> runOnUiThread(() -> render(list)));
    }

    private void render(List<FirebaseManager.CourtCase> list) {
        listContainer.removeAllViews();
        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("لا توجد قضايا بعد. الدولة تعيش بسلام!");
            empty.setTextColor(Color.parseColor("#616161"));
            empty.setTextSize(13);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 40, 0, 0);
            listContainer.addView(empty);
            return;
        }
        for (FirebaseManager.CourtCase c : list) {
            listContainer.addView(buildCase(c));
        }
    }

    private LinearLayout buildCase(final FirebaseManager.CourtCase c) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.parseColor("#141414"));
        card.setPadding(28, 22, 28, 22);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 14);
        card.setLayoutParams(lp);

        // الشاكي
        TextView plaintiff = new TextView(this);
        plaintiff.setText("📢 الشاكي: " + c.plaintiffName);
        plaintiff.setTextColor(Color.parseColor("#4CAF50"));
        plaintiff.setTextSize(13);
        plaintiff.setTypeface(null, Typeface.BOLD);
        card.addView(plaintiff);

        // المدعى عليه
        TextView defendant = new TextView(this);
        defendant.setText("👤 المدعى عليه: " + c.defendantName);
        defendant.setTextColor(Color.parseColor("#F44336"));
        defendant.setTextSize(13);
        defendant.setTypeface(null, Typeface.BOLD);
        defendant.setPadding(0, 4, 0, 10);
        card.addView(defendant);

        // الدعوى
        TextView claim = new TextView(this);
        claim.setText("📝 " + c.claim);
        claim.setTextColor(Color.WHITE);
        claim.setTextSize(14);
        claim.setLineSpacing(5, 1);
        claim.setPadding(0, 0, 0, 12);
        card.addView(claim);

        // الحكم
        if (c.recommendation != null && !c.recommendation.isEmpty()) {
            LinearLayout verdict = new LinearLayout(this);
            verdict.setOrientation(LinearLayout.VERTICAL);
            verdict.setBackgroundColor(Color.parseColor("#0B4F2C"));
            verdict.setPadding(20, 14, 20, 14);

            TextView vh = new TextView(this);
            vh.setText("⚖️ الحكم:");
            vh.setTextColor(Color.parseColor("#D4AF37"));
            vh.setTextSize(12);
            vh.setTypeface(null, Typeface.BOLD);
            verdict.addView(vh);

            TextView vt = new TextView(this);
            vt.setText(c.recommendation);
            vt.setTextColor(Color.WHITE);
            vt.setTextSize(13);
            vt.setPadding(0, 6, 0, 0);
            verdict.addView(vt);

            card.addView(verdict);
        }

        // الحالة
        TextView status = new TextView(this);
        String statusText;
        int statusColor;
        if ("agreed".equals(c.status)) {
            statusText = "✅ تم الاتفاق";
            statusColor = Color.parseColor("#4CAF50");
        } else if ("appealed".equals(c.status)) {
            statusText = "⚠️ استئناف";
            statusColor = Color.parseColor("#FF9800");
        } else if ("closed".equals(c.status)) {
            statusText = "🔒 مغلقة";
            statusColor = Color.parseColor("#9E9E9E");
        } else {
            statusText = "⏳ قيد النظر";
            statusColor = Color.parseColor("#2196F3");
        }
        status.setText(statusText);
        status.setTextColor(statusColor);
        status.setTextSize(12);
        status.setPadding(0, 12, 0, 8);
        card.addView(status);

        // التاريخ
        TextView date = new TextView(this);
        date.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date(c.timestamp)));
        date.setTextColor(Color.parseColor("#616161"));
        date.setTextSize(10);
        card.addView(date);

        // أزرار القرار (فقط إذا كنت طرفاً في القضية)
        Citizen me = im.getCitizen();
        if (me != null && "open".equals(c.status)) {
            boolean isInvolved = me.nationalId.equals(c.plaintiffId) || me.nationalId.equals(c.defendantId);
            if (isInvolved) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(0, 12, 0, 0);

                Button agreeBtn = new Button(this);
                agreeBtn.setText("✅ أوافق على الحكم");
                agreeBtn.setTextSize(12);
                agreeBtn.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                agreeBtn.setOnClickListener(v -> updateStatus(c.id, "agreed"));
                row.addView(agreeBtn);

                Button appealBtn = new Button(this);
                appealBtn.setText("⚠️ أستأنف");
                appealBtn.setTextSize(12);
                appealBtn.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                appealBtn.setOnClickListener(v -> updateStatus(c.id, "appealed"));
                row.addView(appealBtn);

                card.addView(row);
            }
        }

        return card;
    }

    private void updateStatus(final String caseId, final String status) {
        fm.updateCaseStatus(caseId, status, new FirebaseManager.OnDone() {
            @Override public void onSuccess() {
                Toast.makeText(CourtActivity.this, "تم التحديث", Toast.LENGTH_SHORT).show();
            }
            @Override public void onError(String msg) {
                Toast.makeText(CourtActivity.this, "خطأ: " + msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showFileDialog() {
        final Citizen me = im.getCitizen();
        if (me == null) return;

        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(40, 20, 40, 20);

        final EditText idInput = new EditText(this);
        idInput.setHint("رقم المدعى عليه الوطني");
        idInput.setTextColor(Color.WHITE);
        idInput.setHintTextColor(Color.GRAY);
        idInput.setInputType(InputType.TYPE_CLASS_TEXT);
        c.addView(idInput);

        final EditText claimInput = new EditText(this);
        claimInput.setHint("اكتب دعواك بالتفصيل...");
        claimInput.setTextColor(Color.WHITE);
        claimInput.setHintTextColor(Color.GRAY);
        claimInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        claimInput.setMinLines(3);
        c.addView(claimInput);

        new AlertDialog.Builder(this)
            .setTitle("رفع دعوى")
            .setMessage("⚠️ الدعاوى الكاذبة تُعاقب بخصم 50 Đ")
            .setView(c)
            .setPositiveButton("رفع", (d, w) -> {
                String defId = idInput.getText().toString().trim();
                String claim = claimInput.getText().toString().trim();
                if (defId.isEmpty() || claim.isEmpty()) {
                    Toast.makeText(this, "املأ الحقول", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (defId.equals(me.nationalId)) {
                    Toast.makeText(this, "لا يمكنك رفع دعوى ضد نفسك", Toast.LENGTH_SHORT).show();
                    return;
                }
                // ابحث عن المدعى عليه
                fm.lookupCitizen(defId, new FirebaseManager.LookupListener() {
                    @Override public void onFound(String defName) {
                        String rec = generateRecommendation(claim);
                        fm.fileCase(me.nationalId, me.name, defId, defName, claim, rec, new FirebaseManager.OnDone() {
                            @Override public void onSuccess() {
                                Toast.makeText(CourtActivity.this, "📜 تم رفع الدعوى", Toast.LENGTH_LONG).show();
                            }
                            @Override public void onError(String msg) {
                                Toast.makeText(CourtActivity.this, "خطأ: " + msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    @Override public void onNotFound() {
                        Toast.makeText(CourtActivity.this, "❌ المدعى عليه غير موجود", Toast.LENGTH_LONG).show();
                    }
                });
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    private String generateRecommendation(String claim) {
        // توصية مبدئية بسيطة
        return "بعد دراسة الدعوى، تقترح المحكمة:\n" +
               "• حوار سلمي بين الطرفين.\n" +
               "• تعويض عادل إن ثبت الضرر.\n" +
               "• للمدعى عليه حق الرد خلال 3 أيام.\n\n" +
               "يمكن للطرفين الاتفاق أو الاستئناف.";
    }
}
