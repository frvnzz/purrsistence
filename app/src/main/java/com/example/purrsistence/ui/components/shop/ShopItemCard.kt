package com.example.purrsistence.ui.components.shop

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.purrsistence.R
import com.example.purrsistence.domain.model.ShopItem
import com.example.purrsistence.ui.theme.Elevation
import com.example.purrsistence.ui.theme.Shapes
import com.example.purrsistence.ui.theme.Spacing

@Composable
fun ShopItemCard(
    item: ShopItem,
    balance: Int,
    isOwned: Boolean,
    onBuy: () -> Unit
) {
    val canAfford = balance >= item.price

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = Shapes.cards,
        elevation = CardDefaults.cardElevation(
            defaultElevation = Elevation.Lvl2
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {

            Image(
                painter = painterResource(item.imageRes),
                contentDescription = item.name,
                modifier = Modifier.size(96.dp)
            )

            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {

                Image(
                    painter = painterResource(R.drawable.coin_64),
                    contentDescription = "Currency",
                    modifier = Modifier.size(18.dp)
                )

                Text(
                    text = item.price.toString(),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Button(
                onClick = onBuy,
                enabled = canAfford && !isOwned,

                shape = Shapes.buttons,
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = Elevation.None // no elevation because button is on card
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                ),
                contentPadding = PaddingValues(vertical = Spacing.sm, horizontal = Spacing.xl)
            ) {

                Icon(
                    imageVector = when {
                        isOwned -> Icons.Outlined.Check
                        else -> Icons.Outlined.Sell
                    },
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(Spacing.sm))

                Text(
                    when {
                        isOwned -> "Owned"
                        !canAfford -> "No Funds"
                        else -> "Adopt"
                    }
                )
            }
        }
    }
}