package com.example.borrowbuddy.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500)
        )
        delay(1000)
        navController.navigate("login") {
            popUpTo("splash") { inclusive = true }
        }
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF352B75), Color.Black)
    )

    val iconGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF6C5CE7), Color(0xFF03A9F4))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alphaAnim.value)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                // To simulate icon gradient, we draw the icon. (Applying gradient to icon tint requires Modifier.graphicsLayer in deeper composables)
                // For direct use:
                Icon(
                    imageVector = Icons.Filled.Handshake,
                    contentDescription = "Logo",
                    tint = Color(0xFF6C5CE7),
                    modifier = Modifier.size(64.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "BorrowBuddy",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Share. Help. Connect.",
                color = Color(0xFFA855F7), // Light Purple
                fontSize = 16.sp
            )
        }
    }
}
