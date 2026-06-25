package com.example.purrsistence.data.remote.supabase.datasource

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

interface AuthRemoteDataSource {
    suspend fun signUp(email: String, password: String, username: String)
    suspend fun signIn(email: String, password: String)
    suspend fun signOut()
    suspend fun updatePassword(currentPassword: String, newPassword: String)
    fun currentUserId(): String?
    val sessionStatus: Flow<SessionStatus>
}

class SupabaseAuthRemoteDataSource(
    private val supabase: SupabaseClient
) : AuthRemoteDataSource {

    override suspend fun signUp(
        email: String,
        password: String,
        username: String
    ) {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("username", username)
            }
        }
    }

    override suspend fun signIn(
        email: String,
        password: String
    ) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signOut() {
        supabase.auth.signOut()
    }

    override suspend fun updatePassword(currentPassword: String, newPassword: String) {
        val email = supabase.auth.currentUserOrNull()?.email
            ?: throw Exception("User email not found")

        //verify old password by signing in again
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = currentPassword
        }

        //if old password correct, update password
        supabase.auth.updateUser {
            password = newPassword
        }
    }

    override fun currentUserId(): String? {
        val user = supabase.auth.currentUserOrNull()

        Log.d(
            "SUPABASE_AUTH",
            "Current user = ${user?.id}"
        )

        return user?.id
    }

    override val sessionStatus: Flow<SessionStatus> = supabase.auth.sessionStatus
}
