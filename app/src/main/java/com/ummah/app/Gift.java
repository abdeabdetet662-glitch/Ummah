package com.ummah.app;

public class Gift {
    public String emoji;
    public String name;
    public int price;
    public String meaning;
    public int tier;

    public Gift(String emoji, String name, int price, String meaning, int tier) {
        this.emoji = emoji;
        this.name = name;
        this.price = price;
        this.meaning = meaning;
        this.tier = tier;
    }
}
