package com.example.purrsistence.service

import com.example.purrsistence.domain.model.PlacedCat
import com.example.purrsistence.domain.model.RoomSpot
import kotlin.random.Random

class RoomService {

    fun getRoomSpots(): List<RoomSpot> {
        return listOf(
            // All possible spots + coordinates where a CatImage can be rendered in RoomView
            RoomSpot("cat_tree_left",   0.221f, 0.445f),
            RoomSpot("bed_back",   0.400f, 0.672f),
            RoomSpot("floor_center", 0.596f, 0.723f),
            RoomSpot("floor_right",  0.750f, 0.850f),
            RoomSpot("floor_front",  0.391f, 0.906f)
        )
    }

    fun assignCatsToSpots(
        ownedCatIds: List<String>,
        spots: List<RoomSpot>
    ): List<PlacedCat> {
        if (spots.isEmpty()) return emptyList()
        val shuffledSpots = spots.shuffled()
        return ownedCatIds.mapIndexed { index, catId ->
            val spot = shuffledSpots[index % spots.size]
            PlacedCat(
                catId = catId,
                spotId = spot.id,
                isMirrored = Random.nextBoolean()
            )
        }
    }
}
