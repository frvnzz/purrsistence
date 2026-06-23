package com.example.purrsistence.service

import com.example.purrsistence.domain.model.PlacedCat
import com.example.purrsistence.domain.model.RoomSpot
import kotlin.random.Random

class RoomService {

    fun getRoomSpots(): List<RoomSpot> {
        return listOf(
            // All possible spots + coordinates where a CatImage can be rendered in RoomView
            RoomSpot("cat_tree_left",   0.221f, 0.445f, isMirrored = true),
            RoomSpot("bed_back",   0.400f, 0.672f, isMirrored = false),
            RoomSpot("floor_center", 0.596f, 0.723f, isMirrored = false),
            RoomSpot("floor_right",  0.750f, 0.850f, isMirrored = true),
            RoomSpot("floor_front",  0.391f, 0.906f, isMirrored = false)
        )
    }

    fun assignCatsToSpots(
        ownedCatIds: List<String>,
        spots: List<RoomSpot>
    ): List<PlacedCat> {
        if (spots.isEmpty()) return emptyList()

        // Randomize spot assignment
        val shuffledSpots = spots.shuffled()

        // 10 frames possible in spritesheet
        val frameOffsets = (0 until 10).shuffled()

        return ownedCatIds.mapIndexed { index, catId ->
            val spot = shuffledSpots[index % shuffledSpots.size]
            val initialFrame = frameOffsets.getOrElse(index % frameOffsets.size) { 0 }

            val animationRoll = Random.nextFloat()
            val isSleeping = animationRoll < 0.2f // 20% chance of sleeping
            val isSitting = animationRoll in 0.2f..<0.5f // 20% chance of sitting

            PlacedCat(
                catId = catId,
                spotId = spot.id,

                // Mirror orientation belongs to the spot
                isMirrored = spot.isMirrored,
                initialFrame = initialFrame,
                isSleeping = isSleeping,
                isSitting = isSitting
            )
        }
    }
}
