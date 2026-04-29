package com.example.purrsistence.domain.model

import com.example.purrsistence.domain.model.types.GoalType
import java.time.Duration
import java.time.Instant

data class Goal(
    val id: Int,
    val userId: Int,
    val title: String,
    val type: GoalType,
    val targetDuration: Duration,
    val deepFocus: Boolean,
    val inactive: Boolean,
    val createdAt: Instant,
    val isCompleted: Boolean
)