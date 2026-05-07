package com.example.purrsistence.data.remote.supabase.dto

@kotlinx.serialization.Serializable
data class FriendProfileDto(
    val id: String,
    val username: String,
    val displayName: String? = null,
    val avatarPath: String? = null
)