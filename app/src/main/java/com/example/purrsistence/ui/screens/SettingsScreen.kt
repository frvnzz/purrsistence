package com.example.purrsistence.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.state.TopBarState
import com.example.purrsistence.ui.theme.Shapes
import com.example.purrsistence.ui.theme.Spacing
import com.example.purrsistence.ui.viewmodel.UserViewModel

@Composable
fun SettingsScreen(
    userViewModel: UserViewModel,
    onNavigateToAuth: () -> Unit,
    onBack: () -> Unit,
    setTopBar: (TopBarState) -> Unit
) {
    val isSignedIn by userViewModel.isSupabaseSignedIn.collectAsState()
    val user by userViewModel.user.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    // set TopBar content with back button
    LaunchedEffect(Unit) {
        setTopBar(
            TopBarState(
                title = "Settings",
                onBackClick = onBack
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .semantics { paneTitle = "Settings Screen" }
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg)
    ) {
        //ACCOUNT SECTION
        SettingsSection(title = "Account") {
            SettingsItem(
                title = "Profile",
                description = if (isSignedIn) user?.username else "Login to sync and unlock more features",
                icon = Icons.Default.AccountCircle,
                onClick = { if (!isSignedIn) onNavigateToAuth() },
                enabled = !isSignedIn
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = Spacing.lg),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            SettingsItem(
                title = "Cloud Sync",
                description = if (isSignedIn) "Purrsistence Cloud Synced" else "Local Only Mode",
                icon = Icons.Default.Cloud,
                enabled = false
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = Spacing.lg),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            if (isSignedIn) {
                SettingsItem(
                    title = "Logout",
                    icon = Icons.AutoMirrored.Filled.Logout,
                    onClick = { userViewModel.signOutFromSupabase() },
                    tint = MaterialTheme.colorScheme.error,
                    textColor = MaterialTheme.colorScheme.error
                )
            } else {
                SettingsItem(
                    title = "Login / Register",
                    icon = Icons.AutoMirrored.Filled.Login,
                    onClick = { onNavigateToAuth() },
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xl))

        //PREFERENCES SECTION
        SettingsSection(title = "App Preferences") {
            SettingsItem(
                title = "Notifications",
                description = "Coming soon",
                icon = Icons.Default.Notifications,
                enabled = false
            )
        }

        Spacer(modifier = Modifier.height(Spacing.xl))

        // DANGER ZONE SECTION
        SettingsSection(title = "Danger Zone") {
            SettingsItem(
                title = "Reset Tracking Data",
                description = "Wipe all history and progress",
                icon = Icons.Default.DeleteForever,
                onClick = { showResetDialog = true },
                tint = MaterialTheme.colorScheme.error,
                textColor = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(Spacing.xxl))
        
        Text(
            text = "Purrsistence v0.0.1",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Tracking Sessions?") },
            text = {
                Text("This will permanently delete all your tracking history and reset goal completion status. This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        userViewModel.resetTrackingSessions()
                        showResetDialog = false
                    },
                    shape = Shapes.buttons,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetDialog = false },
                    shape = Shapes.buttons
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}



//Composables
@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = Spacing.sm, start = Spacing.xs)
        )
        Surface(
            shape = Shapes.cards,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            shadowElevation = 0.5.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    icon: ImageVector,
    description: String? = null,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(Spacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (enabled) tint else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.width(Spacing.lg))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) textColor else MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Medium
            )
            if (description != null) {
                Text(
                    text = if (enabled) description else "$description",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        if (enabled) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}
