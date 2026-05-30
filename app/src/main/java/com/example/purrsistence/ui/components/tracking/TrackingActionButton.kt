package com.example.purrsistence.ui.components.tracking

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.purrsistence.ui.theme.Elevation
import com.example.purrsistence.ui.theme.Spacing

@Composable
fun TrackingActionButton(
    modifier: Modifier = Modifier,
    text: String,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {

    Button(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = Elevation.Lvl1
        ),
        contentPadding = PaddingValues(
            horizontal = Spacing.xl,
            vertical = Spacing.md
        ),
        modifier = modifier
    ) {
        Text(text)
    }
}