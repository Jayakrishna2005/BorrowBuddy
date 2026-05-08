package com.example.borrowbuddy.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.layout.ContentScale
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.borrowbuddy.ui.viewmodel.HomeViewModel
import com.example.borrowbuddy.util.SessionManager
import com.example.borrowbuddy.model.Item
import com.example.borrowbuddy.network.CategoryDTO
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = viewModel()) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val session = remember { SessionManager(context) }
    val user = session.getUser()
    val itemsState by viewModel.items.collectAsState()

    // Initialize with defaults for better UX
    var categories by remember { mutableStateOf<List<CategoryDTO>>(
        listOf(
            CategoryDTO(1, "Electronics"),
            CategoryDTO(2, "Stationery"),
            CategoryDTO(3, "Books"),
            CategoryDTO(4, "Tools"),
            CategoryDTO(5, "Fashion")
        )
    ) }
    var selectedCategoryName by remember { mutableStateOf("All") }



    var showReviewReminder by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadItems()
        try {
            val api = com.example.borrowbuddy.network.BorrowBuddyApi.create()
            
            // Parallel Review Check and Category Fetching
            coroutineScope.launch {
                try {
                    if (user != null) {
                        val bookings = api.getUserBookings(user.id.toString())
                        val hasPendingReview = bookings.sent?.any { it.status == "COMPLETED" && it.hasReview == false } == true
                        if (hasPendingReview) {
                            showReviewReminder = true
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
            
            coroutineScope.launch {
                try {
                    val apiCategories = api.getCategories()
                    if (apiCategories.isNotEmpty()) {
                        categories = apiCategories
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Refresh when returning to this screen
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        viewModel.loadItems()
    }

    if (showReviewReminder) {
        AlertDialog(
            onDismissRequest = { /* Force action */ },
            title = { Text("Pending Feedback Required") },
            text = { Text("You have completed a borrow but haven't left a review yet. Your feedback helps the community grow and increases user trust!") },
            confirmButton = {
                Button(onClick = { 
                    showReviewReminder = false
                    navController.navigate("requests") 
                }) {
                    Text("Review Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewReminder = false }) {
                    Text("Later")
                }
            }
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedCondition by remember { mutableStateOf("Any") }
    var selectedBuilding by remember { mutableStateOf("All") }
    
    val filteredItems = itemsState.filter {
        // If searching, ignore category filter (make it global search)
        val matchesCategory = if (searchQuery.isNotBlank()) true else {
            (selectedCategoryName == "All") || 
            (it.categoryName?.trim()?.equals(selectedCategoryName, ignoreCase = true) == true)
        }
        
        val matchesSearch = if (searchQuery.isBlank()) true else {
            it.title.contains(searchQuery, ignoreCase = true) || 
            it.description.contains(searchQuery, ignoreCase = true) ||
            (it.categoryName?.contains(searchQuery, ignoreCase = true) == true) ||
            (it.ownerName?.contains(searchQuery, ignoreCase = true) == true)
        }
        
        val matchesCondition = (selectedCondition == "Any") || (it.condition == selectedCondition)
        val matchesBuilding = (selectedBuilding == "All") 
        
        matchesCategory && matchesSearch && matchesCondition && matchesBuilding
    }
    
    // Auto-scroll to results if searching
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            scrollState.animateScrollTo(0)
        }
    }
    
    var selectedItemForPopup by remember { mutableStateOf<Item?>(null) }
    var showDetailSheet by remember { mutableStateOf(false) }

    if (showDetailSheet && selectedItemForPopup != null) {
        val item = selectedItemForPopup!!
        ModalBottomSheet(
            onDismissRequest = { showDetailSheet = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth().verticalScroll(rememberScrollState())) {
                // Item Image
                val imageUrl = if (!item.image.isNullOrBlank()) {
                    "${session.getBaseUrl().removeSuffix("/")}${item.image}"
                } else null
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFF4F6FA))
                ) {
                    if (imageUrl != null) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = item.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(64.dp).align(Alignment.Center), tint = Color.LightGray)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(item.title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                Text(item.categoryName ?: "Category", color = Color(0xFF6C5CE7), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Description", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(item.description, color = Color.Gray, fontSize = 14.sp)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                if (item.ownerId != user?.id?.toString()) {
                    Button(
                        onClick = { 
                            coroutineScope.launch {
                                try {
                                    val api = com.example.borrowbuddy.network.BorrowBuddyApi.create()
                                    api.createBooking(com.example.borrowbuddy.network.BookingRequest(
                                        item = item.id.toString(),
                                        borrower = user?.id.toString()
                                    ))
                                    android.widget.Toast.makeText(context, "Request Sent! 🚀", android.widget.Toast.LENGTH_SHORT).show()
                                    showDetailSheet = false
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C5CE7))
                    ) {
                        Text("Request to Borrow", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = { showDetailSheet = false },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Back", color = Color.Black)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                Text("Filters", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("Condition", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Any", "New", "Good", "Used").forEach { cond ->
                        FilterChip(
                            selected = selectedCondition == cond,
                            onClick = { selectedCondition = cond },
                            label = { Text(cond) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF6C5CE7),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("Building", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                val buildings = listOf("All", "Library", "Hostel A", "Block B", "Food Court")
                buildings.forEach { bld ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedBuilding = bld }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedBuilding == bld, onClick = { selectedBuilding = bld }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF6C5CE7)))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(bld)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { showFilterSheet = false },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C5CE7))
                ) {
                    Text("Apply Filters", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Top Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Hi, ${user?.fullName ?: "User"} 👋", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                    Text("What do you need today?", fontSize = 14.sp, color = Color.Gray)
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6C5CE7).copy(alpha = 0.1f))
                        .clickable { navController.navigate("profile") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color(0xFF6C5CE7))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Search & Filter Row
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEF5))
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search title, category...", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF6C5CE7)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.Black),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = Color(0xFF6C5CE7),
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = { showFilterSheet = true },
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFFEEEEF5), RoundedCornerShape(16.dp))
                ) {
                    Icon(Icons.Default.Tune, contentDescription = "Filter", tint = Color(0xFF6C5CE7))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Banner Card
        item {
            val bannerGradient = Brush.horizontalGradient(
                colors = listOf(Color(0xFF6C5CE7), Color(0xFFA855F7))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(bannerGradient),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Share today, help forever 💜",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(start = 24.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard("Shared", user?.itemsLent?.toString() ?: "0", modifier = Modifier.weight(1f))
                StatCard("Borrowed", user?.itemsBorrowed?.toString() ?: "0", modifier = Modifier.weight(1f))
                StatCard("Trust", "${user?.trustScore ?: 0.0f}★", modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Categories Header
        item {
            Text("Categories", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Categories List
        item {
            val categoryNames = listOf("All") + categories.map { it.name }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(categoryNames) { catName ->
                    val isSelected = selectedCategoryName == catName
                    CategoryItemUI(
                        name = catName,
                        isSelected = isSelected,
                        onClick = { selectedCategoryName = catName }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Items Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val sectionTitle = when {
                    searchQuery.isNotEmpty() -> "Search Results"
                    selectedCategoryName != "All" -> "$selectedCategoryName Items"
                    else -> "Available Items"
                }
                Text(
                    text = sectionTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                if (searchQuery.isBlank() && selectedCategoryName == "All") {
                    TextButton(onClick = { navController.navigate("items") }) {
                        Text("See All", color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    TextButton(onClick = { 
                        searchQuery = ""
                        selectedCategoryName = "All"
                    }) {
                        Text("Clear", color = Color.Gray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Items Content (Skeleton / Empty / Grid)
        if (isLoading && filteredItems.isEmpty()) {
            items(6) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    repeat(2) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(220.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFFF4F6FA))
                        )
                    }
                }
            }
        } else if (filteredItems.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.GridView, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No items found", color = Color.Gray)
                    }
                }
            }
        } else {
            // Optimized Grid using items chunked
            val rows = filteredItems.chunked(2)
            items(rows) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowItems.forEach { item ->
                        Box(modifier = Modifier.weight(1f)) {
                            ItemCardUI(navController, item, user?.id.toString(), 
                                onDetailClick = {
                                    navController.navigate("item_detail/${item.id}")
                                },
                                onDelete = {
                                    coroutineScope.launch {
                                        try {
                                            val api = com.example.borrowbuddy.network.BorrowBuddyApi.create()
                                            val response = api.deleteItem(item.id.toString())
                                            if (response.isSuccessful) {
                                                viewModel.loadItems()
                                                android.widget.Toast.makeText(context, "Item deleted!", android.widget.Toast.LENGTH_SHORT).show()
                                            } else {
                                                android.widget.Toast.makeText(context, "Failed to delete item", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            android.widget.Toast.makeText(context, "Network error", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            )
                        }
                    }
                    if (rowItems.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ItemCardUI(navController: NavController, item: Item, currentUserId: String? = null, onDetailClick: () -> Unit = {}, onDelete: () -> Unit = {}) {
    val context = LocalContext.current
    val session = remember { SessionManager(context) }
    var isFavorite by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDetailClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEF5))
    ) {
        Column {
            val imageUrl = if (!item.image.isNullOrBlank()) {
                "${session.getBaseUrl().removeSuffix("/")}${item.image}"
            } else {
                null
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color(0xFFF4F6FA))
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = coil.request.ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize().then(if (!item.isAvailable) androidx.compose.ui.Modifier.alpha(0.5f) else androidx.compose.ui.Modifier),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Image, contentDescription = null, tint = Color.LightGray, modifier = Modifier.align(Alignment.Center))
                }
                
                // Delete Button (Owner Only)
                if (item.ownerId == currentUserId) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // New / Borrowed Badge
                if (item.isAvailable && item.ownerId != currentUserId) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .background(Color(0xFF6C5CE7), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("NEW", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (!item.isAvailable) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .background(Color.Red.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("BORROWED", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    color = Color(0xFF1A1A1A)
                )
                
                Text(
                    text = "By: ${item.ownerName ?: "Unknown"}",
                    fontSize = 11.sp,
                    color = Color(0xFF6C5CE7),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.description.take(20) + "...",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FREE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF6C5CE7)
                    )
                    
                    if (item.reviewsCount > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB400), modifier = Modifier.size(14.dp))
                            Text(text = item.averageRating.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryItemUI(name: String, isSelected: Boolean, onClick: () -> Unit) {
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
            .width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFF4F6FA)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
        )
    }
}
