package com.example.purrsistence.data.remote.supabase

import com.example.purrsistence.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.auth.minimalConfig
import io.github.jan.supabase.auth.minimalSettings
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClientProvider {

    fun create(
        url: String = BuildConfig.SUPABASE_URL,
        publishableKey: String = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    ): SupabaseClient {
        require(url.isNotBlank()) { "SUPABASE_URL is missing." }
        require(publishableKey.isNotBlank()) { "SUPABASE_PUBLISHABLE_KEY is missing." }

        return createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = publishableKey
        ) {
            install(Auth) {
                MemorySessionManager()
                MemoryCodeVerifierCache()
                minimalConfig()
            }

            install(Postgrest)
            install(Storage)
        }
    }
}