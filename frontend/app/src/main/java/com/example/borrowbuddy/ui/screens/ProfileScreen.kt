package com.example.borrowbuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.example.borrowbuddy.util.SessionManager
import com.example.borrowbuddy.network.BorrowBuddyApi
import com.example.borrowbuddy.model.User
import kotlinx.coroutines.launch

import coil.compose.AsyncImage

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val session = remember { SessionManager(context) }
    
    // Use a state for the user to reflect updates
    var userState by remember { mutableStateOf(session.getUser()) }
    var isLoading by remember { mutableStateOf(true) }
    
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        val current = session.getUser()
        if (current != null) {
            try {
                val api = BorrowBuddyApi.create()
                val freshUser = api.getProfile(current.id.toString())
                // Update local session
                session.saveUser(freshUser)
                userState = freshUser
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Profile Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E7FF)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!userState?.profilePhoto.isNullOrBlank()) {
                        val photoUrl = "${session.getBaseUrl().removeSuffix("/")}${userState?.profilePhoto}"
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Profile Pic",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = "Profile Pic", modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(userState?.fullName ?: "Guest", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFF6C5CE7),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Lvl ${userState?.level ?: 1}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        if (!userState?.badge.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Transparent
                            ) {
                                Box(modifier = Modifier.background(androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFEA580C))))) {
                                    Text(
                                        text = userState?.badge ?: "Novice",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                    Text(userState?.email ?: "No Email", fontSize = 14.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Stats Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard("Shared", userState?.itemsLent?.toString() ?: "0", Modifier.weight(1f))
                StatCard("Borrowed", userState?.itemsBorrowed?.toString() ?: "0", Modifier.weight(1f))
                StatCard("Sentiment Score", "${userState?.sellerSentiment ?: 100}%", Modifier.weight(1f))
                StatCard("Points", ((userState?.points ?: 50) - 50).coerceAtLeast(0).toString(), Modifier.weight(1f), Color(0xFFF59E0B))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Badge Progress Section
            BadgeProgressSection(userState)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Menu List
        Text("Menu", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(16.dp))
        
        ProfileMenuItem("My Listings", Icons.Default.List) { navController.navigate("home") }
        ProfileMenuItem("My Requests", Icons.Default.Notifications) { navController.navigate("requests") }
        ProfileMenuItem("Badges & Leaderboard", Icons.Default.EmojiEvents) { navController.navigate("badges") }
        ProfileMenuItem("Settings", Icons.Default.Settings) { navController.navigate("settings") }
        
        Spacer(modifier = Modifier.height(32.dp))

        // Logout Button
        Button(
            onClick = {
                session.clearSession()
                navController.navigate("login") {
                    popUpTo(0)
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Logout", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    Card(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ProfileMenuItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(Color(0xFFF4F6FA), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), color = Color.Black)
        Icon(Icons.Default.ChevronRight, contentDescription = "Arrow", tint = Color.Gray)
    }
}

@Composable
fun BadgeProgressSection(user: User?) {
    val currentPoints = user?.points ?: 0
    val currentLevel = user?.level ?: 1
    val currentShared = user?.itemsLent ?: 0
    val currentBorrowed = user?.itemsBorrowed ?: 0

    var pointsTarget = 100
    var sharedTarget = 1
    var borrowedTarget = 1
    var nextBadgeName = "Helper"

    if (currentLevel == 2) {
        pointsTarget = 500
        sharedTarget = 3
        borrowedTarget = 3
        nextBadgeName = "Rising Star"
    } else if (currentLevel == 3) {
        pointsTarget = 1500
        sharedTarget = 8
        borrowedTarget = 8
        nextBadgeName = "Community Hero"
    } else if (currentLevel == 4) {
        pointsTarget = 5000
        sharedTarget = 15
        borrowedTarget = 15
        nextBadgeName = "Legend"
    } else if (currentLevel >= 5) {
        pointsTarget = 5000
        sharedTarget = 15
        borrowedTarget = 15
        nextBadgeName = "Max Level"
    }

    val currentPointsDisplay = (currentPoints - 50).coerceAtLeast(0)
    val pointsTargetDisplay = (pointsTarget - 50).coerceAtLeast(1)

    val pointsPct = if (currentLevel >= 5) 1f else (currentPointsDisplay.toFloat() / pointsTargetDisplay).coerceIn(0f, 1f)
    val sharedPct = if (currentLevel >= 5) 1f else (currentShared.toFloat() / sharedTarget).coerceIn(0f, 1f)
    val borrowedPct = if (currentLevel >= 5) 1f else (currentBorrowed.toFloat() / borrowedTarget).coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Active Badge", fontSize = 12.sp, color = Color.Gray)
                    Text("${user?.badge ?: "Novice"} 🏅", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                }
                if (currentLevel < 5) {
                    Text(
                        text = "Next: $nextBadgeName (Lvl ${currentLevel + 1})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                } else {
                    Text("Legendary Status Reached 🎉", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Points Progress
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Points / Score", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("$currentPointsDisplay / $pointsTargetDisplay pts", fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { pointsPct },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = Color(0xFF6C5CE7),
                    trackColor = Color(0xFFE5E7EB)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Shared Progress
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Items Shared (Lent)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("$currentShared / $sharedTarget items", fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { sharedPct },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = Color(0xFF10B981),
                    trackColor = Color(0xFFE5E7EB)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Borrowed Progress
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Items Borrowed", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("$currentBorrowed / $borrowedTarget items", fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { borrowedPct },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = Color(0xFF3B82F6),
                    trackColor = Color(0xFFE5E7EB)
                )
            }
        }
    }
}