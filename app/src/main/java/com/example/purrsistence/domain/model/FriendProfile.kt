package com.example.purrsistence.domain.model

data class FriendProfile(
    val id: String,
    val username: String,
    val avatarPath: String? = null,
    val friendshipId: Long? = null
)