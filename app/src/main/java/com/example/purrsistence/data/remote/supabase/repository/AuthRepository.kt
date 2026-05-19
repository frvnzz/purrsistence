package com.example.purrsistence.data.remote.supabase.repository

import com.example.purrsistence.data.remote.supabase.datasource.AuthRemoteDataSource

interface AuthRepository {
    fun isSignedIn(): Boolean
    fun currentUserId(): String?
    suspend fun signUp(email: String, password: String, username: String)
    suspend fun signIn(email: String, password: String)
    suspend fun signOut()
}

class AuthRepositoryImpl(
    private val remoteDataSource: AuthRemoteDataSource
) : AuthRepository {

    override fun isSignedIn(): Boolean {
        return remoteDataSource.currentUserId() != null
    }

    override fun currentUserId(): String? {
        return remoteDataSource.currentUserId()
    }

    override suspend fun signUp(
        email: String,
        password: String,
        username: String
    ) {
        remoteDataSource.signUp(
            email = email,
            password = password,
            username = username
        )
    }

    override suspend fun signIn(
        email: String,
        password: String
    ) {
        remoteDataSource.signIn(
            email = email,
            password = password
        )
    }

    override suspend fun signOut() {
        remoteDataSource.signOut()
    }
}