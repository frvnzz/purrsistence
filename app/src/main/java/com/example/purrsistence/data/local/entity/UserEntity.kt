package com.example.purrsistence.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

//INFO if this is updated, check if tests are still running (if not, update them accordingly)
@Entity
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,
    val username: String,
    val profileImageUrl: String?,
    val balance: Int,
    val isSupabaseLinked: Boolean,
    val supabaseUserId: String?,
    val friends: List<String>,
    val collectedCatsIds: List<String>,
    val selectedCatIds: List<String>,
    val localUpdatedAt: Long? = null,
    val lastSyncedAt: Long? = null,
    val hasPendingLocalChanges: Boolean = false
)
