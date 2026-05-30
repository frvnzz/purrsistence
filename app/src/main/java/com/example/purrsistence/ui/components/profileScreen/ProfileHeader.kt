package com.example.purrsistence.ui.components.profileScreen

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.purrsistence.domain.model.User
import com.example.purrsistence.ui.theme.Shapes
import com.example.purrsistence.ui.theme.Spacing

data class ProfileHeaderCallbacks(
    val onUsernameChange: (String) -> Unit,
    val onEditingChange: (Boolean) -> Unit,
    val onSaveUsername: () -> Unit,
    val onPickProfileImage: () -> Unit,
    val onRemoveProfileImage: () -> Unit
)

@Composable
fun ProfileHeaderSection(
    user: User?,
    username: String,
    isEditing: Boolean,
    profileImageUri: Uri?,
    usernameFocusRequester: FocusRequester,
    callbacks: ProfileHeaderCallbacks,
    modifier: Modifier = Modifier,
    isLandscape: Boolean = false
) {
    if (isLandscape) {
        LandscapeProfileHeader(
            user = user,
            username = username,
            isEditing = isEditing,
            profileImageUri = profileImageUri,
            usernameFocusRequester = usernameFocusRequester,
            callbacks = callbacks,
            modifier = modifier
        )
    } else {
        PortraitProfileHeader(
            user = user,
            username = username,
            isEditing = isEditing,
            profileImageUri = profileImageUri,
            usernameFocusRequester = usernameFocusRequester,
            callbacks = callbacks,
            modifier = modifier
        )
    }
}

@Composable
private fun PortraitProfileHeader(
    user: User?,
    username: String,
    isEditing: Boolean,
    profileImageUri: Uri?,
    usernameFocusRequester: FocusRequester,
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
            modifier = Modifier.size(104.dp)
        )

        // Username Section
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            UsernameDisplay(
                user = user,
                username = username,
                isEditing = isEditing,
                usernameFocusRequester = usernameFocusRequester,
                callbacks = callbacks
            )

            UserStatsRow(user = user)
        }
    }
}

@Composable
private fun LandscapeProfileHeader(
    user: User?,
    username: String,
    isEditing: Boolean,
    profileImageUri: Uri?,
    usernameFocusRequester: FocusRequester,
    callbacks: ProfileHeaderCallbacks,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        EditableProfileAvatar(
            profileImageUri = profileImageUri,
            onPickProfileImage = callbacks.onPickProfileImage,
            onRemoveProfileImage = callbacks.onRemoveProfileImage,
            modifier = Modifier.size(96.dp),
            isLandscape = true
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            UsernameDisplay(
                user = user,
                username = username,
                isEditing = isEditing,
                usernameFocusRequester = usernameFocusRequester,
                callbacks = callbacks,
                textAlign = TextAlign.Center
            )

            UserStatsRow(user = user)
        }
    }
}

@Composable
private fun UsernameDisplay(
    user: User?,
    username: String,
    isEditing: Boolean,
    usernameFocusRequester: FocusRequester,
    callbacks: ProfileHeaderCallbacks,
    textAlign: TextAlign = TextAlign.Start
) {
    if (isEditing) {
        // Edit mode
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = if (textAlign == TextAlign.Center) Alignment.CenterHorizontally else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = callbacks.onUsernameChange,
                singleLine = true,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(usernameFocusRequester),
                shape = Shapes.inputs,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                textStyle = MaterialTheme.typography.titleMedium.copy(textAlign = textAlign),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { callbacks.onSaveUsername() })
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
                        .heightIn(min = 32.dp),
                    shape = Shapes.buttons,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Save", style = MaterialTheme.typography.labelSmall)
                    }
                }

                OutlinedButton(
                    onClick = { callbacks.onEditingChange(false) },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 32.dp),
                    shape = Shapes.buttons,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Cancel", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    } else {
        // View mode
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (textAlign == TextAlign.Center) Arrangement.Center else Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = user?.username ?: "Username",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = textAlign,
                modifier = Modifier
                    .then(if (textAlign == TextAlign.Center) Modifier else Modifier.weight(1f))
                    .semantics {
                        contentDescription = "Username: ${user?.username ?: "Username"}"
                    },
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
    }
}

@Composable
private fun UserStatsRow(user: User?) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${user?.collectedCatsIds?.size ?: 0} Cats",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.semantics {
                contentDescription = "Number of collected cats by User is ${user?.collectedCatsIds?.size ?: 0}"
            }
        )

        if (user?.isSupabaseLinked == true) {
            Surface(
                color = MaterialTheme.colorScheme.tertiary,
                shape = Shapes.buttons,
                modifier = Modifier.size(width = 48.dp, height = 16.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "Linked",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiary,
                        fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.8
                    )
                }
            }
        }
    }
}
