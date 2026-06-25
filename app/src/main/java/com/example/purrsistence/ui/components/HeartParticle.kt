package com.example.purrsistence.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.purrsistence.R
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

data class HeartBurstState(
    val id: String = UUID.randomUUID().toString(),
    val catId: String? = null,
    val x: Dp,
    val y: Dp,
    val zIndex: Float
)

@Composable
fun HeartParticleEffect(
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val particles = remember {
        List(8) {
            HeartParticleData(
                initialX = Random.nextInt(-40, 40),
                initialY = Random.nextInt(-20, 20),
                targetX = Random.nextInt(-100, 100),
                targetY = Random.nextInt(-200, -100),
                delay = Random.nextInt(0, 300)
            )
        }
    }

    Box(modifier = modifier) {
        particles.forEachIndexed { index, particle ->
            key(index) {
                HeartParticle(
                    data = particle,
                    onAnimationComplete = if (index == 0) onAnimationComplete else null
                )
            }
        }
    }
}

@Composable
private fun HeartParticle(
    data: HeartParticleData,
    onAnimationComplete: (() -> Unit)? = null
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1500,
                    delayMillis = data.delay,
                    easing = LinearEasing
                )
            )
            onAnimationComplete?.invoke()
        }
    }

    val x = data.initialX + (data.targetX - data.initialX) * progress.value
    val y = data.initialY + (data.targetY - data.initialY) * progress.value
    val alpha = 1f - progress.value
    val scale = 0.5f + progress.value * 0.5f

    Icon(
        painter = painterResource(id = R.drawable.ic_heart),
        contentDescription = null,
        tint = Color.Red,
        modifier = Modifier
            .offset { IntOffset(x.toInt(), y.toInt()) }
            .size(24.dp)
            .scale(scale)
            .alpha(alpha)
    )
}

private data class HeartParticleData(
    val initialX: Int,
    val initialY: Int,
    val targetX: Int,
    val targetY: Int,
    val delay: Int
)
