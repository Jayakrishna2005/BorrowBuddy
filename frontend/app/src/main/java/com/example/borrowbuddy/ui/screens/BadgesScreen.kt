package com.example.borrowbuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun BadgesScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Badges Section
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Your Badges", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                item { BadgeCard("First Help", Icons.Default.MilitaryTech, Color(0xFFFF9800)) }
                item { BadgeCard("10 Helps", Icons.Default.WorkspacePremium, Color(0xFF4CAF50)) }
                item { BadgeCard("50 Helps", Icons.Default.EmojiEvents, Color(0xFFE91E63)) }
            }
        }

        // Leaderboard
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(24.dp)
        ) {
            Column {
                Text("Leaderboard", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { LeaderboardItem(rank = 1, name = "Alex Johnson", points = "1500 pts", isHighlighted = true) }
                    item { LeaderboardItem(rank = 2, name = "Jane Doe", points = "1200 pts", isHighlighted = true) }
                    item { LeaderboardItem(rank = 3, name = "Sam Smith", points = "950 pts", isHighlighted = true) }
                    item { LeaderboardItem(rank = 4, name = "Mark Lee", points = "800 pts", isHighlighted = false) }
                    item { LeaderboardItem(rank = 5, name = "Emily Chen", points = "720 pts", isHighlighted = false) }
                }
            }
        }
    }
}

@Composable
fun BadgeCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color) {
    Card(
        modifier = Modifier.size(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(tint.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = tint, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun LeaderboardItem(rank: Int, name: String, points: String, isHighlighted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isHighlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#$rank",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (isHighlighted) MaterialTheme.colorScheme.primary else Color.Gray,
            modifier = Modifier.width(32.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(name, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Text(points, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
    }
}
