package com.example.purrsistence.domain.model

import com.example.purrsistence.ui.components.animation.SpriteSheetData

data class ShopItem(
    val id: String,
    val name: String,
    val price: Int,
    val imageRes: Int,
    val animationData: SpriteSheetData? = null
)