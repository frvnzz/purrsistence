package com.example.purrsistence.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun ZzzParticleEffect(
    modifier: Modifier = Modifier,
) {
    val particles = remember { mutableStateListOf<ZParticleData>() }

    LaunchedEffect(Unit) {
        while (true) {
            particles.add(
                ZParticleData(
                    initialX = Random.nextInt(-10, 10),
                    initialY = 0,
                    targetX = Random.nextInt(-30, 30),
                    targetY = Random.nextInt(-100, -60),
                    duration = Random.nextInt(2000, 3000),
                    size = Random.nextInt(12, 24).sp
                )
            )
            delay(1000)
            if (particles.size > 5) {
                particles.removeAt(0)
            }
        }
    }

    Box(modifier = modifier) {
        particles.forEach { particle ->
            key(particle.id) {
                ZParticle(data = particle)
            }
        }
    }
}

@Composable
private fun ZParticle(data: ZParticleData) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = data.duration,
                easing = LinearEasing
            )
        )
    }

    val x = data.initialX + ((data.targetX - data.initialX) * progress.value)
    val y = data.initialY + ((data.targetY - data.initialY) * progress.value)
    val alpha = 1f - (progress.value * progress.value) // Faster fade out at the end
    val scale = 0.8f + progress.value * 0.4f

    Text(
        text = "Z",
        color = Color.White,
        fontSize = data.size,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .offset { IntOffset(x.toInt(), y.toInt()) }
            .scale(scale)
            .alpha(alpha)
    )
}

private data class ZParticleData(
    val id: Long = Random.nextLong(),
    val initialX: Int,
    val initialY: Int,
    val targetX: Int,
    val targetY: Int,
    val duration: Int,
    val size: androidx.compose.ui.unit.TextUnit
)
