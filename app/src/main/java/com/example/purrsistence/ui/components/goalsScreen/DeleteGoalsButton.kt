package com.example.purrsistence.ui.components.goalsScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.theme.Elevation
import com.example.purrsistence.ui.theme.Shapes
import com.example.purrsistence.ui.theme.Spacing

@Composable
fun DeleteGoalsButton(
    isDeleteMode: Boolean,
    selectedGoalsCount: Int,
    onEnterDeleteMode: () -> Unit,
    onCancel: () -> Unit,
    onDeleteClick: () -> Unit
) {

    if (!isDeleteMode) {
        // INITIAL DELETE BUTTON (to get to bulk delete)
        Button(
            onClick = onEnterDeleteMode,
            shape = Shapes.buttons,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,

                disabledContainerColor =
                    MaterialTheme.colorScheme.surfaceDim,

                disabledContentColor =
                    MaterialTheme.colorScheme.onSurfaceVariant
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = Elevation.Lvl1
            ),
            modifier = Modifier.height(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Enter Delete Mode"
            )
        }

    } else {
        // when in
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {

            OutlinedButton(
                onClick = onCancel,
                shape = Shapes.buttons,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.height(40.dp)
            ) {

                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Button(
                onClick = onDeleteClick,
                shape = Shapes.buttons,
                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        if (selectedGoalsCount > 0) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.surfaceDim
                        },
                    contentColor =
                        if (selectedGoalsCount > 0) {
                            MaterialTheme.colorScheme.onTertiary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = Elevation.Lvl1
                ),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = if (selectedGoalsCount > 0) "Delete ${selectedGoalsCount} selected goals" else "Delete"
                )

                if (selectedGoalsCount > 0) {

                    Text(
                        text = "(${selectedGoalsCount})",
                        modifier = Modifier.padding(start = Spacing.xs),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}