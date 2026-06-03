package com.example.purrsistence.data.remote.supabase.datasource

import com.example.purrsistence.data.remote.supabase.dto.SelectedCatDto
import com.example.purrsistence.data.remote.supabase.dto.UserCatDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

interface CatRemoteDataSource {
    suspend fun fetchCollectedCatIds(userId: String): List<String>

    suspend fun fetchVisibleCollectedCatIds(userId: String): List<String>

    suspend fun addCollectedCat(
        userId: String,
        catId: String
    )

    suspend fun uploadLocalCollectedCats(
        userId: String,
        catIds: List<String>
    )

    suspend fun fetchSelectedCatIds(userId: String): List<String>

    suspend fun fetchSelectedCatRows(userId: String): List<SelectedCatDto>

    suspend fun fetchVisibleSelectedCatIds(userId: String): List<String>

    suspend fun replaceSelectedCats(
        userId: String,
        selectedCatIds: List<String>
    )
}

class SupabaseCatRemoteDataSource(
    private val supabase: SupabaseClient
) : CatRemoteDataSource{

    override suspend fun fetchCollectedCatIds(userId: String): List<String> {
        return supabase
            .from("user_cats")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeList<UserCatDto>()
            .map { it.catId }
    }

    override suspend fun addCollectedCat(
        userId: String,
        catId: String
    ) {
        val row = UserCatDto(
            userId = userId,
            catId = catId,
            source = "shop"
        )

        supabase
            .from("user_cats")
            .upsert(row) {
                onConflict = "user_id,cat_id"
            }
    }

    override suspend fun uploadLocalCollectedCats(
        userId: String,
        catIds: List<String>
    ) {
        catIds.distinct().forEach { catId ->
            addCollectedCat(
                userId = userId,
                catId = catId
            )
        }
    }

    override suspend fun fetchSelectedCatIds(userId: String): List<String> {
        return fetchSelectedCatRows(userId)
            .sortedBy { it.slot }
            .map { it.catId }
    }

    override suspend fun fetchSelectedCatRows(userId: String): List<SelectedCatDto> {
        return supabase
            .from("selected_cats")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeList<SelectedCatDto>()
    }

    override suspend fun fetchVisibleCollectedCatIds(
        userId: String
    ): List<String> {
        return fetchCollectedCatIds(userId)
    }

    override suspend fun fetchVisibleSelectedCatIds(
        userId: String
    ): List<String> {
        return fetchSelectedCatIds(userId)
    }

    override suspend fun replaceSelectedCats(
        userId: String,
        selectedCatIds: List<String>
    ) {
        supabase
            .from("selected_cats")
            .delete {
                filter {
                    eq("user_id", userId)
                }
            }

        selectedCatIds
            .take(5)
            .forEachIndexed { index, catId ->
                supabase
                    .from("selected_cats")
                    .insert(
                        SelectedCatDto(
                            userId = userId,
                            slot = index + 1,
                            catId = catId
                        )
                    )
            }
    }
}