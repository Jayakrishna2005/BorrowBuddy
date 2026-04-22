package com.example.borrowbuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.borrowbuddy.ui.viewmodel.HomeViewModel
import com.example.borrowbuddy.util.SessionManager
import com.example.borrowbuddy.network.BorrowBuddyApi
import com.example.borrowbuddy.network.BookingRequest
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@Composable
fun ItemDetailScreen(navController: NavController, itemId: String?, viewModel: HomeViewModel = viewModel()) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val session = remember { SessionManager(context) }
    val user = session.getUser()
    
    val items by viewModel.items.collectAsState()
    val item = items.find { it.id.toString() == itemId }
    
    val api = remember {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/") 
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BorrowBuddyApi::class.java)
    }

    val scrollState = rememberScrollState()
    
    val buttonGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF6C5CE7), Color(0xFFA855F7))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        // Large Image Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color(0xFFE0E7FF))
        )
        
        Column(modifier = Modifier.padding(24.dp)) {
            // Title & Status
            Text(
                text = item?.title ?: "Item Details",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                val isAvail = item?.isAvailable ?: false
                val color = if (isAvail) Color(0xFF4CAF50) else Color.Red
                Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isAvail) "Available Now" else "Not Available", fontSize = 14.sp, color = color, fontWeight = FontWeight.Medium)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Description", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                item?.description ?: "No description available.",
                color = Color.Gray,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Location
            Text("Pickup Location", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hostel Block A, Main Gate", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
            }
            
            // Map Placeholder
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF4F6FA)),
                contentAlignment = Alignment.Center
            ) {
                Text("Map View Placeholder", color = Color.Gray)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Request Button
            Button(
                onClick = { 
                    if (item != null && user != null) {
                        coroutineScope.launch {
                            try {
                                api.createBooking(BookingRequest(
                                    item = item.id.toString(),
                                    borrower = user.id.toString()
                                ))
                                Toast.makeText(context, "Request Sent!", Toast.LENGTH_SHORT).show()
                                navController.navigate("chat")
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "Request failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
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
                        .background(buttonGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Request to Borrow", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}