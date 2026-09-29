package com.ummah.app;

public class MarketItem {
    public String id;
    public String name;
    public String brand;
    public String category; // "vehicle" | "property" | "electronics" | "clothing"
    public String type;     // "car" | "motorcycle" | "villa" | ...
    public int price;
    public String imageUrl;
    public String description;
    public String rarity;   // "common" | "rare" | "epic" | "legendary"
    public int stock;
    public long createdAt;

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
