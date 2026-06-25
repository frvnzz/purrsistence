package com.example.purrsistence.ui.util

import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager

/**
 * Safely sends an accessibility announcement if accessibility is enabled.
 * This handles the potential [IllegalStateException] if accessibility is disabled
 */
fun AccessibilityManager.safeAnnounce(message: String) {
    if (isEnabled) {
        try {
            val event = AccessibilityEvent.obtain().apply {
                eventType = AccessibilityEvent.TYPE_ANNOUNCEMENT
                text.add(message)
            }
            sendAccessibilityEvent(event)
        } catch (e: IllegalStateException) {
            // Accessibility service state changed
        } catch (e: Exception) {
            // any other internal accessibility errors
        }
    }
}

/**
 * Returns true if animations are enabled in the system settings.
 * Specifically checks the "Remove animations" setting in Accessibility.
 */
fun Context.isAnimationEnabled(): Boolean {
    return try {
        Settings.Global.getFloat(
            contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) > 0f
    } catch (e: Exception) {
        true
    }
}
