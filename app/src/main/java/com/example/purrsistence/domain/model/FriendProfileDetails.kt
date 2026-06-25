package com.example.purrsistence.domain.model

data class FriendProfileDetails(
    val profile: FriendProfile,
    val collectedCatIds: List<String>,
    val selectedCatIds: List<String>,
    val weeklyTrackedMinutes: Long
)