package com.ummah.app;

/**
 * SupportMessage — رسالة في التذكرة
 */
public class SupportMessage {

    public String id;
    public String senderId;
    public String senderName;
    public String senderType; // user | admin
    public String text;
    public long createdAt;

    public SupportMessage() {}

    public boolean isFromAdmin() {
        return "admin".equals(senderType);
    }
}
