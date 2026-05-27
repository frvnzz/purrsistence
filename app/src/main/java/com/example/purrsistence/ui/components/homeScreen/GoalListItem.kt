package com.example.purrsistence.ui.components.homeScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.purrsistence.domain.model.types.GoalType
import com.example.purrsistence.ui.theme.Elevation
import com.example.purrsistence.ui.theme.Spacing
import java.time.Duration
import java.util.Locale

@Composable
fun GoalListItem(
    title: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    type: GoalType? = null,
    targetDuration: Duration? = null,
    deepFocus: Boolean = false,
    isPlaceholder: Boolean = false,
    onClick: (() -> Unit)? = null
) {

    val formattedSubtitle =
        if (type != null && targetDuration != null) {
            val totalMinutes = targetDuration.toMinutes()
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60

            val formattedDuration =
                if (minutes == 0L) {
                    "${hours}h"
                } else {
                    "${hours}h ${minutes}m"
                }

            val formattedType =
                type.name
                    .lowercase(Locale.ROOT)
                    .replaceFirstChar {
                        it.titlecase(Locale.ROOT)
                    }

            "$formattedDuration - $formattedType"
        } else {
            null
        }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = backgroundColor,
        tonalElevation = Elevation.Lvl2,
        modifier = modifier.then(
            if (onClick != null) {
                Modifier.clickable { onClick() }
            } else {
                Modifier
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color =
                        if (isPlaceholder)
                            MaterialTheme.colorScheme.surfaceDim
                        else
                            MaterialTheme.colorScheme.onBackground
                )

                if (formattedSubtitle != null) {
                    Spacer(modifier = Modifier.height(Spacing.xs))

                    Text(
                        text = formattedSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (deepFocus) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "Deep Focus",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}