package com.ummah.app;

public class Citizen {
    public String nationalId;
    public String name;
    public String joinDate;
    public String seedPhrase;

    public Citizen(String nationalId, String name, String joinDate, String seedPhrase) {
        this.nationalId = nationalId;
        this.name = name;
        this.joinDate = joinDate;
        this.seedPhrase = seedPhrase;
    }
}
