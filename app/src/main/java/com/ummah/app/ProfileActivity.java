package com.ummah.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.ListenerRegistration;

public class ProfileActivity extends Activity {

    private static final int PICK_IMAGE = 101;

    private FirebaseManager fm;
    private IdentityManager im;
    private Citizen me;
    private ImageView photoView;
    private ListenerRegistration photoReg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        fm = FirebaseManager.get();
        im = new IdentityManager(this);
        me = im.getCitizen();
        if (me == null) { finish(); return; }

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0A"));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(40, 60, 40, 60);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText(getString(R.string.profile_title));
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 30);
        root.addView(title);

        photoView = new ImageView(this);
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(400, 400);
        plp.setMargins(0, 0, 0, 20);
        photoView.setLayoutParams(plp);
        photoView.setBackgroundColor(Color.parseColor("#1E1E1E"));
        photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        root.addView(photoView);

        Button changePhoto = new Button(this);
        changePhoto.setText(getString(R.string.profile_change_photo));
        changePhoto.setTextSize(16);
        changePhoto.setTextColor(Color.WHITE);
        changePhoto.setBackgroundColor(Color.parseColor("#1565C0"));
        changePhoto.setPadding(30, 24, 30, 24);
        changePhoto.setOnClickListener(v -> pickImage());
        root.addView(changePhoto);

        addInfo(root, getString(R.string.profile_name), me.name);
        addInfo(root, getString(R.string.profile_national_id), me.nationalId);
        addInfo(root, getString(R.string.profile_country), CountryList.getName(me.country));
        addInfo(root, getString(R.string.profile_join_date), me.joinDate);

        setContentView(scroll);
        startPhotoListener();
    }

    private void addInfo(LinearLayout parent, String label, String value) {
        TextView l = new TextView(this);
        l.setText(label);
        l.setTextColor(Color.parseColor("#9E9E9E"));
        l.setTextSize(11);
        l.setGravity(Gravity.CENTER);
        l.setPadding(0, 20, 0, 4);
        parent.addView(l);

        TextView v = new TextView(this);
        v.setText(value);
        v.setTextColor(Color.WHITE);
        v.setTextSize(16);
        v.setTypeface(null, Typeface.BOLD);
        v.setGravity(Gravity.CENTER);
        parent.addView(v);
    }

    private void startPhotoListener() {
        photoReg = fm.listenPhoto(me.nationalId, url -> {
            if (url != null && !url.isEmpty()) {
                runOnUiThread(() -> Glide.with(ProfileActivity.this).load(url)
                    .placeholder(android.R.drawable.ic_menu_myplaces)
                    .into(photoView));
            }
        });
    }

    private void pickImage() {
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.profile_choose_image))
            .setItems(new String[]{getString(R.string.profile_gallery)}, (d, w) -> {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                i.setType("image/*");
                startActivityForResult(i, PICK_IMAGE);
            })
            .show();
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == PICK_IMAGE && res == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            Toast.makeText(this, getString(R.string.profile_uploading), Toast.LENGTH_LONG).show();
            fm.uploadToImgBB(me.nationalId, uri, new FirebaseManager.PhotoUploadListener() {
                @Override public void onSuccess(String url) {
                    Toast.makeText(ProfileActivity.this, getString(R.string.profile_updated), Toast.LENGTH_LONG).show();
                    Glide.with(ProfileActivity.this).load(url).into(photoView);
                }
                @Override public void onError(String msg) {
                    Toast.makeText(ProfileActivity.this, "❌ " + msg, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (photoReg != null) photoReg.remove();
    }

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

}
