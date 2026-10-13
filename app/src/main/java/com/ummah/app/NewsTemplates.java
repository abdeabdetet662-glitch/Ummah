package com.ummah.app;

import java.util.Random;

/**
 * NewsTemplates — قوالب الأخبار الذكية
 * 15 حدث × 5-10 قوالب لكل حدث = 100+ قالب
 */
public class NewsTemplates {

    private static final Random RNG = new Random();

    /**
     * نوع الحدث
     */
    public static class EventType {
        public static final String PRESIDENT_ELECTED = "presidentElected";
        public static final String PRESIDENT_APPOINTED = "presidentAppointed";
        public static final String LAW_PASSED = "lawPassed";
        public static final String LAW_REJECTED = "lawRejected";
        public static final String TREASURY_MILESTONE = "treasuryMilestone";
        public static final String COURT_VERDICT = "courtVerdict";
        public static final String CITIZEN_ACHIEVEMENT = "citizenAchievement";
        public static final String ELECTION_STARTED = "electionStarted";
        public static final String NEW_BUILDING = "newBuilding";
        public static final String MARKET_TRENDING = "marketTrending";
        public static final String JOB_PROMOTION = "jobPromotion";
        public static final String GIFT_RECEIVED = "giftReceived";
        public static final String DAILY_REPORT = "dailyReport";
        public static final String WEEKLY_REPORT = "weeklyReport";
        public static final String MONTHLY_REPORT = "monthlyReport";
    }

    // ═══════════════════════════════════════════
    //  1. رئيس جديد (انتُخب)
    // ═══════════════════════════════════════════

    public static String presidentElected(String name) {
        String[] templates = {
            "👑 إنجاز تاريخي!\n\nانتخب مواطنو أُمّة السيد " + name + " رئيساً للدولة في انتخابات نزيهة.\n\n🎉 مبروك للرئيس الجديد، وعهداً بمستقبل أفضل لأمتنا!",
            "🗳️ إعلان رسمي من رئاسة أُمّة:\n\nفاز السيد " + name + " بمنصب الرئيس بعد حصوله على ثقة الأغلبية.\n\n🌟 نسأل الله له التوفيق في خدمة الوطن والمواطنين.",
            "🏆 لحظة تاريخية في أُمّة!\n\nالسيد " + name + " يصبح الرئيس الجديد بعد فوزه في انتخابات تنافسية.\n\n💫 رحلة جديدة تبدأ اليوم لبناء أمتنا الرقمية.",
            "📢 بيان رسمي:\n\nأعلنت اللجنة الانتخابية فوز السيد " + name + " برئاسة أُمّة.\n\n🎊 نبارك له ونتمنى له التوفيق في قيادة البلاد نحو الازدهار.",
            "👑 رئيس جديد يقود أُمّة!\n\nالسيد " + name + " يتولى منصب الرئاسة بعد انتخابات شهدت مشاركة واسعة.\n\n✨ أهلاً بك يا رئيسنا الجديد!"
        };
        return pick(templates);
    }

    // ═══════════════════════════════════════════
    //  2. رئيس معتمد (من قِبَل الإدارة)
    // ═══════════════════════════════════════════

    public static String presidentAppointed(String name) {
        String[] templates = {
            "🎖️ قرار رسمي:\n\nتم تعيين السيد " + name + " رئيساً معتمداً للدولة.\n\n🌟 نتمنى له النجاح في مهامه الجديدة.",
            "📜 مرسوم رئاسي:\n\nتقرر تعيين السيد " + name + " في منصب الرئيس المعتمد لأُمّة.\n\n✨ مبروك!",
            "👑 إعلان:\n\nالسيد " + name + " يصبح الرئيس المعتمد للدولة.\n\n🏆 قرار يستحق الاحترام.",
            "🎉 خبر سار:\n\nتم اعتماد السيد " + name + " رئيساً رسمياً لأُمّة.\n\n💫 بالتوفيق في مهمته الجديدة."
        };
        return pick(templates);
    }

    // ═══════════════════════════════════════════
    //  3. قانون تم إقراره
    // ═══════════════════════════════════════════

