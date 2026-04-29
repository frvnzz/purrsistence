package com.example.purrsistence.ui.components.homeScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.purrsistence.domain.cats.CatList

@Composable
fun CatImage(
    catId: String,
    modifier: Modifier = Modifier,
    isMirrored: Boolean = false
) {
    val cat = CatList.getCatById(catId)
    if (cat != null) {
        Image(
            painter = painterResource(cat.imageRes),
            contentDescription = cat.name,
            modifier = modifier
                .size(100.dp)
                .graphicsLayer {
                    if (isMirrored) {
                        scaleX = -1f
                    }
                }
        )
    }
}