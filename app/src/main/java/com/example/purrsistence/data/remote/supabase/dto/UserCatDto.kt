package com.example.purrsistence.data.remote.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserCatDto(
    @SerialName("user_id")
    val userId: String,

    @SerialName("cat_id")
    val catId: String,

    val source: String? = null
)