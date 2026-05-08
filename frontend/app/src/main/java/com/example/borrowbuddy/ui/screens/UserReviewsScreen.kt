package com.example.borrowbuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.borrowbuddy.network.BorrowBuddyApi
import com.example.borrowbuddy.network.ReviewDTO
import com.example.borrowbuddy.util.SessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserReviewsScreen(navController: NavController) {
    val context = LocalContext.current
    val session = remember { SessionManager(context) }
    val user = session.getUser()
    val api = remember { BorrowBuddyApi.create() }
    
    var reviews by remember { mutableStateOf<List<ReviewDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(user) {
        if (user != null) {
            try {
                reviews = api.getUserReviews(user.id.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reviews for My Items", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (reviews.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("You haven't received any reviews yet.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(reviews) { review ->
                        OwnerReviewCard(review)
                    }
                }
            }
        }
    }
}

@Composable
fun OwnerReviewCard(review: ReviewDTO) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEF5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "On: ${review.itemTitle ?: "Deleted Item"}",
                        fontSize = 12.sp,
                        color = Color(0xFF6C5CE7),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "From: ${review.reviewerName ?: "Anonymous"}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${review.rating}", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 14.sp)
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(14.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = review.comment,
                fontSize = 15.sp,
                color = Color.DarkGray,
                lineHeight = 22.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = review.createdAt?.split("T")?.get(0) ?: "",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
