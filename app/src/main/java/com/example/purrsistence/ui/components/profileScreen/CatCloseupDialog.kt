package com.example.purrsistence.ui.components.profileScreen

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.purrsistence.domain.model.ShopItem
import com.example.purrsistence.ui.components.HeartBurstState
import com.example.purrsistence.ui.components.HeartParticleEffect
import com.example.purrsistence.ui.components.homeScreen.CatImage
import com.example.purrsistence.ui.theme.Elevation
import com.example.purrsistence.ui.theme.Shapes
import com.example.purrsistence.ui.theme.Spacing
import com.example.purrsistence.ui.util.SoundManager
import com.example.purrsistence.ui.util.isAnimationEnabled

@Composable
fun CatCloseupDialog(
    cat: ShopItem,
    onDismiss: () -> Unit,
    soundManager: SoundManager? = null
) {
    val context = LocalContext.current
    val isAnimationEnabled = remember(context) { context.isAnimationEnabled() }
    val activeBursts = remember { mutableStateListOf<HeartBurstState>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = Shapes.cards,
        tonalElevation = Elevation.Lvl3,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        title = {
            Text(
                text = cat.name,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                soundManager?.playMeow()
                                if (isAnimationEnabled) {
                                    activeBursts.add(
                                        HeartBurstState(
                                            x = offset.x.toDp(),
                                            y = offset.y.toDp(),
                                            zIndex = 1f
                                        )
                                    )
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    CatImage(
                        cat = cat,
                        isAnimated = isAnimationEnabled,
                        modifier = Modifier.size(180.dp)
                    )

                    if (isAnimationEnabled) {
                        activeBursts.forEach { burst ->
                            key(burst.id) {
                                HeartParticleEffect(
                                    onAnimationComplete = { activeBursts.remove(burst) },
                                    modifier = Modifier
                                        .zIndex(burst.zIndex)
                                        .padding(start = burst.x, top = burst.y)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Tap to pet ${cat.name}!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onDismiss,
                    shape = Shapes.buttons
                ) {
                    Text("Close")
                }
            }
        }
    )
}
