package com.example.purrsistence.ui.components.profileScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.purrsistence.domain.cats.CatList
import com.example.purrsistence.domain.model.ShopItem
import com.example.purrsistence.domain.model.User
import com.example.purrsistence.ui.components.homeScreen.CatImage
import com.example.purrsistence.ui.theme.Shapes
import com.example.purrsistence.ui.theme.Spacing

@Composable
fun InventorySection(
    user: User?,
    modifier: Modifier = Modifier,
    maxGridHeight: Dp,
    isLandscape: Boolean = false
) {
    Column(modifier = modifier) {
        Text(
            text = "Inventory",
            style = if (isLandscape) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = if (isLandscape) Spacing.sm else Spacing.md)
        )

        if (user != null && user.collectedCatsIds.isNotEmpty()) {
            LazyVerticalGrid(
                columns = if (isLandscape) GridCells.Adaptive(100.dp) else GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxGridHeight),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                val ids = user.collectedCatsIds
                items(count = ids.size) { index ->
                    val catId = ids[index]
                    val cat = CatList.getCatById(catId)
                    if (cat != null) {
                        CatInventoryCard(cat = cat)
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = Shapes.cards
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No cats in inventory yet.\nVisit the Shop to adopt some!",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(Spacing.md),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun CatInventoryCard(
    cat: ShopItem,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .clip(Shapes.cards)
            .clearAndSetSemantics {
                contentDescription = "Cat: ${cat.name}"
            },
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CatImage(
                cat = cat,
                isAnimated = false,
                modifier = Modifier
                    .size(60.dp)
                    .clip(Shapes.cards)
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = cat.name,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
