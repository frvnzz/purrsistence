package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import com.example.purrsistence.ui.state.TopBarState
import com.example.purrsistence.ui.theme.Spacing

@Composable
fun FriendsScreen(
    onBack: () -> Unit,
    setTopBar: (TopBarState) -> Unit
) {
    // set TopBar content with back button
    LaunchedEffect(Unit) {
        setTopBar(
            TopBarState(
                title = "Friends",
                onBackClick = onBack
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .semantics { paneTitle = "Friends Screen" }
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Friends",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        Text(
            text = "Friends coming soon!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

