package com.example.purrsistence.ui.components.homeScreen

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import com.example.purrsistence.domain.cats.CatList
import com.example.purrsistence.domain.model.ShopItem
import com.example.purrsistence.ui.components.animation.SpriteAnimation

@Composable
fun CatImage(
    catId: String,
    modifier: Modifier = Modifier,
    isMirrored: Boolean = false,
    isAnimated: Boolean = true
) {
    val cat = CatList.getCatById(catId)
    if (cat != null) {
        CatImage(
            cat = cat,
            modifier = modifier,
            isMirrored = isMirrored,
            isAnimated = isAnimated
        )
    }
}

@Composable
fun CatImage(
    cat: ShopItem,
    modifier: Modifier = Modifier,
    isMirrored: Boolean = false,
    isAnimated: Boolean = true
) {
    val finalModifier = modifier
        .graphicsLayer {
            if (isMirrored) {
                scaleX = -1f
            }
        }

    if (cat.animationData != null) {
        SpriteAnimation(
            spriteSheetRes = cat.imageRes,
            data = cat.animationData,
            modifier = finalModifier,
            contentDescription = cat.name,
            isAnimated = isAnimated
        )
    } else {
        Image(
            painter = painterResource(cat.imageRes),
            contentDescription = cat.name,
            modifier = finalModifier
        )
    }
}