package com.example.purrsistence.ui.components.profileScreen

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun EditableProfileAvatar(
    profileImageUri: Uri?,
    onPickProfileImage: () -> Unit,
    onRemoveProfileImage: () -> Unit,
    modifier: Modifier = Modifier,
    isLandscape: Boolean = false
) {
    val actionButtonSize = if (isLandscape) 22.dp else 28.dp
    val actionIconSize = if (isLandscape) 12.dp else 16.dp

    Box(
        modifier = if (isLandscape) Modifier.size(72.dp) else modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary)
                .clickable(onClick = onPickProfileImage)
                .semantics { 
                    contentDescription = if (profileImageUri != null) "Profile Picture" else "No profile picture set"
                    traversalIndex = 1f 
                },
            contentAlignment = Alignment.Center
        ) {
            if (profileImageUri != null) {
                AsyncImage(
                    model = profileImageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = null,
                    modifier = Modifier.size(if (isLandscape) 24.dp else 36.dp),
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
        }

        if (profileImageUri != null) {
            AvatarActionButton(
                icon = Icons.Outlined.Delete,
                contentDescription = "Remove Profile Picture",
                onClick = onRemoveProfileImage,
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .semantics { traversalIndex = 5f },
                size = actionButtonSize,
                iconSize = actionIconSize
            )
        }

        AvatarActionButton(
            icon = Icons.Default.Camera,
            contentDescription = if (profileImageUri == null) "Add Profile Picture" else "Change Profile Picture",
            onClick = onPickProfileImage,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .semantics { traversalIndex = 6f },
            size = actionButtonSize,
            iconSize = actionIconSize
        )
    }
}

@Composable
fun AvatarActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    size: androidx.compose.ui.unit.Dp = 28.dp,
    iconSize: androidx.compose.ui.unit.Dp = 16.dp
) {
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = containerColor,
        shadowElevation = 2.dp
    ) {
        IconButton(onClick = onClick, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(iconSize),
                tint = contentColor
            )
        }
    }
}
