package com.ummah.app;

public class WheelSegment {
    public String id;
    public String emoji;
    public String label;
    public int value;         // المبلغ أو رقم المنتج
    public String type;       // "money" | "item" | "nothing" | "jackpot" | "privilege"
    public String color;      // #hex
    public int weight;        // الاحتمال (كل ما زاد، زاد الاحتمال)
    public int order;         // ترتيب القطاع (0-11)
    public boolean active;
    public String itemName;   // اسم المنتج (إذا type="item")
    public String privilege;  // "vip" | "president_badge" | "free_spin" | ...

    public WheelSegment() {}

    public WheelSegment(String emoji, String label, int value, String type,
                        String color, int weight, int order) {
        this.emoji = emoji;
        this.label = label;
        this.value = value;
        this.type = type;
        this.color = color;
        this.weight = weight;
        this.order = order;
        this.active = true;
    }
}
