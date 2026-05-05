package com.example.purrsistence.data.local.mappers

import com.example.purrsistence.data.local.entity.UserEntity
import com.example.purrsistence.data.local.mapping.toDomain
import com.example.purrsistence.data.local.mapping.toEntity
import com.example.purrsistence.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.URL

class UserMapperTest {

    @Test
    fun userEntity_toDomain_mapsCorrectly() {
        val entity = UserEntity(
            userId = 1,
            username = "TestUser",
            balance = 120,
            friends = listOf("friend_1", "friend_2"),
            collectedCatsIds = listOf("cat_grey", "cat_lucky"),
            selectedCatIds = listOf("cat_grey", "cat_lucky"),
            profileImageUrl = "https://example.com/profile.png",
            isSupabaseLinked = true,
            supabaseUserId = "supabase-123"
        )

        val domain = entity.toDomain()

        assertEquals(1, domain.id)
        assertEquals("TestUser", domain.username)
        assertEquals(120, domain.balance)
        assertEquals(listOf("friend_1", "friend_2"), domain.friends)
        assertEquals(listOf("cat_grey", "cat_lucky"), domain.collectedCatsIds)
        assertEquals(listOf("cat_grey", "cat_lucky"), domain.selectedCatIds)
        assertEquals(URL("https://example.com/profile.png"), domain.profileImageUrl)
        assertEquals(true, domain.isSupabaseLinked)
        assertEquals("supabase-123", domain.supabaseUserId)
    }

    @Test
    fun user_toEntity_mapsCorrectly() {
        val domain = User(
            id = 1,
            username = "TestUser",
            balance = 120,
            friends = listOf("friend_1", "friend_2"),
            collectedCatsIds = listOf("cat_grey", "cat_lucky"),
            selectedCatIds = listOf("cat_grey", "cat_lucky"),
            profileImageUrl = URL("https://example.com/profile.png"),
            isSupabaseLinked = true,
            supabaseUserId = "supabase-123"
        )

        val entity = domain.toEntity()

        assertEquals(1, entity.userId)
        assertEquals("TestUser", entity.username)
        assertEquals(120, entity.balance)
        assertEquals(listOf("friend_1", "friend_2"), entity.friends)
        assertEquals(listOf("cat_grey", "cat_lucky"), entity.collectedCatsIds)
        assertEquals(listOf("cat_grey", "cat_lucky"), entity.selectedCatIds)
        assertEquals("https://example.com/profile.png", entity.profileImageUrl)
        assertEquals(true, entity.isSupabaseLinked)
        assertEquals("supabase-123", entity.supabaseUserId)
    }
    @Test
    fun userEntity_toDomain_mapsNullProfileFieldsCorrectly() {
        val entity = UserEntity(
            userId = 2,
            username = "LocalUser",
            balance = 0,
            friends = emptyList(),
            collectedCatsIds = emptyList(),
            selectedCatIds = emptyList(),
            profileImageUrl = null,
            isSupabaseLinked = false,
            supabaseUserId = null
        )

        val domain = entity.toDomain()

        assertEquals(2, domain.id)
        assertEquals("LocalUser", domain.username)
        assertEquals(0, domain.balance)
        assertEquals(emptyList<String>(), domain.friends)
        assertEquals(emptyList<String>(), domain.collectedCatsIds)
        assertEquals(emptyList<String>(), domain.selectedCatIds)
        assertEquals(null, domain.profileImageUrl)
        assertEquals(false, domain.isSupabaseLinked)
        assertEquals(null, domain.supabaseUserId)
    }
}