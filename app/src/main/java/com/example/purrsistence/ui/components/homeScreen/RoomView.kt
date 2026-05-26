package com.example.purrsistence.ui.components.homeScreen

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.purrsistence.R
import com.example.purrsistence.domain.model.PlacedCat
import com.example.purrsistence.domain.model.RoomSpot

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun RoomView(
    placedCats: List<PlacedCat>,
    spots: List<RoomSpot>,
    modifier: Modifier = Modifier,
    onCatTap: () -> Unit = {}
) {
    val painter = painterResource(R.drawable.room_ph)
    val imageSize = painter.intrinsicSize

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val containerWidth = maxWidth
        val containerHeight = maxHeight

        // Calculate actual scale and size of the image inside ContentScale.Fit
        val imageAspect = imageSize.width / imageSize.height
        val containerAspect = containerWidth.value / containerHeight.value

        val actualWidth: Dp
        val actualHeight: Dp

        if (containerAspect > imageAspect) {
            actualHeight = containerHeight
            actualWidth = containerHeight * imageAspect
        } else {
            actualWidth = containerWidth
            actualHeight = containerWidth / imageAspect
        }

        // Room area (matching the image exactly)
        Box(
            modifier = Modifier
                .size(actualWidth, actualHeight)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        // Correctly calculate percentages relative to the image
                        val xPct = offset.x / size.width
                        val yPct = offset.y / size.height
                        Log.d("RoomCoords", "Tapped at: xPercent = ${"%.3f".format(xPct)}f, yPercent = ${"%.3f".format(yPct)}f")
                    }
                }
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            placedCats.forEach { placedCat ->
                val spot = spots.find { it.id == placedCat.spotId } ?: return@forEach

                Box(
                    modifier = Modifier
                        .offset(
                            x = (actualWidth * spot.xPercent) - 50.dp,
                            y = (actualHeight * spot.yPercent) - 100.dp
                        )
                        .zIndex(spot.yPercent)
                        .pointerInput(Unit) {
                            detectTapGestures {
                                onCatTap()
                            }
                        }
                ) {
                    CatImage(
                        catId = placedCat.catId,
                        isMirrored = placedCat.isMirrored,
                        modifier = Modifier.size(100.dp)
                    )
                }
            }
        }
    }
}
