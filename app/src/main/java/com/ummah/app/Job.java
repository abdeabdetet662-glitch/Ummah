package com.ummah.app;

public class Job {
    public String id;
    public String title;
    public String emoji;
    public String description;
    public int salary;       // الراتب لكل عملة
    public int cooldownMin;  // المدة بين كل عملة (بالدقائق)
    public int requiredLevel;
    public String color;     // لون البطاقة

    public Job() {}

    public Job(String id, String title, String emoji, String description,
               int salary, int cooldownMin, int requiredLevel, String color) {
        this.id = id;
        this.title = title;
        this.emoji = emoji;
        this.description = description;
        this.salary = salary;
        this.cooldownMin = cooldownMin;
        this.requiredLevel = requiredLevel;
        this.color = color;
    }
}
