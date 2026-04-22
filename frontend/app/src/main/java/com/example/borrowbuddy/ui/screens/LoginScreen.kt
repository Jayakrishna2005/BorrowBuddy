package com.example.borrowbuddy.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.borrowbuddy.ui.viewmodel.AuthViewModel
import com.example.borrowbuddy.ui.viewmodel.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel = viewModel()) {
    var name by remember { mutableStateOf("") }
    var regNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()
    
    val buttonGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF6C5CE7), Color(0xFFA855F7))
    )

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
            is AuthState.Error -> {
                val msg = (authState as AuthState.Error).message
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Illustration placeholder
        Box(
            modifier = Modifier
                .size(150.dp)
                .background(Color(0xFFE0E7FF), shape = RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Group,
                contentDescription = "Students swapping items",
                tint = Color(0xFF6C5CE7),
                modifier = Modifier.size(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Welcome to BorrowBuddy",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter your details to continue.",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Name Field
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Register Number Field
        OutlinedTextField(
            value = regNumber,
            onValueChange = { 
                // Only allow up to 9 digits
                if (it.length <= 9 && it.all { char -> char.isDigit() }) {
                    regNumber = it 
                }
            },
            label = { Text("College Registration Number") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // College Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("College Email Address") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (name.isNotBlank() && regNumber.isNotBlank() && email.isNotBlank()) {
                    val regRegex = Regex("^19\\d{7}$")
                    if (!regRegex.matches(regNumber)) {
                        Toast.makeText(context, "Registration number must be 9 digits and start with 19", Toast.LENGTH_SHORT).show()
                    } else if (!email.trim().endsWith(".sse@saveetha.com", ignoreCase = true)) {
                        Toast.makeText(context, "Email must end with .sse@saveetha.com", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.login(name, regNumber, email)
                    }
                } else {
                    Toast.makeText(context, "Please fill in all details to login", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(buttonGradient),
                contentAlignment = Alignment.Center
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Login", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}