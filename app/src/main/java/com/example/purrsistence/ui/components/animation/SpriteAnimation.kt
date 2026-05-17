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

        val col = currentFrame % data.columns
        val row = currentFrame / data.columns

        drawImage(
            image = bitmap,
            srcOffset = IntOffset(col * frameWidth, row * frameHeight),
            srcSize = IntSize(frameWidth, frameHeight),
            dstSize = IntSize(size.width.toInt(), size.height.toInt())
        )
    }
}
