package com.example.purrsistence.domain.time

import java.time.Instant

interface TimeProvider {
    fun now(): Instant
}