package com.example.purrsistence.service

import com.example.purrsistence.data.local.repository.UserRepository
import com.example.purrsistence.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class ShopService(
    private val userRepository: UserRepository
) {

    fun getUser(userId: Int): Flow<User?> {
        return userRepository.getUser(userId)
    }

    suspend fun buyCat(userId: Int, catId: String, price: Int) {
        val user = userRepository.getUser(userId).firstOrNull() ?: return

        if (catId in user.collectedCatsIds) return
        if (user.balance < price) return

        // When user buys a cat, it is auto-selected for the RoomView
        // (if user has selected less than the maximum of cats)
        val shouldAutoSelect = user.selectedCatIds.size < 5
        val updatedSelectedCats = if (shouldAutoSelect) {
            user.selectedCatIds + catId
        } else {
            user.selectedCatIds
        }

        val updatedUser = user.copy(
            balance = user.balance - price,
            collectedCatsIds = user.collectedCatsIds + catId,
            selectedCatIds = updatedSelectedCats
        )

        userRepository.updateUser(updatedUser)
    }

    // Update selected cats to show in RoomView
    suspend fun updateSelectedCats(userId: Int, selectedIds: List<String>) {
        val user = userRepository.getUser(userId).firstOrNull() ?: return
        val updatedUser = user.copy(selectedCatIds = selectedIds)
        userRepository.updateUser(updatedUser)
    }
}