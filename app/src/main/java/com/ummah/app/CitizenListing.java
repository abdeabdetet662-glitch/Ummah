package com.ummah.app;

public class CitizenListing {
    public String id;
    public String sellerId;
    public String sellerName;
    public String itemId;         // ID في market_items
    public String itemName;
    public String itemBrand;
    public String itemCategory;
    public String itemType;
    public String imageUrl;
    public String rarity;
    public String description;
    public int originalPrice;     // السعر الأصلي
    public int price;             // السعر المطلوب من البائع
    public long listedAt;
    public String status;         // "active" | "sold" | "cancelled"
    public String buyerId;        // المشتري (كي يتباع)
    public long soldAt;

    public CitizenListing() {}
}
