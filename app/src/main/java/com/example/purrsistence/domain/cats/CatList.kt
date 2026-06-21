package com.example.purrsistence.domain.cats


import com.example.purrsistence.R
import com.example.purrsistence.domain.model.ShopItem
import com.example.purrsistence.ui.components.animation.SpriteSheetData

object CatList {
    private val defaultAnimation = SpriteSheetData(columns = 4, rows = 3, totalFrames = 10, frameDurationMs = 400L)
    private val sittingAnimation = SpriteSheetData(columns = 2, rows = 1, totalFrames = 2, frameDurationMs = 800L)

    // List of all cats the user can buy in the Shop
    val cats = listOf(
        ShopItem("cat_1", "House Cat", 0,
            R.drawable.default_idle, defaultAnimation,
            R.drawable.default_sleeping, null,
            R.drawable.default_sitting, sittingAnimation),
        ShopItem("cat_2", "Black Cat", 1500,
            R.drawable.black_idle, defaultAnimation,
            R.drawable.black_sleeping, null,
            R.drawable.black_sitting, sittingAnimation),
        ShopItem("cat_13", "Larry The Cat", 3500,
            R.drawable.black2_idle, defaultAnimation,
            R.drawable.black2_sleeping, null,
            R.drawable.black2_sitting, sittingAnimation),
        ShopItem("cat_3", "Creme Cat", 3500,
            R.drawable.creme_idle, defaultAnimation,
            R.drawable.creme_sleeping, null,
            R.drawable.creme_sitting, sittingAnimation),
        ShopItem("cat_4", "Brown Cat", 3500,
            R.drawable.brown_idle, defaultAnimation,
            R.drawable.brown_sleeping, null,
            R.drawable.brown_sitting, sittingAnimation),
        ShopItem("cat_5", "Gray Cat", 3500,
            R.drawable.gray_idle, defaultAnimation,
            R.drawable.darkgray_sleeping, null,
            R.drawable.gray_sitting, sittingAnimation),
        ShopItem("cat_6", "Light Gray Cat", 3500,
            R.drawable.white_idle, defaultAnimation,
            R.drawable.lightgray_sleeping, null,
            R.drawable.white_sitting, sittingAnimation),
        ShopItem("cat_11", "White Cat", 3500,
            R.drawable.white2_idle, defaultAnimation,
            R.drawable.white2_sleeping, null,
            R.drawable.white2_sitting, sittingAnimation),
        ShopItem("cat_7", "Orange Cat", 7500,
            R.drawable.orange_idle, defaultAnimation,
            R.drawable.orange_sleeping, null,
            R.drawable.orange_sitting, sittingAnimation),
        ShopItem("cat_10", "Lucky Cat", 7777,
            R.drawable.lucky_idle, defaultAnimation,
            R.drawable.lucky_sleeping, null,
            R.drawable.lucky_sitting, sittingAnimation),
        ShopItem("cat_8", "Gray Spot Cat", 10000,
            R.drawable.spotted_idle, defaultAnimation,
            R.drawable.spotted_sleeping, null,
            R.drawable.spotted_sitting, sittingAnimation),
        ShopItem("cat_12", "Black Spot Cat", 10000,
            R.drawable.black_spotted_idle, defaultAnimation,
            R.drawable.black_spotted_sleeping, null,
            R.drawable.black_spotted_sitting, sittingAnimation),
        ShopItem("cat_9", "Siamese Cat", 10000,
            R.drawable.siamese_idle, defaultAnimation,
            R.drawable.siamese_sleeping, null,
            R.drawable.siamese_sitting, sittingAnimation),
        ShopItem("cat_14", "Heart Cat", 30000,
            R.drawable.heart_idle, defaultAnimation,
            R.drawable.heart_sleeping, null,
            R.drawable.heart_sitting, sittingAnimation),
        ShopItem("cat_15", "Blue Spot Cat", 30000,
            R.drawable.blue_spotted_idle, defaultAnimation,
            R.drawable.blue_spotted_sleeping, null,
            R.drawable.blue_spotted_sitting, sittingAnimation),
        ShopItem("cat_16", "Crown Cat", 60000,
            R.drawable.crown_idle, defaultAnimation,
            R.drawable.crown_sleeping, null,
            R.drawable.crown_sitting, sittingAnimation),
    )

    private val catMap = cats.associateBy { it.id }
    fun getCatById(id: String): ShopItem? = catMap[id]
}