    public static String lawPassed(String title) {
        String[] templates = {
            "⚖️ قانون جديد في أُمّة!\n\nأقرّ البرلمان بالأغلبية: «" + title + "»\n\n📜 القانون يدخل حيّز التنفيذ فوراً.",
            "📢 إعلان تشريعي:\n\nتم تمرير قانون «" + title + "» بعد تصويت البرلمان.\n\n🎯 قانون جديد يخدم مصلحة المواطنين.",
            "🏛️ إنجاز ديمقراطي:\n\nالبرلمان يقر «" + title + "» بأغلبية الأصوات.\n\n✨ الديمقراطية تعمل!",
            "⚖️ قرار برلماني:\n\nتمت الموافقة على «" + title + "» رسمياً.\n\n🌟 خطوة جديدة نحو التطور."
        };
        return pick(templates);
    }

    // ═══════════════════════════════════════════
    //  4. قانون مرفوض
    // ═══════════════════════════════════════════

    public static String lawRejected(String title) {
        String[] templates = {
            "🗳️ نتيجة تصويت:\n\nرُفض قانون «" + title + "» في البرلمان.\n\n📊 الأغلبية قالت لا!",
            "📢 إعلان برلماني:\n\nلم يحصل «" + title + "» على الأصوات الكافية.\n\n⚖️ الديمقراطية تحترم صوت الأغلبية.",
            "❌ قرار:\n\nالبرلمان يرفض «" + title + "».\n\n🎯 صوت الشعب هو الفيصل.",
            "🏛️ تصويت:\n\n«" + title + "» لا يمرّ.\n\n📊 القانون كان محل نقاش واسع."
        };
        return pick(templates);
    }

    // ═══════════════════════════════════════════
    //  5. إنجاز اقتصادي
    // ═══════════════════════════════════════════

    public static String treasuryMilestone(int amount) {
        String formatted = String.format("%,d", amount);
        String[] templates = {
            "💰 إنجاز اقتصادي!\n\nخزينة أُمّة تصل إلى " + formatted + " دينار!\n\n📈 اقتصادنا ينمو بفضل مساهمات المواطنين.",
            "💎 رقم قياسي جديد:\n\nخزينة الدولة تبلغ " + formatted + " Đ\\.\n\n🎉 مبروك لأُمّة!",
            "🏆 إنجاز:\n\nالخزينة العامة تجاوزت " + formatted + " دينار!\n\n📊 الاقتصاد في نمو مستمر.",
            "💵 خبر اقتصادي:\n\nالخزينة العامة لأُمّة بلغت " + formatted + " Đ.\n\n🌟 شكراً لكل من ساهم!"
        };
        return pick(templates);
    }

    // ═══════════════════════════════════════════
    //  6. حكم محكمة
    // ═══════════════════════════════════════════

    public static String courtVerdict(String caseTitle) {
        String[] templates = {
            "⚖️ حكم قضائي:\n\nأصدرت محكمة أُمّة حكمها في قضية «" + caseTitle + "».\n\n📜 العدالة تحققت.",
            "🏛️ قرار المحكمة:\n\nحكم صادر في «" + caseTitle + "».\n\n⚖️ القضاء يقول كلمته.",
            "📋 حكم نهائي:\n\nالمحكمة أعلنت الحكم في «" + caseTitle + "».\n\n✨ لا محاباة في العدالة.",
            "⚖️ محكمة أُمّة:\n\nالحكم صدر في «" + caseTitle + "».\n\n📜 القانون فوق الجميع."
        };
        return pick(templates);
    }

    // ═══════════════════════════════════════════
    //  7. إنجاز مواطن
    // ═══════════════════════════════════════════

    public static String citizenAchievement(String name, String achievement) {
        String[] templates = {
            "🏆 إنجاز مواطن:\n\n" + name + " حقق إنجازاً رائعاً: " + achievement + "\n\n🎉 مبروك!",
            "⭐ نجم أُمّة:\n\n" + name + " يتألق بإنجازه: " + achievement + "\n\n💫 فخر لأمتنا.",
            "🎖️ تكريم:\n\n" + name + " يستحق التقدير على: " + achievement + "\n\n👏 أحسنت!",
            "🌟 خبر سار:\n\n" + name + " " + achievement + "\n\n🎊 إنجاز يستحق الاحتفال."
        };
        return pick(templates);
    }

