package com.example.purrsistence.ui.components.tracking

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.example.purrsistence.ui.theme.DarkTertiary
import com.example.purrsistence.ui.theme.Elevation
import com.example.purrsistence.ui.theme.Shapes
import com.example.purrsistence.ui.theme.Spacing
import com.example.purrsistence.ui.util.formatDuration
import kotlin.math.roundToInt

@Composable
fun FocusTimerProgress(
    elapsedMillis: Long,
    pausedMillis: Long,
    multiplier: Float,
    multiplierProgress: Float,
    checkpointedCurrency: Int,
    minutesSinceReset: Int,
    isPaused: Boolean,
    modifier: Modifier = Modifier
) {

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.surfaceVariant

    val density = LocalDensity.current
    val strokeWidth = with(density) { 18.dp.toPx() }

    BoxWithConstraints(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {

        val timerTextStyle =
            if (maxWidth < 300.dp) {
                MaterialTheme.typography.displayMedium
            } else {
                MaterialTheme.typography.displayLarge
            }

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {

            drawArc(
                color = secondaryColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )

            drawArc(
                color =
                    if (multiplier >= 2f) {
                        DarkTertiary
                    } else {
                        primaryColor
                    },
                startAngle = -90f,
                sweepAngle = 360f * multiplierProgress,
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
                style = timerTextStyle,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            //checkpointed currency + potential currency = total currency in tracking session
            val potentialCurrency = (minutesSinceReset * multiplier).roundToInt()
            val totalLiveCurrency = checkpointedCurrency + potentialCurrency

            Text(
                text = "Earned: $totalLiveCurrency",
                style = MaterialTheme.typography.bodyLarge,
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
                color =
                    if (multiplier >= 2f) {
                        DarkTertiary
                    } else {
                        primaryColor
                    }
            )
        }

        if (isPaused) {
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter),
                shape = Shapes.cards,
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