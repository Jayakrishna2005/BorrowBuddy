package com.example.borrowbuddy.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.example.borrowbuddy.util.SessionManager
import com.example.borrowbuddy.network.BorrowBuddyApi
import com.example.borrowbuddy.network.BookingDTO
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.compose.foundation.lazy.items
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RequestsScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val session = remember { SessionManager(context) }
    val user = session.getUser()
    
    val api = remember { BorrowBuddyApi.create() }

    var receivedRequests by remember { mutableStateOf<List<BookingDTO>>(emptyList()) }
    var sentRequests by remember { mutableStateOf<List<BookingDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var showReviewDialogBooking by remember { mutableStateOf<BookingDTO?>(null) }

    fun refreshData() {
        if (user == null) {
            isLoading = false
            return
        }
        isRefreshing = true
        coroutineScope.launch {
            try {
                val response = api.getUserBookings(user.id.toString(), userQueryId = user.id.toString())
                
                val statusOrder = mapOf(
                    "APPROVED" to 1,
                    "PENDING" to 2,
                    "REJECTED" to 3,
                    "COMPLETED" to 4
                )
                val sortFunc = { list: List<BookingDTO> ->
                    list.sortedBy { statusOrder[it.status] ?: 99 }
                }
                
                receivedRequests = sortFunc(response.received ?: emptyList())
                sentRequests = sortFunc(response.sent ?: emptyList())
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isRefreshing = false
                isLoading = false
            }
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { refreshData() }
    )

    LaunchedEffect(user) {
        refreshData()
    }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Received", "Sent")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                val hasNew = if (index == 0) receivedRequests.any { (it.unreadCount ?: 0) > 0 }
                             else sentRequests.any { (it.unreadCount ?: 0) > 0 }
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(title, fontWeight = FontWeight.Bold)
                            if (hasNew) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(color = Color.Red, shape = RoundedCornerShape(12.dp)) {
                                    Text("New", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val baseUrl = session.getBaseUrl()
            Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                val list = if (selectedTab == 0) receivedRequests else sentRequests
                items(list, key = { it.id ?: "" }) { booking ->
                    if (selectedTab == 0) {
                        ReceivedRequestCard(booking, baseUrl) { newStatus ->
                            if (newStatus == "CHAT") {
                                booking.id?.let { id ->
                                    try {
                                        navController.navigate("chat/$id")
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            } else {
                                coroutineScope.launch {
                                    try {
                                        val bookingId = booking.id
                                        if (bookingId != null) {
                                            val statusUpdate = com.example.borrowbuddy.network.StatusUpdate(newStatus)
                                            api.updateBookingStatus(bookingId, statusUpdate)
                                            
                                            if (newStatus == "APPROVED") {
                                                navController.navigate("chat/$bookingId")
                                            } else if (newStatus == "COMPLETED") {
                                                val userId = user?.id?.toString()
                                                if (userId != null) {
                                                    try {
                                                        val freshUser = api.getProfile(userId)
                                                        session.saveUser(freshUser)
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                    }
                                                }
                                                navController.navigate("thank_you")
                                            } else {
                                                // Refresh only if needed
                                                val userId = user?.id?.toString()
                                                if (userId != null) {
                                                    val response = api.getUserBookings(userId, userQueryId = userId)
                                                    receivedRequests = response.received ?: emptyList()
                                                    sentRequests = response.sent ?: emptyList()
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }
                    } else {
                        SentRequestCard(booking, baseUrl,
                            onChat = { 
                                booking.id?.let { id ->
                                    navController.navigate("chat/$id")
                                }
                            },
                            onReviewClick = { showReviewDialogBooking = booking },
                            api = api,
                            user = user
                        )
                    }
                }
            }
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }

    if (showReviewDialogBooking != null) {
        val booking = showReviewDialogBooking!!
        var rating by remember { mutableStateOf(5) }
        var comment by remember { mutableStateOf("") }
        var isSubmitting by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { if (!isSubmitting) showReviewDialogBooking = null },
            title = { Text("Review Item", color = Color.Black, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "How was your experience borrowing ${booking.itemName}?",
                        color = Color.DarkGray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Star Rating selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (star in 1..5) {
                            IconButton(onClick = { rating = star }) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "$star stars",
                                    tint = if (star <= rating) Color(0xFFFFB400) else Color.LightGray.copy(alpha = 0.5f),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        placeholder = { Text("Write a comment about the item or owner...") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (comment.isBlank()) {
                            Toast.makeText(context, "Please write a comment", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSubmitting = true
                        coroutineScope.launch {
                            try {
                                val reviewRequest = com.example.borrowbuddy.network.ReviewRequest(
                                    item = booking.item ?: "",
                                    reviewer = user?.id?.toString() ?: "",
                                    rating = rating,
                                    comment = comment.trim(),
                                    booking = booking.id ?: ""
                                )
                                api.createReview(reviewRequest)
                                Toast.makeText(context, "Review submitted successfully!", Toast.LENGTH_SHORT).show()
                                showReviewDialogBooking = null
                                refreshData()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "Failed to submit review: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isSubmitting = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C5CE7)),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Confirm Request")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showReviewDialogBooking = null },
                    enabled = !isSubmitting
                ) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
}

@Composable
fun ReceivedRequestCard(booking: BookingDTO, baseUrl: String, onUpdate: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Item Image
                val imageUrl = if (!booking.itemImage.isNullOrBlank()) {
                    val path = booking.itemImage
                    if (path.startsWith("http")) path 
                    else "${baseUrl.removeSuffix("/")}${if (path.startsWith("/")) "" else "/"}$path"
                } else null

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF3F4F6)),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUrl != null) {
                        AsyncImage(
                            model = coil.request.ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_menu_report_image)
                                .build(),
                            contentDescription = booking.itemName ?: "Item",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(booking.itemName?.ifBlank { "Unknown Item" } ?: "Unknown Item", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    val bName = booking.borrowerName?.ifBlank { null } ?: booking.borrower?.take(8) ?: "N/A"
                    Text("From: $bName", fontSize = 12.sp, color = Color.Gray)
                    
                    if (booking.dueDate != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Due: ${booking.dueDate.take(10)}", fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
                    }
                    if ((booking.penaltyAmount ?: 0) > 0) {
                        Text("Penalty: ${booking.penaltyAmount} units", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    val statusText = booking.status ?: "PENDING"
                    val statusColor = when (statusText) {
                        "PENDING" -> Color(0xFFFF9800)
                        "APPROVED" -> Color(0xFF4CAF50)
                        "REJECTED" -> Color.Red
                        else -> Color.Gray
                    }
                    Text(statusText, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    
                    if ((booking.unreadCount ?: 0) > 0) {
                        Surface(
                            color = Color.Red,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "New Message",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (booking.status == "PENDING") {
                    TextButton(onClick = { onUpdate("REJECTED") }) {
                        Text("Reject", color = Color.Red)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onUpdate("APPROVED") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Accept")
                    }
                } else if (booking.status == "APPROVED") {
                    Button(
                        onClick = { onUpdate("CHAT") },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Chat with Borrower")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onUpdate("COMPLETED") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C5CE7)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Mark Returned")
                    }
                } else if (booking.status == "COMPLETED") {
                    Text("Transaction Completed", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SentRequestCard(
    booking: BookingDTO, 
    baseUrl: String,
    onChat: () -> Unit, 
    onReviewClick: () -> Unit,
    api: BorrowBuddyApi,
    user: com.example.borrowbuddy.model.User?
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current




    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Item Image
                val imageUrl = if (!booking.itemImage.isNullOrBlank()) {
                    val path = booking.itemImage
                    if (path.startsWith("http")) path 
                    else "${baseUrl.removeSuffix("/")}${if (path.startsWith("/")) "" else "/"}$path"
                } else null

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF3F4F6)),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUrl != null) {
                        AsyncImage(
                            model = coil.request.ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_menu_report_image)
                                .build(),
                            contentDescription = booking.itemName ?: "Item",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(booking.itemName?.ifBlank { "Unknown Item" } ?: "Unknown Item", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Owner: ${booking.ownerName?.ifBlank { "Unknown" } ?: "Unknown"}", fontSize = 12.sp, color = Color.Gray)
                    
                    if (booking.dueDate != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Due: ${booking.dueDate.take(10)}", fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
                    }
                    if ((booking.penaltyAmount ?: 0) > 0) {
                        Text("Penalty: ${booking.penaltyAmount} units", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    val statusText = booking.status ?: "PENDING"
                    val statusColor = when (statusText) {
                        "PENDING" -> Color(0xFFFF9800)
                        "APPROVED" -> Color(0xFF4CAF50)
                        "REJECTED" -> Color.Red
                        "COMPLETED" -> Color(0xFF6C5CE7)
                        else -> Color.Gray
                    }
                    Text(statusText, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    
                    if ((booking.unreadCount ?: 0) > 0) {
                        Surface(
                            color = Color.Red,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "New Message",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            if (booking.status == "APPROVED") {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Your request was accepted! Contact owner.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onChat,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chat with Owner")
                }
            } else if (booking.status == "COMPLETED") {
                Spacer(modifier = Modifier.height(16.dp))
                if (booking.hasReview == true) {
                    Text(
                        text = "✓ Reviewed",
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                } else {
                    Button(
                        onClick = onReviewClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Review Item", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


