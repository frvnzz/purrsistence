package com.example.purrsistence.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.purrsistence.PurrsistenceApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TrackingStopReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val trackingId = intent?.getIntExtra(
            TrackingForegroundService.EXTRA_TRACKING_ID,
            -1
        ) ?: -1

        if (trackingId == -1) return

        val appContainer = (context.applicationContext as PurrsistenceApplication).appContainer

        CoroutineScope(Dispatchers.IO).launch {
            appContainer.trackingService.stopTracking(trackingId)
            TrackingForegroundService.stop(context)
        }
    }
}