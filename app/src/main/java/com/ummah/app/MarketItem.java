package com.ummah.app;

public class MarketItem {
    public String id;
    public String name;
    public String brand;
    public String category;
    public String type;
    public int price;
    public String imageUrl;
    public String description;
    public String rarity;
    public int stock;
    public long createdAt;
    public String wearType;  // shirt/pants/shoes/hat/glasses/phone
    public String color;     // #hex

    public MarketItem() {}

    public MarketItem(String name, String brand, String category, String type,
                      int price, String imageUrl, String description, String rarity) {
        this.name = name;
        this.brand = brand;
        this.category = category;
        this.type = type;
        this.price = price;
        this.imageUrl = imageUrl;
        this.description = description;
        this.rarity = rarity;
        this.stock = 99;
    }
}
