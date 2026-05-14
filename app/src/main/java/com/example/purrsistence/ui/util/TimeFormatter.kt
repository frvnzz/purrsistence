package com.example.purrsistence.ui.util

import android.annotation.SuppressLint

/**
 * Formats minutes into a human-readable time string.
 *
 * Logic:
 * - Less than 60 minutes: displays as "X min" (e.g., "45 min")
 * - 60 minutes or more: displays as "Xh YYmin" (e.g., "2h 30min")
 * - Exactly on the hour: displays as "Xh" (e.g., "2h")
 */
fun formatMinutes(totalMinutes: Int): String {
    return when {
        totalMinutes < 60 -> "$totalMinutes min"
        totalMinutes % 60 == 0 -> "${totalMinutes / 60}h"
        else -> {
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            "${hours}h ${minutes}min"
        }
    }
}

@SuppressLint("DefaultLocale")
fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

