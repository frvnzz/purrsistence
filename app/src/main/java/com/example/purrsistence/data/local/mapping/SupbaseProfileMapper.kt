package com.example.purrsistence.data.local.mapping

import com.example.purrsistence.data.remote.supabase.dto.ProfileDto
import com.example.purrsistence.data.remote.supabase.dto.ProfileSearchResultDto
import com.example.purrsistence.domain.model.FriendProfile

fun ProfileDto.toFriendProfile(): FriendProfile {
    return FriendProfile(
        id = id,
        username = username,
        avatarPath = avatarPath
    )
}

fun ProfileSearchResultDto.toFriendProfile(): FriendProfile {
    return FriendProfile(
        id = id,
        username = username,
        avatarPath = avatarPath
    )
}

fun FriendProfile.toSupabaseDto(): ProfileDto {
    return ProfileDto(
        id = id,
        username = username,
        avatarPath = avatarPath
    )
}