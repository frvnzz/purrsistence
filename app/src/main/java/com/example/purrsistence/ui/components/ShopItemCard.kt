package com.example.purrsistence.ui.components

import androidx.compose.foundation.Image
import com.example.purrsistence.R
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.purrsistence.domain.model.ShopItem

@Composable
fun ShopItemCard(
    item: ShopItem,
    balance: Int,
    isOwned: Boolean,
    onBuy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Cat Image
                Image(
                    painter = painterResource(id = item.imageRes),
                    contentDescription = item.name,
                    modifier = Modifier.size(100.dp)
                )

                // Cat Name
                Text(item.name, style = MaterialTheme.typography.titleSmall)

                // Price
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.coin_64),
                        contentDescription = "Currency",
                        modifier = Modifier.size(16.dp)
                    )

                    Text(
                        item.price.toString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            // Adopt Button
            Button(
                onClick = onBuy,
                enabled = balance >= item.price && !isOwned,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(
                    when {
                        isOwned -> "Owned"
                        balance < item.price -> "No Funds"
                        else -> "Adopt"
                    }
                )
            }
        }
    }
}