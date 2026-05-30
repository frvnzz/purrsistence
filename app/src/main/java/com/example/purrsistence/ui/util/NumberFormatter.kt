package com.example.purrsistence.ui.util

import java.text.NumberFormat
import java.util.Locale

private fun createNumberFormatter(
    minFractionDigits: Int,
    maxFractionDigits: Int,
    useGrouping: Boolean,
): NumberFormat {
    return NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = minFractionDigits
        maximumFractionDigits = maxFractionDigits
        isGroupingUsed = useGrouping
    }
}

fun formatLocalizedNumber(
    value: Number,
    minFractionDigits: Int = 0,
    maxFractionDigits: Int = 0,
    useGrouping: Boolean = true,
): String {
    return createNumberFormatter(
        minFractionDigits = minFractionDigits,
        maxFractionDigits = maxFractionDigits,
        useGrouping = useGrouping,
    ).format(value)
}

fun formatLocalizedDecimal(value: Double, fractionDigits: Int): String {
    return formatLocalizedNumber(
        value = value,
        minFractionDigits = fractionDigits,
        maxFractionDigits = fractionDigits,
        useGrouping = false,
    )
}

fun formatLocalizedInteger(value: Int): String {
    return formatLocalizedNumber(value = value, minFractionDigits = 0, maxFractionDigits = 0)
}
