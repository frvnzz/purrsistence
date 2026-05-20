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
import com.example.purrsistence.ui.theme.Spacing
import com.example.purrsistence.ui.viewmodel.FriendViewModel

@Composable
fun FriendSearchScreen(
    viewModel: FriendViewModel,
    onBack: () -> Unit,
    setTopBar: (TopBarState) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

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
            onValueChange = {
                searchQuery = it
                viewModel.searchProfiles(it)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search by username") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = Spacing.sm)
            ) {
                items(searchResults) { profile ->
                    SearchResultItem(
                        profile = profile,
                        onAddClick = { viewModel.sendFriendRequest(profile.id) }
                    )
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
    Card(
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
            Text(
                text = profile.username,
                style = MaterialTheme.typography.bodyLarge
            )
            Button(onClick = onAddClick) {
                Text("Add")
            }
        }
    }
}
