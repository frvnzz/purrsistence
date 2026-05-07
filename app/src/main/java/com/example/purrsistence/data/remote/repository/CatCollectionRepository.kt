package com.example.purrsistence.data.remote.repository


import com.example.purrsistence.data.local.dao.UserDao
import com.example.purrsistence.data.remote.supabase.datasource.AuthRemoteDataSource
import com.example.purrsistence.data.remote.supabase.datasource.SupabaseCatRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CatCollectionRepository(
    private val userDao: UserDao,
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val catRemoteDataSource: SupabaseCatRemoteDataSource
) {

    fun observeOwnedCatIds(): Flow<List<String>> {
        val supabaseUserId = authRemoteDataSource.currentUserId()
            ?: return kotlinx.coroutines.flow.flowOf(emptyList())

        return userDao
            .observeBySupabaseUserId(supabaseUserId)
            .map { user ->
                user?.collectedCatsIds ?: emptyList()
            }
    }

    suspend fun refreshOwnedCatsFromSupabase() {
        val supabaseUserId = authRemoteDataSource.currentUserId()
            ?: error("No authenticated Supabase user.")

        val remoteCatIds = catRemoteDataSource.fetchCollectedCatIds(supabaseUserId)

        userDao.updateOwnedCatIds(
            supabaseUserId = supabaseUserId,
            ownedCatIds = remoteCatIds.distinct()
        )
    }

    suspend fun addOwnedCat(catId: String) {
        val supabaseUserId = authRemoteDataSource.currentUserId()
            ?: error("No authenticated Supabase user.")

        catRemoteDataSource.addCollectedCat(
            userId = supabaseUserId,
            catId = catId
        )

        val currentUser = userDao.getUserBySupabaseId(supabaseUserId)
            ?: error("Local user is not linked to the Supabase user.")

        val updatedCatIds = (currentUser.collectedCatsIds + catId).distinct()

        userDao.updateOwnedCatIds(
            supabaseUserId = supabaseUserId,
            ownedCatIds = updatedCatIds
        )
    }
}