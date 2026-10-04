package com.ummah.app;

/**
 * MMClue — دليل في القضية
 */
public class MMClue {

    public String id;
    public String title;
    public String description;
    public String icon;
    public long revealedAt;
    public String revealsFor;  // "all" أو "specific"
    public String targetUser;  // لمن يُكشف
    public boolean isKeyClue;  // دليل مهم
    public int order;          // ترتيب الظهور

    public MMClue() {}
}
