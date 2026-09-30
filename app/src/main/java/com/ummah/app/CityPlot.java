package com.ummah.app;

public class CityPlot {
    public String id;
    public String districtId;
    public int x, z;
    public int price;
    public String ownerId;
    public String ownerName;
    public String buildingType;   // null | "house" | "villa" | "palace" | "shop" | "factory"
    public int buildingLevel;     // 1-5
    public long boughtAt;
    public long builtAt;

    public CityPlot() {}

    public CityPlot(String id, String districtId, int x, int z, int price) {
        this.id = id;
        this.districtId = districtId;
        this.x = x;
        this.z = z;
        this.price = price;
        this.buildingLevel = 0;
    }

    public boolean isOwned() {
        return ownerId != null && !ownerId.isEmpty();
    }

    public boolean hasBuilding() {
        return buildingType != null && !buildingType.isEmpty();
    }
}
