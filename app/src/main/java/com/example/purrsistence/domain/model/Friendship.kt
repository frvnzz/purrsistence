package com.example.purrsistence.domain.model

import com.example.purrsistence.domain.model.types.FriendshipStatus

data class Friendship(
    val id: Long?,
    val requesterId: String,
    val addresseeId: String,
    val status: FriendshipStatus
)