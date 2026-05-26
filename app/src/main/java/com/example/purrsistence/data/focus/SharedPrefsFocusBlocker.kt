package com.example.purrsistence.data.focus

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.purrsistence.domain.focus.FocusBlocker
import com.example.purrsistence.focus.DeepFocusConfig

class SharedPrefsFocusBlocker(
    private val sharedPreferences: SharedPreferences
) : FocusBlocker {

    override fun startBlocking() {
        sharedPreferences.edit { putBoolean(DeepFocusConfig.KEY_BLOCKING_ACTIVE, true) }
    }

    override fun stopBlocking() {
        sharedPreferences.edit { putBoolean(DeepFocusConfig.KEY_BLOCKING_ACTIVE, false) }
    }
}
