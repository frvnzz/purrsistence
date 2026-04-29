package com.example.purrsistence.domain.service

import com.example.purrsistence.domain.model.RoomSpot
import com.example.purrsistence.service.RoomService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomServiceTest {

    private val service = RoomService()

    @Test
    fun getRoomSpots_returnsExpectedSpots() {
        val spots = service.getRoomSpots()

        assertEquals(5, spots.size)

        assertEquals("floor_center", spots[0].id)
        assertEquals(0.498f, spots[0].xPercent, 0.0001f)
        assertEquals(0.757f, spots[0].yPercent, 0.0001f)

        assertEquals("floor_left", spots[1].id)
        assertEquals("floor_right", spots[2].id)
        assertEquals("floor_front", spots[3].id)
        assertEquals("floor_back", spots[4].id)
    }

    @Test
    fun assignCatsToSpots_withEmptySpots_returnsEmptyList() {
        val ownedCatIds = listOf("cat_1", "cat_2")

        val result = service.assignCatsToSpots(
            ownedCatIds = ownedCatIds,
            spots = emptyList()
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun assignCatsToSpots_returnsOnePlacedCatPerOwnedCat() {
        val ownedCatIds = listOf("cat_1", "cat_2", "cat_3")
        val spots = service.getRoomSpots()

        val result = service.assignCatsToSpots(
            ownedCatIds = ownedCatIds,
            spots = spots
        )

        assertEquals(3, result.size)
        assertEquals(ownedCatIds, result.map { it.catId })
    }

    @Test
    fun assignCatsToSpots_usesOnlyProvidedSpotIds() {
        val ownedCatIds = listOf("cat_1", "cat_2", "cat_3", "cat_4")
        val spots = service.getRoomSpots()
        val validSpotIds = spots.map { it.id }.toSet()

        val result = service.assignCatsToSpots(
            ownedCatIds = ownedCatIds,
            spots = spots
        )

        assertTrue(result.all { it.spotId in validSpotIds })
    }

    @Test
    fun assignCatsToSpots_reusesSpotsCyclically_whenMoreCatsThanSpots() {
        val ownedCatIds = listOf(
            "cat_1", "cat_2", "cat_3", "cat_4", "cat_5", "cat_6", "cat_7"
        )

        val spots = listOf(
            RoomSpot("spot_a", 0.1f, 0.1f),
            RoomSpot("spot_b", 0.2f, 0.2f)
        )

        val result = service.assignCatsToSpots(
            ownedCatIds = ownedCatIds,
            spots = spots
        )

        assertEquals(7, result.size)

        val usedSpotIds = result.map { it.spotId }.toSet()
        assertEquals(setOf("spot_a", "spot_b"), usedSpotIds)

        val countSpotA = result.count { it.spotId == "spot_a" }
        val countSpotB = result.count { it.spotId == "spot_b" }

        assertTrue(countSpotA + countSpotB == 7)
        assertTrue(countSpotA >= 3)
        assertTrue(countSpotB >= 3)
    }
}