    // ═══════════════════════════════════════════
    //  8. بدء الانتخابات
    // ═══════════════════════════════════════════

    public static String electionStarted() {
        String[] templates = {
            "🗳️ انتخابات أُمّة بدأت!\n\nصوتك يبني المستقبل. شارك الآن واختر رئيسك.\n\n⭐ كل صوت مهم!",
            "📢 إعلان انتخابي:\n\nفُتح باب التصويت لانتخابات الرئاسة.\n\n🎯 صوتك أمانة، لا تتهاون!",
            "🇩🇿 ديمقراطية أُمّة:\n\nالانتخابات الرئاسية انطلقت!\n\n✨ صوتك يغيّر التاريخ.",
            "🎊 وقت التصويت:\n\nانتخابات أُمّة بدأت رسمياً.\n\n💫 كن جزءاً من القرار!"
        };
        return pick(templates);
    }

    // ═══════════════════════════════════════════
    //  9. بناء جديد
    // ═══════════════════════════════════════════

    public static String newBuilding(String name, String building) {
        String[] templates = {
            "🏗️ بناء جديد:\n\n" + name + " شيّد " + building + " في المدينة!\n\n🏙️ مدينتنا تتوسع.",
            "🏙️ إنجاز معماري:\n\n" + name + " أضاف " + building + " لمدينة أُمّة.\n\n✨ مبروك!",
            "🏛️ مشروع جديد:\n\n" + name + " بنى " + building + " في قلب المدينة.\n\n🎉 تطور عمراني.",
            "🏢 خبر عمراني:\n\n" + name + " أعلن عن بناء " + building + ".\n\n🌟 خطوة جديدة في نمو المدينة."
        };
        return pick(templates);
    }

    // ═══════════════════════════════════════════
    //  10. منتج رائج في السوق
    // ═══════════════════════════════════════════

    public static String marketTrending(String item) {
        String[] templates = {
            "🔥 منتج رائج:\n\n«" + item + "» يتصدر السوق!\n\n📈 مبيعات قياسية.",
            "🛒 سوق أُمّة:\n\n«" + item + "» هو الأكثر طلباً هذا الأسبوع.\n\n💰 عرض مغري!",
            "📈 اتجاهات السوق:\n\n«" + item + "» يستحوذ على اهتمام المواطنين.\n\n🌟 انضم للتجربة!",
            "🎯 رائج اليوم:\n\n«" + item + "» يحقق مبيعات كبيرة.\n\n💎 لا تفوّت الفرصة!"
        };
        return pick(templates);
    }

    // ═══════════════════════════════════════════
    //  11. ترقية في الوظيفة
    // ═══════════════════════════════════════════

    public static String jobPromotion(String name, String job, int level) {
        String[] templates = {
            "📈 ترقية:\n\n" + name + " ترقّى إلى المستوى " + level + " في " + job + "!\n\n🎉 مبروك!",
            "🎖️ نجاح مهني:\n\n" + name + " وصل للمستوى " + level + " كـ" + job + ".\n\n⭐ أحسنت!",
            "🏆 إنجاز وظيفي:\n\n" + name + " حقق المستوى " + level + " في " + job + ".\n\n💼 تقدم رائع.",
            "🌟 ترقية مستحقة:\n\n" + name + " أصبح في المستوى " + level + " كـ" + job + ".\n\n👏 نتمنى له الأفضل."
        };
        return pick(templates);
    }

    // ═══════════════════════════════════════════
    //  12. هدية
    // ═══════════════════════════════════════════

    public static String giftReceived(String from, String to, int amount) {
        String[] templates = {
            "🎁 هدية:\n\n" + from + " أهدى " + to + " " + amount + " دينار!\n\n💝 كرم يستحق التقدير.",
            "💖 كرم:\n\n" + from + " قدّم هدية بقيمة " + amount + " Đ لـ" + to + ".\n\n🌟 روح التعاون.",
            "🎉 هدية:\n\n" + to + " استلم " + amount + " Đ من " + from + ".\n\n💫 مبروك!"
        };
        return pick(templates);
    }

