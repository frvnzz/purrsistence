package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.purrsistence.domain.cats.CatList
import com.example.purrsistence.ui.components.CurrencyBadge
import com.example.purrsistence.ui.components.ShopItemCard
import com.example.purrsistence.ui.components.TopBar
import com.example.purrsistence.ui.viewmodel.UserViewModel

@Composable
fun ShopScreen(
    userViewModel: UserViewModel
) {
    // Get current user data (balance, collected cats)
    val user by userViewModel.user.collectAsState()
    val balance = user?.balance ?: 0
    val collectedCats = user?.collectedCatsIds ?: emptyList()

    // Get list of all cats (+ details) available
    val shopItems = CatList.cats

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        TopBar(
            title = "Cat Shop",
            actions = {
                CurrencyBadge(balance = balance)
            }
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = shopItems,
                key = { it.id }
            ) { item ->

                val isOwned = item.id in collectedCats

                ShopItemCard(
                    item = item,
                    balance = balance,
                    isOwned = isOwned,
                    onBuy = {
                        userViewModel.buyCat(item.id, item.price)
                    }
                )
            }
        }
    }
}