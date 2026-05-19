package com.example.purrsistence.domain.model

import java.net.URL
import java.time.Instant

data class User(
    val id: Int,
    val username: String,
    val profileImageUrl: URL?,
    val balance: Int,
    val friends: List<String>,
    val isSupabaseLinked: Boolean,
    val supabaseUserId: String?,
    val collectedCatsIds: List<String>,
    val selectedCatIds: List<String>,
    val localUpdatedAt: Instant?,
    val lastSyncedAt: Instant?,
    val hasPendingLocalChanges: Boolean
)