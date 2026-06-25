package com.example.purrsistence.data.remote.supabase.repository

import com.example.purrsistence.data.remote.supabase.datasource.AuthRemoteDataSource
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun isSignedIn(): Boolean
    fun currentUserId(): String?
    suspend fun signUp(email: String, password: String, username: String)
    suspend fun signIn(email: String, password: String)
    suspend fun signOut()
    suspend fun updatePassword(currentPassword: String, newPassword: String)
    val sessionStatus: Flow<SessionStatus>
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

    override suspend fun updatePassword(currentPassword: String, newPassword: String) {
        remoteDataSource.updatePassword(currentPassword, newPassword)
    }

    override val sessionStatus: Flow<SessionStatus> = remoteDataSource.sessionStatus
}
