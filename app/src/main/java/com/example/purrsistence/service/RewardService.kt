package com.example.purrsistence.service

import java.time.Duration
import kotlin.math.round

class RewardService {

    fun calculateReward(duration: Duration, hasLongPause: Boolean = false, checkpointedCurrency: Int = 0): Pair<Int, Double> {
        val effectiveMillis = duration.toMillis().toInt()

        val rewardIntervals = (effectiveMillis / 30_000L).toInt()

        val multiplier = calculateRewardMultiplier(effectiveMillis)

        val currentMultiplier = if (hasLongPause) {
            1.0
        } else {
            multiplier
        }

        val currentCoins = round(rewardIntervals * currentMultiplier).toInt()

        return (currentCoins + checkpointedCurrency) to currentMultiplier
    }

    fun calculateRewardIntervals(trackedMillis: Int): Int {
        return (trackedMillis.coerceAtLeast(0) / 30_000L).toInt()
    }

    fun calculateRewardMultiplier(trackedMillis: Int): Double {
        val rewardIntervals = calculateRewardIntervals(trackedMillis)

        if (rewardIntervals < 1) return 1.0

        val additionalReward = rewardIntervals - 1
        val multiplier = 1.15 + (additionalReward * 0.10)

        return multiplier.coerceAtMost(2.0)
    }
}