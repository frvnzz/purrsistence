package com.example.purrsistence.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ],
    indices = [androidx.room.Index(value = ["userId"])]
)

// TODO: refactor entity + add "canonical Goal" :)

//TODO add tracked time
//INFO if this is updated, check if tests are still running (if not, update them accordingly)
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val goalId: Int = 0,

    val userId: Int,
    val title: String,
    val type: String,
    val targetDuration: Int, // in minutes
    val deepFocus: Boolean,
    val inactive: Boolean, // if user deletes a goal, we still want to keep the data for statistics, so we just mark it as inactive
    val createdAt: Long,
    val isCompleted: Boolean
)
