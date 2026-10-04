package com.ummah.app;

import java.util.List;
import java.util.Map;

/**
 * MMSuspect — مشتبه في جريمة أُمّة
 */
public class MMSuspect {
    public String id;
    public String name;
    public String emoji;
    public String title;
    public String description;
    public String secret;          // السر (يظهر للنهاية فقط)
    public boolean isKiller;
    public int suspicionLevel;     // 0-100 (يبدأ عشوائياً)
    
    // أسئلة وأجوبة (إذا كاذب/صادق)
    public Map<String, String> truths;   // الأجوبة الصادقة
    public Map<String, String> lies;     // الأجوبة الكاذبة (للقتلة)
    
    public MMSuspect() {}

    public String getAnswer(String questionId) {
        if (isKiller && lies != null && lies.containsKey(questionId)) {
            return lies.get(questionId);
        }
        if (truths != null && truths.containsKey(questionId)) {
            return truths.get(questionId);
        }
        return "...";
    }
}
