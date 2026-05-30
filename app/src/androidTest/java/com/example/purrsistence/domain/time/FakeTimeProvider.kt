package com.example.purrsistence.domain.time

import java.time.Duration
import java.time.Instant

class FakeTimeProvider(
    initialInstant: Instant = Instant.parse("2025-01-01T12:00:00Z")
) : TimeProvider {

    private var currentInstant: Instant = initialInstant

    override fun now(): Instant {
        return currentInstant
    }

    fun setNow(instant: Instant) {
        currentInstant = instant
    }

    fun advanceBy(duration: Duration) {
        currentInstant = currentInstant.plus(duration)
    }

    fun advanceSeconds(seconds: Long) {
        currentInstant = currentInstant.plusSeconds(seconds)
    }

    fun advanceMinutes(minutes: Long) {
        currentInstant = currentInstant.plusSeconds(minutes * 60)
    }

    fun advanceDays(days: Long) {
        currentInstant = currentInstant.plus(Duration.ofDays(days))
    }
}