package com.example.purrsistence.ui.components.tracking

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.theme.Elevation
import com.example.purrsistence.ui.theme.Spacing
import com.example.purrsistence.ui.util.formatDuration

@Composable
fun FocusTimerProgress(
    elapsedMillis: Long,
    pausedMillis: Long,
    multiplier: Float,
    multiplierProgress: Float,
    isPaused: Boolean,
    modifier: Modifier = Modifier
) {
    // Multiplier Progress Ring:
    // 1.0 -> 0%
    // 2.0 -> 100%
    val progress = multiplierProgress

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.surfaceVariant

    val density = LocalDensity.current

    val strokeWidth = with(density) { 18.dp.toPx() }

    val canvasSize = 320.dp

    Box(
        modifier = modifier.size(canvasSize),
        contentAlignment = Alignment.Center
    ) {

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {

            drawArc(
                color = secondaryColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth
                )
            )

            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            // Elapsed Time for this tracking session
            Text(
                text = formatDuration(elapsedMillis),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            // TODO: Time paused text for debugging -> please remove after
            Text(
                text = "Paused: ${formatDuration(pausedMillis)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // Multiplier (x2.0 maximum)
            Text(
                text =
                    if (multiplier >= 2f) {
                        "x2.0 Max. Multiplier"
                    } else {
                        "x${"%.2f".format(multiplier)} Multiplier"
                    },
                style = MaterialTheme.typography.titleMedium,
                color = primaryColor
            )
        }

        if (isPaused) {
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                tonalElevation = Elevation.Lvl3
            ) {
                Text(
                    text = "Paused",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(
                        horizontal = Spacing.md,
                        vertical = Spacing.sm
                    )
                )
            }
        }
    }
}