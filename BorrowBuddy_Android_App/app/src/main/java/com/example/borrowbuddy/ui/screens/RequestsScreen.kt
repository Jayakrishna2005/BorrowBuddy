package com.example.borrowbuddy.ui.screens

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
                items(list) { booking ->
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
    api: BorrowBuddyApi,
    user: com.example.borrowbuddy.model.User?
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var showReviewDialog by remember { mutableStateOf(false) }

    if (showReviewDialog) {
        ReviewDialog(
            onDismiss = { showReviewDialog = false },
            onSubmit = { rating, comment ->
                coroutineScope.launch {
                    try {
                        val itemId = booking.item
                        val bookingId = booking.id
                        if (itemId != null && bookingId != null) {
                            api.createReview(com.example.borrowbuddy.network.ReviewRequest(
                                item = itemId,
                                reviewer = user?.id.toString(),
                                rating = rating,
                                comment = comment,
                                booking = bookingId
                            ))
                            android.widget.Toast.makeText(context, "Review submitted!", android.widget.Toast.LENGTH_SHORT).show()
                            showReviewDialog = false
                            
                            // Refresh the list to reflect "Review Submitted ✅"
                            val userId = user?.id?.toString()
                            if (userId != null) {
                                val response = api.getUserBookings(userId, userQueryId = userId)
                                // We need a way to update the parent state. 
                                // For now, the user can pull to refresh or navigate back.
                                // Actually, we'll just let the UI reflect the local state change if possible.
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        android.widget.Toast.makeText(context, "Failed to submit review", android.widget.Toast.LENGTH_SHORT).show()
                        showReviewDialog = false // Close anyway to avoid getting stuck
                    }
                }
            }
        )
    }

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
            } else if (booking.status == "COMPLETED" && booking.hasReview != true) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showReviewDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Review this Item")
                }
            } else if (booking.status == "COMPLETED" && booking.hasReview == true) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Review Submitted Successfully ✅", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ReviewDialog(onDismiss: () -> Unit, onSubmit: (Int, String) -> Unit) {
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate your experience") },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(5) { index ->
                        val starIndex = index + 1
                        IconButton(onClick = { rating = starIndex }) {
                            Icon(
                                imageVector = if (starIndex <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Rate $starIndex stars",
                                tint = if (starIndex <= rating) Color(0xFFFF9800) else Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    placeholder = { Text("How was the item? (Condition, etc.)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(rating, comment) }) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
