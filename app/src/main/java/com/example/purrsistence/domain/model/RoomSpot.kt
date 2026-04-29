package com.example.purrsistence.domain.model

data class RoomSpot(
    val id: String,
    // x and yPercent can be 0f–1f (responsive)
    val xPercent: Float,
    val yPercent: Float
)