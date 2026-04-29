package com.example.purrsistence.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

//INFO if this is updated, check if tests are still running (if not, update them accordingly)
@Entity(
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["goalId"],
            childColumns = ["goalId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ],
    indices = [androidx.room.Index(value = ["goalId"])]
)

data class TrackingSessionEntity(
    @PrimaryKey(autoGenerate = true) val trackingId: Int = 0,
    val goalId: Int,
    val userId: Int,
    val pauseReminder: Boolean,
    val deepFocus: Boolean,
    val startTime: Long,
    val endTime: Long?
)
