package com.example.purrsistence.ui.components.tracking

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.purrsistence.ui.theme.Elevation
import com.example.purrsistence.ui.theme.Spacing

@Composable
fun TrackingActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Button(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
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