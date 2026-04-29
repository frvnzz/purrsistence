package com.example.purrsistence.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.purrsistence.data.local.entity.GoalEntity
import com.example.purrsistence.data.local.entity.TrackingSessionEntity

data class GoalWithSessionsEntity(
    @Embedded val goalEntity: GoalEntity,

    @Relation(
        parentColumn = "goalId",
        entityColumn = "goalId"
    )
    val sessions: List<TrackingSessionEntity>
)