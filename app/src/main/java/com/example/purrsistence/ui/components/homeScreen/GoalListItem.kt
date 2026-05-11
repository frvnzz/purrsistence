package com.example.purrsistence.ui.components.homeScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.theme.Elevation
import com.example.purrsistence.ui.theme.Spacing

@Composable
fun GoalListItem(
    title: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    durationText: String? = null,
    isPlaceholder: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = backgroundColor,
        tonalElevation = Elevation.Lvl2,
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable { onClick() }
            else Modifier
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalArrangement =
                if (isPlaceholder) Arrangement.Center
                else Arrangement.Top
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

            if (durationText != null) {
                Spacer(modifier = Modifier.height(Spacing.xs))

                Text(
                    text = durationText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}