package com.example.purrsistence.ui.components.shop

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.unit.dp
import com.example.purrsistence.R
import com.example.purrsistence.domain.model.ShopItem
import com.example.purrsistence.ui.components.homeScreen.CatImage
import com.example.purrsistence.ui.theme.Elevation
import com.example.purrsistence.ui.theme.Shapes
import com.example.purrsistence.ui.theme.Spacing
import com.example.purrsistence.ui.util.formatLocalizedInteger

@Composable
fun ShopItemCard(
    item: ShopItem,
    balance: Int,
    isOwned: Boolean,
    onBuy: () -> Unit
) {
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val canAfford = balance >= item.price

    val buttonText = when {
        isOwned -> "Owned"
        !canAfford -> "Need Fish"
        else -> "Adopt"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics {
                contentDescription = "${item.name}, ${item.price} fish, $buttonText"
                role = Role.Button
                if (!canAfford || isOwned) {
                    disabled()
                }
            }
            .clickable(
                enabled = canAfford && !isOwned,
                onClick = onBuy
            ),
        shape = Shapes.cards,
        elevation = CardDefaults.cardElevation(
            defaultElevation = Elevation.Lvl2
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        if (isLandscape) {
            // LANDSCAPE CARD
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
                verticalAlignment = Alignment.CenterVertically
            ) {

                CatImage(
                    cat = item,
                    isAnimated = false,
                    modifier = Modifier.size(96.dp)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {

                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {

                        Image(
                            painter = painterResource(R.drawable.fish_blue2_24),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )

                        Text(
                            text = formatLocalizedInteger(item.price),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    StatusButton(
                        canAfford = canAfford,
                        isOwned = isOwned,
                        buttonText = buttonText
                    )
                }
            }

        } else {
            // PORTRAIT CARD
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                CatImage(
                    cat = item,
                    isAnimated = false,
                    modifier = Modifier.size(96.dp)
                )

                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {

                    Image(
                        painter = painterResource(R.drawable.fish_blue2_24),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )

                    Text(
                        text = formatLocalizedInteger(item.price),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                StatusButton(
                    canAfford = canAfford,
                    isOwned = isOwned,
                    buttonText = buttonText
                )
            }
        }
    }
}