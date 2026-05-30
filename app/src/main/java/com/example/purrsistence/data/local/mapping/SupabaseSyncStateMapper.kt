package com.example.purrsistence.data.local.mapping

import com.example.purrsistence.data.local.converter.toSupabaseInstant
import com.example.purrsistence.data.remote.supabase.dto.UserSyncStateDto
import java.time.Instant

fun UserSyncStateDto.remoteUpdatedAtAsInstant(): Instant {
    return remoteUpdatedAt.toSupabaseInstant()
}