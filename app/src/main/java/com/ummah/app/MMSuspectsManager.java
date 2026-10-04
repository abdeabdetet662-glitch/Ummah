package com.ummah.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MMSuspectsManager — إدارة المشتبهين لكل قضية
 */
public class MMSuspectsManager {

    // ═══ أسئلة الاستجواب (مشتركة) ═══
    public static List<MMQuestion> getQuestions() {
        List<MMQuestion> list = new ArrayList<>();
        list.add(new MMQuestion("where", "أين كنت وقت الجريمة؟", "📍"));
        list.add(new MMQuestion("relation", "ما علاقتك بالملك؟", "🤝"));
        list.add(new MMQuestion("motive", "هل كان عندك دافع؟", "🎯"));
        list.add(new MMQuestion("weapon", "شفت الخنجر قبل؟", "🗡️"));
        list.add(new MMQuestion("suspect", "من تشك فيه؟", "🕵️"));
        list.add(new MMQuestion("secret", "تخفي شي؟", "🤫"));
        return list;
    }

    // ═══ بناء المشتبهين للجريمة ═══
    public static List<MMSuspect> buildSuspects(String caseId) {
        List<MMSuspect> list = new ArrayList<>();

        // 1. رئيس الوزراء
        MMSuspect s1 = new MMSuspect();
        s1.id = "vizier";
        s1.name = "الأمير خالد";
        s1.emoji = "👔";
        s1.title = "رئيس الوزراء";
        s1.description = "رجل في الخمسين، هادئ، مريب";
        s1.secret = "كان يخطط لانقلاب على الملك";
        s1.suspicionLevel = 40;
        s1.truths = new HashMap<>();
        s1.truths.put("where", "كنت في مكتبي أراجع الأوراق");
        s1.truths.put("relation", "كنت مستشاره المخلص منذ 20 سنة");
        s1.truths.put("motive", "لا، ما عندي أي دافع");
        s1.truths.put("weapon", "شفت الخنجر في متحف القصر");
        s1.truths.put("suspect", "أشك في ابن أخيه، كان دائماً يطمع بالعرش");
        s1.truths.put("secret", "لا أخفي شيئاً، أنا مواطن شريف");
        s1.lies = new HashMap<>();
        s1.lies.put("where", "كنت في الحديقة أتمشى");
        s1.lies.put("relation", "علاقة عادية، لا ود لا عداء");
        s1.lies.put("motive", "ليس عندي دافع، أنا رجل طاعن في السن");
        s1.lies.put("weapon", "لا، لم أشاهد هذا الخنجر من قبل");
        s1.lies.put("suspect", "الطبيب، كان يبدو متوتراً");
        s1.lies.put("secret", "أخفي فقط ضعفي أمام الملك");
        list.add(s1);

        // 2. الطبيب الشخصي
        MMSuspect s2 = new MMSuspect();
        s2.id = "doctor";
        s2.name = "الدكتور سليم";
        s2.emoji = "👨‍⚕️";
        s2.title = "الطبيب الشخصي";
        s2.description = "شاب، خجول، يتجنب النظرات";
        s2.secret = "كان يخفي مرض الملك الخطير";
        s2.suspicionLevel = 30;
        s2.truths = new HashMap<>();
        s2.truths.put("where", "كنت في غرفتي أكتب التقارير الطبية");
        s2.truths.put("relation", "كنت طبيبه الشخصي منذ 3 سنوات");
        s2.truths.put("motive", "لا، لماذا أقتل من أنقذ حياته؟");
        s2.truths.put("weapon", "الخنجر يشبه الذي في المجموعة الملكية");
        s2.truths.put("suspect", "رئيس الوزراء، رأيته قرب المكتب");
        s2.truths.put("secret", "كنت أخفي أن الملك مريض بمرض خطير");
        s2.lies = new HashMap<>();
        s2.lies.put("where", "كنت في الحديقة أتنفس الهواء");
        s2.lies.put("relation", "طبيب عادي، لا علاقة شخصية");
        s2.lies.put("motive", "لا يوجد أي دافع");
        s2.lies.put("weapon", "لا، أول مرة أشاهد هذا الخنجر");
        s2.lies.put("suspect", "لا أشك في أحد محدد");
        s2.lies.put("secret", "لا أخفي شيئاً");
        list.add(s2);

        // 3. كبير الخدم
        MMSuspect s3 = new MMSuspect();
        s3.id = "butler";
        s3.name = "الحاج عمر";
        s3.emoji = "🧔";
        s3.title = "كبير الخدم";
        s3.description = "رجل عجوز، ذو لحية بيضاء، هادئ";
        s3.secret = "كان يسرق من خزينة الملك";
        s3.suspicionLevel = 25;
        s3.truths = new HashMap<>();
        s3.truths.put("where", "كنت أرتب غرفة الطعام");
        s3.truths.put("relation", "خدمت الملك 40 سنة، كوالدي");
        s3.truths.put("motive", "لا، أحب الملك أكثر من نفسي");
        s3.truths.put("weapon", "شفت الخنجر في مكتب الملك قبل يومين");
        s3.truths.put("suspect", "ابن الأخ، كان دائماً مشكوكاً فيه");
        s3.truths.put("secret", "كان يسرقني من الفلوس لأسرتي");
        s3.lies = new HashMap<>();
        s3.lies.put("where", "كنت في المطبخ أجهز العشاء");
        s3.lies.put("relation", "خادم عادي، لا أكثر");
        s3.lies.put("motive", "لا، لا يوجد أي دافع");
        s3.lies.put("weapon", "لا، لم أشاهد هذا الخنجر");
        s3.lies.put("suspect", "لا أشك في أحد");
        s3.lies.put("secret", "لا، لا أخفي شيئاً");
        list.add(s3);

        // 4. ابن الأخ
        MMSuspect s4 = new MMSuspect();
        s4.id = "nephew";
        s4.name = "الأمير راشد";
        s4.emoji = "👦";
        s4.title = "ابن أخ الملك";
        s4.description = "شاب متهور، ثياب فاخرة، عيون طماعة";
        s4.secret = "كان مدين بمليون دينار للعصابة";
        s4.suspicionLevel = 55;
        s4.truths = new HashMap<>();
        s4.truths.put("where", "كنت في السوق أشتري مجوهرات");
        s4.truths.put("relation", "عمي، كان يعاملني كابنه");
        s4.truths.put("motive", "لا! أحبه، لا أريد عرشه");
        s4.truths.put("weapon", "الخنجر؟ نعم شفته، كان معلقاً على الجدار");
        s4.truths.put("suspect", "الوزير، كان دائماً يكره عمي");
        s4.truths.put("secret", "أنا مدين بمليون دينار");
        s4.lies = new HashMap<>();
        s4.lies.put("where", "كنت في القصر أتمشى");
        s4.lies.put("relation", "علاقة عادية، ما بينا مشاكل");
        s4.lies.put("motive", "لا! أنا مستقبل العرش، لماذا أقتله؟");
        s4.lies.put("weapon", "لا، لم أشاهد أي خنجر");
        s4.lies.put("suspect", "الطبيب، شفته يدخل الغرفة متوتراً");
        s4.lies.put("secret", "لا أخفي شيئاً");
        list.add(s4);

        // 5. المستشار
        MMSuspect s5 = new MMSuspect();
        s5.id = "advisor";
        s5.name = "الشيخ يوسف";
        s5.emoji = "📚";
        s5.title = "المستشار الملكي";
        s5.description = "شيخ حكيم، يضع نظارة، يقظ جداً";
        s5.secret = "كان على خلاف مع الملك حول ميزانية الدولة";
        s5.suspicionLevel = 35;
        s5.truths = new HashMap<>();
        s5.truths.put("where", "كنت في المكتبة أقرأ");
        s5.truths.put("relation", "مستشاره منذ 15 سنة");
        s5.truths.put("motive", "لا، اختلفنا سياسياً لكن أحترمه");
        s5.truths.put("weapon", "الخنجر من مجموعة الملك الخاصة");
        s5.truths.put("suspect", "الوزير، كان يخفي شيئاً");
        s5.truths.put("secret", "كانت بيننا خلافات حول الميزانية");
        s5.lies = new HashMap<>();
        s5.lies.put("where", "كنت في غرفتي أنام");
        s5.lies.put("relation", "علاقة عمل فقط");
        s5.lies.put("motive", "لا، لا يوجد دافع");
        s5.lies.put("weapon", "لا، لم أشاهد هذا الخنجر من قبل");
        s5.lies.put("suspect", "لا أشك في أحد");
        s5.lies.put("secret", "لا أخفي شيئاً");
        list.add(s5);

        return list;
    }
}
