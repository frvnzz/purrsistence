package com.example.purrsistence.ui.components.homeScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.purrsistence.domain.cats.CatList

@Composable
fun CatSelectionDialog(
    collectedCatIds: List<String>,
    initiallySelectedIds: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    var selectedIds by remember { mutableStateOf(initiallySelectedIds.toSet()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Select Cats for your Room",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${selectedIds.size} / 5 selected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        selectedIds.isEmpty() -> MaterialTheme.colorScheme.error
                        selectedIds.size > 5 -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                if (selectedIds.isEmpty()) {
                    Text(
                        text = "Select at least 1 cat",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    // Placeholder to keep layout stable when warning is not shown
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // SELECT CATS TO DISPLAY
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.heightIn(max = 400.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(collectedCatIds) { catId ->
                        val cat = CatList.getCatById(catId)
                        if (cat != null) {
                            val isSelected = catId in selectedIds
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .border(
                                        width = 2.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        if (isSelected) {
                                            selectedIds = selectedIds - catId
                                        } else if (selectedIds.size < 5) {
                                            selectedIds = selectedIds + catId
                                        }
                                    }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = cat.imageRes),
                                    contentDescription = cat.name,
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        }
                    }
                }

                // BUTTONS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onConfirm(selectedIds.toList()) },
                        // Deactivate confirm button if no cats or too many cats are selected
                        enabled = selectedIds.isNotEmpty() && selectedIds.size <= 5
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}
