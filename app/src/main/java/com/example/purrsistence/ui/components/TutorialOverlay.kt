package com.example.purrsistence.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.example.purrsistence.ui.theme.Spacing

data class TutorialStep(
    val title: String,
    val description: String,
    val targetCoordinates: LayoutCoordinates? = null,
    val onEnter: () -> Unit = {}
)

@Composable
fun TutorialOverlay(
    currentStep: TutorialStep?,
    isLastStep: Boolean,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    if (currentStep == null) return

    var overlayCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { overlayCoords = it }
            .pointerInput(Unit) {
                //consume all touch events to prevent background interaction
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                    }
                }
            }
            .graphicsLayer(alpha = 0.99f)
    ) {
        val screenHeight = constraints.maxHeight.toFloat()

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = Color.Black.copy(alpha = 0.8f))

            currentStep.targetCoordinates?.let { coords ->
                if (coords.isAttached && overlayCoords != null) {
                    val position = overlayCoords!!.localPositionOf(coords, Offset.Zero)
                    val size = coords.size.toSize()

                    val padding = 8.dp.toPx()
                    val rect = Rect(
                        offset = Offset(position.x - padding, position.y - padding),
                        size = Size(size.width + padding * 2, size.height + padding * 2)
                    )

                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = rect.topLeft,
                        size = rect.size,
                        cornerRadius = CornerRadius(12.dp.toPx()),
                        blendMode = BlendMode.Clear
                    )
                }
            }
        }

        // Info Box logic
        val targetPosition = currentStep.targetCoordinates?.let { coords ->
            if (coords.isAttached && overlayCoords != null) {
                overlayCoords!!.localPositionOf(coords, Offset.Zero)
            } else null
        }

        val alignment = when {
            targetPosition == null -> Alignment.Center
            targetPosition.y < screenHeight / 2 -> Alignment.BottomCenter
            else -> Alignment.TopCenter
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.xl),
            contentAlignment = alignment
        ) {
            Surface(
                modifier = Modifier.width(320.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(Spacing.lg)) {
                    Text(
                        text = currentStep.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text(
                        text = currentStep.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(Spacing.lg))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onSkip) {
                            Text("Skip")
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Button(onClick = onNext) {
                            Text(if (isLastStep) "Finish" else "Next")
                        }
                    }
                }
            }
        }
    }
}
