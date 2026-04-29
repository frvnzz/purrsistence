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

        assertEquals(10, coins)
        assertEquals(1.0, multiplier, 0.0001)
    }

    @Test
    fun reward_15Minutes_appliesMultiplier(){
        val (coins, multiplier) = service.calculateReward(Duration.ofMinutes(15))

        assertEquals(17, coins)
        assertEquals(1.15, multiplier, 0.0001)
    }

    @Test
    fun reward_30Minutes_appliesHigherMultiplier() {
        val (coins, multiplier) = service.calculateReward(Duration.ofMinutes(30))

        assertEquals(38, coins)
        assertEquals(1.25, multiplier, 0.0001)
    }

    @Test
    fun reward_isCappedAtTwoTimes() {
        val (coins, multiplier) = service.calculateReward(Duration.ofMinutes(150))

        assertEquals(300, coins)
        assertEquals(2.0, multiplier, 0.0001)
    }



}