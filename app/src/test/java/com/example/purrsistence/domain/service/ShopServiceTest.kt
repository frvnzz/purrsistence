package com.example.purrsistence.domain.service

import com.example.purrsistence.data.local.repository.FakeUserRepository
import com.example.purrsistence.domain.model.User
import com.example.purrsistence.service.ShopService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Test

class ShopServiceTest {

    @Test
    fun buyCat_reducesBalance_andAddsCat() = runBlocking {
        val userRepository = FakeUserRepository()
        val service = ShopService(userRepository)

        userRepository.insertUser(
            User(
                id = 1,
                username = "TestUser",
                balance = 100,
                friends = emptyList(),
                collectedCatsIds = emptyList(),
                selectedCatIds = emptyList()
            )
        )

        service.buyCat(userId = 1, catId = "cat_grey", price = 30)

        val updated = userRepository.getUser(1)
        assertEquals(70, updated.first()?.balance)
        assertTrue(updated.first()?.collectedCatsIds?.contains("cat_grey") == true )
    }

    @Test
    fun buyCat_doesNothing_ifAlreadyOwned() = runBlocking {
        val userRepository = FakeUserRepository()
        val service = ShopService(userRepository)

        userRepository.insertUser(
            User(
                id = 1,
                username = "TestUser",
                balance = 100,
                friends = emptyList(),
                collectedCatsIds = listOf("cat_lucky"),
                selectedCatIds = listOf("cat_lucky")
            )
        )

        service.buyCat(userId = 1, catId = "cat_lucky", price = 30)

        val updated = userRepository.getUser(1)
        assertEquals(100, updated.first()?.balance)
        assertEquals(listOf("cat_lucky"), updated.first()?.collectedCatsIds)
    }

    @Test
    fun buyCat_doesNothing_ifNotEnoughMoney() = runBlocking {
        val userRepository = FakeUserRepository()
        val service = ShopService(userRepository)

        userRepository.insertUser(
            User(
                id = 1,
                username = "TestUser",
                balance = 10,
                friends = emptyList(),
                collectedCatsIds = emptyList(),
                selectedCatIds = emptyList()
            )
        )

        service.buyCat(userId = 1, catId = "cat_white", price = 30)

        val updated = userRepository.getUser(1)
        assertEquals(10, updated.first()?.balance)
        assertTrue(updated.first()?.collectedCatsIds?.isEmpty() == true)
    }

    @Test
    fun buyCat_doesNothing_whenUserDoesNotExist() = runBlocking {
        val userRepository = FakeUserRepository()
        val service = ShopService(userRepository)

        service.buyCat(
            userId = 999,
            catId = "cat_white",
            price = 30
        )

        val missingUser = userRepository.getUser(999).firstOrNull()
        assertNull(missingUser)
    }

    @Test
    fun buyCat_doesNothing_whenBalanceIsTooLow() = runBlocking {
        val userRepository = FakeUserRepository()
        val service = ShopService(userRepository)

        userRepository.insertUser(
            User(
                id = 1,
                username = "TestUser",
                balance = 10,
                friends = emptyList(),
                collectedCatsIds = emptyList(),
                selectedCatIds = emptyList()
            )
        )

        service.buyCat(
            userId = 1,
            catId = "cat_lucky",
            price = 30
        )

        val updatedUser = userRepository.getUser(1).firstOrNull()
        assertEquals(10, updatedUser!!.balance)
        assertTrue(updatedUser.collectedCatsIds.isEmpty())
    }

}