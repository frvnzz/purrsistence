package com.example.purrsistence.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import com.example.purrsistence.ui.util.isAnimationEnabled
import kotlin.random.Random

@Composable
fun ConfettiEffect(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val isAnimationEnabled = remember(context) { context.isAnimationEnabled() }
    
    if (!isAnimationEnabled) return

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val colors = listOf(
        Color(0xFFFFC107),
        Color(0xFFFF5722),
        Color(0xFF4CAF50),
        Color(0xFF2196F3),
        Color(0xFFE91E63),
        Color(0xFF9C27B0)
    )

    val particles = remember {
        List(100) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * -1f, //start above the screen
                size = Random.nextFloat() * 20f + 10f,
                color = colors.random(),
                speed = Random.nextFloat() * 0.01f + 0.005f,
                rotationSpeed = Random.nextFloat() * 5f + 2f,
                drift = Random.nextFloat() * 0.002f - 0.001f
            )
        }
    }

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Canvas(modifier = modifier) {
        particles.forEach { particle ->
            val currentY = (particle.y + progress * 2f) % 1.5f
            val currentX = particle.x + (progress * particle.drift * 100)
            
            val xPos = currentX * size.width
            val yPos = currentY * size.height

            rotate(degrees = progress * 360 * particle.rotationSpeed, pivot = Offset(xPos, yPos)) {
                drawRect(
                    color = particle.color,
                    topLeft = Offset(xPos, yPos),
                    size = Size(particle.size, particle.size / 2)
                )
            }
        }
    }
}

private data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val color: Color,
    val speed: Float,
    val rotationSpeed: Float,
    val drift: Float
)
