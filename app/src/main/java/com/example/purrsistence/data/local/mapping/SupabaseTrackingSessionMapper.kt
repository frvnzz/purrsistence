package com.example.purrsistence.data.local.mapping

import com.example.purrsistence.data.local.entity.GoalEntity
import com.example.purrsistence.data.local.entity.TrackingSessionEntity
import com.example.purrsistence.data.remote.supabase.dto.TrackingSessionDto
import com.example.purrsistence.domain.model.TrackingSession
import java.time.Instant

fun TrackingSessionEntity.toSupabaseDto(
    supabaseUserId: String
): TrackingSessionDto {
    return TrackingSessionDto(
        userId = supabaseUserId,
        trackingId = trackingId,
        goalId = goalId,
        pauseReminder = pauseReminder,
        deepFocus = deepFocus,
        startTime = Instant.ofEpochMilli(startTime).toString(),
        endTime = endTime?.let {
            Instant.ofEpochMilli(it).toString()
        }
    )
}

fun TrackingSession.toSupabaseDto(
    supabaseUserId: String
): TrackingSessionDto {
    return TrackingSessionDto(
        userId = supabaseUserId,
        trackingId = id,
        goalId = goalId,
        pauseReminder = pauseReminder,
        deepFocus = deepFocus,
        startTime = startTime.toString(),
        endTime = endTime?.toString()
    )
}

fun TrackingSessionDto.toDomain(
    localUserId: Int
): TrackingSession {
    return TrackingSession(
        id = trackingId,
        goalId = goalId,
        userId = localUserId,
        pauseReminder = pauseReminder,
        deepFocus = deepFocus,
        startTime = parseSupabaseInstant(startTime),
        endTime = endTime?.let {
            parseSupabaseInstant(it)
        }
    )
}