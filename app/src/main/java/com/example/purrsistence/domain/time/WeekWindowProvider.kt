package com.example.purrsistence.domain.time

import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

data class WeekWindow(
    val start: Instant,
    val end: Instant
)

interface WeekWindowProvider {
    fun currentWeek(): WeekWindow
}

class CurrentWeekWindowProvider(
    private val timeProvider: TimeProvider,
    private val zoneId: ZoneId
) : WeekWindowProvider {

    override fun currentWeek(): WeekWindow {
        val localDate = timeProvider
            .now()
            .atZone(zoneId)
            .toLocalDate()

        val weekStartDate = localDate.with(
            TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
        )

        val start = weekStartDate
            .atStartOfDay(zoneId)
            .toInstant()

        val end = start.plus(7, ChronoUnit.DAYS)

        return WeekWindow(
            start = start,
            end = end
        )
    }
}