    // ═══════════════════════════════════════════
    //  13. التقرير اليومي
    // ═══════════════════════════════════════════

    public static String dailyReport(int citizens, int transactions, int activeToday, int treasury) {
        String[] templates = {
            "📊 التقرير اليومي لأُمّة\n\n" +
            "👥 المواطنون: " + citizens + "\n" +
            "📈 المعاملات: " + transactions + "\n" +
            "🔥 النشطون اليوم: " + activeToday + "\n" +
            "💰 الخزينة: " + String.format("%,d", treasury) + " Đ\n\n" +
            "🌙 صحيفة الأمة اليومية",

            "🗓️ تقرير اليوم\n\n" +
            "📌 عدد المواطنين: " + citizens + "\n" +
            "💳 معاملات اليوم: " + transactions + "\n" +
            "⭐ نشاط اليوم: " + activeToday + " مواطن\n" +
            "🏦 رصيد الخزينة: " + String.format("%,d", treasury) + " Đ\n\n" +
            "✨ إلى الغد!",

            "📰 ملخص اليوم في أُمّة\n\n" +
            "• " + citizens + " مواطن مسجّل\n" +
            "• " + transactions + " معاملة\n" +
            "• " + activeToday + " نشط اليوم\n" +
            "• خزينة: " + String.format("%,d", treasury) + " Đ\n\n" +
            "🌟 يوم حافل!"
        };
        return pick(templates);
    }

    // ═══════════════════════════════════════════
    //  14. التقرير الأسبوعي
    // ═══════════════════════════════════════════

    public static String weeklyReport(int newCitizens, int totalTransactions, int topEarner, int totalTreasury) {
        String[] templates = {
            "📊 التقرير الأسبوعي لأُمّة\n\n" +
            "👥 مواطنون جدد: " + newCitizens + "\n" +
            "💳 المعاملات: " + totalTransactions + "\n" +
            "🏆 الأكثر ربحاً: " + topEarner + " Đ\n" +
            "💰 الخزينة: " + String.format("%,d", totalTreasury) + " Đ\n\n" +
            "🌟 أسبوع من النجاحات!",

            "🗓️ ملخص الأسبوع\n\n" +
            "📈 نمو: " + newCitizens + " مواطن جديد\n" +
            "💼 نشاط: " + totalTransactions + " معاملة\n" +
            "👑 الأكثر: " + topEarner + " Đ\n" +
            "🏦 خزينة: " + String.format("%,d", totalTreasury) + " Đ\n\n" +
            "✨ إلى أسبوع أفضل!"
        };
        return pick(templates);
    }

    // ═══════════════════════════════════════════
    //  15. التقرير الشهري
    // ═══════════════════════════════════════════

    public static String monthlyReport(int citizens, int treasury, int buildings, int laws) {
        String[] templates = {
            "📊 التقرير الشهري لأُمّة\n\n" +
            "👥 المواطنون: " + citizens + "\n" +
            "💰 الخزينة: " + String.format("%,d", treasury) + " Đ\n" +
            "🏛️ المباني: " + buildings + "\n" +
            "⚖️ القوانين: " + laws + "\n\n" +
            "🎉 شهر من الإنجازات!",

            "🗓️ حصيلة الشهر\n\n" +
            "• " + citizens + " مواطن\n" +
            "• " + String.format("%,d", treasury) + " Đ خزينة\n" +
            "• " + buildings + " مبنى\n" +
            "• " + laws + " قانون\n\n" +
            "🌟 أُمّة تتقدم!"
        };
        return pick(templates);
    }

    // ═══════════════════════════════════════════
    //  Helper
    // ═══════════════════════════════════════════

    // ═══════════════════════════════════════════
    //  Helper — اختيار عشوائي من مصفوفة
    // ═══════════════════════════════════════════

    private static String pick(String[] arr) {
        if (arr == null || arr.length == 0) return "";
        return arr[RNG.nextInt(arr.length)];
    }
}
