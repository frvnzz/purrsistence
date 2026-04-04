package com.example.purrsistence.domain.time

class FakeTimeProvider(var currentTime: Long) : TimeProvider {
    override fun now(): Long = currentTime
}