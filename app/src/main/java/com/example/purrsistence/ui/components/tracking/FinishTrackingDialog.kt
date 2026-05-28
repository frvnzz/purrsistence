package com.example.purrsistence.ui.components.tracking

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.purrsistence.ui.theme.Elevation
import com.example.purrsistence.ui.theme.Shapes

@Composable
fun FinishTrackingDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = Shapes.cards,
        tonalElevation = Elevation.Lvl3,
        containerColor = MaterialTheme.colorScheme.surface,
        textContentColor = MaterialTheme.colorScheme.onSurface,

        title = {
            Text("Finished Tracking?")
        },
        text = {
            Text(
                "Your tracking session is still running. Do you want to finish and save this session?"
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = Shapes.buttons
            ) {
                Text("Finish Session")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = Shapes.buttons
            ) {
                Text("Continue")
            }
        }
    )
}