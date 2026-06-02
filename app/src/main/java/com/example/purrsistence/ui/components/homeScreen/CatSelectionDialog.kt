package com.example.purrsistence.ui.components.homeScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.purrsistence.domain.cats.CatList
import com.example.purrsistence.ui.theme.Elevation
import com.example.purrsistence.ui.theme.Shapes
import com.example.purrsistence.ui.theme.Spacing

@Composable
fun CatSelectionDialog(
    collectedCatIds: List<String>,
    initiallySelectedIds: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val focusManager = LocalFocusManager.current

    var selectedIds by remember {
        mutableStateOf(initiallySelectedIds.toSet())
    }

    AlertDialog(
        onDismissRequest = {
            focusManager.clearFocus()
            onDismiss()
        },
        shape = Shapes.cards,
        tonalElevation = Elevation.Lvl3,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,

        icon = {
            Icon(
                imageVector = Icons.Outlined.Pets,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },

        title = {
            Text(
                text = "Select Cats",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge
            )
        },

        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                // Number of selected cats
                Text(
                    text = "${selectedIds.size} / 5 selected",
                    modifier = Modifier.semantics { 
                        contentDescription = "${selectedIds.size} out of 5 selected" 
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = when {
                        selectedIds.isEmpty() || selectedIds.size > 5 ->
                            MaterialTheme.colorScheme.error

                        else ->
                            MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                // Fixed-height warning container
                // -> Prevents dialog jumping when warning appears/disappears
                Box(
                    modifier = Modifier.height(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Warning text when no cats are selected (0 / 5)
                    if (selectedIds.isEmpty()) {
                        Text(
                            text = "Select at least 1 cat",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    // fixed min/max height prevents clipping/cut-off
                    modifier = Modifier.heightIn(min = 98.dp, max = 188.dp),
                    contentPadding = PaddingValues(bottom = Spacing.xxs),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {

                    items(collectedCatIds) { catId ->

                        val cat = CatList.getCatById(catId)

                        if (cat != null) {

                            val isSelected = catId in selectedIds

                            Card(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clearAndSetSemantics {
                                        contentDescription = "${cat.name}${if (isSelected) ", selected" else ", not selected"}"
                                        role = Role.Button
                                    }
                                    // every cat card can be de-/selected
                                    .clickable {
                                        selectedIds = when {
                                            isSelected -> {
                                                selectedIds - catId
                                            }
                                            selectedIds.size < 5 -> {
                                                selectedIds + catId
                                            }
                                            else -> {
                                                selectedIds
                                            }
                                        }
                                    },
                                shape = Shapes.cards,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.background,
                                    contentColor = MaterialTheme.colorScheme.onBackground
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = Elevation.Lvl1
                                ),

                                // Border when card is selected
                                border = if (isSelected) {
                                    BorderStroke(
                                        width = 1.8.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else null
                            ) {

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(Spacing.sm),
                                    contentAlignment = Alignment.Center
                                ) {

                                    CatImage(
                                        cat = cat,
                                        isAnimated = false,
                                        modifier = Modifier.size(72.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },

        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Cancel Button
                OutlinedButton(
                    onClick = onDismiss,
                    shape = Shapes.buttons
                ) {

                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                // Confirm Button
                Button(
                    onClick = {
                        onConfirm(selectedIds.toList())
                    },

                    enabled = selectedIds.isNotEmpty() &&
                            selectedIds.size <= 5,

                    shape = Shapes.buttons
                ) {

                    Text(
                        text = "Confirm",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    )
}