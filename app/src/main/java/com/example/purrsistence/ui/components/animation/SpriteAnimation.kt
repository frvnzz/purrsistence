package com.example.purrsistence.ui.components.animation

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
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
    isAnimated: Boolean = true
) {
    val bitmap = ImageBitmap.imageResource(id = spriteSheetRes)
    var currentFrame by remember { mutableIntStateOf(initialFrame) }

    if (isAnimated && data.frameDurationMs < Long.MAX_VALUE) {
        LaunchedEffect(data) {
            while (true) {
                delay(data.frameDurationMs)
                currentFrame = (currentFrame + 1) % data.totalFrames
            }
        }
    }

    Canvas(modifier = modifier) {
        val frameWidth = bitmap.width / data.columns
        val frameHeight = bitmap.height / data.rows

        val frameAspectRatio = frameWidth.toFloat() / frameHeight.toFloat()
        val canvasAspectRatio = size.width / size.height

        val drawWidth: Float
        val drawHeight: Float

        if (frameAspectRatio > canvasAspectRatio) {
            drawWidth = size.width
            drawHeight = size.width / frameAspectRatio
        } else {
            drawHeight = size.height
            drawWidth = size.height * frameAspectRatio
        }

        val xOffset = (size.width - drawWidth) / 2f
        val yOffset = (size.height - drawHeight) / 2f

        val col = currentFrame % data.columns
        val row = currentFrame / data.columns

        drawImage(
            image = bitmap,
            srcOffset = IntOffset(col * frameWidth, row * frameHeight),
            srcSize = IntSize(frameWidth, frameHeight),
            dstOffset = IntOffset(xOffset.toInt(), yOffset.toInt()),
            dstSize = IntSize(drawWidth.toInt(), drawHeight.toInt())
        )
    }
}
