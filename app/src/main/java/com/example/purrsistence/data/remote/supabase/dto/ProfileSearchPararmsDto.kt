package com.example.purrsistence.data.remote.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileSearchParamsDto(
    @SerialName("search_query")
    val searchQuery: String,

    @SerialName("max_results")
    val maxResults: Int
)