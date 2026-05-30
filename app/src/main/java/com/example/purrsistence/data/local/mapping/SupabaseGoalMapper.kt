package com.example.purrsistence.data.local.mapping

import com.example.purrsistence.data.local.entity.GoalEntity
import com.example.purrsistence.data.remote.supabase.dto.GoalsDto
import com.example.purrsistence.domain.model.Goal
import com.example.purrsistence.domain.model.types.GoalType
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime

fun GoalEntity.toSupabaseDto(
    supabaseUserId: String
): GoalsDto {
    return GoalsDto(
        userId = supabaseUserId,
        goalId = goalId,
        title = title,
        type = type,
        targetDuration = targetDuration,
        deepFocus = deepFocus,
        inactive = inactive,
        createdAt = Instant.ofEpochMilli(createdAt).toString(),
        isCompleted = isCompleted,
        lastCompletedAt = lastCompletedAt?.let {
            Instant.ofEpochMilli(it).toString()
        }
    )
}

fun Goal.toSupabaseDto(
    supabaseUserId: String
): GoalsDto {
    return GoalsDto(
        userId = supabaseUserId,
        goalId = id,
        title = title,
        type = type.name.lowercase(),
        targetDuration = targetDuration.toMinutes().toInt(),
        deepFocus = deepFocus,
        inactive = inactive,
        createdAt = createdAt.toString(),
        isCompleted = isCompleted,
        lastCompletedAt = lastCompletedAt?.toString()
    )
}

fun GoalsDto.toDomain(
    localUserId: Int
): Goal {
    return Goal(
        id = goalId,
        userId = localUserId,
        title = title,
        type = GoalType.valueOf(type.uppercase()),
        targetDuration = Duration.ofMinutes(targetDuration.toLong()),
        deepFocus = deepFocus,
        inactive = inactive,
        createdAt = parseSupabaseInstant(createdAt),
        isCompleted = isCompleted,
        lastCompletedAt = lastCompletedAt?.let {
            parseSupabaseInstant(it)
        }
    )
}

fun parseSupabaseInstant(value: String): Instant {
    return runCatching {
        Instant.parse(value)
    }.getOrElse {
        OffsetDateTime.parse(value).toInstant()
    }
}