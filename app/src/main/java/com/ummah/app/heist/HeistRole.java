package com.ummah.app.heist;

/**
 * HeistRole — الأدوار الخمسة في فريق السرقة
 */
public enum HeistRole {

    LEADER(
        "leader",
        "القائد",
        "🎯",
        "يوجّه الفريق ويتخذ القرارات",
        "#D4AF37",   // ذهبي
        100,          // HP
        1.0f,         // Speed multiplier
        "مسدس كاتم صوت",
        25
    ),

    HACKER(
        "hacker",
        "الهاكر",
        "💻",
        "يعطل الأنظمة والكاميرات",
        "#3B82F6",   // أزرق
        80,
        1.15f,
        "مسدس صاعق",
        15
    ),

    SNIPER(
        "sniper",
        "القناص",
        "🎯",
        "يدعم الفريق من بعيد",
        "#8B5CF6",   // بنفسجي
        90,
        0.85f,
        "قناصة دقيقة",
        75
    ),

    DEMOLITION(
        "demolition",
        "المفجّر",
        "💣",
        "يفتح الخزائن والأبواب",
        "#DC2626",   // أحمر
        120,
        0.85f,
        "شوزن قوي",
        15
    ),

    DRIVER(
        "driver",
        "السائق",
        "🚗",
        "يضمن الهروب السريع",
        "#10B981",   // أخضر
        100,
        1.25f,
        "رشاش سريع",
        10
    );

    public final String id;
    public final String nameAr;
    public final String emoji;
    public final String description;
    public final String colorHex;
    public final int baseHp;
    public final float speedMultiplier;
    public final String weaponName;
    public final int weaponDamage;

    HeistRole(String id, String nameAr, String emoji, String description,
              String colorHex, int baseHp, float speedMultiplier,
              String weaponName, int weaponDamage) {
        this.id = id;
        this.nameAr = nameAr;
        this.emoji = emoji;
        this.description = description;
        this.colorHex = colorHex;
        this.baseHp = baseHp;
        this.speedMultiplier = speedMultiplier;
        this.weaponName = weaponName;
        this.weaponDamage = weaponDamage;
    }

    public static HeistRole fromId(String id) {
        if (id == null) return LEADER;
        for (HeistRole r : values()) {
            if (r.id.equals(id)) return r;
        }
        return LEADER;
    }
}
