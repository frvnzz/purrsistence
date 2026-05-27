package com.example.purrsistence.ui.components.tracking

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.purrsistence.ui.theme.Shapes

@Composable
fun TrackingStopWarningDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Stop Tracking?") },
        text = {
            Text(
                "You have been tracking for less than a minute. If you stop now, you will not receive any rewards for this session."
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = Shapes.buttons
            ) {
                Text("Stop Anyway")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = Shapes.buttons
            ) {
                Text("Keep Tracking")
            }
        }
    )
}
