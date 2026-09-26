package com.ummah.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;

public class ProposalManager {
    private static final String PREFS = "ummah_proposals";
    private final SharedPreferences prefs;

    public ProposalManager(Context c) {
        prefs = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public List<Proposal> getAll() {
        List<Proposal> list = new ArrayList<>();
        String raw = prefs.getString("list", "");
        if (raw.isEmpty()) return list;
        String[] items = raw.split("###");
        for (String item : items) {
            if (item.isEmpty()) continue;
            String[] parts = item.split("\\|\\|");
            if (parts.length < 6) continue;
            Proposal p = new Proposal(parts[0], parts[1], parts[2], parts[3]);
            p.yes = parseInt(parts[4]);
            p.no = parseInt(parts[5]);
            list.add(p);
        }
        return list;
    }

    public void add(Proposal p) {
        List<Proposal> list = getAll();
        list.add(p);
        save(list);
    }

    public void vote(String id, boolean yes) {
        List<Proposal> list = getAll();
        for (Proposal p : list) {
            if (p.id.equals(id)) {
                String key = "myvote_" + id;
                if (prefs.contains(key)) return;
                if (yes) p.yes++; else p.no++;
                prefs.edit().putBoolean(key, true).apply();
            }
        }
        save(list);
    }

    public boolean hasVoted(String id) {
        return prefs.contains("myvote_" + id);
    }

    private void save(List<Proposal> list) {
        StringBuilder sb = new StringBuilder();
        for (Proposal p : list) {
            if (sb.length() > 0) sb.append("###");
            sb.append(p.id).append("||").append(p.title).append("||")
              .append(p.body).append("||").append(p.author).append("||")
              .append(p.yes).append("||").append(p.no);
        }
        prefs.edit().putString("list", sb.toString()).apply();
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }
}
