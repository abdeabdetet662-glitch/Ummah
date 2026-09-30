package com.ummah.app;

public class CityDistrict {
    public String id;
    public String name;
    public String emoji;
    public String description;
    public int basePrice;
    public String colorHex;
    public int gridX, gridZ;
    public int width, depth;
    public String tier;
    public long createdAt;

    public CityDistrict() {}

    public CityDistrict(String id, String name, String emoji, String description,
                        int basePrice, String colorHex, int gridX, int gridZ,
                        int width, int depth, String tier) {
        this.id = id;
        this.name = name;
        this.emoji = emoji;
        this.description = description;
        this.basePrice = basePrice;
        this.colorHex = colorHex;
        this.gridX = gridX;
        this.gridZ = gridZ;
        this.width = width;
        this.depth = depth;
        this.tier = tier;
    }
}
