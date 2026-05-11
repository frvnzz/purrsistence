package com.example.purrsistence.ui.components.profileScreen

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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun EditableProfileAvatar(
    profileImageUri: android.net.Uri?,
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
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
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
fun AvatarActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
