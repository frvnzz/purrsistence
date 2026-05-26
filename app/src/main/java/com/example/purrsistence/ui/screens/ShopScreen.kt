package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import com.example.purrsistence.domain.cats.CatList
import com.example.purrsistence.domain.model.ShopItem
import com.example.purrsistence.ui.components.CurrencyBadge
import com.example.purrsistence.ui.components.shop.ShopItemCard
import com.example.purrsistence.ui.components.shop.ShopItemDialog
import com.example.purrsistence.ui.state.TopBarState
import com.example.purrsistence.ui.theme.Spacing
import com.example.purrsistence.ui.viewmodel.UserViewModel

@Composable
fun ShopScreen(
    userViewModel: UserViewModel,
    setTopBar: (TopBarState) -> Unit
) {
    // Get current user data (balance, collected cats)
    val user by userViewModel.user.collectAsState()
    val balance = user?.balance ?: 0
    val collectedCats = user?.collectedCatsIds ?: emptyList()

    // Get list of all cats (+ details) available
    val shopItems = CatList.cats

    var selectedItem by remember {
        mutableStateOf<ShopItem?>(null)
    }

    setTopBar(
        TopBarState(
            title = "Cat Shop",
            actions = {
                CurrencyBadge(balance = balance)
            }
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { paneTitle = "Cat Shop Screen" }
            .padding(horizontal = Spacing.lg)
    ) {
        // Shop Grid that holds all ShopItemCards (cats)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
            contentPadding = PaddingValues(
                top = Spacing.lg,
                bottom = Spacing.lg
            )
        ) {

            items(
                items = shopItems,
                key = { it.id }
            ) { item ->
                // see if user already has a certain cat
                val isOwned = item.id in collectedCats

                ShopItemCard(
                    item = item,
                    balance = balance,
                    isOwned = isOwned,
                    onBuy = {
                        selectedItem = item
                    }
                )
            }
        }

        // Dialog if user wants to adopt a cat
        selectedItem?.let { item ->
            ShopItemDialog(
                item = item,
                onDismiss = {
                    selectedItem = null
                },
                onConfirm = {
                    userViewModel.buyCat(item.id, item.price)
                    selectedItem = null
                }
            )
        }
    }
}