package com.example.purrsistence.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.purrsistence.data.local.entity.Goal
import com.example.purrsistence.data.local.entity.TrackingSession

// TODO: please explain, and maybe remove this logic so that we can access Goal directly?
// -> see GoalsScreen LazyColumn items (goalWithSessions.goal.title)

data class GoalWithSessions(
    @Embedded val goal: Goal,

    @Relation(
        parentColumn = "goalId",
        entityColumn = "goalId"
    )
    val sessions: List<TrackingSession>
)
