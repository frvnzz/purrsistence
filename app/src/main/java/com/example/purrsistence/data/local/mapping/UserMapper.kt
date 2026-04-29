package com.example.purrsistence.data.local.mapping

import com.example.purrsistence.data.local.entity.UserEntity
import com.example.purrsistence.domain.model.User

fun UserEntity.toDomain(): User =
    User(
        id = userId,
        username = username,
        balance = balance,
        friends = friends,
        collectedCatsIds = collectedCatsIds,
        selectedCatIds = selectedCatIds
    )

fun User.toEntity(): UserEntity =
    UserEntity(
        userId = id,
        username = username,
        balance = balance,
        friends = friends,
        collectedCatsIds = collectedCatsIds,
        selectedCatIds = selectedCatIds
    )