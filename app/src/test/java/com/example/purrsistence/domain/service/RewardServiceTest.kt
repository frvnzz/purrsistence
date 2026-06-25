package com.example.purrsistence.domain.service

import com.example.purrsistence.service.RewardService
import org.junit.Test
import java.time.Duration
import org.junit.Assert.assertEquals

class RewardServiceTest {
    private val service = RewardService()

    @Test
    fun reward_under15Min_noMultiplier() {
        val (coins, multiplier) = service.calculateReward(Duration.ofMinutes(10))

        assertEquals(100, coins)
        assertEquals(1.0, multiplier, 0.0001)
    }

    @Test
    fun reward_15Minutes_appliesMultiplier(){
        val (coins, multiplier) = service.calculateReward(Duration.ofMinutes(15))

        assertEquals(172, coins)
        assertEquals(1.15, multiplier, 0.0001)
    }

    @Test
    fun reward_withCheckpointedCurrency_sumsCorrectly() {
        // 10 minutes (10 coins) + 38 checkpointed coins = 48
        val (coins, multiplier) = service.calculateReward(
            duration = Duration.ofMinutes(10),
            hasLongPause = false,
            checkpointedCurrency = 38
        )

        assertEquals(138, coins)
        assertEquals(1.0, multiplier, 0.0001)
    }

    @Test
    fun reward_afterReset_multiplierIsOneButCurrencyKept() {
        //even if we have focus time, if hasLongPause is true (meaning the current time block ended in a long pause)
        //the current block multiplier is 1.0
        val (coins, multiplier) = service.calculateReward(
            duration = Duration.ofMinutes(20),
            hasLongPause = true,
            checkpointedCurrency = 50
        )

        assertEquals(250, coins) // 20 + 50 (checkpoint)
        assertEquals(1.0, multiplier, 0.0001)
    }
}