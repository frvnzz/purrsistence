package com.example.purrsistence.data.local.repository

import com.example.purrsistence.data.local.dao.UserDao
import com.example.purrsistence.data.local.mapping.toDomain
import com.example.purrsistence.data.local.mapping.toEntity
import com.example.purrsistence.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface UserRepository {
    fun getUser(userId: Int): Flow<User?>
    suspend fun insertUser(user: User)
    suspend fun updateUser(user: User)
    suspend fun addCurrency(userId: Int, amount: Int)
}

class UserRepositoryImpl (
    private val userDao: UserDao
) : UserRepository {

    override fun getUser(userId: Int): Flow<User?> {
        return userDao.getUser(userId).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun insertUser(user: User) {
        userDao.insertUser(user.toEntity())
    }

    override suspend fun updateUser(user: User) {
        userDao.updateUser(user.toEntity())
    }

    override suspend fun addCurrency(userId: Int, amount: Int) {
        userDao.addCurrency(userId, amount)
    }
}