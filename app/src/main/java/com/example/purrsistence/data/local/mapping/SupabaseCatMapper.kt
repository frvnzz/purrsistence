package com.example.purrsistence.data.local.mapping

import com.example.purrsistence.data.remote.supabase.dto.SelectedCatDto
import com.example.purrsistence.data.remote.supabase.dto.UserCatDto

fun UserCatDto.toDomainCatId(): String {
    return catId
}

fun String.toUserCatDto(
    supabaseUserId: String,
    source: String = "shop"
): UserCatDto {
    return UserCatDto(
        userId = supabaseUserId,
        catId = this,
        source = source
    )
}

fun SelectedCatDto.toDomainCatId(): String {
    return catId
}

fun String.toSelectedCatDto(
    supabaseUserId: String,
    slot: Int
): SelectedCatDto {
    return SelectedCatDto(
        userId = supabaseUserId,
        slot = slot,
        catId = this
    )
}

fun List<SelectedCatDto>.toSelectedCatIds(): List<String> {
    return sortedBy { selectedCat -> selectedCat.slot }
        .map { selectedCat -> selectedCat.catId }
}

fun List<String>.toSelectedCatDtos(
    supabaseUserId: String
): List<SelectedCatDto> {
    return take(3)
        .mapIndexed { index, catId ->
            catId.toSelectedCatDto(
                supabaseUserId = supabaseUserId,
                slot = index + 1
            )
        }
}