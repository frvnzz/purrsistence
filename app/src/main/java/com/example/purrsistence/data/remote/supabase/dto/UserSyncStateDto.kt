package com.example.purrsistence.data.remote.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserSyncStateDto(
    @SerialName("user_id")
    val userId: String,

    @SerialName("remote_updated_at")
    val remoteUpdatedAt: String
)