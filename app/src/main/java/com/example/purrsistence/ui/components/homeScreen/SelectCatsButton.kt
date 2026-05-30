package com.example.purrsistence.ui.components.homeScreen

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.purrsistence.ui.theme.Elevation
import com.example.purrsistence.ui.theme.Spacing

@Composable
fun SelectCatsButton (
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = Elevation.Lvl2,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = Spacing.md,
                vertical = Spacing.xs
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Pets,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(Spacing.xs))

            Text(
                "Select Cats",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}