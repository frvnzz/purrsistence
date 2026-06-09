package com.example.purrsistence.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.purrsistence.domain.model.FriendProfile
import com.example.purrsistence.domain.model.Friendship
import com.example.purrsistence.ui.state.TopBarState
import com.example.purrsistence.ui.theme.Shapes
import com.example.purrsistence.ui.theme.Spacing
import com.example.purrsistence.ui.viewmodel.FriendViewModel

@Composable
fun FriendsScreen(
    viewModel: FriendViewModel,
    onAddFriendClick: () -> Unit,
    onFriendClick: (String) -> Unit,
    onBack: () -> Unit,
    setTopBar: (TopBarState) -> Unit
) {
    val friends by viewModel.friends.collectAsState()
    val incomingRequests by viewModel.incomingRequests.collectAsState()
    val outgoingRequests by viewModel.outgoingRequests.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var requestToCancel by remember { mutableStateOf<Friendship?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadFriendsData()
        setTopBar(
            TopBarState(
                title = "Friends",
                onBackClick = onBack,
                actions = {
                    IconButton(onClick = onAddFriendClick) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Friend"
                        )
                    }
                }
            )
        )
    }


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            isLoading &&
                    friends.isEmpty() &&
                    incomingRequests.isEmpty() &&
                    outgoingRequests.isEmpty() -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Spacing.md),
                    contentPadding = PaddingValues(
                        vertical = Spacing.md
                    )
                ) {
                    if (error != null) {
                        item {
                            ErrorText(
                                message = error ?: ""
                            )

                            Spacer(
                                modifier = Modifier.height(Spacing.sm)
                            )
                        }
                    }

                    if (incomingRequests.isNotEmpty()) {
                        item {
                            SectionTitle("Friend Requests")
                        }

                        items(incomingRequests) { request ->
                            FriendRequestItem(
                                request = request,
                                onAccept = {
                                    viewModel.acceptFriendRequest(it)
                                },
                                onDecline = {
                                    viewModel.declineFriendRequest(it)
                                }
                            )
                        }

                        item {
                            HorizontalDivider(
                                modifier = Modifier.padding(
                                    vertical = Spacing.md
                                )
                            )
                        }
                    }

                    if (outgoingRequests.isNotEmpty()) {
                        item {
                            SectionTitle("Outgoing Requests")
                        }

                        items(outgoingRequests) { request ->
                            OutgoingFriendRequestItem(
                                request = request,
                                onCancel = {
                                    requestToCancel = request
                                }
                            )
                        }

                        item {
                            HorizontalDivider(
                                modifier = Modifier.padding(
                                    vertical = Spacing.md
                                )
                            )
                        }
                    }

                    item {
                        SectionTitle("Your Friends")
                    }

                    if (friends.isEmpty()) {
                        item {
                            Text(
                                text = "No friends yet. Add some!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(
                                    vertical = Spacing.lg
                                )
                            )
                        }
                    } else {
                        items(friends) { friend ->
                            FriendListItem(
                                friend = friend,
                                onClick = {
                                    onFriendClick(friend.id)
                                },
                                onDelete = friend.friendshipId?.let { friendshipId ->
                                    {
                                        viewModel.deleteFriendship(friendshipId)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        requestToCancel?.let { request ->
            AlertDialog(
                onDismissRequest = { requestToCancel = null },
                title = { Text("Cancel Request?") },
                text = {
                    val name = request.addresseeUsername ?: request.addresseeId
                    Text("Are you sure you want to cancel the friend request to $name?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            request.id?.let { viewModel.deleteFriendship(it) }
                            requestToCancel = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = Shapes.buttons
                    ) {
                        Text("Cancel Request")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { requestToCancel = null },
                        shape = Shapes.buttons
                    ) {
                        Text("Keep")
                    }
                }
            )
        }
    }
}

@Composable
private fun SectionTitle(
    text: String
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(
            vertical = Spacing.sm
        )
    )
}

@Composable
private fun ErrorText(
    message: String
) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error
    )
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
                    text = request.requesterUsername ?: request.requesterId,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Row {
                IconButton(
                    onClick = {
                        onAccept(request.id!!)
                    }
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Accept",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = {
                        onDecline(request.id!!)
                    }
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Decline",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun OutgoingFriendRequestItem(
    request: Friendship,
    onCancel: (Long) -> Unit
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
                    text = "Pending request",
                    style = MaterialTheme.typography.labelSmall
                )

                Text(
                    text = request.addresseeUsername ?: request.addresseeId,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            IconButton(
                onClick = {
                    onCancel(request.id!!)
                }
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}


@Composable
fun FriendListItem(
    friend: FriendProfile,
    onClick: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs)
            .clickable(onClick = onClick)
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
                    text = friend.username,
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = "View profile",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (onDelete != null) {
                IconButton(
                    onClick = onDelete
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove Friend",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
