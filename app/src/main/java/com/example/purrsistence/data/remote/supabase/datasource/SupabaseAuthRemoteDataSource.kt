package com.example.purrsistence.data.remote.supabase.datasource

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

interface AuthRemoteDataSource {
    suspend fun signUp(email: String, password: String, username: String)
    suspend fun signIn(email: String, password: String)
    suspend fun signOut()
    fun currentUserId(): String?
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

    override fun currentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }

    fun isSignedIn(): Boolean {
        return currentUserId() != null
    }
}