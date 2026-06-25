package com.example.purrsistence.ui.components.animation

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay

@Composable
fun SpriteAnimation(
    spriteSheetRes: Int,
    data: SpriteSheetData,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    initialFrame: Int = 0,
    isAnimated: Boolean = true,
    alignment: Alignment = Alignment.Center
) {
    val context = LocalContext.current
    val bitmap = remember(spriteSheetRes) {
        ImageBitmap.imageResource(context.resources, spriteSheetRes)
    }
    
    SpriteAnimation(
        bitmap = bitmap,
        data = data,
        modifier = modifier,
        contentDescription = contentDescription,
        initialFrame = initialFrame,
        isAnimated = isAnimated,
        alignment = alignment
    )
}

@Composable
fun SpriteAnimation(
    bitmap: ImageBitmap,
    data: SpriteSheetData,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    initialFrame: Int = 0,
    isAnimated: Boolean = true,
    alignment: Alignment = Alignment.Center
) {
    var currentFrame by remember(data, initialFrame) { mutableIntStateOf(initialFrame % data.totalFrames) }

    if (isAnimated && data.frameDurationMs < Long.MAX_VALUE) {
        LaunchedEffect(data, initialFrame) {
            while (true) {
                delay(data.frameDurationMs)
                currentFrame = (currentFrame + 1) % data.totalFrames
            }
        }
    }

    Canvas(
        modifier = modifier.semantics {
            contentDescription?.let { this.contentDescription = it }
        }
    ) {
        val frameWidth = bitmap.width / data.columns
        val frameHeight = bitmap.height / data.rows

        val frameAspectRatio = frameWidth.toFloat() / frameHeight.toFloat()
        val canvasAspectRatio = size.width / size.height

        var drawWidth: Float
        var drawHeight: Float

        if (frameAspectRatio > canvasAspectRatio) {
            drawWidth = size.width
            drawHeight = size.width / frameAspectRatio
        } else {
            drawHeight = size.height
            drawWidth = size.height * frameAspectRatio
        }

        // Apply scale from data
        drawWidth *= data.scale
        drawHeight *= data.scale

        val xOffset: Float
        val yOffset: Float

        when (alignment) {
            Alignment.TopStart -> {
                xOffset = 0f
                yOffset = 0f
            }
            Alignment.TopCenter -> {
                xOffset = (size.width - drawWidth) / 2f
                yOffset = 0f
            }
            Alignment.TopEnd -> {
                xOffset = size.width - drawWidth
                yOffset = 0f
            }
            Alignment.CenterStart -> {
                xOffset = 0f
                yOffset = (size.height - drawHeight) / 2f
            }
            Alignment.Center -> {
                xOffset = (size.width - drawWidth) / 2f
                yOffset = (size.height - drawHeight) / 2f
            }
            Alignment.CenterEnd -> {
                xOffset = size.width - drawWidth
                yOffset = (size.height - drawHeight) / 2f
            }
            Alignment.BottomStart -> {
                xOffset = 0f
                yOffset = size.height - drawHeight
            }
            Alignment.BottomCenter -> {
                xOffset = (size.width - drawWidth) / 2f
                yOffset = size.height - drawHeight
            }
            Alignment.BottomEnd -> {
                xOffset = size.width - drawWidth
                yOffset = size.height - drawHeight
            }
            else -> {
                xOffset = (size.width - drawWidth) / 2f
                yOffset = (size.height - drawHeight) / 2f
            }
        }

        // Apply offsetY (normalized to canvas height)
        val finalYOffset = yOffset + (data.offsetY * size.height)

        val col = currentFrame % data.columns
        val row = currentFrame / data.columns

        drawImage(
            image = bitmap,
            srcOffset = IntOffset(col * frameWidth, row * frameHeight),
            srcSize = IntSize(frameWidth, frameHeight),
            dstOffset = IntOffset(xOffset.toInt(), finalYOffset.toInt()),
            dstSize = IntSize(drawWidth.toInt(), drawHeight.toInt())
        )
    }
}
