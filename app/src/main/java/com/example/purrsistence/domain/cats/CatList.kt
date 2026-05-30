package com.example.purrsistence.domain.cats

import com.example.purrsistence.R
import com.example.purrsistence.domain.model.ShopItem
import com.example.purrsistence.ui.components.animation.SpriteSheetData

object CatList {
    // List of all cats the user can buy in the Shop
    val cats = listOf(
        ShopItem("cat_1", "House Cat", 0, R.drawable.cat_idle,SpriteSheetData(columns = 4, rows = 3, totalFrames = 10, frameDurationMs = 400L)),
        ShopItem("cat_2", "Black Cat", 13, R.drawable.cat_black),
        ShopItem("cat_3", "Creme Cat", 20, R.drawable.cat_creme),
        ShopItem("cat_4", "Brown Cat", 10, R.drawable.cat_brown),
        ShopItem("cat_5", "Gray Cat", 15, R.drawable.cat_darkgray),
        ShopItem("cat_6", "White Cat", 2, R.drawable.cat_lightgray),
        ShopItem("cat_7", "Orange Cat", 3, R.drawable.cat_orange),
        ShopItem("cat_8", "Spotted Cat", 2, R.drawable.cat_spotted),
        ShopItem("cat_9", "Siamese Cat", 50, R.drawable.cat_siamese),
        ShopItem("cat_10", "Lucky Cat", 777, R.drawable.cat_lucky)
    )

    private val catMap = cats.associateBy { it.id }
    fun getCatById(id: String): ShopItem? = catMap[id]
}