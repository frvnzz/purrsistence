package com.example.purrsistence.ui.components.goalsScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.purrsistence.ui.theme.Elevation
import com.example.purrsistence.ui.theme.Shapes
import com.example.purrsistence.ui.theme.Spacing

@Composable
fun DeleteGoalDialog(
    title: String = "Delete Goals",
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = Shapes.cards,
        tonalElevation = Elevation.Lvl3,
        containerColor = MaterialTheme.colorScheme.surface,

        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },

        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },

        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.width(Spacing.md))

                TextButton(
                    onClick = onConfirm,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = "Delete",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    )
}