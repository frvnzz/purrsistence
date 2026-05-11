package com.example.purrsistence.ui.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeekDisplayTest {

    @Test
    fun `getWeekDisplay clamps future offsets to current`() {
        val (label, _) = getWeekDisplay(2)
        // Future offsets should be clamped to current week
        assertEquals("This Week", label)
    }

    @Test
    fun `getWeekDisplay labels recent past correctly`() {
        val (label0, range0) = getWeekDisplay(0)
        assertEquals("This Week", label0)
        assertTrue(range0.isNotBlank())

        val (label1, range1) = getWeekDisplay(-1)
        assertEquals("Last Week", label1)
        assertTrue(range1.isNotBlank())
    }
}

