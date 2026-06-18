package com.example.purrsistence.service

import com.example.purrsistence.domain.model.Goal
import com.example.purrsistence.domain.model.types.GoalType
import java.time.Duration
import kotlin.math.round

class RewardService {

    fun calculateReward(duration: Duration, hasLongPause: Boolean = false, checkpointedCurrency: Int = 0): Pair<Int, Double> {
        val effectiveMinutes = duration.toMinutes().toInt()
        val multiplier = calculateRewardMultiplier(effectiveMinutes)
        
        //if multiplier was reset in the last time block, we only return 1.0 for the current block.
        //but the total coins should include checkpointed ones.
        val currentMultiplier = if (hasLongPause) 1.0 else multiplier
        val currentCoins = round(effectiveMinutes * currentMultiplier).toInt()
        
        return (currentCoins + checkpointedCurrency) to currentMultiplier
    }

    fun calculateRewardMultiplier(trackedMinutes: Int): Double {
        if (trackedMinutes < 15) return 1.0

        val additionalReward = (trackedMinutes - 15) / 15
        val multiplier = 1.15 + (additionalReward * 0.10)

        return multiplier.coerceAtMost(2.0)
    }
    fun minimumGoalRewardDuration(type: GoalType): Duration {
        return when (type) {
            GoalType.DAILY -> Duration.ofMinutes(15)
            GoalType.WEEKLY -> Duration.ofHours(2)

            // Keep existing monthly behavior unless a monthly minimum is added later.
            GoalType.MONTHLY -> Duration.ZERO
        }
    }

    fun isEligibleForCompletionReward(
        type: GoalType,
        targetDuration: Duration
    ): Boolean {
        return targetDuration >= minimumGoalRewardDuration(type)
    }

    fun calculateGoalCompletionReward(goal: Goal): Int {
        if (!isEligibleForCompletionReward(goal.type, goal.targetDuration)) {
            return 0
        }

        return when (goal.type) {
            GoalType.DAILY -> 50
            GoalType.WEEKLY -> 200
            GoalType.MONTHLY -> 500
        }
    }
}