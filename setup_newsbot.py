#!/usr/bin/env python3
import firebase_admin
from firebase_admin import credentials, firestore
import time

cred = credentials.Certificate("/data/data/com.termux/files/home/service-account.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

now = int(time.time() * 1000)

config = {
    "enabled": True,
    "autoPublish": True,
    "requireApproval": False,
    "scheduleEnabled": True,
    "dailyReportHour": 20,
    "weeklyReportDay": 5,
    "monthlyReportDay": 1,
    "lastDailyReport": 0,
    "lastWeeklyReport": 0,
    "lastMonthlyReport": 0,
    "style": "mixed",
    "language": "ar",
    "eventsEnabled": {
        "presidentElected": True,
        "presidentAppointed": True,
        "lawPassed": True,
        "lawRejected": True,
        "treasuryMilestone": True,
        "courtVerdict": True,
        "citizenAchievement": True,
        "electionStarted": True,
        "newBuilding": True,
        "marketTrending": True,
        "jobPromotion": True,
        "giftReceived": True,
        "dailyReport": True,
        "weeklyReport": True,
        "monthlyReport": True
    },
    "createdAt": now,
    "updatedAt": now
}

db.collection("news_bot_config").document("main").set(config)
print("OK: news_bot_config/main")

log = {
    "action": "init",
    "message": "Bot activated",
    "timestamp": now,
    "newsCount": 0
}
db.collection("news_bot_logs").add(log)
print("OK: log added")

print("")
print("Enabled:", config["enabled"])
print("AutoPublish:", config["autoPublish"])
print("Schedule:", config["scheduleEnabled"])
print("Style:", config["style"])
print("Events:", sum(1 for v in config["eventsEnabled"].values() if v), "/ 15")
