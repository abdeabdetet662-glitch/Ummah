package com.ummah.app;

import android.content.Context;
import android.content.SharedPreferences;

public class VoteManager {
    private static final String PREFS = "ummah_votes";
    private final SharedPreferences prefs;

    public VoteManager(Context c) {
        prefs = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
    public int getVote(int n) { return prefs.getInt("vote_" + n, 0); }
    public void setVote(int n, int v) { prefs.edit().putInt("vote_" + n, v).apply(); }
    public int getTotalVotes() {
        int c = 0;
        for (int i = 1; i <= 10; i++) if (getVote(i) != 0) c++;
        return c;
    }
}
