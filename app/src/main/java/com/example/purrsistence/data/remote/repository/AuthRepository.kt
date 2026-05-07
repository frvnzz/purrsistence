package com.example.purrsistence.data.remote.repository

import com.example.purrsistence.data.remote.supabase.datasource.AuthRemoteDataSource

interface AuthRepository {
    suspend fun signUp(email: String, password: String, username: String)
    suspend fun signIn(email: String, password: String)
    suspend fun signOut()
    fun currentUserId(): String?
}

class AuthRepositoryImpl(
    private val remoteDataSource: AuthRemoteDataSource
) : AuthRepository {

    override suspend fun signUp(
        email: String,
        password: String,
        username: String
    ) {
        remoteDataSource.signUp(email, password, username)
    }

    override suspend fun signIn(
        email: String,
        password: String
    ) {
        remoteDataSource.signIn(email, password)
    }

    override suspend fun signOut() {
        remoteDataSource.signOut()
    }

    override fun currentUserId(): String? {
        return remoteDataSource.currentUserId()
    }
}