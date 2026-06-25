package com.example.purrsistence.data.remote.supabase.repository

import com.example.purrsistence.data.remote.supabase.datasource.SupabaseCatRemoteDataSource

interface CatCollectionRepository {
    suspend fun getCollectedCatIds(userId: String): List<String>
    suspend fun getSelectedCatIds(userId: String): List<String>
    suspend fun addCollectedCat(userId: String, catId: String)
    suspend fun uploadCollectedCats(userId: String, catIds: List<String>)
    suspend fun replaceSelectedCats(userId: String, selectedCatIds: List<String>)
    suspend fun fetchVisibleCollectedCatIds(userId: String): List<String>
    suspend fun fetchVisibleSelectedCatIds(userId: String): List<String>
}

class CatCollectionRepositoryImpl(
    private val catRemoteDataSource: SupabaseCatRemoteDataSource
) : CatCollectionRepository {

    override suspend fun getCollectedCatIds(
        userId: String
    ): List<String> {
        return catRemoteDataSource
            .fetchCollectedCatIds(userId)
            .distinct()
    }

    override suspend fun getSelectedCatIds(
        userId: String
    ): List<String> {
        return catRemoteDataSource
            .fetchSelectedCatIds(userId)
            .distinct()
            .take(5)
    }

    override suspend fun addCollectedCat(
        userId: String,
        catId: String
    ) {
        catRemoteDataSource.addCollectedCat(
            userId = userId,
            catId = catId
        )
    }

    override suspend fun uploadCollectedCats(
        userId: String,
        catIds: List<String>
    ) {
        catRemoteDataSource.uploadLocalCollectedCats(
            userId = userId,
            catIds = catIds.distinct()
        )
    }

    override suspend fun replaceSelectedCats(
        userId: String,
        selectedCatIds: List<String>
    ) {
        catRemoteDataSource.replaceSelectedCats(
            userId = userId,
            selectedCatIds = selectedCatIds
                .distinct()
                .take(5)
        )
    }

    override suspend fun fetchVisibleCollectedCatIds(
        userId: String
    ): List<String> {
        return catRemoteDataSource
            .fetchVisibleCollectedCatIds(userId)
            .distinct()
    }

    override suspend fun fetchVisibleSelectedCatIds(
        userId: String
    ): List<String> {
        return catRemoteDataSource
            .fetchVisibleSelectedCatIds(userId)
            .distinct()
            .take(5)
    }

}