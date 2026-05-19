package com.example.purrsistence.data.local.converter

import java.time.Instant
import java.time.OffsetDateTime

internal fun String.toSupabaseInstant(): Instant {
    return runCatching {
        Instant.parse(this)
    }.getOrElse {
        OffsetDateTime.parse(this).toInstant()
    }
}

internal fun String?.toSupabaseInstantOrNull(): Instant? {
    return this
        ?.takeIf { value -> value.isNotBlank() }
        ?.let { value -> value.toSupabaseInstant() }
}

internal fun Instant.toSupabaseTimestamp(): String {
    return this.toString()
}

internal fun Instant?.toSupabaseTimestampOrNull(): String? {
    return this?.toString()
}