package com.example.purrsistence.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.DoDisturb
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavBar(navController: NavController) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Determine the current route to know which tab is selected
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    // list of all possible tab routes from AppNavHost
    // TODO: use single source of truth for routes (screen model) in the future
    val items = listOf("statistics", "goals", "home", "shop", "profile")

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        modifier = if (isLandscape) Modifier.height(80.dp) else Modifier,
        windowInsets = if (isLandscape) WindowInsets(0, 0, 0, 46) else NavigationBarDefaults.windowInsets
    ) {
        items.forEach { screen ->
            val isSelected = currentRoute == screen

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(screen) {
                        // make sure that only one tab is selected at a time, and back navigates to home (startDestination)
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    // pill highlight for selected tab
                    indicatorColor = MaterialTheme.colorScheme.tertiary
                ),

                icon = {
                    Icon(
                        imageVector = when (screen) {
                            "statistics" -> if (isSelected) Icons.Filled.Assessment else Icons.Outlined.Assessment
                            "goals" -> if (isSelected) Icons.Filled.Checklist else Icons.Outlined.Checklist
                            "home" -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
                            "shop" -> if (isSelected) Icons.Filled.Pets else Icons.Outlined.Pets
                            "profile" -> if (isSelected) Icons.Filled.AccountBox else Icons.Outlined.AccountBox
                            else -> Icons.Filled.DoDisturb
                        },
                        contentDescription = if (screen == "shop") "Shelter" else screen
                    )
                },

                label = {
                    Text(if (screen == "shop") "Shelter" else screen.replaceFirstChar { it.uppercase() })
                }
            )
        }
    }
}