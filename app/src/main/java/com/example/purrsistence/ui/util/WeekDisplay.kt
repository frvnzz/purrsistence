package com.example.purrsistence.ui.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Generates a contextual label and formatted date range for a week.
 *
 * Returns a pair of:
 * - Label: "This Week", "Last Week", "2 weeks ago", etc.
 * - DateRange: "27 Apr - 3 May" (compact format, no year unless different from current)
 */
fun getWeekDisplay(weekOffset: Int): Pair<String, String> {
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val safeOffset = weekOffset.coerceAtMost(0)

    // Calculate week start (Monday)
    val currentWeekStart = today.with(DayOfWeek.MONDAY)
    val targetWeekStart = currentWeekStart.plusWeeks(safeOffset.toLong())
    val targetWeekEnd = targetWeekStart.plusDays(6)

    // Generate contextual label
    val label = when (safeOffset) {
        0 -> "This Week"
        -1 -> "Last Week"
        -2 -> "2 weeks ago"
        -3 -> "3 weeks ago"
        else -> "${-safeOffset} weeks ago"
    }

    // Format dates: "27 Apr - 3 May" or "27 Apr - 3 May 2025" if year differs
    val startMonth = targetWeekStart.format(DateTimeFormatter.ofPattern("d MMM"))
    val endMonth = targetWeekEnd.format(DateTimeFormatter.ofPattern("d MMM"))
    val currentYear = today.year
    val targetYear = targetWeekStart.year

    val dateRange = if (currentYear != targetYear) {
        val startWithYear = targetWeekStart.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
        "$startWithYear - $endMonth"
    } else {
        "$startMonth - $endMonth"
    }

    return Pair(label, dateRange)
}

