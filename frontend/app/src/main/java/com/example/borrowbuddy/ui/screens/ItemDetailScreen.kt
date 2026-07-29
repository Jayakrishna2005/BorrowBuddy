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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.borrowbuddy.util.SessionManager
import com.example.borrowbuddy.network.BorrowBuddyApi
import com.example.borrowbuddy.network.BookingRequest
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@Composable
fun ItemDetailScreen(navController: NavController, itemId: String?) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val session = remember { SessionManager(context) }
    val user = session.getUser()
    
    var item by remember { mutableStateOf<com.example.borrowbuddy.model.Item?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var requestQuantity by remember { mutableStateOf(1) }
    
    val api = remember { BorrowBuddyApi.create() }

    LaunchedEffect(itemId) {
        if (itemId != null) {
            try {
                item = api.getItem(itemId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    val scrollState = rememberScrollState()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF6C5CE7))
        }
    } else if (item == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Item not found")
        }
    } else {
        Scaffold(
            bottomBar = {
                val isOwner = item?.ownerId == user?.id?.toString()
                if (!isOwner && item != null) {
                    StickyBottomActions(
                        isAvailable = item?.isAvailable == true,
                        onChat = {
                            coroutineScope.launch {
                                try {
                                    val response = api.createBooking(BookingRequest(
                                        item = item!!.id.toString(),
                                        borrower = user!!.id.toString(),
                                        quantity = requestQuantity
                                    ))
                                    navController.navigate("chat/${response.id}")
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Opening chat...", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onRequest = {
                            coroutineScope.launch {
                                try {
                                    val response = api.createBooking(BookingRequest(
                                        item = item!!.id.toString(),
                                        borrower = user!!.id.toString(),
                                        quantity = requestQuantity
                                    ))
                                    Toast.makeText(context, "Request Sent! 🚀", Toast.LENGTH_SHORT).show()
                                    navController.navigate("chat/${response.id}")
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(context, "Request failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F9FE))
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
            ) {
                // Immersive Image Header
                Box(modifier = Modifier.fillMaxWidth().height(350.dp)) {
                    val imageUrl = if (!item?.image.isNullOrBlank()) {
                        "${session.getBaseUrl().removeSuffix("/")}${item?.image}"
                    } else null

                    if (imageUrl != null) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE0E7FF)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        }
                    }

                    // Top Actions
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.background(Color.White.copy(alpha = 0.8f), CircleShape)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                        IconButton(
                            onClick = { },
                            modifier = Modifier.background(Color.White.copy(alpha = 0.8f), CircleShape)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                    }
                }

                Column(modifier = Modifier.padding(24.dp)) {
                    // Title and Price simulation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (item?.isAvailable == true) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (item?.isAvailable == true) "Available" else "Borrowed",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = if (item?.isAvailable == true) Color(0xFF2E7D32) else Color(0xFFC62828),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = item?.title ?: "",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A1A1A)
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Posted by: ${item?.ownerName ?: "Unknown"}",
                        fontSize = 15.sp,
                        color = Color(0xFF6C5CE7),
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    Spacer(modifier = Modifier.height(16.dp))

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Show actions only if NOT the owner
                    val isOwner = item?.ownerId == user?.id?.toString()
                    
                    if (!isOwner) {
                        // Already handled in bottomBar, but we could add more here
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Seller Card
                    Text("Seller Information", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEF5))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(50.dp).background(Color(0xFF6C5CE7).copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF6C5CE7))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item?.ownerName ?: "Unknown User", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Excellent Experience", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text("Description", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = item?.description ?: "No description provided.",
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        color = Color(0xFF4A4A4A)
                    )

                    val hasQuantity = (item?.quantity ?: 1) > 1
                    val isCurrentlyAvailable = item?.isAvailable == true
                    if (hasQuantity && !isOwner && isCurrentlyAvailable) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text("Select Quantity", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEF5))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Available: ${item?.quantity}", color = Color.Gray, fontSize = 14.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { if (requestQuantity > 1) requestQuantity-- },
                                        enabled = requestQuantity > 1
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color(0xFF6C5CE7))
                                    }
                                    Text(
                                        text = requestQuantity.toString(),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                    IconButton(
                                        onClick = { if (requestQuantity < (item?.quantity ?: 1)) requestQuantity++ },
                                        enabled = requestQuantity < (item?.quantity ?: 1)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color(0xFF6C5CE7))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Reviews Section
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("User Reviews", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        if (item?.reviewsCount ?: 0 > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) {
                                Text(
                                    " ${item?.averageRating} ★",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    if (item?.reviews.isNullOrEmpty()) {
                        Text("No reviews yet. Be the first to borrow!", color = Color.Gray, fontSize = 14.sp)
                    } else {
                        item?.reviews?.forEach { review ->
                            ReviewItem(review)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    if (item?.isAvailable == false) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Card(
                            modifier = Modifier.clickable {
                                navController.navigate("items")
                            }.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1))
                        ) {
                            Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF009688))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("This item is currently borrowed.", fontWeight = FontWeight.Bold)
                                Text("Click here to browse similar available items", fontSize = 12.sp, color = Color.DarkGray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(100.dp)) // Extra space for bottom bar
                }
            }
        }
    }
}

@Composable
fun ReviewItem(review: com.example.borrowbuddy.model.Review) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEF5))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { index ->
                    Icon(
                        imageVector = if (index < review.rating) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = if (index < review.rating) Color(0xFFFF9800) else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = review.createdAt?.split("T")?.get(0) ?: "",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = review.comment,
                fontSize = 14.sp,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun StickyBottomActions(isAvailable: Boolean, onChat: () -> Unit, onRequest: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 16.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onChat,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF6C5CE7))
            ) {
                Text("Chat", color = Color(0xFF6C5CE7), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Button(
                onClick = onRequest,
                modifier = Modifier.weight(1.5f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C5CE7)),
                enabled = isAvailable
            ) {
                Text(if (isAvailable) "Request to Borrow" else "Borrowed", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
