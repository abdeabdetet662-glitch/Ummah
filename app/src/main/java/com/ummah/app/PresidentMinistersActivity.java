package com.ummah.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class PresidentMinistersActivity extends Activity {

    private IdentityManager im;
    private PresidentManager pm;
    private LinearLayout container;
    private ListenerRegistration reg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        im = new IdentityManager(this);
        pm = new PresidentManager();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 50, 30, 60);
        scroll.addView(root);

        TextView title = UiHelper.goldTitle(this, "👥  تعيين الوزراء", 26);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("اختر مواطنين لمناصب وزارية");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 24);
        root.addView(sub);

        Button addBtn = UiHelper.primaryButton(this, "➕  تعيين وزير جديد");
        addBtn.setOnClickListener(v -> showAppointDialog());
        root.addView(addBtn);

        TextView listTitle = new TextView(this);
        listTitle.setText("🏛️  الوزراء الحاليون");
        listTitle.setTextColor(Color.parseColor("#D4AF37"));
        listTitle.setTextSize(15);
        listTitle.setTypeface(null, Typeface.BOLD);
        listTitle.setGravity(Gravity.CENTER);
        listTitle.setPadding(0, 32, 0, 16);
        root.addView(listTitle);

        container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        root.addView(container);

        setContentView(scroll);
        startListener();
    }

    private void startListener() {
        if (reg != null) reg.remove();
        reg = pm.listenAllCitizensForPresident(new PresidentManager.PresidentListListener() {
            @Override public void onList(List<PresidentManager.PresidentCitizen> list) {
                runOnUiThread(() -> render(list));
            }
        });
    }

    private void render(List<PresidentManager.PresidentCitizen> list) {
        container.removeAllViews();
        boolean any = false;
        for (PresidentManager.PresidentCitizen pc : list) {
            if (pc.isMinister) {
                any = true;
                addMinisterCard(pc);
            }
        }
        if (!any) {
            TextView empty = new TextView(this);
            empty.setText("ما فيه وزراء حالياً");
            empty.setTextColor(Color.parseColor("#9E9E9E"));
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 40, 0, 0);
            container.addView(empty);
        }
    }

    private void addMinisterCard(final PresidentManager.PresidentCitizen pc) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_president_card);
        card.setPadding(24, 24, 24, 24);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 8, 0, 8);
        card.setLayoutParams(lp);

        TextView name = new TextView(this);
        name.setText("👤  " + pc.name);
        name.setTextColor(Color.WHITE);
        name.setTextSize(17);
        name.setTypeface(null, Typeface.BOLD);
        card.addView(name);

        TextView role = new TextView(this);
        role.setText("🏛️  " + (pc.ministerRole != null ? pc.ministerRole : "وزير"));
        role.setTextColor(Color.parseColor("#FFD700"));
        role.setTextSize(13);
        role.setPadding(0, 6, 0, 12);
        card.addView(role);

        Button remove = UiHelper.dangerButton(this, "🚫  إلغاء التعيين");
        remove.setOnClickListener(v -> confirmRemove(pc));
        card.addView(remove);

        container.addView(card);
    }

    private void showAppointDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(40, 20, 40, 20);

        final EditText idInput = UiHelper.input(this, "الرقم الوطني");
        box.addView(idInput);

        final EditText roleInput = UiHelper.input(this, "المنصب (وزير المالية...)");
        box.addView(roleInput);

        new AlertDialog.Builder(this)
                .setTitle("👥  تعيين وزير")
                .setView(box)
                .setPositiveButton("تعيين", (d, w) -> {
                    String id = idInput.getText().toString().trim();
                    String role = roleInput.getText().toString().trim();
                    if (id.isEmpty() || role.isEmpty()) {
                        Toast.makeText(this, "أكمل الحقول", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Citizen me = im.getCitizen();
                    if (me == null) return;
                    pm.appointMinister(me.nationalId, me.name, id, role,
                            new PresidentManager.OnDone() {
                        @Override public void onSuccess() {
                            Toast.makeText(PresidentMinistersActivity.this,
                                    "✅ تم التعيين!", Toast.LENGTH_LONG).show();
                        }
                        @Override public void onError(String m) {
                            Toast.makeText(PresidentMinistersActivity.this,
                                    "❌ " + m, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void confirmRemove(final PresidentManager.PresidentCitizen pc) {
        new AlertDialog.Builder(this)
                .setTitle("إلغاء التعيين")
                .setMessage("إلغاء منصب: " + pc.name + "؟")
                .setPositiveButton("إلغاء التعيين", (d, w) -> {
                    Citizen me = im.getCitizen();
                    if (me == null) return;
                    pm.removeMinister(me.nationalId, pc.nationalId,
                            new PresidentManager.OnDone() {
                        @Override public void onSuccess() {
                            Toast.makeText(PresidentMinistersActivity.this,
                                    "✅ تم الإلغاء", Toast.LENGTH_LONG).show();
                        }
                        @Override public void onError(String m) {
                            Toast.makeText(PresidentMinistersActivity.this,
                                    "❌ " + m, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("رجوع", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) reg.remove();
    }
}
