package com.example.borrowbuddy.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun OnboardingScreen(navController: NavController) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(animationSpec = tween(1000))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = buildAnnotatedString {
                        append("Share Today,\n")
                        withStyle(style = SpanStyle(color = Color(0xFF6C5CE7))) {
                            append("Help Forever 💜")
                        }
                    },
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 40.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Join your campus community and make a difference.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Illustration Area (Placeholder for the illustration in Image 1)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            // We can use a simple vector or icon to represent the sharing illustration
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = Color(0xFF6C5CE7).copy(alpha = 0.2f)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Features List
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            OnboardingFeature(
                icon = Icons.Default.LocalLibrary,
                title = "Borrow Anything",
                description = "From books to gadgets",
                color = Color(0xFF6C5CE7),
                delay = 200,
                visible = visible
            )
            OnboardingFeature(
                icon = Icons.Default.VerifiedUser,
                title = "Verified Community",
                description = "Only students, always safe",
                color = Color(0xFF4CAF50),
                delay = 400,
                visible = visible
            )
            OnboardingFeature(
                icon = Icons.Default.AutoAwesome,
                title = "Earn Rewards",
                description = "Help others and level up",
                color = Color(0xFFFF9800),
                delay = 600,
                visible = visible
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Buttons
        Button(
            onClick = { navController.navigate("login") },
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
                Text("Get Started", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("login") }) {
            Text(
                text = buildAnnotatedString {
                    append("Already have an account? ")
                    withStyle(style = SpanStyle(color = Color(0xFF6C5CE7), fontWeight = FontWeight.Bold)) {
                        append("Login")
                    }
                },
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun OnboardingFeature(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color,
    delay: Int,
    visible: Boolean
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(1000, delayMillis = delay)) + slideInHorizontally(animationSpec = tween(1000, delayMillis = delay))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(description, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}
