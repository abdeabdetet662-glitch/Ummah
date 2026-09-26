package com.ummah.app;

public class Citizen {
    public String nationalId;
    public String name;
    public String joinDate;
    public String seedPhrase;
    public String country; // رمز مثل DZ, EG, SA

    public Citizen(String nationalId, String name, String joinDate, String seedPhrase, String country) {
        this.nationalId = nationalId;
        this.name = name;
        this.joinDate = joinDate;
        this.seedPhrase = seedPhrase;
        this.country = country != null ? country : "DZ";
    }
}
