package com.example.purrsistence.data.local.mapping

import com.example.purrsistence.data.local.relation.GoalWithSessionsEntity
import com.example.purrsistence.domain.model.GoalWithSessions

fun GoalWithSessionsEntity.toDomain(): GoalWithSessions =
    GoalWithSessions(
        goal = goalEntity.toDomain(),
        sessions = sessions.map { it.toDomain() }
    )