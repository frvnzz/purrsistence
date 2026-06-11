package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.purrsistence.domain.model.FriendProfile
import com.example.purrsistence.ui.state.TopBarState
import com.example.purrsistence.ui.theme.Elevation
import com.example.purrsistence.ui.theme.Shapes
import com.example.purrsistence.ui.theme.Spacing
import com.example.purrsistence.ui.viewmodel.FriendViewModel

@Composable
fun FriendSearchScreen(
    viewModel: FriendViewModel,
    onBack: () -> Unit,
    setTopBar: (TopBarState) -> Unit
) {
    var searchQuery by remember {
        mutableStateOf("")
    }

    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        setTopBar(
            TopBarState(
                title = "Search Friends",
                onBackClick = onBack
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.md)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { value ->
                searchQuery = value
                viewModel.searchProfiles(value)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Search by username")
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null
                )
            },
            singleLine = true,
            shape = Shapes.inputs
        )

        Spacer(
            modifier = Modifier.height(Spacing.md)
        )

        if (error != null) {
            Text(
                text = error ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(
                modifier = Modifier.height(Spacing.sm)
            )
        }

        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(
                        Alignment.CenterHorizontally
                    )
                )
            }

            searchQuery.trim().length < 2 -> {
                Text(
                    text = "Enter at least 2 characters.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            searchResults.isEmpty() -> {
                Text(
                    text = "No users found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        vertical = Spacing.sm
                    )
                ) {
                    items(searchResults) { profile ->
                        SearchResultItem(
                            profile = profile,
                            onAddClick = {
                                viewModel.sendFriendRequest(profile.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(
    profile: FriendProfile,
    onAddClick: () -> Unit
) {
    Surface(
        shape = Shapes.cards,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = Elevation.Lvl2,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs)
    ) {
        Row(
            modifier = Modifier
                .padding(Spacing.md)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = profile.username,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Send friend request",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = Shapes.buttons
            ) {
                Text("Add")
            }
        }
    }
}
