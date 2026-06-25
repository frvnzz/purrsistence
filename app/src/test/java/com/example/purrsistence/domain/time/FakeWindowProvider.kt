package com.example.purrsistence.domain.time

import java.time.Instant

class FakeWeekWindowProvider : WeekWindowProvider {
    override fun currentWeek(): WeekWindow {
        return WeekWindow(
            start = Instant.parse("2026-05-04T00:00:00Z"),
            end = Instant.parse("2026-05-11T00:00:00Z")
        )
    }
}