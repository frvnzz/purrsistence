package com.example.purrsistence.domain.preferences

interface CleanupPreferences {
    fun getLastCleanupTimestamp(): Long
    fun setLastCleanupTimestamp(value: Long)
}