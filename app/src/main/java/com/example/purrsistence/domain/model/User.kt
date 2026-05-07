package com.example.purrsistence.domain.model

data class User(
    val id: Int,
    val username: String,
    val profileImageUrl: String?,
    val balance: Int,
    val friends: List<String>,
    val isSupabaseLinked: Boolean,
    val supabaseUserId: String?,
    val collectedCatsIds: List<String>,
    val selectedCatIds: List<String>
)