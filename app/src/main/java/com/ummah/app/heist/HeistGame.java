package com.ummah.app.heist;

import java.util.ArrayList;
import java.util.List;

/**
 * HeistGame — جلسة سرقة القرن
 */
public class HeistGame {

    // ═══ States ═══
    public static final int STATE_LOBBY = 0;
    public static final int STATE_BRIEFING = 1;
    public static final int STATE_PLANNING = 2;
    public static final int STATE_HEIST = 3;
    public static final int STATE_ESCAPE = 4;
    public static final int STATE_RESULT = 5;

    // ═══ Team size ═══
    public static final int MIN_PLAYERS = 3;
    public static final int MAX_PLAYERS = 5;

    // ═══ Reward system ═══
    public static final int ENTRY_FEE = 500;
    public static final int TOTAL_LOOT = 100_000; // 100,000 Đ
    public static final int TRAITOR_BONUS = 200_000; // مكافأة الخائن

    // ═══ Data ═══
    public String gameId;
    public int state = STATE_LOBBY;
    public long createdAt;
    public String hostId;

    public List<HeistPlayer> players = new ArrayList<>();
    public List<HeistRole> availableRoles = new ArrayList<>();

    // ميزات الخائن
    public String traitorId; // UID الخائن (سري)

    // النتيجة
    public boolean heistSuccess = false;
    public int moneyEscaped = 0;
    public long durationSeconds = 0;

    public HeistGame() {
        resetRoles();
    }

    public void resetRoles() {
        availableRoles.clear();
        for (HeistRole r : HeistRole.values()) {
            availableRoles.add(r);
        }
    }

    public HeistPlayer getPlayer(String userId) {
        for (HeistPlayer p : players) {
            if (p.userId != null && p.userId.equals(userId)) return p;
        }
        return null;
    }

    public HeistPlayer getHost() {
        for (HeistPlayer p : players) {
            if (p.isHost) return p;
        }
        return null;
    }

    public boolean isFull() {
        return players.size() >= MAX_PLAYERS;
    }

    public boolean canStart() {
        return players.size() >= MIN_PLAYERS;
    }

    public int getRoleCount(HeistRole role) {
        int count = 0;
        for (HeistPlayer p : players) {
            if (p.role == role) count++;
        }
        return count;
    }

    public boolean isRoleTaken(HeistRole role) {
        return getRoleCount(role) > 0;
    }
}
