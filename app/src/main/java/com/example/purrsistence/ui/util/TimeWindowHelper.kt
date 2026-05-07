package com.example.purrsistence.ui.util

import java.time.DayOfWeek
import java.time.Instant
import java.time.ZonedDateTime

data class TimeWindow ( // Represents a time window with a start and end Instant
    val start: Instant,
    val end: Instant,
)

fun currentDayWindow(now: ZonedDateTime): TimeWindow{ //get time window for current day with cutoff hour
    val cutoffHour = 3 //Time when a new day starts, e.g. 3 means that the day changes at 3am

    val adjustedNow = if(now.hour < cutoffHour) { // If it's before the cutoff hour, we consider it part of the previous day
        now.minusDays(1)
    } else now

    val start = adjustedNow
        .withHour(cutoffHour)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)

    val end = start.plusDays(1)

    return TimeWindow(start.toInstant(), end.toInstant())
}

fun currentWeekWindow(now: ZonedDateTime): TimeWindow{ //get time window for week starting on Monday, with cutoff hour
    val cutoffHour = 3

    val adjustedNow = if(now.hour < cutoffHour) { // If it's before the cutoff hour, we consider it part of the previous day
        now.minusDays(1)
    } else now

    val startOfWeek = adjustedNow
        .with(DayOfWeek.MONDAY)
        .withHour(cutoffHour)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)

    val end = startOfWeek.plusWeeks(1)

    return TimeWindow(startOfWeek.toInstant(), end.toInstant())
}

fun currentMonthWindow(now: ZonedDateTime): TimeWindow { //get time window for current month with cutoff hour
    val cutoffHour = 3

    val adjustedNow = if (now.hour < cutoffHour) {
        now.minusDays(1)
    } else now

    val startOfMonth = adjustedNow
        .withDayOfMonth(1)
        .withHour(cutoffHour)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)

    val end = startOfMonth.plusMonths(1)

    return TimeWindow(
        start = startOfMonth.toInstant(),
        end = end.toInstant()
    )
}