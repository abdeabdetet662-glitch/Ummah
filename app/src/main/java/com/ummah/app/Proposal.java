package com.ummah.app;

public class Proposal {
    public String id;
    public String title;
    public String body;
    public String author;
    public int yes;
    public int no;

    public Proposal(String id, String title, String body, String author) {
        this.id = id; this.title = title; this.body = body;
        this.author = author; this.yes = 0; this.no = 0;
    }
}
