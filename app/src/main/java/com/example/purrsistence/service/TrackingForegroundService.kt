package com.example.purrsistence.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.example.purrsistence.MainActivity
import com.example.purrsistence.R
import com.example.purrsistence.notifications.NotificationChannels

class TrackingForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val trackingId = intent?.getIntExtra(EXTRA_TRACKING_ID, -1) ?: -1
        val goalTitle = intent?.getStringExtra(EXTRA_GOAL_TITLE).orEmpty()
        val startTimeMillis = intent?.getLongExtra(EXTRA_START_TIME_MILLIS, System.currentTimeMillis())
            ?: System.currentTimeMillis()

        val notification = buildTrackingNotification(
            context = this,
            trackingId = trackingId,
            goalTitle = goalTitle,
            startTimeMillis = startTimeMillis
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
        super.onDestroy()
    }

    companion object {
        private const val NOTIFICATION_ID = 3001

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

            androidx.core.content.ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, TrackingForegroundService::class.java))
        }

        fun buildTrackingNotification(
            context: Context,
            trackingId: Int,
            goalTitle: String,
            startTimeMillis: Long
        ): android.app.Notification {
            val openAppIntent = Intent(context, MainActivity::class.java)
            val openAppPendingIntent = android.app.PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val stopIntent = Intent(context, TrackingStopReceiver::class.java).apply {
                putExtra(EXTRA_TRACKING_ID, trackingId)
            }
            val stopPendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                trackingId,
                stopIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            return NotificationCompat.Builder(context, NotificationChannels.TRACKING)
                .setSmallIcon(R.drawable.cat_tracking)
                .setContentTitle("Tracking in progress")
                .setContentText(goalTitle.ifBlank { "Current focus session" })
                .setWhen(startTimeMillis)
                .setUsesChronometer(true)
                .setChronometerCountDown(false)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setContentIntent(openAppPendingIntent)
                .addAction(
                    R.drawable.ic_launcher_foreground,
                    "Stop",
                    stopPendingIntent
                )
                .build()
        }
    }
}