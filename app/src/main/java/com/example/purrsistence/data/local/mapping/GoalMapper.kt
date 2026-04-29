package com.example.purrsistence.data.local.mapping

import com.example.purrsistence.data.local.entity.GoalEntity
import com.example.purrsistence.domain.model.Goal
import com.example.purrsistence.domain.model.types.GoalType
import java.time.Duration
import java.time.Instant

fun GoalEntity.toDomain(): Goal =
    Goal(
        id = goalId,
        userId = userId,
        title = title,
        type = GoalType.valueOf(type.uppercase()),
        targetDuration = Duration.ofMinutes(targetDuration.toLong()),
        deepFocus = deepFocus,
        inactive = inactive,
        createdAt = Instant.ofEpochMilli(createdAt),
        isCompleted = isCompleted
    )

fun Goal.toEntity(): GoalEntity =
    GoalEntity(
        goalId = id,
        userId = userId,
        title = title,
        type = type.name,
        targetDuration = targetDuration.toMinutes().toInt(),
        deepFocus = deepFocus,
        inactive = inactive,
        createdAt = createdAt.toEpochMilli(),
        isCompleted = isCompleted
    )