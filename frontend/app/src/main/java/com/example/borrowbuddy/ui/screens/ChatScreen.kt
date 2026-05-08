package com.example.borrowbuddy.ui.screens

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.borrowbuddy.network.BorrowBuddyApi
import com.example.borrowbuddy.network.MessageDTO
import com.example.borrowbuddy.network.ChatWebSocketManager
import com.example.borrowbuddy.util.SessionManager
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import org.json.JSONObject
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController, bookingId: String?) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val session = remember { SessionManager(context) }
    val user = session.getUser()
    val api = remember { BorrowBuddyApi.create() }

    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<MessageDTO>>(emptyList()) }
    var bookingInfo by remember { mutableStateOf<com.example.borrowbuddy.network.BookingDTO?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val quickReplies = listOf("Is this available?", "Where can I pick up?", "Thanks!", "I'm on my way")

    val listState = rememberLazyListState()
    var isConnected by remember { mutableStateOf(false) }

    val wsManager = remember { ChatWebSocketManager(session.getBaseUrl(), bookingId ?: "") }

    // Initial data fetch
    LaunchedEffect(bookingId) {
        if (bookingId != null) {
            try {
                val booking = api.getBooking(bookingId)
                bookingInfo = booking
                
                if (booking.status == "APPROVED" || booking.status == "COMPLETED") {
                    val initialMessages = api.getMessages(bookingId, userId = user?.id?.toString())
                    messages = initialMessages
                    isLoading = false
                    if (initialMessages.isNotEmpty()) {
                        listState.scrollToItem(initialMessages.size - 1)
                    }
                } else {
                    isLoading = false
                }
            } catch (e: Exception) { 
                e.printStackTrace()
                isLoading = false
            }
        }
    }

    // Connect immediately to improve perceived speed
    DisposableEffect(bookingId) {
        if (bookingId != null) {
            wsManager.connect(
                onMessageReceived = { json ->
                    coroutineScope.launch {
                        val type = json.optString("type")
                        if (type == "chat_message") {
                            val newMessage = MessageDTO(
                                id = json.optString("message_id"),
                                booking = bookingId,
                                sender = json.optString("sender_id"),
                                messageText = json.optString("message"),
                                timestamp = json.optString("timestamp"),
                                status = json.optString("status"),
                                isSeen = json.optBoolean("is_seen", false)
                            )
                            
                            // Check if this message is already in the list (either as real ID or as temp optimistic)
                            val isDuplicate = messages.any { it.id == newMessage.id }
                            if (!isDuplicate) {
                                // Find if there's an optimistic version of this message
                                val tempMessage = messages.find { 
                                    it.id?.startsWith("temp_") == true && 
                                    it.messageText == newMessage.messageText && 
                                    it.sender == newMessage.sender 
                                }
                                
                                if (tempMessage != null) {
                                    // Replace temp message with real one
                                    messages = messages.map { if (it.id == tempMessage.id) newMessage else it }
                                } else {
                                    // Add as new message
                                    messages = messages + newMessage
                                }
                                
                                if (messages.isNotEmpty()) {
                                    listState.animateScrollToItem(messages.size - 1)
                                }
                            }
                        } else if (type == "messages_seen") {
                            messages = messages.map { it.copy(status = "SEEN", isSeen = true) }
                        }
                    }
                },
                onStatusChanged = { connected -> isConnected = connected }
            )
        }
        onDispose { wsManager.disconnect() }
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 4.dp, color = Color.White) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(bookingInfo?.itemName ?: "Chat", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (isConnected) Color(0xFF4CAF50) else Color.Red))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (!isConnected) "Connecting..." else "Active Now", 
                                fontSize = 11.sp, 
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF6C5CE7))
                }
            } else {
                // Chat List
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(messages) { msg ->
                        val myId = user?.id?.toString() ?: ""
                        val isMe = msg.sender.equals(myId, ignoreCase = true)
                        ChatBubble(
                            text = msg.messageText,
                            isSender = isMe,
                            status = msg.status ?: "SENT",
                            time = if (msg.id?.startsWith("temp_") == true) "Sending..." else "Just now"
                        )
                    }
                }
                
                // Quick Replies
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickReplies) { reply ->
                        Surface(
                            modifier = Modifier.clickable { messageText = reply },
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEF5))
                        ) {
                            Text(reply, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontSize = 13.sp, color = Color(0xFF6C5CE7), fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Input area
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...", color = Color.Gray) },
                        shape = RoundedCornerShape(24.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.Black),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF4F6FA),
                            focusedContainerColor = Color(0xFFF4F6FA),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color(0xFF6C5CE7)
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    FloatingActionButton(
                        onClick = { 
                            if (messageText.isNotBlank() && bookingId != null && user != null) {
                                val myId = user.id.toString()
                                val receiverId = if (myId.equals(bookingInfo?.borrower, ignoreCase = true)) {
                                    bookingInfo?.itemOwnerId
                                } else {
                                    bookingInfo?.borrower
                                }
                                
                                // Optimistic Update: Add message to list immediately
                                val tempId = "temp_${UUID.randomUUID()}"
                                val optimisticMessage = MessageDTO(
                                    id = tempId,
                                    booking = bookingId,
                                    sender = myId,
                                    messageText = messageText,
                                    timestamp = "",
                                    status = "SENDING",
                                    isSeen = false
                                )
                                messages = messages + optimisticMessage
                                
                                val sentText = messageText
                                messageText = ""
                                
                                coroutineScope.launch {
                                    listState.animateScrollToItem(messages.size - 1)
                                    try {
                                        wsManager.sendMessage(sentText, myId, receiverId)
                                    } catch (e: Exception) {
                                        // If failed, remove optimistic message
                                        messages = messages.filterNot { it.id == tempId }
                                    }
                                }
                            }
                        },
                        containerColor = Color(0xFF6C5CE7),
                        contentColor = Color.White,
                        modifier = Modifier.size(50.dp),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(text: String, isSender: Boolean, status: String, time: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isSender) Arrangement.End else Arrangement.Start
    ) {
        Column(horizontalAlignment = if (isSender) Alignment.End else Alignment.Start) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(
                        topStart = 22.dp,
                        topEnd = 22.dp,
                        bottomStart = if (isSender) 22.dp else 4.dp,
                        bottomEnd = if (isSender) 4.dp else 22.dp
                    ))
                    .background(
                        if (isSender) Brush.linearGradient(listOf(Color(0xFF0095F6), Color(0xFF0074CC))) 
                        else Brush.linearGradient(listOf(Color(0xFFEFEFEF), Color(0xFFEFEFEF)))
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .widthIn(max = 260.dp)
            ) {
                Text(
                    text = text,
                    color = if (isSender) Color.White else Color.Black,
                    fontSize = 15.sp
                )
            }
            
            if (isSender) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp, end = 4.dp)) {
                    Text(time, fontSize = 10.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (status == "SENDING") Icons.Default.Schedule else Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = if (status == "SEEN") Color(0xFF0095F6) else Color.Gray
                    )
                }
            }
        }
    }
}
