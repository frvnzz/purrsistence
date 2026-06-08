package com.example.purrsistence.ui.util

import android.annotation.SuppressLint

/**
 * Formats minutes into a human-readable time string.
 *
 * Logic:
 * - Less than 60 minutes: displays as "X min" (e.g., "45 min")
 * - 60 minutes to under 24 hours: displays as "Xh YYmin" or "Xh" (e.g., "2h 30min", "2h")
 * - 24 hours or more: displays as "Xd Yh ZZmin" (e.g., "2d 1h 30min")
 */
fun formatMinutes(totalMinutes: Int): String {
    val days = totalMinutes / (24 * 60)
    val remainingMinutesAfterDays = totalMinutes % (24 * 60)
    val hours = remainingMinutesAfterDays / 60
    val minutes = remainingMinutesAfterDays % 60

    return when {
        days > 0 -> {
            val parts = mutableListOf<String>()
            parts.add("${formatLocalizedInteger(days)}d")
            if (hours > 0 || minutes > 0) parts.add("${hours}h")
            if (minutes > 0) parts.add("${minutes}min")
            parts.joinToString(" ")
        }

        hours > 0 -> {
            if (minutes == 0) {
                "${formatLocalizedInteger(hours)}h"
            } else {
                "${formatLocalizedInteger(hours)}h ${minutes}min"
            }
        }

        else -> {
            "${formatLocalizedInteger(totalMinutes)} min"
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

//format minutes into hours and minutes string for screen reader
fun formatMinutesForAccessibility(totalMinutes: Int): String {
    if (totalMinutes == 0) return "0 minutes"
    val d = totalMinutes / (24 * 60)
    val rem = totalMinutes % (24 * 60)
    val h = rem / 60
    val m = rem % 60

    val parts = mutableListOf<String>()
    if (d > 0) parts.add("$d days")
    if (h > 0) parts.add("$h hours")
    if (m > 0) parts.add("$m minutes")

    return when (parts.size) {
        0 -> "0 minutes"
        1 -> parts[0]
        2 -> "${parts[0]} and ${parts[1]}"
        else -> "${parts[0]}, ${parts[1]} and ${parts[2]}"
    }
}

