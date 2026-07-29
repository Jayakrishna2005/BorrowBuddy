package com.example.borrowbuddy.ui.navigation

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.borrowbuddy.ui.screens.*

import androidx.compose.animation.*
import androidx.compose.animation.core.tween

@Composable
fun BorrowBuddyApp() {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var totalUnread by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        val session = com.example.borrowbuddy.util.SessionManager(context)
        val api = com.example.borrowbuddy.network.BorrowBuddyApi.create()
        while(true) {
            try {
                val user = session.getUser()
                if (user != null) {
                    val response = api.getUserBookings(user.id.toString(), userQueryId = user.id.toString())
                    var unread = 0
                    response.sent?.forEach { unread += it.unreadCount ?: 0 }
                    response.received?.forEach { unread += it.unreadCount ?: 0 }
                    
                    if (unread > totalUnread && totalUnread != 0) {
                        android.widget.Toast.makeText(context, "New message received! 💬", android.widget.Toast.LENGTH_LONG).show()
                    }
                    totalUnread = unread
                }
            } catch (e: Exception) {}
            kotlinx.coroutines.delay(3000)
        }
    }

    Scaffold(
        modifier = Modifier.navigationBarsPadding(),
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            val bottomBarRoutes = listOf("home", "items", "add_item", "requests", "profile")

            if (currentRoute in bottomBarRoutes) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = currentRoute == "home",
                        onClick = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "items",
                        onClick = { navController.navigate("items") },
                        icon = { Icon(Icons.Filled.Search, contentDescription = "Browse") },
                        label = { Text("Browse") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "add_item",
                        onClick = { navController.navigate("add_item") },
                        icon = { Icon(Icons.Filled.AddCircle, contentDescription = "Add Item", tint = Color(0xFF6C5CE7)) },
                        label = { Text("Add") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "requests",
                        onClick = { navController.navigate("requests") },
                        icon = { 
                            androidx.compose.foundation.layout.Box {
                                Icon(Icons.Filled.Notifications, contentDescription = "Requests")
                                if (totalUnread > 0) {
                                    Surface(
                                        modifier = Modifier.padding(start = 12.dp, bottom = 12.dp),
                                        shape = androidx.compose.foundation.shape.CircleShape,
                                        color = Color.Red
                                    ) {
                                        Text(
                                            text = totalUnread.toString(),
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        },
                        label = { Text("Requests") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "profile",
                        onClick = { navController.navigate("profile") },
                        icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                        label = { Text("Profile") }
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
            composable("chat/{bookingId}") { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId")
                ChatScreen(navController, bookingId)
            }
            composable("badges") { BadgesScreen(navController) }
            composable("settings") { SettingsScreen(navController) }
            composable("onboarding") { OnboardingScreen(navController) }
            composable("thank_you") { ThankYouScreen(navController) }
            composable("my_reviews") { UserReviewsScreen(navController) }
        }
    }
}