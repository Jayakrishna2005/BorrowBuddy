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
    var showQuantityDialog by remember { mutableStateOf(false) }
    
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
                        onRequest = {
                            if ((item?.quantity ?: 1) > 1) {
                                requestQuantity = 1
                                showQuantityDialog = true
                            } else {
                                coroutineScope.launch {
                                    try {
                                        api.createBooking(BookingRequest(
                                            item = item!!.id.toString(),
                                            borrower = user!!.id.toString(),
                                            quantity = 1
                                        ))
                                        Toast.makeText(context, "Request Sent! 🚀", Toast.LENGTH_SHORT).show()
                                        navController.navigate("requests")
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Toast.makeText(context, "Request failed", Toast.LENGTH_SHORT).show()
                                    }
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
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.align(Alignment.TopStart).background(Color.White.copy(alpha = 0.8f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack, 
                                contentDescription = "Back",
                                tint = Color.Black
                            )
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





                    Spacer(modifier = Modifier.height(32.dp))

                    // Reviews Section
                    if (!item?.reviews.isNullOrEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("User Reviews", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            if (item?.reviewsCount ?: 0 > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) {
                                    Text(
                                        text = " ${item?.averageRating} ★ (${item?.reviewsCount ?: 0} ${if ((item?.reviewsCount ?: 0) == 1) "review" else "reviews"})",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

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

    if (showQuantityDialog && item != null) {
        AlertDialog(
            onDismissRequest = { showQuantityDialog = false },
            title = {
                Text(
                    text = "Select Quantity",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "How many of ${item!!.title} do you need?",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = { if (requestQuantity > 1) requestQuantity-- },
                            enabled = requestQuantity > 1
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease",
                                tint = if (requestQuantity > 1) Color(0xFF6C5CE7) else Color.Gray
                            )
                        }
                        Text(
                            text = requestQuantity.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        IconButton(
                            onClick = { if (requestQuantity < (item!!.quantity ?: 1)) requestQuantity++ },
                            enabled = requestQuantity < (item!!.quantity ?: 1)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase",
                                tint = if (requestQuantity < (item!!.quantity ?: 1)) Color(0xFF6C5CE7) else Color.Gray
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Available: ${item!!.quantity}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showQuantityDialog = false
                        val qty = requestQuantity
                        coroutineScope.launch {
                            try {
                                api.createBooking(BookingRequest(
                                    item = item!!.id.toString(),
                                    borrower = user!!.id.toString(),
                                    quantity = qty
                                ))
                                Toast.makeText(context, "Request Sent! 🚀", Toast.LENGTH_SHORT).show()
                                navController.navigate("requests")
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "Request failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C5CE7)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Confirm Request", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showQuantityDialog = false }
                ) {
                    Text("Cancel", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun ReviewItem(review: com.example.borrowbuddy.model.Review) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Reviewer Name & Profile Icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6C5CE7).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF6C5CE7),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = review.reviewerName ?: "Anonymous",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                        Text(
                            text = review.createdAt?.split("T")?.get(0) ?: "",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                // Rating Badges
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index < review.rating) Color(0xFFFFB400) else Color.LightGray.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Comment Text
            Text(
                text = "\"${review.comment}\"",
                fontSize = 14.sp,
                color = Color(0xFF2D3748),
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun StickyBottomActions(isAvailable: Boolean, onRequest: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 16.dp,
        color = Color.White
    ) {
        Box(
            modifier = Modifier.padding(20.dp).fillMaxWidth()
        ) {
            Button(
                onClick = onRequest,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C5CE7)),
                enabled = isAvailable
            ) {
                Text(if (isAvailable) "Request to Borrow" else "Borrowed", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
