package com.example.purrsistence.data.remote.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto(
    val id: String,
    val username: String,

    @SerialName("display_name")
    val displayName: String? = null,

    @SerialName("avatar_path")
    val avatarPath: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null,

    val balance: Int = 0
)