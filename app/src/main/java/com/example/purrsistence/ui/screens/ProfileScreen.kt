package com.example.purrsistence.ui.screens

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.purrsistence.domain.cats.CatList
import com.example.purrsistence.ui.state.TopBarState
import com.example.purrsistence.ui.theme.Shapes
import com.example.purrsistence.ui.theme.Spacing
import com.example.purrsistence.ui.viewmodel.UserViewModel

@Composable
fun ProfileScreen(
    userViewModel: UserViewModel,
    setTopBar: (TopBarState) -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToFriends: () -> Unit = {}
) {
    // set TopBar content (header only)
    setTopBar(
        TopBarState(
            title = "Profile"
        )
    )

    val user by userViewModel.user.collectAsState()
    var isEditingName by remember { mutableStateOf(false) }
    var editedUsername by remember(user?.username) { mutableStateOf(user?.username ?: "") }
    var selectedProfileImageUri by remember(user?.profileImageUrl) {
        mutableStateOf(user?.profileImageUrl?.let(Uri::parse))
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedProfileImageUri = uri
            userViewModel.updateProfileImage(uri.toString())
        }
    }

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val onSaveUsername = {
        userViewModel.updateUsername(editedUsername)
        isEditingName = false
    }

    val onPickProfileImage = {
        imagePickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    val onRemoveProfileImage = {
        selectedProfileImageUri = null
        userViewModel.updateProfileImage(null)
    }

    val headerCallbacks = ProfileHeaderCallbacks(
        onUsernameChange = { editedUsername = it },
        onEditingChange = { isEditingName = it },
        onSaveUsername = onSaveUsername,
        onPickProfileImage = onPickProfileImage,
        onRemoveProfileImage = onRemoveProfileImage
    )

    if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                ProfileHeaderSection(
                    user = user,
                    username = editedUsername,
                    isEditing = isEditingName,
                    profileImageUri = selectedProfileImageUri,
                    callbacks = headerCallbacks
                )

                Spacer(modifier = Modifier.height(Spacing.xl))

                ProfileActionButtons(
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToFriends = onNavigateToFriends
                )
            }

            InventorySection(
                user = user,
                modifier = Modifier.weight(1f),
                maxGridHeight = 650.dp
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg)
        ) {
            ProfileHeaderSection(
                user = user,
                username = editedUsername,
                isEditing = isEditingName,
                profileImageUri = selectedProfileImageUri,
                callbacks = headerCallbacks
            )

            Spacer(modifier = Modifier.height(Spacing.xl))

            ProfileActionButtons(
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToFriends = onNavigateToFriends
            )

            Spacer(modifier = Modifier.height(Spacing.xl))

            InventorySection(
                user = user,
                maxGridHeight = 500.dp
            )

            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

private data class ProfileHeaderCallbacks(
    val onUsernameChange: (String) -> Unit,
    val onEditingChange: (Boolean) -> Unit,
    val onSaveUsername: () -> Unit,
    val onPickProfileImage: () -> Unit,
    val onRemoveProfileImage: () -> Unit
)

@Composable
private fun ProfileActionButtons(
    onNavigateToSettings: () -> Unit,
    onNavigateToFriends: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        ActionButton(
            label = "Settings",
            icon = Icons.Outlined.Edit,
            onClick = onNavigateToSettings,
            modifier = Modifier.weight(1f)
        )
        ActionButton(
            label = "Friends",
            icon = Icons.Outlined.Edit,
            onClick = onNavigateToFriends,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun InventorySection(
    user: com.example.purrsistence.domain.model.User?,
    modifier: Modifier = Modifier,
    maxGridHeight: Dp
) {
    Column(modifier = modifier) {
        Text(
            text = "Inventory",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = Spacing.md)
        )

        if (user != null && user.collectedCatsIds.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxGridHeight),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                items(
                    items = user.collectedCatsIds,
                    key = { it }
                ) { catId ->
                    val cat = CatList.getCatById(catId)
                    if (cat != null) {
                        CatInventoryCard(cat = cat)
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = Shapes.cards
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No cats in inventory yet.\nVisit the Shop to adopt some!",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(Spacing.md),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ProfileHeaderSection(
    user: com.example.purrsistence.domain.model.User?,
    username: String,
    isEditing: Boolean,
    profileImageUri: Uri?,
    callbacks: ProfileHeaderCallbacks,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = Shapes.cards
            )
            .padding(Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        EditableProfileAvatar(
            profileImageUri = profileImageUri,
            onPickProfileImage = callbacks.onPickProfileImage,
            onRemoveProfileImage = callbacks.onRemoveProfileImage,
            modifier = Modifier
                .size(104.dp)
        )

        // Username Section
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            if (isEditing) {
                // Edit mode
                TextField(
                    value = username,
                    onValueChange = callbacks.onUsernameChange,
                    singleLine = true,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                    shape = Shapes.inputs,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    textStyle = MaterialTheme.typography.titleMedium
                )

                // Save/Cancel buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Button(
                        onClick = callbacks.onSaveUsername,
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp),
                        shape = Shapes.buttons
                    ) {
                        Text("Save", style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedButton(
                        onClick = { callbacks.onEditingChange(false) },
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp),
                        shape = Shapes.buttons
                    ) {
                        Text("Cancel", style = MaterialTheme.typography.labelSmall)
                    }
                }
            } else {
                // View mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user?.username ?: "Username",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { callbacks.onEditingChange(true) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit Username",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Additional info
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${user?.collectedCatsIds?.size ?: 0} Cats",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (user?.isSupabaseLinked == true) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiary,
                            shape = Shapes.buttons,
                            modifier = Modifier.size(width = 60.dp, height = 20.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "Linked",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditableProfileAvatar(
    profileImageUri: Uri?,
    onPickProfileImage: () -> Unit,
    onRemoveProfileImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary)
                .clickable(onClick = onPickProfileImage),
            contentAlignment = Alignment.Center
        ) {
            if (profileImageUri != null) {
                AsyncImage(
                    model = profileImageUri,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = "Add Profile Picture",
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
        }

        AvatarActionButton(
            icon = Icons.Default.Camera,
            contentDescription = "Change Profile Picture",
            onClick = onPickProfileImage,
            modifier = Modifier.align(Alignment.BottomEnd)
        )

        if (profileImageUri != null) {
            AvatarActionButton(
                icon = Icons.Outlined.Delete,
                contentDescription = "Remove Profile Picture",
                onClick = onRemoveProfileImage,
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}

@Composable
private fun AvatarActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onPrimary
) {
    Surface(
        modifier = modifier.size(28.dp),
        shape = CircleShape,
        color = containerColor,
        shadowElevation = 2.dp
    ) {
        IconButton(onClick = onClick, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(16.dp),
                tint = contentColor
            )
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = Shapes.buttons,
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = MaterialTheme.colorScheme.secondary
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSecondary
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondary
            )
        }
    }
}

@Composable
private fun CatInventoryCard(
    cat: com.example.purrsistence.domain.model.ShopItem,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .clip(Shapes.cards),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = cat.imageRes),
                contentDescription = cat.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(Shapes.cards)
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = cat.name,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}