package com.example.purrsistence.service

import com.example.purrsistence.data.local.repository.StatisticsRepository
import com.example.purrsistence.domain.model.DailyStat
import com.example.purrsistence.domain.model.Goal
import com.example.purrsistence.domain.model.GoalStat
import com.example.purrsistence.domain.model.TrackingSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

class StatisticsService(
    private val statisticsRepository: StatisticsRepository
) {

    fun getWeeklyStats(weekOffset: Int): Flow<Pair<List<DailyStat>, List<GoalStat>>> {
        return combine(
            statisticsRepository.getCompletedSessionsForUser(),
            statisticsRepository.getGoalsForUser()
        ) { sessions, goals ->

            val zone = ZoneId.systemDefault()
            val weekRange = getWeekRange(zone, weekOffset)

            val weekSessions = filterSessionsInRange(sessions, weekRange, zone)

            val dailyStats = mapToDailyStats(weekSessions, zone)
            val goalStats = mapToGoalStats(weekSessions, goals)

            dailyStats to goalStats
        }
    }

    private fun getWeekRange(
        zone: ZoneId,
        weekOffset: Int
    ): ClosedRange<LocalDate> {
        val today = LocalDate.now(zone)
        val startOfCurrentWeek = today.with(DayOfWeek.MONDAY)
        val start = startOfCurrentWeek.plusWeeks(weekOffset.toLong())
        val end = start.plusDays(6)
        return start..end
    }

    private fun filterSessionsInRange(
        sessions: List<TrackingSession>,
        range: ClosedRange<LocalDate>,
        zone: ZoneId
    ): List<TrackingSession> {
        return sessions.filter { session ->
            val date = session.startTime.atZone(zone).toLocalDate()
            date in range
        }
    }

    private fun durationMinutes(session: TrackingSession): Int {
        val duration = session.finishedDuration() ?: return 0
        return duration.toMinutes().toInt().coerceAtLeast(0)
    }

    private fun mapToDailyStats(
        sessions: List<TrackingSession>,
        zone: ZoneId
    ): List<DailyStat> {
        val grouped = sessions.groupBy { session ->
            session.startTime.atZone(zone).dayOfWeek
        }

        return DayOfWeek.entries.map { day ->
            val total = grouped[day]?.sumOf { durationMinutes(it) } ?: 0
            DailyStat(day, total)
        }
    }

    private fun mapToGoalStats(
        sessions: List<TrackingSession>,
        goals: List<Goal>
    ): List<GoalStat> {
        val goalMap = goals.associateBy { it.id }

        return sessions
            .groupBy { it.goalId }
            .mapNotNull { (goalId, sessionsForGoal) ->
                val goal = goalMap[goalId] ?: return@mapNotNull null
                val total = sessionsForGoal.sumOf { durationMinutes(it) }

                GoalStat(
                    goalId = goalId,
                    goalName = goal.title,
                    totalMinutes = total
                )
            }
            .sortedByDescending { it.totalMinutes }
    }
}