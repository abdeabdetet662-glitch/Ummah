package com.ummah.app;

/**
 * MMQuestion — سؤال للاستجواب
 */
public class MMQuestion {
    public String id;
    public String text;
    public String emoji;
    
    public MMQuestion() {}
    public MMQuestion(String id, String text, String emoji) {
        this.id = id;
        this.text = text;
        this.emoji = emoji;
    }
}
