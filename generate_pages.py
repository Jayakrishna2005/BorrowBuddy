import os

base_android = "c:/projects/borrow_buddy_app/frontend/app/src/main/java/com/example/borrowbuddy"

def write(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content.strip())

# Navigation Graph
write(f"{base_android}/ui/navigation/BorrowBuddyApp.kt", """
package com.example.borrowbuddy.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.borrowbuddy.ui.screens.*

@Composable
fun BorrowBuddyApp() {
    val navController = rememberNavController()
    
    // Theme colors: Soft Green & Blue
    val primaryColor = Color(0xFF4CAF50)
    val secondaryColor = Color(0xFF03A9F4)

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            if (currentRoute != "login") {
                NavigationBar(containerColor = Color.White) {
                    NavigationBarItem(
                        selected = currentRoute == "home",
                        onClick = { navController.navigate("home") },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = primaryColor,
                            selectedTextColor = primaryColor,
                            indicatorColor = primaryColor.copy(alpha = 0.2f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == "items",
                        onClick = { navController.navigate("items") },
                        icon = { Icon(Icons.Filled.Search, contentDescription = "Browse") },
                        label = { Text("Browse") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = primaryColor,
                            selectedTextColor = primaryColor,
                            indicatorColor = primaryColor.copy(alpha = 0.2f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == "add_item",
                        onClick = { navController.navigate("add_item") },
                        icon = { Icon(Icons.Filled.AddCircle, contentDescription = "List Item", tint = secondaryColor) },
                        label = { Text("List") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = secondaryColor,
                            selectedTextColor = secondaryColor,
                            indicatorColor = secondaryColor.copy(alpha = 0.2f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == "profile",
                        onClick = { navController.navigate("profile") },
                        icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = primaryColor,
                            selectedTextColor = primaryColor,
                            indicatorColor = primaryColor.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") { LoginScreen(navController) }
            composable("home") { HomeScreen(navController) }
            composable("items") { BrowseItemsScreen(navController) }
            composable("add_item") { AddItemScreen(navController) }
            composable("profile") { ProfileScreen(navController) }
            composable("item_detail") { ItemDetailScreen(navController) }
        }
    }
}
""")

write(f"{base_android}/ui/screens/LoginScreen.kt", """
package com.example.borrowbuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    var regNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFE8F5E9), Color(0xFFE1F5FE))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Borrow Buddy", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
        Text("Campus-exclusive sharing platfom", fontSize = 16.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 40.dp))

        OutlinedTextField(
            value = regNumber,
            onValueChange = { regNumber = it },
            label = { Text("College Registration Number") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { navController.navigate("home") },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Login", fontSize = 18.sp, color = Color.White)
        }
    }
}
""")

write(f"{base_android}/ui/screens/HomeScreen.kt", """
package com.example.borrowbuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .padding(16.dp)
    ) {
        // Welcome Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Hello, John! 👋", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Trust Score: 95/100 🌟", fontSize = 14.sp, color = Color(0xFF4CAF50))
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF81C784))
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SOS Button
        Card(
            modifier = Modifier.fillMaxWidth().clickable { /* Fire SOS Broadcast */ },
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Warning, contentDescription = "SOS", tint = Color(0xFFD32F2F))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Emergency SOS Borrow", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                    Text("Broadcast urgent need to nearby students", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Recent Activity", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal List
        val mockItems = listOf("Calculus Textbook", "Engineering Drafter", "MacBook Charger")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(mockItems) { item ->
                Card(
                    modifier = Modifier.width(160.dp).height(120.dp).clickable { navController.navigate("item_detail") },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Center) {
                        Text(item, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Available now", fontSize = 12.sp, color = Color(0xFF4CAF50))
                    }
                }
            }
        }
    }
}
""")

write(f"{base_android}/ui/screens/BrowseItemsScreen.kt", """
package com.example.borrowbuddy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseItemsScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Browse Items", fontWeight = FontWeight.Bold) })
        
        // Mock Filter UI
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = true, onClick = {}, label = { Text("All") })
            FilterChip(selected = false, onClick = {}, label = { Text("Notes") })
            FilterChip(selected = false, onClick = {}, label = { Text("Electronics") })
        }

        LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
            items(5) { 
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    navigationIcon = { },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Scientific Calculator", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Condition: Like New", color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { navController.navigate("item_detail") }) {
                            Text("View Details")
                        }
                    }
                }
            }
        }
    }
}
""")

write(f"{base_android}/ui/screens/AddItemScreen.kt", """
package com.example.borrowbuddy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(navController: NavController) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("List a New Item", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Item Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth().height(120.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Publish Listing")
        }
    }
}
""")

write(f"{base_android}/ui/screens/ItemDetailScreen.kt", """
package com.example.borrowbuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(Color(0xFFE0E0E0)))
            
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Scientific Calculator", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Listed by Sarah • Trust Score: 98", color = Color(0xFF4CAF50), fontSize = 14.sp)
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Description", fontWeight = FontWeight.Bold)
                Text("A fully working Casio FX-991EX. Please return it with care. I only lend it on weekends.", color = Color.Gray)
                
                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = { /* Request Logic */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03A9F4))
                ) {
                    Text("Request Borrow")
                }
            }
        }
    }
}
""")

write(f"{base_android}/ui/screens/ProfileScreen.kt", """
package com.example.borrowbuddy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun ProfileScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Your Profile", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("John Doe", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Reg No: 2024CS100", color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Trust Score: 95/100 🌟", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                Text("Items Lent: 12", color = Color.Gray)
                Text("Items Borrowed: 5", color = Color.Gray)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Recent Gratitudes", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Thank you so much for the notes! - via Dave", color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
        Text("Real lifesaver for the exam! - via Maria", color = Color.Gray)
        
        Spacer(modifier = Modifier.weight(1f))
        OutlinedButton(
            onClick = { navController.navigate("login") { popUpTo(0) } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }
    }
}
""")

print("Pages scaffolded successfully!")
