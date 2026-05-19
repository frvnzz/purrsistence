package com.example.purrsistence.data.remote.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FriendshipDto(
    val id: Long? = null,

    @SerialName("requester_id")
    val requesterId: String,

    @SerialName("addressee_id")
    val addresseeId: String,

    val status: String
)