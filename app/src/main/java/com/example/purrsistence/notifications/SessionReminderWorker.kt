package com.example.purrsistence.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.purrsistence.MainActivity
import com.example.purrsistence.R

class SessionReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: "The cats are waiting"
        val message = inputData.getString(KEY_MESSAGE)
            ?: "A tiny paw committee has noticed the silence. A short focus session would calm everyone down."

        val openAppIntent = Intent(applicationContext, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            applicationContext,
            NotificationChannels.REMINDERS
        )
            .setSmallIcon(R.drawable.coin_64)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(
            REMINDER_NOTIFICATION_ID,
            notification
        )

        return Result.success()
    }

    companion object {
        private const val REMINDER_NOTIFICATION_ID = 4001

        const val KEY_TITLE = "key_title"
        const val KEY_MESSAGE = "key_message"
    }
}