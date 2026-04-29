package com.example.purrsistence.domain.time

import java.time.Instant

class FakeTimeProvider(var currentTime: Instant) : TimeProvider {
    override fun now(): Instant = currentTime

    fun setNow(newNow: Instant) {
        currentTime  = newNow
    }
}