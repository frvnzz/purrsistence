package com.example.purrsistence.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.purrsistence.MainActivity
import com.example.purrsistence.R
import com.example.purrsistence.notifications.NotificationChannels
import androidx.core.content.ContextCompat.startForegroundService

class TrackingForegroundService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val trackingId = intent?.getIntExtra(EXTRA_TRACKING_ID, -1) ?: -1
        val goalTitle = intent?.getStringExtra(EXTRA_GOAL_TITLE).orEmpty()
        val startTimeMillis = intent?.getLongExtra(
            EXTRA_START_TIME_MILLIS,
            System.currentTimeMillis()
        ) ?: System.currentTimeMillis()

        val notification = buildTrackingNotification(
            context = this,
            trackingId = trackingId,
            goalTitle = goalTitle,
            startTimeMillis = startTimeMillis
        )

        startAsForeground(notification)

        return START_STICKY
    }

    override fun onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    private fun startAsForeground(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 3001

        const val ACTION_OPEN_TRACKING_FROM_NOTIFICATION =
            "com.example.purrsistence.action.OPEN_TRACKING_FROM_NOTIFICATION"

        const val EXTRA_TRACKING_ID = "extra_tracking_id"
        const val EXTRA_GOAL_TITLE = "extra_goal_title"
        const val EXTRA_START_TIME_MILLIS = "extra_start_time_millis"

        fun start(
            context: Context,
            trackingId: Int,
            goalTitle: String,
            startTimeMillis: Long
        ) {
            val intent = Intent(context, TrackingForegroundService::class.java).apply {
                putExtra(EXTRA_TRACKING_ID, trackingId)
                putExtra(EXTRA_GOAL_TITLE, goalTitle)
                putExtra(EXTRA_START_TIME_MILLIS, startTimeMillis)
            }

            startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, TrackingForegroundService::class.java))
        }

        fun buildTrackingNotification(
            context: Context,
            trackingId: Int,
            goalTitle: String,
            startTimeMillis: Long
        ): Notification {
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_OPEN_TRACKING_FROM_NOTIFICATION
                putExtra(EXTRA_TRACKING_ID, trackingId)
                putExtra(EXTRA_GOAL_TITLE, goalTitle)
                putExtra(EXTRA_START_TIME_MILLIS, startTimeMillis)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                trackingId,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val stopIntent = Intent(context, TrackingStopReceiver::class.java).apply {
                putExtra(EXTRA_TRACKING_ID, trackingId)
            }

            val stopPendingIntent = PendingIntent.getBroadcast(
                context,
                trackingId,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            return NotificationCompat.Builder(context, NotificationChannels.TRACKING)
                .setSmallIcon(R.drawable.cat_tracking)
                .setContentTitle("Tracking in progress")
                .setContentText(goalTitle.ifBlank { "Current focus session" })
                .setContentIntent(openAppPendingIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setWhen(startTimeMillis)
                .setUsesChronometer(true)
                .setChronometerCountDown(false)
                .addAction(
                    R.drawable.cat_tracking,
                    "Stop",
                    stopPendingIntent
                )
                .build()
        }
    }
}