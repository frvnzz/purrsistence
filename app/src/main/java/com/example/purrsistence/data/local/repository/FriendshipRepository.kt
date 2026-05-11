package com.example.purrsistence.data.local.repository

import com.example.purrsistence.domain.model.User
import kotlinx.coroutines.flow.Flow

interface FriendshipRepository {
    fun getFriends(userId: Int): Flow<List<User>>
}