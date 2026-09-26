package com.ummah.app;

import android.content.Context;
import android.content.SharedPreferences;

public class NotificationCenter {
    private static final String PREFS = "ummah_notifs";
    private final SharedPreferences prefs;

    public NotificationCenter(Context c) {
        prefs = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public long getLastGiftSeen() { return prefs.getLong("last_gift", 0); }
    public void setLastGiftSeen(long t) { prefs.edit().putLong("last_gift", t).apply(); }
    public long getLastChatSeen() { return prefs.getLong("last_chat", 0); }
    public void setLastChatSeen(long t) { prefs.edit().putLong("last_chat", t).apply(); }
    public int getUnreadGifts() { return prefs.getInt("unread_gifts", 0); }
    public void setUnreadGifts(int n) { prefs.edit().putInt("unread_gifts", n).apply(); }
    public int getUnreadMessages() { return prefs.getInt("unread_msgs", 0); }
    public void setUnreadMessages(int n) { prefs.edit().putInt("unread_msgs", n).apply(); }
    public void clearGifts() { prefs.edit().putInt("unread_gifts", 0).apply(); }
    public void clearMessages() { prefs.edit().putInt("unread_msgs", 0).apply(); }
}
