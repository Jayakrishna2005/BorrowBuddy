package com.example.borrowbuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun RequestsScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Received", "Sent")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = FontWeight.Bold) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(5) { index ->
                if (selectedTab == 0) {
                    ReceivedRequestCard()
                } else {
                    SentRequestCard()
                }
            }
        }
    }
}

@Composable
fun ReceivedRequestCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Scientific Calculator", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Pending", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Requested by: John Doe", fontSize = 14.sp, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { /* Reject */ }) {
                    Text("Reject", color = Color.Red)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { /* Accept */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Accept")
                }
            }
        }
    }
}

@Composable
fun SentRequestCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Engineering Drawing Kit", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Accepted", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Owner: Alice Smith", fontSize = 14.sp, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "You can now pick up the item. Contact the owner.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
