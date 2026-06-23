package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
        modifier = Modifier
            .fillMaxSize()
            .semantics { paneTitle = "Friend Profile Screen" },
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator()
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
    val selectedCatIdsSet = details.selectedCatIds.toSet()
    val collectedCats = details.collectedCatIds.mapNotNull { catId ->
        CatList.getCatById(catId)
    }.sortedByDescending { selectedCatIdsSet.contains(it.id) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.md),
        contentPadding = PaddingValues(bottom = Spacing.xl),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Spacer(modifier = Modifier.height(Spacing.md))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = Shapes.cards,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = Elevation.Lvl2
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.lg),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = details.profile.username,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.semantics { heading() }
                        )

                        Spacer(modifier = Modifier.height(Spacing.xs))

                        Text(
                            text = "Tracked: ${formatTrackedMinutes(details.weeklyTrackedMinutes)} this week",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xl))

                Text(
                    text = "Cat Collection",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(horizontal = Spacing.xs)
                        .semantics { heading() }
                )

                Spacer(modifier = Modifier.height(Spacing.sm))
            }
        }

        if (collectedCats.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyText("No cats collected yet.")
            }
        } else {
            items(collectedCats) { cat ->
                FriendCatItem(
                    cat = cat,
                    isSelected = selectedCatIdsSet.contains(cat.id)
                )
            }
        }
    }
}

@Composable
fun FriendCatItem(
    cat: ShopItem,
    isSelected: Boolean
) {
    Surface(
        shape = Shapes.cards,
        color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = if (isSelected) Elevation.Lvl2 else Elevation.None,
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                if (isSelected) {
                    stateDescription = "Currently selected cat"
                }
            }
    ) {
        Column(
            modifier = Modifier
                .padding(Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                CatImage(
                    cat = cat,
                    isAnimated = false,
                    modifier = Modifier.size(64.dp)
                )

                if (isSelected) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = androidx.compose.foundation.shape.CircleShape,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(10.dp)
                    ) {}
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = cat.name,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            if (isSelected) {
                Text(
                    text = "Selected",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
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
        modifier = Modifier.padding(vertical = Spacing.sm),
        textAlign = TextAlign.Center
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
