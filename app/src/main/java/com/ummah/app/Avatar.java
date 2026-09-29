package com.ummah.app;

public class Avatar {
    public String userId;
    public String skinColor;    // "light" | "medium" | "dark" | "brown"
    public String hairStyle;    // "short" | "long" | "bald" | "curly"
    public String hairColor;    // "black" | "brown" | "blond" | "red"
    public String shirtEmoji;   // "👕" | "👔" | "👗" | "🧥"
    public String shirtColor;
    public String pantsEmoji;   // "👖" | "🩳"
    public String pantsColor;
    public String shoesEmoji;   // "👟" | "👞" | "🥾"
    public String accessoryEmoji; // "🕶️" | "🎩" | "" | "🧢"
    public long updatedAt;

    public Avatar() {
        this.skinColor = "light";
        this.hairStyle = "short";
        this.hairColor = "black";
        this.shirtEmoji = "👕";
        this.shirtColor = "#1565C0";
        this.pantsEmoji = "👖";
        this.pantsColor = "#212121";
        this.shoesEmoji = "👟";
        this.shoesColor_placeholder();
        this.accessoryEmoji = "";
    }

    private void shoesColor_placeholder() {}
}
