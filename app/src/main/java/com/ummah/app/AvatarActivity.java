package com.ummah.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration;

public class AvatarActivity extends Activity {

    private IdentityManager im;
    private AvatarManager am;
    private LinearLayout previewBox;
    private Avatar current;
    private ListenerRegistration reg;
    private TextView previewEmoji;
    private TextView previewName;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        im = new IdentityManager(this);
        am = new AvatarManager();
        current = new Avatar();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 50, 30, 60);
        scroll.addView(root);

        TextView title = UiHelper.goldTitle(this, "🎨 شخصيتي", 28);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("بدّل ملابسك ولون شعرك ومظهرك");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 24);
        root.addView(sub);

        // Preview
        previewBox = UiHelper.goldCard(this);
        previewBox.setGravity(Gravity.CENTER);

        previewEmoji = new TextView(this);
        previewEmoji.setText("🧑");
        previewEmoji.setTextSize(140);
        previewEmoji.setGravity(Gravity.CENTER);
        previewBox.addView(previewEmoji);

        previewName = new TextView(this);
        previewName.setText("");
        previewName.setTextColor(Color.parseColor("#D4AF37"));
        previewName.setTextSize(14);
        previewName.setTypeface(null, Typeface.BOLD);
        previewName.setGravity(Gravity.CENTER);
        previewName.setPadding(0, 12, 0, 0);
        previewBox.addView(previewName);

        root.addView(previewBox);

        // Sections
        addSection(root, "🎨 لون البشرة", new String[]{"فاتح", "متوسط", "داكن", "بني"},
                new String[]{"light", "medium", "dark", "brown"}, "skin");

        addSection(root, "💇 تسريحة الشعر", new String[]{"قصير", "طويل", "أصلع", "مجعد"},
                new String[]{"short", "long", "bald", "curly"}, "hairStyle");

        addSection(root, "🎨 لون الشعر", new String[]{"أسود", "بني", "أشقر", "أحمر"},
                new String[]{"black", "brown", "blond", "red"}, "hairColor");

        addSection(root, "👕 القميص", new String[]{"تي شيرت", "قميص رسمي", "فستان", "جاكيت"},
                new String[]{"👕", "👔", "👗", "🧥"}, "shirtEmoji");

        addSection(root, "👖 البنطال", new String[]{"جينز", "شورت"},
                new String[]{"👖", "🩳"}, "pantsEmoji");

        addSection(root, "👟 الحذاء", new String[]{"رياضي", "كلاسيك", "بوت"},
                new String[]{"👟", "👞", "🥾"}, "shoesEmoji");

        addSection(root, "🎩 إكسسوار", new String[]{"بدون", "نظارات", "قبعة", "طاقية"},
                new String[]{"", "🕶️", "🎩", "🧢"}, "accessoryEmoji");

        // زر حفظ
        Button saveBtn = UiHelper.primaryButton(this, "💾  حفظ شخصيتي");
        saveBtn.setOnClickListener(v -> save());
        root.addView(saveBtn);

        setContentView(scroll);

        startListener();
    }

    private void startListener() {
        Citizen c = im.getCitizen();
        if (c == null) { finish(); return; }
        if (reg != null) reg.remove();
        reg = am.listenAvatar(c.nationalId, new AvatarManager.AvatarListener() {
            @Override public void onAvatar(Avatar avatar) {
                current = avatar;
                runOnUiThread(() -> updatePreview());
            }
            @Override public void onError(String msg) {}
        });
    }

    private void addSection(LinearLayout root, String title, final String[] labels,
                             final String[] values, final String field) {
        LinearLayout card = UiHelper.card(this);

        TextView t = new TextView(this);
        t.setText(title);
        t.setTextColor(Color.parseColor("#D4AF37"));
        t.setTextSize(14);
        t.setTypeface(null, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, 0, 0, 12);
        card.addView(t);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        for (int i = 0; i < labels.length; i++) {
            final String value = values[i];
            final String label = labels[i];

            Button optBtn = new Button(this);
            optBtn.setText(label);
            optBtn.setTextSize(11);
            optBtn.setAllCaps(false);
            optBtn.setPadding(20, 20, 20, 20);
            optBtn.setBackgroundResource(R.drawable.bg_btn_outline);
            optBtn.setTextColor(Color.parseColor("#D4AF37"));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(4, 0, 4, 0);
            optBtn.setLayoutParams(lp);

            optBtn.setOnClickListener(v -> {
                applyField(field, value);
                updatePreview();
            });

            row.addView(optBtn);
        }

        card.addView(row);
        root.addView(card);
    }

    private void applyField(String field, String value) {
        switch (field) {
            case "skin": current.skinColor = value; break;
            case "hairStyle": current.hairStyle = value; break;
            case "hairColor": current.hairColor = value; break;
            case "shirtEmoji": current.shirtEmoji = value; break;
            case "pantsEmoji": current.pantsEmoji = value; break;
            case "shoesEmoji": current.shoesEmoji = value; break;
            case "accessoryEmoji": current.accessoryEmoji = value; break;
        }
    }

    private void updatePreview() {
        if (previewEmoji == null) return;

        // نبنيو الشخصية من الإيموجي
        StringBuilder sb = new StringBuilder();
        if (current.accessoryEmoji != null && !current.accessoryEmoji.isEmpty()) {
            sb.append(current.accessoryEmoji);
        } else {
            sb.append("🧑");
        }

        previewEmoji.setText(sb.toString());

        // اسم الشخصية
        String skin = arabicSkin(current.skinColor);
        String hair = arabicHair(current.hairStyle, current.hairColor);
        String shirt = arabicShirt(current.shirtEmoji);
        String pants = arabicPants(current.pantsEmoji);
        String shoes = arabicShoes(current.shoesEmoji);

        previewName.setText(skin + " • " + hair + "\n" + shirt + " + " + pants + " + " + shoes);
    }

    private String arabicSkin(String s) {
        if (s == null) return "";
        switch (s) {
            case "light": return "بشرة فاتحة";
            case "medium": return "بشرة متوسطة";
            case "dark": return "بشرة داكنة";
            case "brown": return "بشرة بنية";
        }
        return "";
    }

    private String arabicHair(String style, String color) {
        String st = "";
        if ("short".equals(style)) st = "شعر قصير";
        else if ("long".equals(style)) st = "شعر طويل";
        else if ("bald".equals(style)) st = "أصلع";
        else if ("curly".equals(style)) st = "شعر مجعد";

        String c = "";
        if ("black".equals(color)) c = "أسود";
        else if ("brown".equals(color)) c = "بني";
        else if ("blond".equals(color)) c = "أشقر";
        else if ("red".equals(color)) c = "أحمر";

        return st + " " + c;
    }

    private String arabicShirt(String s) {
        if ("👕".equals(s)) return "تي شيرت";
        if ("👔".equals(s)) return "قميص";
        if ("👗".equals(s)) return "فستان";
        if ("🧥".equals(s)) return "جاكيت";
        return "";
    }

    private String arabicPants(String s) {
        if ("👖".equals(s)) return "بنطال";
        if ("🩳".equals(s)) return "شورت";
        return "";
    }

    private String arabicShoes(String s) {
        if ("👟".equals(s)) return "حذاء رياضي";
        if ("👞".equals(s)) return "حذاء كلاسيك";
        if ("🥾".equals(s)) return "بوت";
        return "";
    }

    private void save() {
        Citizen c = im.getCitizen();
        if (c == null) return;
        am.saveAvatar(c.nationalId, current, new AvatarManager.OnDone() {
            @Override public void onSuccess() {
                Toast.makeText(AvatarActivity.this, "✅ تم حفظ شخصيتك!", Toast.LENGTH_LONG).show();
            }
            @Override public void onError(String msg) {
                Toast.makeText(AvatarActivity.this, "❌ " + msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) reg.remove();
    }
}
