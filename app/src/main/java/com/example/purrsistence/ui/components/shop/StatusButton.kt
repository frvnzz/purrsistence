package com.example.purrsistence.ui.components.shop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Loyalty
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.theme.Shapes
import com.example.purrsistence.ui.theme.Spacing

@Composable
fun StatusButton(
    canAfford: Boolean,
    isOwned: Boolean,
    buttonText: String,
    modifier: Modifier = Modifier
) {

    val backgroundColor = if (canAfford && !isOwned) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f)
    }

    val contentColor = if (canAfford && !isOwned) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
    }

    Surface(
        shape = Shapes.buttons,
        color = backgroundColor,
        modifier = modifier
            .width(120.dp)
            .height(36.dp)
            .clearAndSetSemantics { }
    ) {

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (canAfford || isOwned) {

                Icon(
                    imageVector = when {
                        isOwned -> Icons.Outlined.Check
                        else -> Icons.Outlined.Loyalty
                    },
                    contentDescription = null,
                    tint = contentColor
                )

                Spacer(modifier = Modifier.width(Spacing.sm))
            }

            Text(
                text = buttonText,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}