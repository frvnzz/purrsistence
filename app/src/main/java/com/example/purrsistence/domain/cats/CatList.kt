package com.example.purrsistence.domain.cats

import com.example.purrsistence.R
import com.example.purrsistence.domain.model.ShopItem
import com.example.purrsistence.ui.components.animation.SpriteSheetData

object CatList {
    private val defaultAnimation = SpriteSheetData(columns = 4, rows = 3, totalFrames = 10, frameDurationMs = 400L)

    // List of all cats the user can buy in the Shop
    val cats = listOf(
        ShopItem("cat_1", "House Cat", 0,
            R.drawable.default_idle, defaultAnimation,
            R.drawable.default_sleeping, null),
        ShopItem("cat_2", "Black Cat", 13,
            R.drawable.black_idle, defaultAnimation,
            R.drawable.black_sleeping, null),
        ShopItem("cat_3", "Creme Cat", 20,
            R.drawable.creme_idle, defaultAnimation,
            R.drawable.creme_sleeping, null),
        ShopItem("cat_4", "Brown Cat", 10,
            R.drawable.brown_idle, defaultAnimation,
            R.drawable.brown_sleeping, null),
        ShopItem("cat_5", "Gray Cat", 15,
            R.drawable.gray_idle, defaultAnimation,
            R.drawable.darkgray_sleeping, null),
        ShopItem("cat_6", "White Cat", 2,
            R.drawable.white_idle, defaultAnimation,
            R.drawable.lightgray_sleeping, null),
        ShopItem("cat_7", "Orange Cat", 3,
            R.drawable.orange_idle, defaultAnimation,
            R.drawable.orange_sleeping, null),
        ShopItem("cat_8", "Spotted Cat", 2,
            R.drawable.spotted_idle, defaultAnimation,
            R.drawable.spotted_sleeping, null),
        ShopItem("cat_9", "Siamese Cat", 50,
            R.drawable.siamese_idle, defaultAnimation,
            R.drawable.siamese_sleeping, null),
        ShopItem("cat_10", "Lucky Cat", 777,
            R.drawable.lucky_idle, defaultAnimation,
            R.drawable.lucky_sleeping, null)
    )

    private val catMap = cats.associateBy { it.id }
    fun getCatById(id: String): ShopItem? = catMap[id]
}