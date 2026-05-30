package com.example.purrsistence.ui.util


import java.util.Locale

fun maxHourForGoalType(type: String): Int =
    when (type) {
        "Daily" -> 23
        "Weekly" -> 99
        "Monthly" -> 99
        else -> 23
    }

fun clampDurationParts(
    type: String,
    hours: String,
    minutes: String
): Pair<String, String> {
    val maxHours = maxHourForGoalType(type)

    val safeHours = (hours.toIntOrNull() ?: 0)
        .coerceIn(0, maxHours)

    val safeMinutes = (minutes.toIntOrNull() ?: 0)
        .coerceIn(0, 59)

    return formatDurationPart(safeHours) to formatDurationPart(safeMinutes)
}

fun durationPartsToMinutes(
    type: String,
    hours: String,
    minutes: String
): Int {
    val (safeHours, safeMinutes) = clampDurationParts(
        type = type,
        hours = hours,
        minutes = minutes
    )

    return safeHours.toInt() * 60 + safeMinutes.toInt()
}

private fun formatDurationPart(value: Int): String =
    String.format(Locale.getDefault(), "%02d", value)