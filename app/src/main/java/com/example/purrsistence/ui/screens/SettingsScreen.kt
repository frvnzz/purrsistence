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
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.material3.TextField
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
    val isLoading by userViewModel.isSupabaseLoading.collectAsState()
    val supabaseError by userViewModel.supabaseError.collectAsState()

    var showResetDialog by remember { mutableStateOf(false) }
    var showUsernameDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }

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
                onClick = {
                    if (isSignedIn) {
                        showUsernameDialog = true
                    } else {
                        onNavigateToAuth()
                    }
                },
                enabled = true
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = Spacing.lg),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            if (isSignedIn) {
                SettingsItem(
                    title = "Change Password",
                    description = "Update your account password",
                    icon = Icons.Default.Lock,
                    onClick = { showPasswordDialog = true }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = Spacing.lg),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
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

    if (showUsernameDialog) {
        var newUsername by remember { mutableStateOf(user?.username ?: "") }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        //clear error when starting
        LaunchedEffect(showUsernameDialog) {
            userViewModel.clearSupabaseError()
        }

        //observe ViewModel error and map it
        LaunchedEffect(supabaseError) {
            val error = supabaseError
            errorMessage = when {
                error == null -> null
                error.contains("duplicate key", ignoreCase = true) -> "Username is already taken."
                else -> "Failed to update username. Please try again."
            }
        }

        AlertDialog(
            onDismissRequest = { if (!isLoading) showUsernameDialog = false },
            title = { Text("Update Username") },
            text = {
                Column {
                    TextField(
                        value = newUsername,
                        onValueChange = { newUsername = it },
                        label = { Text("New Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = errorMessage != null
                    )
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = Spacing.xs)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newUsername.isNotBlank()) {
                            userViewModel.updateUsernameInSupabase(newUsername)
                        }
                    },
                    enabled = (!isLoading && newUsername.isNotBlank() && newUsername != user?.username),
                    shape = Shapes.buttons
                ) {
                    Text(if (isLoading) "Updating..." else "Update")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUsernameDialog = false },
                    enabled = !isLoading,
                    shape = Shapes.buttons
                ) {
                    Text("Cancel")
                }
            }
        )

        //close if isLoading goes from true to false and no error
        var wasLoading by remember { mutableStateOf(false) }
        LaunchedEffect(isLoading) {
            if (wasLoading && !isLoading && supabaseError == null) {
                showUsernameDialog = false
            }
            wasLoading = isLoading
        }
    }

    if (showPasswordDialog) {
        var currentPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(showPasswordDialog) {
            userViewModel.clearSupabaseError()
        }

        LaunchedEffect(supabaseError) {
            val error = supabaseError
            errorMessage = when {
                error == null -> null
                error.contains("invalid login credentials", ignoreCase = true) -> "Current password is incorrect."
                error.contains("weak_password", ignoreCase = true) -> "New password is too weak."
                error.contains("should be at least", ignoreCase = true) -> "Password is too short."
                else -> "Failed to update password. Please try again."
            }
        }

        AlertDialog(
            onDismissRequest = { if (!isLoading) showPasswordDialog = false },
            title = { Text("Change Password") },
            text = {
                Column {
                    TextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Current Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))
                    TextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = errorMessage != null
                    )
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = Spacing.xs)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (currentPassword.isNotBlank() && newPassword.isNotBlank()) {
                            userViewModel.updatePasswordInSupabase(currentPassword, newPassword)
                        }
                    },
                    enabled = !isLoading && currentPassword.isNotBlank() && newPassword.isNotBlank(),
                    shape = Shapes.buttons
                ) {
                    Text(if (isLoading) "Updating..." else "Update")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPasswordDialog = false },
                    enabled = !isLoading,
                    shape = Shapes.buttons
                ) {
                    Text("Cancel")
                }
            }
        )

        var wasLoading by remember { mutableStateOf(false) }
        LaunchedEffect(isLoading) {
            if (wasLoading && !isLoading && supabaseError == null) {
                showPasswordDialog = false
            }
            wasLoading = isLoading
        }
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
                    text = description,
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
