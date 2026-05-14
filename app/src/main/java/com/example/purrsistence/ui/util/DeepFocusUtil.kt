package com.example.purrsistence.ui.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.purrsistence.domain.model.Goal
import com.example.purrsistence.focus.DeepFocusAccessibilityState

fun handleStartTrackingClick(
    goal: Goal?,
    context: Context,
    onStartTracking: (Int, String, Int, Boolean) -> Unit,
    onNeedsAccessibilitySetup: () -> Unit
) {
    goal ?: return

    val needsAccessibilitySetup = goal.deepFocus &&
            !DeepFocusAccessibilityState.isServiceEnabled(context)

    if (needsAccessibilitySetup) {
        onNeedsAccessibilitySetup()
    } else {
        // pass goal id, title, userId & deepFocus when starting a TrackingSession
        // -> this is called when clicking start on GoalBottomDrawer (HomeScreen)
        onStartTracking(goal.id, goal.title, goal.userId, goal.deepFocus)
    }
}

fun openAccessibilitySettings(context: Context) {
    try {
        context.startActivity(
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (_: ActivityNotFoundException) {
        context.startActivity(
            Intent(Settings.ACTION_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}

fun requiresDeepFocusSetup(
    context: Context,
    deepFocusEnabled: Boolean
): Boolean {

    return deepFocusEnabled &&
            !DeepFocusAccessibilityState
                .isServiceEnabled(context)
}