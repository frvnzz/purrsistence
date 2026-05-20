package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.purrsistence.domain.model.FriendProfile
import com.example.purrsistence.domain.model.Friendship
import com.example.purrsistence.ui.state.TopBarState
import com.example.purrsistence.ui.theme.Spacing
import com.example.purrsistence.ui.viewmodel.FriendViewModel

@Composable
fun FriendsScreen(
    viewModel: FriendViewModel,
    onAddFriendClick: () -> Unit,
    onBack: () -> Unit,
    setTopBar: (TopBarState) -> Unit
) {
    val friends by viewModel.friends.collectAsState()
    val incomingRequests by viewModel.incomingRequests.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFriendsData()
        setTopBar(
            TopBarState(
                title = "Friends",
                onBackClick = onBack,
                actions = {
                    IconButton(onClick = onAddFriendClick) {
                        Icon(Icons.Default.Add, contentDescription = "Add Friend")
                    }
                }
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading && friends.isEmpty() && incomingRequests.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacing.md),
                contentPadding = PaddingValues(vertical = Spacing.md)
            ) {
                if (incomingRequests.isNotEmpty()) {
                    item {
                        Text(
                            text = "Friend Requests",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = Spacing.sm)
                        )
                    }
                    items(incomingRequests) { request ->
                        FriendRequestItem(
                            request = request,
                            onAccept = { viewModel.acceptFriendRequest(it) },
                            onDecline = { viewModel.declineFriendRequest(it) }
                        )
                    }
                    item { HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.md)) }
                }

                item {
                    Text(
                        text = "Your Friends",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = Spacing.sm)
                    )
                }

                if (friends.isEmpty()) {
                    item {
                        Text(
                            text = "No friends yet. Add some!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = Spacing.lg)
                        )
                    }
                } else {
                    items(friends) { friend ->
                        FriendListItem(
                            friend = friend,
                            onDelete = {
                                // In a real app we'd probably want a friendship ID here.
                                // For now, let's assume we can find it or the API supports it.
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FriendRequestItem(
    request: Friendship,
    onAccept: (Long) -> Unit,
    onDecline: (Long) -> Unit
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
            Column {
                Text(
                    text = "Request from",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = request.requesterId,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Row {
                IconButton(onClick = { request.id?.let { onAccept(it) } }) {
                    Icon(Icons.Default.Check, contentDescription = "Accept", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { request.id?.let { onDecline(it) } }) {
                    Icon(Icons.Default.Close, contentDescription = "Decline", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun FriendListItem(
    friend: FriendProfile,
    onDelete: () -> Unit
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
                text = friend.username,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
