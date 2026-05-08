package com.example.borrowbuddy.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun ThankYouScreen(navController: NavController) {
    val infiniteTransition = rememberInfiniteTransition()
    val heartScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Confetti / Particles Simulation (Static for now, could be animated dots)
        Box(contentAlignment = Alignment.Center) {
            // Heart Circle
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(Color(0xFF6C5CE7).copy(alpha = 0.05f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(Color(0xFF6C5CE7).copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color(0xFF6C5CE7),
                        modifier = Modifier.size(64.dp).scale(heartScale)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "You're Awesome! 💜",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "You returned the item.\nIt's now available on the homepage for others to borrow again!",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScoreCard(
                label = "Trust Score",
                value = "+10",
                color = Color(0xFF6C5CE7),
                modifier = Modifier.weight(1f)
            )
            ScoreCard(
                label = "Helper Points",
                value = "+20",
                color = Color(0xFF00D2FF),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(64.dp))

        Button(
            onClick = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("Keep Helping", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("home") { popUpTo("home") { inclusive = true } } }) {
            Text("Back to Home", color = Color.Gray, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ScoreCard(label: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FE)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
        }
    }
}
