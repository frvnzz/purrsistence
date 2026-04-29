package com.example.purrsistence.data.local.mapping

import com.example.purrsistence.data.local.entity.TrackingSessionEntity
import com.example.purrsistence.domain.model.TrackingSession
import java.time.Instant

fun TrackingSessionEntity.toDomain(): TrackingSession =
    TrackingSession(
        id = trackingId,
        goalId = goalId,
        userId = userId,
        pauseReminder = pauseReminder,
        deepFocus = deepFocus,
        startTime = Instant.ofEpochMilli(startTime),
        endTime = endTime?.let { Instant.ofEpochMilli(it) }
    )

fun TrackingSession.toEntity(): TrackingSessionEntity =
    TrackingSessionEntity(
        trackingId = id,
        goalId = goalId,
        userId = userId,
        pauseReminder = pauseReminder,
        deepFocus = deepFocus,
        startTime = startTime.toEpochMilli(),
        endTime = endTime?.toEpochMilli()
    )