package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.purrsistence.ui.state.TopBarState
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
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        Text(
            text = "Settings coming soon!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Spacing.xl))

        if (isSignedIn) {
            Button(
                onClick = { userViewModel.signOutFromSupabase() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Logout")
            }
        } else {
            Button(
                onClick = { onNavigateToAuth() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }
        }
    }
}


