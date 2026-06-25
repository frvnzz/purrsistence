package com.example.purrsistence.ui.components.homeScreen

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import com.example.purrsistence.domain.cats.CatList
import com.example.purrsistence.domain.model.ShopItem
import com.example.purrsistence.ui.components.animation.SpriteAnimation
import com.example.purrsistence.ui.components.animation.SpriteSheetData

@Composable
fun CatImage(
    catId: String,
    modifier: Modifier = Modifier,
    isMirrored: Boolean = false,
    isAnimated: Boolean = true,
    initialFrame: Int = 0,
    isSleeping: Boolean = false,
    isSitting: Boolean = false,
    alignment: Alignment = Alignment.Center
) {
    val cat = CatList.getCatById(catId)
    if (cat != null) {
        CatImage(
            cat = cat,
            modifier = modifier,
            isMirrored = isMirrored,
            isAnimated = isAnimated,
            initialFrame = initialFrame,
            isSleeping = isSleeping,
            isSitting = isSitting,
            alignment = alignment
        )
    }
}

@Composable
fun CatImage(
    cat: ShopItem,
    modifier: Modifier = Modifier,
    isMirrored: Boolean = false,
    isAnimated: Boolean = true,
    initialFrame: Int = 0,
    isSleeping: Boolean = false,
    isSitting: Boolean = false,
    alignment: Alignment = Alignment.Center
) {
    val context = LocalContext.current
    
    //Preload
    val idleBitmap = remember(cat.id) { 
        ImageBitmap.imageResource(context.resources, cat.imageRes) 
    }
    val sleepingBitmap = remember(cat.id) { 
        cat.sleepingImageRes?.let { ImageBitmap.imageResource(context.resources, it) } 
    }
    val sittingBitmap = remember(cat.id) { 
        cat.sittingImageRes?.let { ImageBitmap.imageResource(context.resources, it) } 
    }

    val finalModifier = modifier
        .graphicsLayer {
            if (isMirrored) {
                scaleX = -1f
            }
        }

    val activeBitmap: ImageBitmap
    val animData: SpriteSheetData?

    if (isSleeping && cat.sleepingImageRes != null && sleepingBitmap != null) {
        activeBitmap = sleepingBitmap
        animData = cat.sleepingAnimationData
    } else if (isSitting && cat.sittingImageRes != null && sittingBitmap != null) {
        activeBitmap = sittingBitmap
        animData = cat.sittingAnimationData
    } else {
        activeBitmap = idleBitmap
        animData = cat.animationData
    }

    if (animData != null) {
        SpriteAnimation(
            bitmap = activeBitmap,
            data = animData,
            modifier = finalModifier,
            contentDescription = cat.name,
            initialFrame = initialFrame,
            isAnimated = isAnimated,
            alignment = alignment
        )
    } else {
        Image(
            bitmap = activeBitmap,
            contentDescription = cat.name,
            modifier = finalModifier,
            alignment = alignment
        )
    }
}