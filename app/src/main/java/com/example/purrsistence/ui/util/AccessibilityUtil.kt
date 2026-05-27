package com.example.purrsistence.ui.util

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
