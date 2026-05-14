package com.example.purrsistence.service

import java.time.Duration
import kotlin.math.round

class RewardService {

    /*fun calculateReward(duration: Duration): Pair<Int, Double> {
        val trackedMinutes = duration.toMinutes().toInt()
        val multiplier = calculateRewardMultiplier(trackedMinutes)
        val coins = round(trackedMinutes * multiplier).toInt()
        return coins to multiplier
    }*/

    fun calculateReward(duration: Duration, pausedMillis: Long = 0L): Pair<Int, Double> {
        val effectiveMinutes = duration.toMinutes().toInt()
        val pausedMinutes = pausedMillis / (60 * 1000)
        val multiplier = calculateRewardMultiplier(effectiveMinutes)
        if (pausedMinutes > 15) return effectiveMinutes to 1.0  // Reset multiplier
        return round(effectiveMinutes * multiplier).toInt() to multiplier
    }

    private fun calculateRewardMultiplier(trackedMinutes: Int): Double {
        if (trackedMinutes < 15) return 1.0

        val additionalReward = (trackedMinutes - 15) / 15
        val multiplier = 1.15 + (additionalReward * 0.10)

        return multiplier.coerceAtMost(2.0)
    }
}