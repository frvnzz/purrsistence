package com.example.purrsistence.data.local.mappers

import com.example.purrsistence.data.local.entity.UserEntity
import com.example.purrsistence.data.local.mapping.toDomain
import com.example.purrsistence.data.local.mapping.toEntity
import com.example.purrsistence.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Test

class UserMapperTest {

    @Test
    fun userEntity_toDomain_mapsCorrectly() {
        val entity = UserEntity(
            userId = 1,
            username = "TestUser",
            balance = 120,
            friends = listOf(""),
            collectedCatsIds = listOf("cat_grey", "cat_lucky"),
            selectedCatIds =  listOf("cat_grey", "cat_lucky")
        )

        val domain = entity.toDomain()

        assertEquals(1, domain.id)
        assertEquals("TestUser", domain.username)
        assertEquals(120, domain.balance)
        assertEquals(listOf(""), domain.friends)
        assertEquals(listOf("cat_grey", "cat_lucky"), domain.collectedCatsIds)
    }

    @Test
    fun user_toEntity_mapsCorrectly() {
        val domain = User(
            id = 1,
            username = "TestUser",
            balance = 120,
            friends = listOf(""),
            collectedCatsIds = listOf("cat_grey", "cat_lucky"),
            selectedCatIds =  listOf("cat_grey", "cat_lucky")
        )

        val entity = domain.toEntity()

        assertEquals(1, entity.userId)
        assertEquals("TestUser", entity.username)
        assertEquals(120, entity.balance)
        assertEquals(listOf(""), entity.friends)
        assertEquals(listOf("cat_grey", "cat_lucky"), domain.collectedCatsIds)
    }
}