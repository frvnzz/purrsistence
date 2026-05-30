package com.example.purrsistence.data.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserRepositoryRoomIntegrationTest : RoomIntegrationTestBase() {

    @Test
    fun getUser_readsMappedUser() = runBlocking {
        seedUserEntity(
            userId = 1,
            username = "TestUser",
            balance = 120,
            friends = listOf("friend_1", "friend_2"),
            collectedCatsIds = listOf("cat_grey", "cat_lucky"),
            selectedCatIds = listOf("cat_grey"),
            profileImageUrl = "https://example.com/avatar.png",
            isSupabaseLinked = true,
            supabaseUserId = "supabase-123"
        )

        val user = userRepository.getUser(1).first()

        assertNotNull(user)
        assertEquals(1, user!!.id)
        assertEquals("TestUser", user.username)
        assertEquals(120, user.balance)
        assertEquals(listOf("friend_1", "friend_2"), user.friends)
        assertEquals(listOf("cat_grey", "cat_lucky"), user.collectedCatsIds)
        assertEquals(listOf("cat_grey"), user.selectedCatIds)
        assertEquals("https://example.com/avatar.png", user.profileImageUrl.toString())
        assertEquals(true, user.isSupabaseLinked)
        assertEquals("supabase-123", user.supabaseUserId)
    }

    @Test
    fun addCurrency_updatesBalance() = runBlocking {
        seedUserEntity(
            userId = 1,
            username = "TestUser",
            balance = 10
        )

        userRepository.addCurrency(1, 7)

        val balance = userRepository.getUser(1).first()?.balance
        assertEquals(17, balance)
    }
}