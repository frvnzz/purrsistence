package com.example.purrsistence.data.local.mapping

import com.example.purrsistence.data.local.entity.UserEntity
import com.example.purrsistence.domain.model.User
import java.net.URL

fun UserEntity.toDomain(): User =
    User(
        id = userId,
        username = username,
        profileImageUrl = profileImageUrl?.let { URL(it) },
        balance = balance,
        friends = friends,
        isSupabaseLinked = isSupabaseLinked,
        supabaseUserId = supabaseUserId,
        collectedCatsIds = collectedCatsIds,
        selectedCatIds = selectedCatIds
    )

fun User.toEntity(): UserEntity =
    UserEntity(
        userId = id,
        username = username,
        supabaseUserId = supabaseUserId,
        isSupabaseLinked = isSupabaseLinked,
        profileImageUrl = profileImageUrl.toString(),
        balance = balance,
        friends = friends,
        collectedCatsIds = collectedCatsIds,
        selectedCatIds = selectedCatIds
    )