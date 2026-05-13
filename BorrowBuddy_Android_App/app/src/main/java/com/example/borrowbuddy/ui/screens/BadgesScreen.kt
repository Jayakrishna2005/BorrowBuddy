package com.example.borrowbuddy.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.borrowbuddy.network.BorrowBuddyApi
import com.example.borrowbuddy.model.User
import com.example.borrowbuddy.util.SessionManager

@Composable
fun BadgesScreen(navController: NavController) {
    val context = LocalContext.current
    val session = remember { SessionManager(context) }
    val currentUser = session.getUser()
    val api = remember { BorrowBuddyApi.create() }

    var leaderboardUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) } 

    LaunchedEffect(Unit) {
        try {
            leaderboardUsers = api.getLeaderboard()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // App Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
            }
            Text("Game Center", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.Black)
        }

        // Tab Selector
        Box(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .height(54.dp)
                .background(Color(0xFFEEEEF5), RoundedCornerShape(16.dp))
                .padding(4.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                TabItem(
                    title = "My Level",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabItem(
                    title = "Leaderboard",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Crossfade(targetState = selectedTab, label = "") { tab ->
            when (tab) {
                0 -> BadgesContent(currentUser)
                1 -> LeaderboardContent(leaderboardUsers, currentUser, isLoading)
            }
        }
    }
}

@Composable
fun TabItem(title: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFF6C5CE7) else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else Color.Gray,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

@Composable
fun BadgesContent(currentUser: User?) {
    val scrollState = rememberScrollState()
    val points = currentUser?.points ?: 0
    val level = currentUser?.level ?: 1
    
    val levelName = when (level) {
        1 -> "Newbie Helper"
        2 -> "Active Helper"
        3 -> "Super Helper"
        4 -> "Campus Guardian"
        5 -> "Sharing Legend"
        else -> "Newbie"
    }

    val levelIcon = when (level) {
        1 -> Icons.Default.Park
        2 -> Icons.Default.VolunteerActivism
        3 -> Icons.Default.Shield
        4 -> Icons.Default.WorkspacePremium
        5 -> Icons.Default.EmojiEvents
        else -> Icons.Default.Park
    }

    val nextLevelPoints = when (level) {
        1 -> 100
        2 -> 500
        3 -> 1500
        4 -> 5000
        else -> points
    }
    
    val progress = if (level < 5) points.toFloat() / nextLevelPoints else 1f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        // Level Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(
                    brush = Brush.verticalGradient(listOf(Color(0xFF6C5CE7), Color(0xFFA855F7)))
                )
                .padding(32.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(100.dp).background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(levelIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(56.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Level $level", color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                Text(levelName, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Progress Bar
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("$points pts", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if (level < 5) {
                            Text("$nextLevelPoints pts", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Points Guide
        Text("How to earn points?", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        PointGuideItem("Lend an Item", "+50 pts", Icons.Default.Handshake)
        PointGuideItem("Get 5 Star Review", "+20 pts", Icons.Default.Star)
        PointGuideItem("Get Gratitude", "+10 pts", Icons.Default.Favorite)

        Spacer(modifier = Modifier.height(32.dp))

        // Badges Section
        Text("Achievements", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        val badges = listOf(
            BadgeData("First Help", Icons.Default.Celebration, Color(0xFFFF9800), points >= 50),
            BadgeData("Helper", Icons.Default.VolunteerActivism, Color(0xFFE91E63), level >= 2),
            BadgeData("Protector", Icons.Default.Shield, Color(0xFF6C5CE7), level >= 3),
            BadgeData("Guardian", Icons.Default.WorkspacePremium, Color(0xFF00D2FF), level >= 4),
            BadgeData("Legend", Icons.Default.EmojiEvents, Color(0xFFFFB400), level >= 5)
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            items(badges) { badge ->
                BadgeCardPremium(badge.title, badge.icon, if (badge.isUnlocked) badge.tint else Color.LightGray, badge.isUnlocked)
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun PointGuideItem(title: String, pts: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(44.dp).background(Color(0xFFF4F6FA), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color(0xFF6C5CE7), modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Text(pts, fontWeight = FontWeight.Bold, color = Color(0xFF00C9A7))
    }
}

@Composable
fun LeaderboardContent(users: List<User>, currentUser: User?, isLoading: Boolean) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text("Campus Ranking", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Text("Top contributors at SSE", fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF6C5CE7))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                itemsIndexed(users) { index, user ->
                    LeaderboardItemPremium(
                        rank = index + 1,
                        name = user.fullName,
                        points = user.points,
                        level = user.level,
                        isCurrentUser = user.id == currentUser?.id
                    )
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

data class BadgeData(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val tint: Color, val isUnlocked: Boolean)

@Composable
fun BadgeCardPremium(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, isUnlocked: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.alpha(if (isUnlocked) 1f else 0.5f)) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    brush = Brush.radialGradient(listOf(tint.copy(alpha = 0.2f), tint.copy(alpha = 0.05f))),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White, CircleShape)
                    .border(2.dp, if (isUnlocked) tint else Color.Transparent, CircleShape)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = tint, modifier = Modifier.size(32.dp))
            }
            if (!isUnlocked) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp).align(Alignment.BottomEnd).offset(x = (-4).dp, y = (-4).dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isUnlocked) Color.DarkGray else Color.Gray)
    }
}

@Composable
fun LeaderboardItemPremium(rank: Int, name: String, points: Int, level: Int, isCurrentUser: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (isCurrentUser) Color(0xFF6C5CE7).copy(alpha = 0.1f) else Color(0xFFF9FAFB))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = rank.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = when(rank) {
                1 -> Color(0xFFFFD700)
                2 -> Color(0xFFC0C0C0)
                3 -> Color(0xFFCD7F32)
                else -> Color.Gray
            },
            modifier = Modifier.width(32.dp)
        )

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(name.first().uppercase(), fontWeight = FontWeight.Bold, color = Color(0xFF6C5CE7))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = "Level $level", fontSize = 12.sp, color = Color.Gray)
        }

        Text(
            text = "$points pts",
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF6C5CE7)
        )
    }
}
