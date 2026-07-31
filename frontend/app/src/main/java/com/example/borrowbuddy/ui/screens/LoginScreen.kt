package com.example.borrowbuddy.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.borrowbuddy.ui.viewmodel.AuthViewModel
import com.example.borrowbuddy.ui.viewmodel.AuthState
import com.example.borrowbuddy.util.SessionManager
import com.example.borow_buddy_frontend.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel = viewModel()) {
    val context = LocalContext.current
    val session = remember { SessionManager(context) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    
    var mode by remember { mutableStateOf("login") } // login, register, verify-otp, forgot-pwd, reset-pwd
    
    var name by remember { mutableStateOf("") }
    var regNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        disabledTextColor = Color.DarkGray
    )
    
    val authState by viewModel.authState.collectAsState()
    
    val buttonGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF6C5CE7), Color(0xFFA855F7))
    )

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                session.saveUser((authState as AuthState.Success).user)
                Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
            is AuthState.NeedsVerification -> {
                Toast.makeText(context, (authState as AuthState.NeedsVerification).message, Toast.LENGTH_LONG).show()
                mode = "verify-otp"
                viewModel.resetState()
            }
            is AuthState.ActionSuccess -> {
                val msg = (authState as AuthState.ActionSuccess).message
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                if (mode == "forgot-pwd") {
                    mode = "reset-pwd"
                } else if (mode == "reset-pwd") {
                    mode = "login"
                }
                viewModel.resetState()
            }
            is AuthState.Error -> {
                val msg = (authState as AuthState.Error).message
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Illustration
        Image(
            painter = painterResource(id = R.drawable.login_illustration),
            contentDescription = "Students swapping items",
            modifier = Modifier
                .size(240.dp)
                .padding(bottom = 8.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = when(mode) {
                "login" -> "Welcome to BorrowBuddy"
                "register" -> "Create an Account"
                "verify-otp" -> "Verify Email"
                "forgot-pwd" -> "Forgot Password"
                "reset-pwd" -> "Reset Password"
                else -> ""
            },
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when(mode) {
                "login" -> "Enter your details to continue."
                "register" -> "Join the community today."
                "verify-otp" -> "Enter the OTP sent to your email."
                "forgot-pwd" -> "Enter email to receive OTP."
                "reset-pwd" -> "Enter OTP and your new password."
                else -> ""
            },
            fontSize = 14.sp,
            color = Color.DarkGray,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (mode == "register") {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = regNumber,
                onValueChange = { if (it.length <= 9 && it.all { char -> char.isDigit() }) regNumber = it },
                label = { Text("College Registration Number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (mode in listOf("login", "register", "forgot-pwd", "verify-otp", "reset-pwd")) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("College Email Address") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                readOnly = mode in listOf("verify-otp", "reset-pwd")
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (mode in listOf("login", "register")) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        if (mode in listOf("verify-otp", "reset-pwd")) {
            OutlinedTextField(
                value = otp,
                onValueChange = { otp = it },
                label = { Text("OTP") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (mode == "reset-pwd") {
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        if (mode == "login") {
            Text(
                text = "Forgot Password?",
                color = Color(0xFF6C5CE7),
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { mode = "forgot-pwd" }
                    .padding(vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                when (mode) {
                    "login" -> {
                        if (email.isNotBlank() && password.isNotBlank()) {
                            if (!email.trim().endsWith("@gmail.com", ignoreCase = true)) {
                                Toast.makeText(context, "Email must end with @gmail.com", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.login(email.trim(), password, regNumber)
                            }
                        } else {
                            Toast.makeText(context, "Please fill in email and password", Toast.LENGTH_SHORT).show()
                        }
                    }
                    "register" -> {
                        if (name.isNotBlank() && regNumber.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                            if (!email.trim().endsWith("@gmail.com", ignoreCase = true)) {
                                Toast.makeText(context, "Email must end with @gmail.com", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.register(name, regNumber, email.trim(), password)
                            }
                        } else {
                            Toast.makeText(context, "Please fill in all details", Toast.LENGTH_SHORT).show()
                        }
                    }
                    "verify-otp" -> {
                        if (email.isNotBlank() && otp.isNotBlank()) {
                            viewModel.verifyOtp(email.trim(), otp)
                        }
                    }
                    "forgot-pwd" -> {
                        if (email.isNotBlank()) {
                            if (!email.trim().endsWith("@gmail.com", ignoreCase = true)) {
                                Toast.makeText(context, "Email must end with @gmail.com", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.forgotPassword(email.trim())
                            }
                        }
                    }
                    "reset-pwd" -> {
                        if (email.isNotBlank() && otp.isNotBlank() && newPassword.isNotBlank()) {
                            viewModel.resetPassword(email.trim(), otp, newPassword)
                        }
                    }
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
                    val btnText = when(mode) {
                        "login" -> "Login"
                        "register" -> "Register"
                        "verify-otp" -> "Verify"
                        "forgot-pwd" -> "Send OTP"
                        "reset-pwd" -> "Reset Password"
                        else -> "Submit"
                    }
                    Text(btnText, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (mode == "login") {
            Row {
                Text("Don't have an account? ", color = Color.Gray, fontSize = 14.sp)
                Text("Register", color = Color(0xFF6C5CE7), fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.clickable { mode = "register" })
            }
        } else if (mode in listOf("register", "forgot-pwd", "reset-pwd")) {
            Row {
                Text("Back to ", color = Color.Gray, fontSize = 14.sp)
                Text("Login", color = Color(0xFF6C5CE7), fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.clickable { mode = "login" })
            }
        }
    }
        
    IconButton(
            onClick = { showSettingsDialog = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Server Settings",
                tint = Color.Gray
            )
        }

        if (showSettingsDialog) {
            var tempUrl by remember { mutableStateOf(session.getBaseUrl()) }
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = { Text("Server Settings", color = Color.Black) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = tempUrl,
                            onValueChange = { tempUrl = it },
                            label = { Text("API Base URL") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = textFieldColors,
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Quick Connect:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { tempUrl = "http://10.0.2.2:8000/" },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C5CE7))
                            ) {
                                Text("Emulator", color = Color.White)
                            }
                            Button(
                                onClick = { tempUrl = "http://172.20.232.45:8000/" },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C5CE7))
                            ) {
                                Text("Host PC", color = Color.White)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (tempUrl.isNotBlank()) {
                                val formattedUrl = if (!tempUrl.endsWith("/")) "$tempUrl/" else tempUrl
                                session.saveBaseUrl(formattedUrl)
                                Toast.makeText(context, "Base URL updated to: $formattedUrl", Toast.LENGTH_SHORT).show()
                                showSettingsDialog = false
                            }
                        }
                    ) {
                        Text("Save", color = Color(0xFF6C5CE7))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSettingsDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                containerColor = Color.White
            )
        }
    }
}