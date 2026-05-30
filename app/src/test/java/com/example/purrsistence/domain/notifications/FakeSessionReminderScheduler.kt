package com.example.purrsistence.domain.notifications

import com.example.purrsistence.notifications.SessionReminderScheduler

class FakeSessionReminderScheduler : SessionReminderScheduler {

    var scheduleCalls = 0
    var cancelCalls = 0

    var lastDelayMinutes: Long? = null
    var lastTitle: String? = null
    var lastMessage: String? = null

    override fun scheduleReminder(
        delayMinutes: Long,
        title: String,
        message: String
    ) {
        scheduleCalls++
        lastDelayMinutes = delayMinutes
        lastTitle = title
        lastMessage = message
    }

    override fun cancelReminder() {
        cancelCalls++
    }
}