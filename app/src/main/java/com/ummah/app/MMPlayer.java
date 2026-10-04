package com.ummah.app;

/**
 * MMPlayer — لاعب في جريمة أُمّة
 */
public class MMPlayer {

    public String userId;
    public String userName;
    public String role;        // killer | investigator
    public String character;   // شخصية القصة
    public String characterDesc;
    public String avatarEmoji;
    public long joinedAt;
    public String votedFor;
    public int suspicion;      // نسبة الشك
    public String status;      // active | eliminated
    public String lastWords;
    public boolean revealed;   // هل انكشف

    public MMPlayer() {}

    public boolean isKiller() { return "killer".equals(role); }
    public boolean isActive() { return "active".equals(status); }
}
