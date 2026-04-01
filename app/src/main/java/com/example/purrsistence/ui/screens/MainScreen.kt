package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.purrsistence.ui.components.BottomNavBar
import com.example.purrsistence.ui.navigation.AppNavHost

@Composable
fun MainScreen() {

    val navController = rememberNavController()

    // This is the main scaffold for the app -> shows the BottomNavBar on the selected Screen
    Scaffold(
        bottomBar = {
            BottomNavBar(navController)
        }
    ) { padding ->

        Box(modifier = Modifier.padding(padding)) {
            AppNavHost(navController)
        }
    }
}