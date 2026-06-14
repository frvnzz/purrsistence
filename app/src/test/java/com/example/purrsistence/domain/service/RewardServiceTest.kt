package com.example.purrsistence.domain.service

import com.example.purrsistence.service.RewardService
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration

class RewardServiceTest {

    private val service = RewardService()

    @Test
    fun reward_under30Seconds_givesZeroCoins() {
        val (coins, multiplier) = service.calculateReward(Duration.ofSeconds(29))

        assertEquals(0, coins)
        assertEquals(1.0, multiplier, 0.0001)
    }

    @Test
    fun reward_30Seconds_givesOneRewardInterval() {
        val (coins, multiplier) = service.calculateReward(Duration.ofSeconds(30))

        assertEquals(1, coins)
        assertEquals(1.15, multiplier, 0.0001)
    }

    @Test
    fun reward_40Seconds_stillGivesOneRewardInterval() {
        val (coins, multiplier) = service.calculateReward(Duration.ofSeconds(40))

        assertEquals(1, coins)
        assertEquals(1.15, multiplier, 0.0001)
    }

    @Test
    fun reward_60Seconds_givesTwoRewardIntervals() {
        val (coins, multiplier) = service.calculateReward(Duration.ofSeconds(60))

        // 2 intervals * 1.25 = 2.5, rounded to 3
        assertEquals(2, coins)
        assertEquals(1.25, multiplier, 0.0001)
    }

    @Test
    fun reward_15Minutes_capsMultiplierAtTwo() {
        val (coins, multiplier) = service.calculateReward(Duration.ofMinutes(15))

        // 15 minutes = 30 intervals
        // multiplier is capped at 2.0
        // 30 * 2.0 = 60
        assertEquals(60, coins)
        assertEquals(2.0, multiplier, 0.0001)
    }

    @Test
    fun reward_withCheckpointedCurrency_sumsCorrectly() {
        val (coins, multiplier) = service.calculateReward(
            duration = Duration.ofMinutes(10),
            hasLongPause = false,
            checkpointedCurrency = 38
        )

        // 10 minutes = 20 intervals
        // multiplier is capped at 2.0
        // 20 * 2.0 = 40
        // 40 + 38 checkpointed = 78
        assertEquals(78, coins)
        assertEquals(2.0, multiplier, 0.0001)
    }

    @Test
    fun reward_afterReset_multiplierIsOneButCurrencyKept() {
        val (coins, multiplier) = service.calculateReward(
            duration = Duration.ofMinutes(20),
            hasLongPause = true,
            checkpointedCurrency = 50
        )

        // 20 minutes = 40 intervals
        // because hasLongPause = true, current multiplier is forced to 1.0
        // 40 * 1.0 = 40
        // 40 + 50 checkpointed = 90
        assertEquals(90, coins)
        assertEquals(1.0, multiplier, 0.0001)
    }
}