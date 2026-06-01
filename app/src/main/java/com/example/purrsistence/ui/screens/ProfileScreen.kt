package com.example.purrsistence.ui.screens

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.purrsistence.ui.components.profileScreen.InventorySection
import com.example.purrsistence.ui.components.profileScreen.ProfileActionButtons
import com.example.purrsistence.ui.components.profileScreen.ProfileHeaderCallbacks
import com.example.purrsistence.ui.components.profileScreen.ProfileHeaderSection
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
    LaunchedEffect(Unit) {
        setTopBar(
            TopBarState(
                title = "Profile"
            )
        )
    }

    val user by userViewModel.user.collectAsState()
    var usernameError by remember { mutableStateOf<String?>(null) }
    var isEditingName by remember { mutableStateOf(false) }
    var editedUsername by remember(user?.username) { mutableStateOf(user?.username ?: "") }
    var selectedProfileImageUri by remember { mutableStateOf<Uri?>(null) }
    val focusManager = LocalFocusManager.current
    val usernameFocusRequester = remember { FocusRequester() }

    // Synchronize profile image URI whenever user data changes
    val currentUser = user
    LaunchedEffect(currentUser?.profileImageUrl) {
        currentUser?.profileImageUrl?.let { profileImageUrl ->
            selectedProfileImageUri = profileImageUrl.toString().toUri()
        } ?: run {
            selectedProfileImageUri = null
        }
    }

    fun validateUsername(username: String): String? {
        return when {
            username.isBlank() -> "Username cannot be empty"
            username.any { it.isWhitespace() } -> "Username cannot contain spaces"
            else -> null
        }
    }

    LaunchedEffect(isEditingName) {
        if (isEditingName) {
            usernameFocusRequester.requestFocus()
        }
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
        focusManager.clearFocus()

        usernameError = validateUsername(editedUsername)

        if (usernameError == null) {
            userViewModel.updateUsername(editedUsername.trim())
            isEditingName = false
        }
    }

    val onPickProfileImage = {
        focusManager.clearFocus()
        imagePickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    val onRemoveProfileImage = {
        focusManager.clearFocus()
        selectedProfileImageUri = null
        userViewModel.updateProfileImage(null)
    }

    val headerCallbacks = ProfileHeaderCallbacks(
        onUsernameChange = {
            editedUsername = it
            usernameError = null
        },
        onEditingChange = { isEditingName = it },
        onSaveUsername = onSaveUsername,
        onPickProfileImage = onPickProfileImage,
        onRemoveProfileImage = onRemoveProfileImage
    )

    if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .semantics { paneTitle = "Profile Screen" }
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
                .padding(Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            Column(
                modifier = Modifier
                    .weight(0.32f)
                    .fillMaxHeight()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = Shapes.cards
                    )
                    .padding(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                ProfileHeaderSection(
                    user = user,
                    username = editedUsername,
                    isEditing = isEditingName,
                    profileImageUri = selectedProfileImageUri,
                    usernameFocusRequester = usernameFocusRequester,
                    callbacks = headerCallbacks,
                    isLandscape = true,
                    usernameError = usernameError
                )

                Spacer(modifier = Modifier.weight(1f))

                ProfileActionButtons(
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToFriends = onNavigateToFriends,
                    isLandscape = true
                )
            }

            InventorySection(
                user = user,
                modifier = Modifier.weight(0.68f),
                maxGridHeight = 1000.dp,
                isLandscape = true
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .semantics { paneTitle = "Profile Screen" }
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                },
            verticalArrangement = Arrangement.spacedBy(Spacing.xl),
            contentPadding = PaddingValues(Spacing.lg)
        ) {
            item {
                ProfileHeaderSection(
                    user = user,
                    username = editedUsername,
                    isEditing = isEditingName,
                    profileImageUri = selectedProfileImageUri,
                    usernameFocusRequester = usernameFocusRequester,
                    callbacks = headerCallbacks,
                    usernameError = usernameError
                )
            }

            item {
                ProfileActionButtons(
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToFriends = onNavigateToFriends
                )
            }

            item {
                InventorySection(
                    user = user,
                    maxGridHeight = 500.dp
                )
            }
        }
    }
}
