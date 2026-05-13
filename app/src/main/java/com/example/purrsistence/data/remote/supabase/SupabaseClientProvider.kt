package com.example.purrsistence.data.remote.supabase

import android.content.Context
import com.example.purrsistence.BuildConfig
import com.russhwolf.settings.SharedPreferencesSettings
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SettingsSessionManager
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClientProvider {

    fun create(
        context: Context,
        url: String = BuildConfig.SUPABASE_URL,
        publishableKey: String = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    ): SupabaseClient {

        require(url.isNotBlank()) {
            "SUPABASE_URL is missing."
        }

        require(publishableKey.isNotBlank()) {
            "SUPABASE_PUBLISHABLE_KEY is missing."
        }

        return createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = publishableKey
        ) {

            install(Auth) {

                sessionManager = SettingsSessionManager(
                    settings = SharedPreferencesSettings(
                        delegate = context.getSharedPreferences(
                            "supabase_session",
                            Context.MODE_PRIVATE
                        )
                    )
                )

                alwaysAutoRefresh = true
                autoLoadFromStorage = true
            }

            install(Postgrest)
            install(Storage)
        }
    }
}