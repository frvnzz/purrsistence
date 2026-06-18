package com.example.purrsistence.ui.util

import com.example.purrsistence.domain.model.types.GoalType
import com.example.purrsistence.service.RewardService
import java.time.Duration

fun goalCompletionRewardWarningText(
    rewardService: RewardService,
    type: String,
    targetMinutes: Int
): String? {
    if (targetMinutes < 1) return null

    val goalType = GoalType.valueOf(type.uppercase())
    val targetDuration = Duration.ofMinutes(targetMinutes.toLong())

    val isEligible = rewardService.isEligibleForCompletionReward(
        type = goalType,
        targetDuration = targetDuration
    )

    if (isEligible) return null

    val minimumText = when (goalType) {
        GoalType.DAILY -> "15 minutes"
        GoalType.WEEKLY -> "2 hours"
        GoalType.MONTHLY -> return null
    }

    return "If you want to earn extra rewards for completing a $type goal, set yourself a goal of at least $minimumText."
}