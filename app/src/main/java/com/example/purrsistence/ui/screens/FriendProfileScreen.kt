package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.purrsistence.domain.cats.CatList
import com.example.purrsistence.domain.model.FriendProfileDetails
import com.example.purrsistence.domain.model.ShopItem
import com.example.purrsistence.ui.components.homeScreen.CatImage
import com.example.purrsistence.ui.state.TopBarState
import com.example.purrsistence.ui.theme.Elevation
import com.example.purrsistence.ui.theme.Shapes
import com.example.purrsistence.ui.theme.Spacing
import com.example.purrsistence.ui.viewmodel.FriendViewModel

@Composable
fun FriendProfileScreen(
    viewModel: FriendViewModel,
    friendUserId: String,
    onBack: () -> Unit,
    setTopBar: (TopBarState) -> Unit
) {
    val details by viewModel.selectedFriendProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(friendUserId) {
        setTopBar(
            TopBarState(
                title = "Friend Profile",
                onBackClick = onBack
            )
        )

        viewModel.loadFriendProfile(friendUserId)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.padding(Spacing.md)
                )
            }

            error != null -> {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(Spacing.md)
                )
            }

            details != null -> {
                FriendProfileDetailsContent(
                    details = details!!
                )
            }
        }
    }
}

@Composable
fun FriendProfileDetailsContent(
    details: FriendProfileDetails
) {
    val selectedCats =
        details.selectedCatIds.mapNotNull { catId ->
            CatList.getCatById(catId)
        }

    val collectedCats =
        details.collectedCatIds.mapNotNull { catId ->
            CatList.getCatById(catId)
        }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.md),
        contentPadding = PaddingValues(
            vertical = Spacing.md
        )
    ) {
        item {
            Text(
                text = details.profile.username,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            Text(
                text = "Tracked this week: ${formatTrackedMinutes(details.weeklyTrackedMinutes)}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            Text(
                text = "Selected Cats",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(Spacing.sm))
        }

        if (selectedCats.isEmpty()) {
            item {
                EmptyText("No selected cats.")
            }
        } else {
            items(selectedCats) { cat ->
                FriendCatItem(cat = cat)
            }
        }

        item {
            Spacer(modifier = Modifier.height(Spacing.lg))

            Text(
                text = "Cat Collection",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(Spacing.sm))
        }

        if (collectedCats.isEmpty()) {
            item {
                EmptyText("No cats collected yet.")
            }
        } else {
            items(collectedCats) { cat ->
                FriendCatItem(cat = cat)
            }
        }
    }
}

@Composable
fun FriendCatItem(
    cat: ShopItem
) {
    Surface(
        shape = Shapes.cards,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = Elevation.Lvl4,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CatImage(
                cat = cat,
                isAnimated = false,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.width(Spacing.md))

            Column {
                Text(
                    text = cat.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "${cat.price} coins",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyText(
    text: String
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = Spacing.sm)
    )
}

private fun formatTrackedMinutes(
    totalMinutes: Long
): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}