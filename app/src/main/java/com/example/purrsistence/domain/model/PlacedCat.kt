package com.example.purrsistence.domain.model

data class PlacedCat(
    val catId: String,
    val spotId: String,
    val isMirrored: Boolean = false
)