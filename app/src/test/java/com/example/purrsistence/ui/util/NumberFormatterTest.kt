package com.example.purrsistence.ui.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class NumberFormatterTest {

    @Test
    fun `formatLocalizedDecimal uses comma for german locale`() {
        val previous = Locale.getDefault()
        Locale.setDefault(Locale.GERMANY)

        try {
            assertEquals("10,0", formatLocalizedDecimal(10.0, fractionDigits = 1))
            assertEquals("1,25", formatLocalizedDecimal(1.25, fractionDigits = 2))
        } finally {
            Locale.setDefault(previous)
        }
    }

    @Test
    fun `formatLocalizedNumber uses locale specific grouping`() {
        val previous = Locale.getDefault()
        Locale.setDefault(Locale.US)

        try {
            assertEquals("12,345", formatLocalizedInteger(12345))
        } finally {
            Locale.setDefault(previous)
        }
    }
}
