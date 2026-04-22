package com.example.borrowbuddy.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.borrowbuddy.ui.screens.*

@Composable
fun BorrowBuddyApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            val bottomBarRoutes = listOf("home", "items", "add_item", "requests", "profile")

            if (currentRoute in bottomBarRoutes) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    NavigationBarItem(
                        selected = currentRoute == "home",
                        onClick = { navController.navigate("home") },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == "items",
                        onClick = { navController.navigate("items") },
                        icon = { Icon(Icons.Filled.Search, contentDescription = "Browse") },
                        label = { Text("Browse") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == "add_item",
                        onClick = { navController.navigate("add_item") },
                        icon = { Icon(Icons.Filled.AddCircle, contentDescription = "Add Item", tint = MaterialTheme.colorScheme.tertiary) },
                        label = { Text("Add") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.tertiary,
                            selectedTextColor = MaterialTheme.colorScheme.tertiary,
                            indicatorColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == "requests",
                        onClick = { navController.navigate("requests") },
                        icon = { Icon(Icons.Filled.Notifications, contentDescription = "Requests") },
                        label = { Text("Requests") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == "profile",
                        onClick = { navController.navigate("profile") },
                        icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("splash") { SplashScreen(navController) }
            composable("login") { LoginScreen(navController) }
            composable("home") { HomeScreen(navController) }
            composable("items") { BrowseItemsScreen(navController) }
            composable("add_item") { AddItemScreen(navController) }
            composable("requests") { RequestsScreen(navController) }
            composable("profile") { ProfileScreen(navController) }
            composable("item_detail/{itemId}") { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId")
                ItemDetailScreen(navController, itemId)
            }
            composable("chat") { ChatScreen(navController) }
            composable("badges") { BadgesScreen(navController) }
        }
    }
}