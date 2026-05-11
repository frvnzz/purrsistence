package com.example.purrsistence.ui.state

import androidx.compose.runtime.Composable

// Set TopBar content on each screen individually
// -> this is the global UI state for TopBar

data class TopBarState(
    val title: String = "",
    val actions: @Composable (() -> Unit)? = null,
    val navigationIcon: @Composable (() -> Unit)? = null,
    val onBackClick: (() -> Unit)? = null
)
