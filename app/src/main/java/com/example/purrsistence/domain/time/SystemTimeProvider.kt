package com.example.purrsistence.domain.time

import java.lang.System.currentTimeMillis

class SystemTimeProvider : TimeProvider {
    override fun now(): Long= currentTimeMillis()
}