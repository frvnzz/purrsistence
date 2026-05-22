package com.example.purrsistence.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

//INFO if this is updated, check if tests are still running (if not, update them accordingly)
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["goalId"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["goalId"])]
)

data class TrackingSessionEntity(
    @PrimaryKey(autoGenerate = true) val trackingId: Int = 0,
    val goalId: Int,
    val userId: Int,
    val pauseReminder: Boolean,
    val deepFocus: Boolean,
    val startTime: Long,
    val endTime: Long?,
    val pauseHistory: String = "0;;0", //totalPausedMillis;start1-end1,start2-end2;checkpointedCurrency
    val currentPauseStart: Long? = null, //when current pause has started
    val lastResetTime: Long? = null,
)
