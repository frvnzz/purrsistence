package com.example.purrsistence.service

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
}