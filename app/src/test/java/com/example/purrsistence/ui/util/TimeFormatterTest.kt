package com.example.purrsistence.ui.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeFormatterTest {

    @Test
    fun `formatMinutes shows minutes under one hour`() {
        assertEquals("45 min", formatMinutes(45))
        assertEquals("1 min", formatMinutes(1))
    }

    @Test
    fun `formatMinutes shows hours for exact hours`() {
        assertEquals("1h", formatMinutes(60))
        assertEquals("11h", formatMinutes(660))
    }

    @Test
    fun `formatMinutes shows hours and minutes when not exact`() {
        assertEquals("2h 30min", formatMinutes(150))
        assertEquals("3h 5min", formatMinutes(185))
    }

    @Test
    fun `formatMinutes shows days, hours and minutes`() {
        assertEquals("1d 2h 30min", formatMinutes(1440 + 120 + 30))
        assertEquals("2d 0h 5min", formatMinutes(2880 + 5))
        assertEquals("10d", formatMinutes(14400))
        assertEquals("1d 1h", formatMinutes(1500))
    }
}
