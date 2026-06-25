package com.example.purrsistence.data.local.mapping

import com.example.purrsistence.data.remote.supabase.dto.FriendshipDto
import com.example.purrsistence.domain.model.Friendship
import com.example.purrsistence.domain.model.types.FriendshipStatus

fun FriendshipDto.toFriendship(): Friendship {
    return Friendship(
        id = id,
        requesterId = requesterId,
        addresseeId = addresseeId,
        status = status.toFriendshipStatus(),
        requesterUsername = requester?.username,
        addresseeUsername = addressee?.username
    )
}

fun Friendship.toSupabaseDto(): FriendshipDto {
    return FriendshipDto(
        id = id,
        requesterId = requesterId,
        addresseeId = addresseeId,
        status = status.toSupabaseValue()
    )
}

fun String.toFriendshipStatus(): FriendshipStatus {
    return when (lowercase()) {
        "pending" -> FriendshipStatus.PENDING
        "accepted" -> FriendshipStatus.ACCEPTED
        "declined" -> FriendshipStatus.DECLINED
        else -> error("Unknown friendship status: $this")
    }
}

fun FriendshipStatus.toSupabaseValue(): String {
    return when (this) {
        FriendshipStatus.PENDING -> "pending"
        FriendshipStatus.ACCEPTED -> "accepted"
        FriendshipStatus.DECLINED -> "declined"
    }
}