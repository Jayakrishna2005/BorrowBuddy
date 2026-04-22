package com.example.borrowbuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
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
import kotlinx.coroutines.launch
import com.example.borrowbuddy.model.Item
import com.example.borrowbuddy.network.BorrowBuddyApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    val api = remember {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/") 
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BorrowBuddyApi::class.java)
    }
    val scrollState = rememberScrollState()
    var itemName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isAvailable by remember { mutableStateOf(true) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Select Category") }
    val categories = listOf("Electronics", "Stationery", "Books", "Others")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(scrollState)
    ) {
        Text("List an Item", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        // Image Upload Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable { /* Handle Upload */ },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.AddAPhoto, contentDescription = "Add Photo", tint = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Upload Photo", color = Color.Gray)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        // Input Fields
        OutlinedTextField(
            value = itemName,
            onValueChange = { itemName = it },
            label = { Text("Item Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Category Dropdown
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = !categoryExpanded }
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategory = category
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(12.dp),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Pickup Location") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Toggle Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Available Now", fontSize = 16.sp)
            Switch(
                checked = isAvailable,
                onCheckedChange = { isAvailable = it },
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Submit Button
        Button(
            onClick = {
                coroutineScope.launch {
                    try {
                        val newItem = Item(
                            id = null,
                            title = itemName,
                            description = description,
                            condition = "Like New", // hardcoded for brevity
                            isAvailable = isAvailable
                        )
                        api.createItem(newItem)
                        navController.navigate("home")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.horizontalGradient(listOf(Color(0xFF6C5CE7), Color(0xFFA855F7)))),
                contentAlignment = Alignment.Center
            ) {
                Text("Post Item", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}