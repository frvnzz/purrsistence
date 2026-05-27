package com.example.purrsistence.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class SessionReminderScheduler(
    private val context: Context
) {

    fun scheduleReminder(
        delayMinutes: Long = 180,
        title: String,
        message: String
    ) {
        val inputData = Data.Builder()
            .putString(SessionReminderWorker.KEY_TITLE, title)
            .putString(SessionReminderWorker.KEY_MESSAGE, message)
            .build()

        val request = OneTimeWorkRequestBuilder<SessionReminderWorker>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_REMINDER_WORK,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelReminder() {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_REMINDER_WORK)
    }

    companion object {
        private const val UNIQUE_REMINDER_WORK = "session_reminder_work"
    }
}