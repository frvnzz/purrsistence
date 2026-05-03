package com.example.purrsistence.domain.service

import com.example.purrsistence.domain.preferences.CleanupPreferences
import com.example.purrsistence.domain.time.FakeTimeProvider
import com.example.purrsistence.service.CleanupRunner
import com.example.purrsistence.service.CleanupScheduler
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class CleanupSchedulerTest {

    @Test
    fun runIfDue_runsCleanup_whenNoPreviousRunExists() = runTest {
        val preferences = FakeCleanupPreferences(storedLastCleanupTimestamp = 0L)
        val cleanupRunner = FakeCleanupRunner()
        val timeProvider = FakeTimeProvider(Instant.ofEpochMilli(100_000_000L))

        val scheduler = CleanupScheduler(
            preferences = preferences,
            timeProvider = timeProvider,
            cleanupRunner = cleanupRunner
        )

        scheduler.runIfDue()

        assertEquals(1, cleanupRunner.runCalls)
        assertEquals(100_000_000L, preferences.currentValue())
    }

    @Test
    fun runIfDue_doesNotRunCleanup_againWithinOneDay() = runTest {
        val now = 100_000_000L
        val preferences = FakeCleanupPreferences(storedLastCleanupTimestamp = now)
        val cleanupRunner = FakeCleanupRunner()
        val timeProvider = FakeTimeProvider(Instant.ofEpochMilli(now + 1_000L))

        val scheduler = CleanupScheduler(
            preferences = preferences,
            timeProvider = timeProvider,
            cleanupRunner = cleanupRunner
        )

        scheduler.runIfDue()

        assertEquals(0, cleanupRunner.runCalls)
        assertEquals(now, preferences.currentValue())
    }

    @Test
    fun runIfDue_runsCleanup_againAfterOneDay() = runTest {
        val oneDayMillis = 24 * 60 * 60 * 1000L
        val lastRun = 100_000_000L

        val preferences = FakeCleanupPreferences(storedLastCleanupTimestamp = lastRun)
        val cleanupRunner = FakeCleanupRunner()
        val timeProvider = FakeTimeProvider(
            Instant.ofEpochMilli(lastRun + oneDayMillis + 1L)
        )

        val scheduler = CleanupScheduler(
            preferences = preferences,
            timeProvider = timeProvider,
            cleanupRunner = cleanupRunner
        )

        scheduler.runIfDue()

        assertEquals(1, cleanupRunner.runCalls)
        assertEquals(lastRun + oneDayMillis + 1L, preferences.currentValue())
    }

    @Test
    fun runIfDue_doesNotRunCleanup_whenLessThanOneDayPassed() = runTest {
        val oneDayMillis = 24 * 60 * 60 * 1000L
        val lastRun = 100_000_000L

        val preferences = FakeCleanupPreferences(storedLastCleanupTimestamp = lastRun)
        val cleanupRunner = FakeCleanupRunner()
        val timeProvider = FakeTimeProvider(
            Instant.ofEpochMilli(lastRun + oneDayMillis - 1L)
        )

        val scheduler = CleanupScheduler(
            preferences = preferences,
            timeProvider = timeProvider,
            cleanupRunner = cleanupRunner
        )

        scheduler.runIfDue()

        assertEquals(0, cleanupRunner.runCalls)
        assertEquals(lastRun, preferences.currentValue())
    }

    @Test
    fun runIfDue_updatesTimestamp_onlyWhenCleanupWasRun() = runTest {
        val preferences = FakeCleanupPreferences(storedLastCleanupTimestamp = 0L)
        val cleanupRunner = FakeCleanupRunner()
        val timeProvider = FakeTimeProvider(Instant.ofEpochMilli(100_000_000L))

        val scheduler = CleanupScheduler(
            preferences = preferences,
            timeProvider = timeProvider,
            cleanupRunner = cleanupRunner
        )

        scheduler.runIfDue()

        assertEquals(1, cleanupRunner.runCalls)
        assertEquals(100_000_000L, preferences.currentValue())

        timeProvider.setNow(Instant.ofEpochMilli(100_000_000L))
        scheduler.runIfDue()

        assertEquals(1, cleanupRunner.runCalls)
        assertEquals(100_000_000L, preferences.currentValue())
    }
}

private class FakeCleanupRunner : CleanupRunner {
    var runCalls = 0

    override suspend fun runCleanup() {
        runCalls++
    }
}

private class FakeCleanupPreferences(
    private var storedLastCleanupTimestamp: Long
) : CleanupPreferences {

    override fun getLastCleanupTimestamp(): Long = storedLastCleanupTimestamp

    override fun setLastCleanupTimestamp(value: Long) {
        storedLastCleanupTimestamp = value
    }

    fun currentValue(): Long = storedLastCleanupTimestamp
}