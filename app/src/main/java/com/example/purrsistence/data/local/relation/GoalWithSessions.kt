package com.example.purrsistence.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.purrsistence.data.local.entity.Goal
import com.example.purrsistence.data.local.entity.TrackingSession

data class GoalWithSessions(
    @Embedded val goal: Goal,

    @Relation(
        parentColumn = "goalId",
        entityColumn = "goalId"
    )
    val sessions: List<TrackingSession>
)
