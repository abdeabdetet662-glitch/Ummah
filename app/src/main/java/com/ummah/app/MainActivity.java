package com.ummah.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

public class MainActivity extends Activity {

    private NotificationListener notifListener;
    private int unreadNotifs = 0;
    private int unreadChat = 0;
    private com.google.firebase.firestore.ListenerRegistration unreadReg;

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }
    
    


    private IdentityManager im;
    private FirebaseManager fm;
    private WalletManager wm;
    private LinearLayout contentRoot;
    private LinearLayout bottomNav;
    private TextView countView;
    private TextView balanceView;
    private TextView onlineView;
    private Citizen currentCitizen;
    private ListenerRegistration countReg;
    private ListenerRegistration balReg;
    private android.os.Handler heartbeatHandler;

    private int currentTab = BottomNavHelper.TAB_HOME;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        
        // ═══ نظام الإشعارات ═══
        NotificationHelper.createChannels(this);
        
        // طلب إذن الإشعارات (Android 13+)
        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission("android.permission.POST_NOTIFICATIONS")
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    new String[]{"android.permission.POST_NOTIFICATIONS"}, 100);
            }
        }
        
        // بدء الاستماع للإشعارات
        notifListener = new NotificationListener(this);
        
        
        
        
        try {
            IdentityManager imLocal = new IdentityManager(this);
            if (imLocal.isCitizen() && imLocal.getCitizen() != null) {
                String uid = imLocal.getCitizen().nationalId;
                if (uid != null && !uid.isEmpty()) {
                    notifListener.start(uid);

            // ═══ مستمع الأرقام (unread) ═══
            unreadReg = com.google.firebase.firestore.FirebaseFirestore
                    .getInstance()
                    .collection("unread")
                    .document(uid)
                    .addSnapshotListener((snap, e) -> {
                        if (e != null) {
                            android.util.Log.e("UmmahUnread", "خطأ", e);
                            return;
                        }
                        if (snap == null || !snap.exists()) return;
                        
                        Long notifs = snap.getLong("notifications");
                        Long chats = snap.getLong("chat");
                        
                        int newNotifs = notifs != null ? notifs.intValue() : 0;
                        int newChats = chats != null ? chats.intValue() : 0;
                        
                        unreadNotifs = newNotifs;
                        unreadChat = newChats;
                        
                        // حفظ في SharedPreferences
                        getSharedPreferences("notif_state", MODE_PRIVATE)
                                .edit()
                                .putInt("notif_count", newNotifs)
                                .putInt("chat_count", newChats)
                                .apply();
                        
                        // تحديث BottomNav
                        try {
                            BottomNavHelper.updateChatBadge(MainActivity.this, newChats);
                        } catch (Exception ignored) {}
                        
                        // تحديث App Icon Badge
                        try {
                            if (newNotifs > 0) {
                                BadgeHelper.updateAppBadge(MainActivity.this, newNotifs);
                            } else {
                                BadgeHelper.clearAppBadge(MainActivity.this);
                            }
                        } catch (Exception ignored) {}
                        
                        android.util.Log.d("UmmahUnread",
                            "📊 Notifs: " + newNotifs + " | Chats: " + newChats);
                    });
                }
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "خطأ في بدء الإشعارات", e);
        }
        // Feature Flags — تهيئة نظام التحكم في الميزات
        try { FeatureFlags.init(this); } catch (Exception ignored) {}

        im = new IdentityManager(this);
        wm = new WalletManager(this);
        fm = FirebaseManager.get();

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setBackgroundResource(R.drawable.bg_screen);

        // ═══ Top Bar ═══
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(40, 40, 40, 20);
        topBar.setBackgroundColor(Color.parseColor("#0A0A0A"));

        // زر القائمة الجانبية ☰
        TextView menuBtn = new TextView(this);
        menuBtn.setText("☰");
        menuBtn.setTextSize(32);
        menuBtn.setTextColor(Color.parseColor("#D4AF37"));
        menuBtn.setPadding(0, 0, 20, 0);
        menuBtn.setClickable(true);
        menuBtn.setFocusable(true);
        menuBtn.setOnClickListener(v -> openNavDrawer());
        topBar.addView(menuBtn);

        TextView flag = new TextView(this);
        flag.setText("🌍");
        flag.setTextSize(28);
        flag.setPadding(0, 0, 12, 0);
        topBar.addView(flag);

        TextView appName = new TextView(this);
        appName.setText("أُمّة");
        appName.setTextColor(Color.parseColor("#D4AF37"));
        appName.setTextSize(24);
        appName.setTypeface(null, Typeface.BOLD);
        appName.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        topBar.addView(appName);

        main.addView(topBar);

        // ═══ Content Area ═══
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        contentRoot = new LinearLayout(this);
        contentRoot.setOrientation(LinearLayout.VERTICAL);
        contentRoot.setPadding(30, 20, 30, 40);
        scroll.addView(contentRoot);
        main.addView(scroll);

        // ═══ Bottom Nav ═══
        bottomNav = BottomNavHelper.build(this, BottomNavHelper.TAB_HOME,
                new BottomNavHelper.OnTabClick() {
            @Override public void onTab(int tab) {
                handleTabClick(tab);
            }
        });
        main.addView(bottomNav);

        setContentView(main);
        PermissionHelper.requestAll(this);

        // ═══ Firebase ═══
        fm.signIn(new FirebaseManager.OnDone() {
            @Override public void onSuccess() {
                autoSeedOnce();
                if (im.isCitizen()) {
                    currentCitizen = im.getCitizen();
                    syncAndShow(currentCitizen);
                } else {
                    showWelcome();
                }
            }
            @Override public void onError(String msg) {
                Toast.makeText(MainActivity.this, getString(R.string.common_error_connection) + msg, Toast.LENGTH_LONG).show();
                if (im.isCitizen()) {
                    currentCitizen = im.getCitizen();
                    showCard(currentCitizen);
                } else {
                    showWelcome();
                }
            }
        });
    }

    // ═══════════════════════════════════════
    //  Bottom Nav clicks
    // ═══════════════════════════════════════
    private void handleTabClick(int tab) {
        switch (tab) {
            case BottomNavHelper.TAB_MARKET:
                startActivity(new Intent(this, MarketActivity.class));
                break;
            case BottomNavHelper.TAB_CHAT:
                startActivity(new Intent(this, ChatActivity.class));
                break;
            case BottomNavHelper.TAB_PROFILE:
                startActivity(new Intent(this, ProfileActivity.class));
                break;
            case BottomNavHelper.TAB_HOME:
            default:
                // راك في الرئيسية
                break;
        }
    }

    // ═══════════════════════════════════════
    //  Sync + Welcome
    // ═══════════════════════════════════════
    private void syncAndShow(final Citizen c) {
        fm.lookupCitizen(c.nationalId, new FirebaseManager.LookupListener() {
            @Override public void onFound(String name) { showCard(c); }
            @Override public void onNotFound() {
                Toast.makeText(MainActivity.this, getString(R.string.syncing),
                        Toast.LENGTH_SHORT).show();
                fm.registerCitizen(c, wm.getBalance(), new FirebaseManager.OnDone() {
                    @Override public void onSuccess() { showCard(c); }
                    @Override public void onError(String msg) {
                        Toast.makeText(MainActivity.this, getString(R.string.common_error_prefix) + msg, Toast.LENGTH_LONG).show();
                        showCard(c);
                    }
                });
            }
        });
    }

    private void showWelcome() {
        contentRoot.removeAllViews();

        // ═══ Hero ═══
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(Gravity.CENTER);
        hero.setPadding(0, 60, 0, 40);

        TextView flag = new TextView(this);
        flag.setText("🌍");
        flag.setTextSize(100);
        flag.setGravity(Gravity.CENTER);
        hero.addView(flag);

        TextView title = new TextView(this);
        title.setText("أُمّة");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(60);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 20, 0, 8);
        hero.addView(title);

        TextView sub = new TextView(this);
        sub.setText(R.string.main_subtitle);
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(15);
        sub.setGravity(Gravity.CENTER);
        hero.addView(sub);

        contentRoot.addView(hero);

        // ═══ Counter ═══
        LinearLayout counter = new LinearLayout(this);
        counter.setOrientation(LinearLayout.VERTICAL);
        counter.setGravity(Gravity.CENTER);
        counter.setBackgroundResource(R.drawable.bg_feature_card);
        counter.setPadding(40, 40, 40, 40);

        TextView cLabel = new TextView(this);
        cLabel.setText(R.string.main_citizens);
        cLabel.setTextColor(Color.parseColor("#9E9E9E"));
        cLabel.setTextSize(12);
        cLabel.setGravity(Gravity.CENTER);
        counter.addView(cLabel);

        countView = new TextView(this);
        countView.setText("...");
        countView.setTextColor(Color.parseColor("#D4AF37"));
        countView.setTextSize(42);
        countView.setTypeface(null, Typeface.BOLD);
        countView.setGravity(Gravity.CENTER);
        countView.setPadding(0, 8, 0, 0);
        counter.addView(countView);

        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        clp.setMargins(0, 20, 0, 30);
        counter.setLayoutParams(clp);

        contentRoot.addView(counter);
        AnimHelper.fadeInUp(counter, 100);

        startCountListener();

        // ═══ Join button ═══
        android.widget.Button join = UiHelper.primaryButton(this, "🚀  انضم إلى الأمة");
        join.setMinHeight(160);
        join.setTextSize(20);
        join.setOnClickListener(v -> {
            AnimHelper.pressEffect(v);
            AnimHelper.mediumHaptic(this);
            askName();
        });
        contentRoot.addView(join);
        AnimHelper.fadeInUp(join, 200);

        // ═══ Hint ═══
        TextView hint = new TextView(this);
        hint.setText(R.string.main_join_hint);
        hint.setTextColor(Color.parseColor("#666666"));
        hint.setTextSize(12);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, 30, 0, 0);
        contentRoot.addView(hint);
    }

    // ═══════════════════════════════════════
    //  Home Tab (with HomeTabBuilder)
    // ═══════════════════════════════════════
    private void showCard(Citizen c) {
        currentCitizen = c;
        contentRoot.removeAllViews();

        String countryName = CountryList.getName(c.country);

        HomeTabBuilder builder = new HomeTabBuilder(this, contentRoot);
        builder.build(c, countryName, new HomeTabBuilder.OnHomeReady() {
            @Override public void onReady(TextView balanceTV, TextView countTV, TextView onlineTV) {
                balanceView = balanceTV;
                countView = countTV;
                onlineView = onlineTV;
                startHomeListeners();
            }
        });
    }

    // ═══════════════════════════════════════
    //  Listeners
    // ═══════════════════════════════════════
    private void startCountListener() {
        if (countReg != null) countReg.remove();
        countReg = fm.listenCitizensCount(cnt -> runOnUiThread(() -> {
            if (countView != null) countView.setText(String.valueOf(cnt));
        }));
    }

    private void startHomeListeners() {
        // Count
        if (countReg != null) countReg.remove();
        countReg = fm.listenCitizensCount(cnt -> runOnUiThread(() -> {
            if (countView != null) countView.setText(String.valueOf(cnt));
        }));

        // Balance
        if (balReg != null) balReg.remove();
        if (currentCitizen != null) {
            balReg = fm.listenBalance(currentCitizen.nationalId, new FirebaseManager.BalanceListener() {
                @Override public void onBalance(final int balance) {
                    runOnUiThread(() -> {
                        if (balanceView != null) balanceView.setText(balance + " Đ");
                    });
                }
                @Override public void onError(String m) {}
            });
        }

        // Online (نستعملو متصلين الآن — حاليًا نعرضو "—")
        if (onlineView != null) {
            onlineView.setText("—");
        }
    }

    @Override
    protected void onDestroy() {
        if (notifListener != null) notifListener.stop();
        if (unreadReg != null) unreadReg.remove();
if (countReg != null) countReg.remove();
        if (balReg != null) balReg.remove();
        super.onDestroy();
    
    }

    // ═══════════════════════════════════════
    //  Registration Flow
    // ═══════════════════════════════════════
    private void askName() {
        EditText input = UiHelper.input(this, getString(R.string.hint_name));

        LinearLayout c = new LinearLayout(this);
        c.setPadding(40, 20, 40, 20);
        c.addView(input);

        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_choose_name))
            .setView(c)
            .setPositiveButton(getString(R.string.btn_continue), (d, w) -> {
                String n = input.getText().toString().trim();
                if (n.isEmpty()) n = getString(R.string.unknown_citizen);
                askCountry(n);
            })
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show();
    }

    private void askCountry(final String name) {
        final String detected = im.detectCountry(this);
        final String detectedName = CountryList.getName(detected);

        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_country))
            .setMessage(getString(R.string.country_detected, detectedName))
            .setPositiveButton(getString(R.string.btn_yes), (d, w) -> registerNow(name, detected))
            .setNegativeButton(getString(R.string.btn_manual), (d, w) -> showCountryPicker(name))
            .setCancelable(false)
            .show();
    }

    private void showCountryPicker(final String name) {
        final java.util.Map<String, String> countries = CountryList.getCountries();
        final String[] keys = countries.keySet().toArray(new String[0]);
        final String[] labels = new String[keys.length];
        for (int i = 0; i < keys.length; i++) labels[i] = countries.get(keys[i]);

        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_choose_country))
            .setItems(labels, (d, which) -> registerNow(name, keys[which]))
            .setNegativeButton(getString(R.string.btn_back), (d, w) -> askCountry(name))
            .show();
    }

    private void registerNow(String name, String country) {
        Citizen citizen = im.registerCitizen(name, country);
        currentCitizen = citizen;
        fm.registerCitizen(citizen, wm.getBalance(), new FirebaseManager.OnDone() {
            @Override public void onSuccess() {
                String hash = im.hashSeed(citizen.seedPhrase);
                fm.saveSeedHash(citizen.nationalId, hash);
                showSeedDialog(citizen);
            }
            @Override public void onError(String msg) {
                Toast.makeText(MainActivity.this, getString(R.string.common_error_prefix) + msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showSeedDialog(final Citizen c) {
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_seed_title))
            .setMessage(getString(R.string.seed_warning, c.seedPhrase))
            .setNeutralButton(getString(R.string.btn_copy), (d, w) -> {
                android.content.ClipboardManager cm = (android.content.ClipboardManager)
                        getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(android.content.ClipData.newPlainText("Seed", c.seedPhrase));
                Toast.makeText(MainActivity.this, getString(R.string.seed_copied),
                        Toast.LENGTH_LONG).show();
                showSeedDialog(c);
            })
            .setPositiveButton(getString(R.string.btn_saved), (d, w) -> {
                showCard(c);
                Toast.makeText(this, getString(R.string.welcome_toast), Toast.LENGTH_LONG).show();
            })
            .setCancelable(false)
            .show();
    }

    // ═══════════════════════════════════════
    //  Heartbeat
    // ═══════════════════════════════════════
    private void startHeartbeat(final Citizen c) {
        if (heartbeatHandler != null) return;
        heartbeatHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        final Runnable task = new Runnable() {
            @Override public void run() {
                fm.updateLastSeen(c.nationalId);
                if (heartbeatHandler != null) heartbeatHandler.postDelayed(this, 30000);
            }
        };
        heartbeatHandler.post(task);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Citizen c = im.getCitizen();
        if (c != null) fm.setOffline(c.nationalId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Citizen c = im.getCitizen();
        if (c != null) fm.updateLastSeen(c.nationalId);
    }

    

    private void autoSeedOnce() {
        try {
            com.google.firebase.firestore.FirebaseFirestore db =
                    com.google.firebase.firestore.FirebaseFirestore.getInstance();

            // نتحققو من Firestore مباشرة (ماشي من الـ flag)
            db.collection("wheel_segments").limit(1).get()
                .addOnSuccessListener(q -> {
                    if (q.isEmpty()) {
                        // ما فيهش بيانات → نعبيو
                        WheelSeed.seed(db);
                        android.util.Log.d("UMMAH", "✅ Wheel seed done");
                    } else {
                        android.util.Log.d("UMMAH", "✅ Wheel segments already exist");
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("UMMAH", "❌ Wheel check failed: " + e.getMessage());
                });
        } catch (Exception e) {
            android.util.Log.e("UMMAH", "❌ autoSeedOnce error", e);
        }
    }

    // ═══════════════════════════════════════
    //  القائمة الجانبية (Nav Drawer)
    // ═══════════════════════════════════════
    private void openNavDrawer() {
        // قراءة القيم المحدّثة من SharedPreferences
        int freshNotif = getSharedPreferences("notif_state", MODE_PRIVATE)
                .getInt("notif_count", unreadNotifs);
        int freshChat = getSharedPreferences("notif_state", MODE_PRIVATE)
                .getInt("chat_count", unreadChat);
        unreadNotifs = freshNotif;
        unreadChat = freshChat;
        
        NavDrawerHelper.show(this, currentCitizen, wm.getBalance(), freshNotif, freshChat, new NavDrawerHelper.OnDrawerClick() {
            @Override
            public void onItem(int itemId, String title) {
                handleDrawerClick(itemId, title);
            }
        });
    }

    private void handleDrawerClick(int itemId, String title) {
        switch (itemId) {
            case NavDrawerHelper.ITEM_HOME:
                handleTabClick(BottomNavHelper.TAB_HOME);
                break;
            case NavDrawerHelper.ITEM_CITY:
                try { startActivity(new Intent(this, CityMapActivity.class)); } catch (Exception ignored) {}
                break;
            case NavDrawerHelper.ITEM_PARLIAMENT:
                try { startActivity(new Intent(this, ParliamentActivity.class)); } catch (Exception ignored) {}
                break;
            case NavDrawerHelper.ITEM_ELECTIONS:
                try { startActivity(new Intent(this, ElectionActivity.class)); } catch (Exception ignored) {}
                break;
            case NavDrawerHelper.ITEM_MARKET:
                try { startActivity(new Intent(this, MarketActivity.class)); } catch (Exception ignored) {}
                break;
            case NavDrawerHelper.ITEM_JOBS:
                try { startActivity(new Intent(this, JobsActivity.class)); } catch (Exception ignored) {}
                break;
            case NavDrawerHelper.ITEM_GIFTS:
                try { startActivity(new Intent(this, GiftsActivity.class)); } catch (Exception ignored) {}
                break;
            case NavDrawerHelper.ITEM_LEADERBOARD:
                try { startActivity(new Intent(this, LeaderboardActivity.class)); } catch (Exception ignored) {}
                break;
            case NavDrawerHelper.ITEM_NEWS:
                try { startActivity(new Intent(this, NewsActivity.class)); } catch (Exception ignored) {}
                break;
            case NavDrawerHelper.ITEM_STATS:
                try { startActivity(new Intent(this, StatsActivity.class)); } catch (Exception ignored) {}
                break;
            case NavDrawerHelper.ITEM_PROFILE:
                try { startActivity(new Intent(this, ProfileActivity.class)); } catch (Exception ignored) {}
                break;
            case NavDrawerHelper.ITEM_SETTINGS:
                try { startActivity(new Intent(this, SettingsActivity.class)); } catch (Exception e) {
                    Toast.makeText(this, R.string.main_settings_soon, Toast.LENGTH_SHORT).show();
                }
                break;
            case NavDrawerHelper.ITEM_ABOUT:
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://abdeabdetet662-glitch.github.io/ummah-website/about.html"));
                    startActivity(i);
                } catch (Exception ignored) {}
                break;
            case NavDrawerHelper.ITEM_WEBSITE:
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://abdeabdetet662-glitch.github.io/ummah-website/"));
                    startActivity(i);
                } catch (Exception ignored) {}
                break;
            case NavDrawerHelper.ITEM_MURDER:
                    startActivity(new Intent(MainActivity.this, MurderMysterySplashActivity.class));
                    break;
                case NavDrawerHelper.ITEM_NOTIFICATIONS:
                try { 
                    startActivity(new Intent(this, NotificationCenterActivity.class)); 
                } catch (Exception e) {
                    Toast.makeText(this, R.string.main_notif_error, Toast.LENGTH_SHORT).show();
                }
                break;
            case NavDrawerHelper.ITEM_REDEEM:
                try { 
                    startActivity(new Intent(this, RedeemCodeActivity.class)); 
                } catch (Exception e) {
                    Toast.makeText(this, R.string.main_page_error, Toast.LENGTH_SHORT).show();
                }
                break;
            case NavDrawerHelper.ITEM_LOGOUT:
                showLogoutDialog();
                break;
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_logout)
                .setMessage("واش راك متأكد؟ رايح تحتاج كلماتك السرية للدخول مرة ثانية.")
                .setPositiveButton("تسجيل الخروج", (d, w) -> {
                    getSharedPreferences("ummah_prefs", MODE_PRIVATE).edit().clear().apply();
                    getSharedPreferences("ummah", MODE_PRIVATE).edit().clear().apply();
                    Intent intent = new Intent(this, WelcomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }
}
