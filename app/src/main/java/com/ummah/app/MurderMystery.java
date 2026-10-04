package com.ummah.app;

/**
 * MurderMystery — جلسة جريمة غامضة
 */
public class MurderMystery {

    public String id;
    public String title;
    public String description;
    public String story;
    public String victimName;
    public String victimStory;
    public String status;      // registration | playing | voting | ended
    public long registrationEnd;
    public long startTime;
    public long endTime;
    public String killerId;    // مخفي عن المستخدمين
    public int entryFee;
    public int prizePool;
    public int maxPlayers;
    public int currentPlayers;
    public String winnerId;
    public boolean solved;
    public long createdAt;

    public MurderMystery() {}

    public boolean isRegistration() { return "registration".equals(status); }
    public boolean isPlaying() { return "playing".equals(status); }
    public boolean isVoting() { return "voting".equals(status); }
    public boolean isEnded() { return "ended".equals(status); }
}
