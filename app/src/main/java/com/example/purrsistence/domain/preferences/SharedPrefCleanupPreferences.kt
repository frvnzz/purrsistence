package com.example.purrsistence.domain.preferences


import android.content.SharedPreferences
import androidx.core.content.edit

class SharedPrefCleanupPreferences(
    private val sharedPreferences: SharedPreferences
) : CleanupPreferences {

    override fun getLastCleanupTimestamp(): Long {
        return sharedPreferences.getLong(KEY_LAST_CLEANUP_TIMESTAMP, 0L)
    }

    override fun setLastCleanupTimestamp(value: Long) {
        sharedPreferences.edit {
            putLong(KEY_LAST_CLEANUP_TIMESTAMP, value)
        }
    }

    companion object {
        private const val KEY_LAST_CLEANUP_TIMESTAMP = "last_cleanup_timestamp"
    }
}