package com.example.purrsistence.data.local.repository


import com.example.purrsistence.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserRepository : UserRepository {

    private val users = mutableMapOf<Int, User>()
    private val userFlows = mutableMapOf<Int, MutableStateFlow<User?>>()

    override fun getUser(userId: Int): Flow<User?> {
        return userFlows.getOrPut(userId) { MutableStateFlow(users[userId]) }
    }

    override suspend fun insertUser(user: User) {
        users[user.id] = user
        userFlows[user.id] = MutableStateFlow(user)
    }

    override suspend fun updateUser(user: User) {
        users[user.id] = user
        userFlows.getOrPut(user.id) { MutableStateFlow(null) }.value = user
    }

    override suspend fun addCurrency(userId: Int, amount: Int) {
        val user = users[userId] ?: return
        val updated = user.copy(balance = user.balance + amount)
        users[userId] = updated
        userFlows.getOrPut(userId) { MutableStateFlow(null) }.value = updated
    }
}