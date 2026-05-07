package com.example.purrsistence.ui.screens

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.purrsistence.ui.components.profileScreen.InventorySection
import com.example.purrsistence.ui.components.profileScreen.ProfileActionButtons
import com.example.purrsistence.ui.components.profileScreen.ProfileHeaderCallbacks
import com.example.purrsistence.ui.components.profileScreen.ProfileHeaderSection
import com.example.purrsistence.ui.state.TopBarState
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
        userViewModel.updateUsername(editedUsername)
        isEditingName = false
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
                .pointerInput(focusManager) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
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
                    usernameFocusRequester = usernameFocusRequester,
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
                .pointerInput(focusManager) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg)
        ) {
            ProfileHeaderSection(
                user = user,
                username = editedUsername,
                isEditing = isEditingName,
                profileImageUri = selectedProfileImageUri,
                usernameFocusRequester = usernameFocusRequester,
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
