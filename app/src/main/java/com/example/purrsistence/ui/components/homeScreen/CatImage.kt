package com.example.purrsistence.ui.components.homeScreen

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import com.example.purrsistence.domain.cats.CatList
import com.example.purrsistence.domain.model.AnimationState
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
    animationState: AnimationState = AnimationState.IDLE
) {
    val cat = CatList.getCatById(catId)
    if (cat != null) {
        CatImage(
            cat = cat,
            modifier = modifier,
            isMirrored = isMirrored,
            isAnimated = isAnimated,
            initialFrame = initialFrame,
            animationState = animationState
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
    animationState: AnimationState = AnimationState.IDLE
) {
    val finalModifier = modifier
        .graphicsLayer {
            if (isMirrored) {
                scaleX = -1f
            }
        }

    val imageRes: Int
    val animData: SpriteSheetData?

    when (animationState) {
        AnimationState.SLEEPING -> {
            imageRes = cat.sleepingImageRes ?: cat.imageRes
            animData = if (cat.sleepingImageRes != null) cat.sleepingAnimationData else cat.animationData
        }
        AnimationState.SITTING -> {
            imageRes = cat.sittingImageRes ?: cat.imageRes
            animData = if (cat.sittingImageRes != null) cat.sittingAnimationData else cat.animationData
        }
        AnimationState.IDLE -> {
            imageRes = cat.imageRes
            animData = cat.animationData
        }
    }

    if (animData != null) {
        SpriteAnimation(
            spriteSheetRes = imageRes,
            data = animData,
            modifier = finalModifier,
            contentDescription = cat.name,
            initialFrame = initialFrame,
            isAnimated = isAnimated
        )
    } else {
        Image(
            painter = painterResource(imageRes),
            contentDescription = cat.name,
            modifier = finalModifier
        )
    }
}