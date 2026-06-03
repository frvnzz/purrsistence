package com.example.purrsistence.domain.cats

import com.example.purrsistence.R
import com.example.purrsistence.domain.model.ShopItem
import com.example.purrsistence.ui.components.animation.SpriteSheetData

object CatList {
    // List of all cats the user can buy in the Shop
    val cats = listOf(
        ShopItem("cat_1", "House Cat", 0,
            R.drawable.default_idle,SpriteSheetData(columns = 4, rows = 3, totalFrames = 10, frameDurationMs = 400L)),
        ShopItem("cat_2", "Black Cat", 13,
            R.drawable.black_idle,SpriteSheetData(columns = 4, rows = 3, totalFrames = 10, frameDurationMs = 400L)),
        ShopItem("cat_3", "Creme Cat", 20,
            R.drawable.creme_idle,SpriteSheetData(columns = 4, rows = 3, totalFrames = 10, frameDurationMs = 400L)),
        ShopItem("cat_4", "Brown Cat", 10,
            R.drawable.brown_idle,SpriteSheetData(columns = 4, rows = 3, totalFrames = 10, frameDurationMs = 400L)),
        ShopItem("cat_5", "Gray Cat", 15,
            R.drawable.gray_idle,SpriteSheetData(columns = 4, rows = 3, totalFrames = 10, frameDurationMs = 400L)),
        ShopItem("cat_6", "White Cat", 2,
            R.drawable.white_idle,SpriteSheetData(columns = 4, rows = 3, totalFrames = 10, frameDurationMs = 400L)),
        ShopItem("cat_7", "Orange Cat", 3,
            R.drawable.orange_idle,SpriteSheetData(columns = 4, rows = 3, totalFrames = 10, frameDurationMs = 400L)),
        ShopItem("cat_8", "Spotted Cat", 2,
            R.drawable.spotted_idle,SpriteSheetData(columns = 4, rows = 3, totalFrames = 10, frameDurationMs = 400L)),
        ShopItem("cat_9", "Siamese Cat", 50,
            R.drawable.siamese_idle,SpriteSheetData(columns = 4, rows = 3, totalFrames = 10, frameDurationMs = 400L)),
        ShopItem("cat_10", "Lucky Cat", 777,
            R.drawable.lucky_idle,SpriteSheetData(columns = 4, rows = 3, totalFrames = 10, frameDurationMs = 400L))
    )

    private val catMap = cats.associateBy { it.id }
    fun getCatById(id: String): ShopItem? = catMap[id]
}