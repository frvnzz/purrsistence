package com.example.purrsistence.ui.components.shop

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.purrsistence.R
import com.example.purrsistence.domain.model.ShopItem
import com.example.purrsistence.ui.theme.Elevation
import com.example.purrsistence.ui.theme.Shapes
import com.example.purrsistence.ui.theme.Spacing

@Composable
fun ShopItemDialog(
    item: ShopItem,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = Shapes.cards,
        tonalElevation = Elevation.Lvl3,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,

        icon = {
            Icon(
                imageVector = Icons.Outlined.Pets,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },

        title = {
            Text(
                text = "Adopt this cat?",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },

        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                // Cat Shop Item Card (with price)
                Card(
                    shape = Shapes.cards,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = Elevation.Lvl2
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.lg),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Image(
                            painter = painterResource(item.imageRes),
                            contentDescription = item.name,
                            modifier = Modifier.size(96.dp)
                        )

                        Spacer(modifier = Modifier.height(Spacing.sm))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Image(
                                painter = painterResource(R.drawable.coin_64),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(Spacing.xs))

                            Text(
                                item.price.toString(),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }

                Text(
                    text = "${item.name} is ready to join your collection!",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        },

        // Confirm and Cancel Buttons
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                OutlinedButton(
                    onClick = onDismiss,
                    shape = Shapes.buttons
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Button(
                    onClick = onConfirm,
                    shape = Shapes.buttons
                ) {
                    Text(
                        text = "Adopt",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    )
}