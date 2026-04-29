package com.example.purrsistence.ui.util

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import com.example.purrsistence.data.local.entity.GoalEntity
import com.example.purrsistence.domain.model.Goal
import com.example.purrsistence.focus.DeepFocusAccessibilityState

fun handleStartTrackingClick(
    goal: Goal?,
    context: android.content.Context,
    onStartTracking: (Int, Int, Boolean) -> Unit,
    onNeedsAccessibilitySetup: () -> Unit
) {
    goal ?: return

    val needsAccessibilitySetup = goal.deepFocus &&
            !DeepFocusAccessibilityState.isServiceEnabled(context)

    if (needsAccessibilitySetup) {
        onNeedsAccessibilitySetup()
    } else {
        onStartTracking(goal.id, goal.userId, goal.deepFocus)
    }
}

fun openAccessibilitySettings(context: android.content.Context) {
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