package com.ummah.app.heist;

/**
 * HeistPlayer — لاعب في فريق السرقة
 */
public class HeistPlayer {

    // معلومات أساسية
    public String userId;
    public String userName;
    public String avatarEmoji = "👤";

    // الدور
    public HeistRole role;

    // الحالة
    public boolean isReady = false;
    public boolean isAlive = true;
    public boolean isTraitor = false; // عميل مزدوج (سري)
    public boolean isHost = false;

    // الإحصائيات في المهمة
    public int hp = 100;
    public int maxHp = 100;
    public int moneyCarried = 0; // المال اللي يحمله
    public int kills = 0;
    public int objectivesDone = 0;

    // الموقع (للمرحلة 3)
    public float x, y;
    public float aimAngle = 0;

    public HeistPlayer() {}

    public HeistPlayer(String userId, String userName, HeistRole role) {
        this.userId = userId;
        this.userName = userName;
        this.role = role;
        this.hp = role.baseHp;
        this.maxHp = role.baseHp;
    }
}
