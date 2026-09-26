package com.ummah.app;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransferManager {
    private static final String PREFS = "ummah_transfers";
    private final SharedPreferences prefs;

    public static class Transfer {
        public String id;
        public String toId;
        public int amount;
        public String date;
        public String note;

        public Transfer(String id, String toId, int amount, String date, String note) {
            this.id = id;
            this.toId = toId;
            this.amount = amount;
            this.date = date;
            this.note = note;
        }
    }

    public TransferManager(Context c) {
        prefs = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public List<Transfer> getSent() {
        List<Transfer> list = new ArrayList<>();
        String raw = prefs.getString("sent", "");
        if (raw.isEmpty()) return list;
        for (String item : raw.split("###")) {
            if (item.isEmpty()) continue;
            String[] p = item.split("\\|\\|");
            if (p.length < 5) continue;
            list.add(new Transfer(p[0], p[1], parseInt(p[2]), p[3], p[4]));
        }
        return list;
    }

    public void addSent(String toId, int amount, String note) {
        List<Transfer> list = getSent();
        String id = "TX" + System.currentTimeMillis();
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date());
        list.add(0, new Transfer(id, toId, amount, date, note));

        StringBuilder sb = new StringBuilder();
        for (Transfer t : list) {
            if (sb.length() > 0) sb.append("###");
            sb.append(t.id).append("||").append(t.toId).append("||")
              .append(t.amount).append("||").append(t.date).append("||")
              .append(t.note);
        }
        prefs.edit().putString("sent", sb.toString()).apply();
    }

    // استقبالات وهمية للاختبار
    public void addFakeReceive(int amount) {
        String raw = prefs.getString("received", "");
        StringBuilder sb = new StringBuilder(raw);
        if (sb.length() > 0) sb.append("###");
        sb.append("RC").append(System.currentTimeMillis()).append("||")
          .append("مجهول").append("||").append(amount).append("||")
          .append(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date()))
          .append("||").append("استقبال تجريبي");
        prefs.edit().putString("received", sb.toString()).apply();
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }
}
