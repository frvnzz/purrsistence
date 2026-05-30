package com.example.purrsistence.data.remote.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CatDto(
    val id: String,
    val name: String,
    val rarity: String,
    val price: Int,

    @SerialName("image_asset_name")
    val imageAssetName: String? = null,

    val description: String? = null
)