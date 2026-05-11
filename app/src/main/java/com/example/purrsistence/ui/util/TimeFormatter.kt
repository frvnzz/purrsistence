package com.example.purrsistence.ui.util

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

