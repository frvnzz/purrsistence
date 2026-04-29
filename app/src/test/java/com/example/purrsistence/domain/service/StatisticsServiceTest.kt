package com.example.purrsistence.domain.service

import com.example.purrsistence.data.local.repository.FakeStatisticsRepository
import com.example.purrsistence.domain.model.DailyStat
import com.example.purrsistence.domain.model.Goal
import com.example.purrsistence.domain.model.TrackingSession
import com.example.purrsistence.domain.model.types.GoalType
import com.example.purrsistence.service.StatisticsService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class StatisticsServiceTest {

    @Test
    fun getWeeklyStats_returnsCorrectDailyAndGoalStats_forCurrentWeek() = runBlocking {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val monday = today.with(DayOfWeek.MONDAY)
        val tuesday = monday.plusDays(1)
        val previousSunday = monday.minusDays(1)

        val goals = listOf(
            createGoal(id = 1, title = "Read"),
            createGoal(id = 2, title = "Write")
        )

        val sessions = listOf(
            createSession(
                id = 1,
                goalId = 1,
                startDate = monday,
                zone = zone,
                durationMinutes = 30
            ),
            createSession(
                id = 2,
                goalId = 2,
                startDate = tuesday,
                zone = zone,
                durationMinutes = 45
            ),
            // outside current week -> must be ignored
            createSession(
                id = 3,
                goalId = 1,
                startDate = previousSunday,
                zone = zone,
                durationMinutes = 60
            )
        )

        val repository = FakeStatisticsRepository(
            goals = goals,
            sessions = sessions
        )

        val service = StatisticsService(repository)

        val (dailyStats, goalStats) = service.getWeeklyStats(0).first()

        assertEquals(7, dailyStats.size)

        assertDayMinutes(dailyStats, DayOfWeek.MONDAY, 30)
        assertDayMinutes(dailyStats, DayOfWeek.TUESDAY, 45)
        assertDayMinutes(dailyStats, DayOfWeek.WEDNESDAY, 0)
        assertDayMinutes(dailyStats, DayOfWeek.THURSDAY, 0)
        assertDayMinutes(dailyStats, DayOfWeek.FRIDAY, 0)
        assertDayMinutes(dailyStats, DayOfWeek.SATURDAY, 0)
        assertDayMinutes(dailyStats, DayOfWeek.SUNDAY, 0)

        assertEquals(2, goalStats.size)
        assertEquals(2, goalStats[0].goalId)
        assertEquals("Write", goalStats[0].goalName)
        assertEquals(45, goalStats[0].totalMinutes)

        assertEquals(1, goalStats[1].goalId)
        assertEquals("Read", goalStats[1].goalName)
        assertEquals(30, goalStats[1].totalMinutes)
    }

    @Test
    fun getWeeklyStats_withPreviousWeekOffset_returnsOnlyPreviousWeekSessions() = runBlocking {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val currentMonday = today.with(DayOfWeek.MONDAY)
        val previousMonday = currentMonday.minusWeeks(1)

        val goals = listOf(
            createGoal(id = 1, title = "Read")
        )

        val sessions = listOf(
            createSession(
                id = 1,
                goalId = 1,
                startDate = previousMonday,
                zone = zone,
                durationMinutes = 20
            ),
            createSession(
                id = 2,
                goalId = 1,
                startDate = currentMonday,
                zone = zone,
                durationMinutes = 40
            )
        )

        val repository = FakeStatisticsRepository(
            goals = goals,
            sessions = sessions
        )

        val service = StatisticsService(repository)

        val (dailyStats, goalStats) = service.getWeeklyStats(-1).first()

        assertDayMinutes(dailyStats, DayOfWeek.MONDAY, 20)
        assertEquals(1, goalStats.size)
        assertEquals(20, goalStats[0].totalMinutes)
    }

    @Test
    fun getWeeklyStats_ignoresSessionsWithUnknownGoal_inGoalStats() = runBlocking {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val monday = today.with(DayOfWeek.MONDAY)

        val goals = listOf(
            createGoal(id = 1, title = "Known Goal")
        )

        val sessions = listOf(
            createSession(
                id = 1,
                goalId = 1,
                startDate = monday,
                zone = zone,
                durationMinutes = 25
            ),
            createSession(
                id = 2,
                goalId = 999,
                startDate = monday,
                zone = zone,
                durationMinutes = 50
            )
        )

        val repository = FakeStatisticsRepository(
            goals = goals,
            sessions = sessions
        )

        val service = StatisticsService(repository)

        val (dailyStats, goalStats) = service.getWeeklyStats(0).first()

        // Daily stats include all sessions in range
        assertDayMinutes(dailyStats, DayOfWeek.MONDAY, 75)

        // Goal stats include only sessions whose goal exists
        assertEquals(1, goalStats.size)
        assertEquals(1, goalStats[0].goalId)
        assertEquals("Known Goal", goalStats[0].goalName)
        assertEquals(25, goalStats[0].totalMinutes)
    }

    @Test
    fun getWeeklyStats_withNoSessions_returnsZeroDailyStats_andEmptyGoalStats() = runBlocking {
        val repository = FakeStatisticsRepository(
            goals = emptyList(),
            sessions = emptyList()
        )

        val service = StatisticsService(repository)

        val (dailyStats, goalStats) = service.getWeeklyStats(0).first()

        assertEquals(7, dailyStats.size)
        assertTrue(dailyStats.all { it.totalMinutes == 0 })
        assertTrue(goalStats.isEmpty())
    }

    private fun assertDayMinutes(
        dailyStats: List<DailyStat>,
        day: DayOfWeek,
        expectedMinutes: Int
    ) {
        val stat = dailyStats.first { it.dayOfWeek == day }
        assertEquals(expectedMinutes, stat.totalMinutes)
    }

    private fun createGoal(
        id: Int,
        title: String
    ): Goal {
        return Goal(
            id = id,
            userId = 1,
            title = title,
            type = GoalType.WEEKLY,
            targetDuration = Duration.ofMinutes(120),
            deepFocus = false,
            inactive = false,
            createdAt = Instant.ofEpochMilli(1_000L),
            isCompleted = false
        )
    }

    private fun createSession(
        id: Int,
        goalId: Int,
        startDate: LocalDate,
        zone: ZoneId,
        durationMinutes: Long
    ): TrackingSession {
        val start = startDate.atStartOfDay(zone).plusHours(10).toInstant()
        val end = start.plusSeconds(durationMinutes * 60)

        return TrackingSession(
            id = id,
            goalId = goalId,
            userId = 1,
            pauseReminder = false,
            deepFocus = false,
            startTime = start,
            endTime = end
        )
    }
}
