package com.example.purrsistence.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.purrsistence.PurrsistenceApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TrackingStopReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val trackingId = intent?.getIntExtra(
            TrackingForegroundService.EXTRA_TRACKING_ID,
            -1
        ) ?: -1

        if (trackingId == -1) return

        val pendingResult = goAsync()
        val appContainer =
            (context.applicationContext as PurrsistenceApplication).appContainer

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val stopResult = appContainer.trackingService.stopTracking(trackingId)

                if (stopResult != null) {
                    appContainer.focusBlocker.stopBlocking()

                    appContainer.sessionReminderScheduler.scheduleReminder(
                        delayMinutes = 60,
                        title = "The cats have started checking the timer again",
                        message = "A short session would reassure the entire whiskered department."
                    )

                    if (appContainer.supabaseSyncService.isSignedIn()) {
                        appContainer.supabaseSyncService.syncAfterLocalTrackingSessionChanged()
                    }
                }
            } finally {
                TrackingForegroundService.stop(context)
                pendingResult.finish()
            }
        }
    }
}