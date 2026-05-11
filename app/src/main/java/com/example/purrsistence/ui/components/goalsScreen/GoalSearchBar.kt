package com.example.purrsistence.ui.components.goalsScreen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.theme.Shapes
import com.example.purrsistence.ui.theme.Spacing

@Composable
fun GoalSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {

    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .padding(vertical = Spacing.md),
        placeholder = {
            Text("Search Goal Name...")
        },
        singleLine = true,
        shape = Shapes.cards,
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") }
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear"
                    )
                }
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,

            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
        )
    )
}