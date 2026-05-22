package com.example.purrsistence.data.remote.supabase.dto

import kotlinx.serialization.Serializable

@Serializable
data class FriendProfileDto(
    val id: String,
    val username: String,
    val displayName: String? = null,
    val avatarPath: String? = null
)