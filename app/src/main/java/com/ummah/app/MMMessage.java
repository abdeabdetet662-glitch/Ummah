package com.ummah.app;

/**
 * MMMessage — رسالة في شات التحقيق
 */
public class MMMessage {
    public String id;
    public String userId;
    public String userName;
    public String userEmoji;
    public String text;
    public String type; // "chat" | "accusation" | "defense" | "system" | "clue"
    public long createdAt;

    public MMMessage() {}
}
