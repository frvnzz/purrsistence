package com.example.purrsistence.domain.model

data class UserProfile(
    val userId: Int,
    val supabaseUserId: String?,
    val username: String,
    val profileImageUrl: String?,
    val balance: Int,
    val friends: List<FriendProfile>,
    val collectedCatIds: List<String>,
    val isSupabaseLinked: Boolean
)

data class FriendProfile(
    val userId: Int,
    val username: String,
    val profileImageUrl: String?
)