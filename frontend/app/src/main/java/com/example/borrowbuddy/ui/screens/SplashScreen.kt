package com.example.borrowbuddy.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.borrowbuddy.util.SessionManager
import com.example.borow_buddy_frontend.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.8f) }
    val entranceOffset = remember { Animatable(50f) }

    // Continuous floating animation
    val infiniteTransition = rememberInfiniteTransition()
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(key1 = true) {
        // Entrance animation
        launch {
            alphaAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000, easing = EaseInOutQuart)
            )
        }
        launch {
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000, easing = EaseOutBack)
            )
        }
        launch {
            entranceOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic)
            )
        }
        
        delay(800) // Fast load!
        
        val session = SessionManager(context)
        val user = session.getUser()
        if (user != null) {
            navController.navigate("home") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            navController.navigate("onboarding")
            {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color(0xFF0A0F2C), Color(0xFF020617))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset(y = entranceOffset.value.dp)
                .alpha(alphaAnim.value)
                .scale(scaleAnim.value)
        ) {
            // Logo matching the web version
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "BorrowBuddy Logo",
                modifier = Modifier
                    .size(320.dp)
                    .offset(y = floatOffset.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(40.dp))
            )
        }

        // Bottom Branding matching web version
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .alpha(alphaAnim.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color(0xFF00D1B2),
                strokeWidth = 2.dp,
                trackColor = Color(0x1AFFFFFF)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "POWERED BY BORROWBUDDY",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0x80FFFFFF),
                letterSpacing = 3.sp
            )
        }
    }
}
