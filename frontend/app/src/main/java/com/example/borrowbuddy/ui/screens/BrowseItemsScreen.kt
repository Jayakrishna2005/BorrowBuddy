package com.example.borrowbuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.borrowbuddy.ui.viewmodel.HomeViewModel
import com.example.borrowbuddy.model.Item
import com.example.borrowbuddy.util.SessionManager
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseItemsScreen(navController: NavController, viewModel: HomeViewModel = viewModel()) {
    val itemsState by viewModel.items.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredItems = itemsState.filter {
        // Global search overrides category filter
        val matchesCategory = if (searchQuery.isNotBlank()) true else {
            (selectedFilter == "All") || 
            (it.categoryName?.trim()?.equals(selectedFilter, ignoreCase = true) == true)
        }
        val matchesSearch = if (searchQuery.isBlank()) true else {
            it.title.contains(searchQuery, ignoreCase = true) || 
            it.description.contains(searchQuery, ignoreCase = true) ||
            (it.categoryName?.contains(searchQuery, ignoreCase = true) == true) ||
            (it.ownerName?.contains(searchQuery, ignoreCase = true) == true)
        }
        matchesCategory && matchesSearch
    }

    // Refresh items on load
    LaunchedEffect(Unit) {
        viewModel.loadItems()
    }

    val categories = itemsState.mapNotNull { it.categoryName }.distinct()
    val filters = listOf("All") + categories
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Search Bar (same as before)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by title, owner, or category...", color = Color.Gray) },
                leadingIcon = { 
                    Icon(
                        Icons.Default.Search, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.primary 
                    ) 
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = Color.Gray
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF4F6FA),
                    unfocusedContainerColor = Color(0xFFF4F6FA),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filters
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(filters) { filter ->
                CategoryFilterItemUI(
                    name = filter,
                    isSelected = selectedFilter == filter,
                    onClick = { selectedFilter = filter }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Items List
        if (filteredItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No items found matching \"$searchQuery\"", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(filteredItems, key = { it.id ?: "" }) { item ->
                    BrowseItemCardUI(navController, item)
                }
            }
        }
    }
}

@Composable
fun BrowseItemCardUI(navController: NavController, item: Item) {
    val context = LocalContext.current
    val session = remember { SessionManager(context) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("item_detail/${item.id}") },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Image
            val imageUrl = if (!item.image.isNullOrBlank()) {
                "${session.getBaseUrl().removeSuffix("/")}${item.image}"
            } else {
                null
            }
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF4F6FA)),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = coil.request.ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Image, contentDescription = null, tint = Color.LightGray)
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.title,
                        fontWeight = FontWeight.Bold, 
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    if (item.reviewsCount > 0) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF4CAF50)
                        )
                        Text(
                            text = " ${item.averageRating}",
                            fontSize = 12.sp,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (item.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(item.description, fontSize = 11.sp, color = Color.Gray, maxLines = 1)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("By: ${item.ownerName ?: "Unknown"}", fontSize = 11.sp, color = Color(0xFF6C5CE7), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val color = if (item.isAvailable) Color(0xFF4CAF50) else Color.Red
                    val text = if (item.isAvailable) "Available" else "Borrowed"
                    Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text, fontSize = 11.sp, color = color)
                }
            }
        }
    }
}

@Composable
fun CategoryFilterItemUI(name: String, isSelected: Boolean, onClick: () -> Unit) {
    val icon = when (name) {
        "Electronics" -> Icons.Default.DeviceUnknown
        "Stationery" -> Icons.Default.Edit
        "Books" -> Icons.Default.Book
        "Tools" -> Icons.Default.Build
        "Fashion" -> Icons.Default.Checkroom
        else -> Icons.Default.GridView
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .width(70.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFF4F6FA)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
            maxLines = 1
        )
    }
}
