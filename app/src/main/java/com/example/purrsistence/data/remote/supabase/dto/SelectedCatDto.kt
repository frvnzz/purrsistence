package com.example.purrsistence.data.remote.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SelectedCatDto(
    @SerialName("user_id")
    val userId: String,

    val slot: Int,

    @SerialName("cat_id")
    val catId: String
)