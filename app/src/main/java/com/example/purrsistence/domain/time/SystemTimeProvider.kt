package com.example.purrsistence.domain.time

import java.lang.System.currentTimeMillis
import java.time.Instant

class SystemTimeProvider : TimeProvider {
    override fun now(): Instant= Instant.now()
}