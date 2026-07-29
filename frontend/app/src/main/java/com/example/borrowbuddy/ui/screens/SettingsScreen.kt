package com.example.borrowbuddy.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.borrowbuddy.network.BorrowBuddyApi
import com.example.borrowbuddy.util.SessionManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import com.example.borrowbuddy.network.ChangePwdRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val session = remember { SessionManager(context) }
    val user = remember { session.getUser() }
    
    var fullName by remember { mutableStateOf(user?.fullName ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUpdating by remember { mutableStateOf(false) }
    
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var isChangingPwd by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()

    // Image Picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Photo Update
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF4F6FA))
                    .border(2.dp, Color(0xFF6C5CE7).copy(alpha = 0.3f), CircleShape)
                    .clickable { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "New Profile Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (!user?.profilePhoto.isNullOrBlank()) {
                    val photoUrl = "${session.getBaseUrl().removeSuffix("/")}${user?.profilePhoto}"
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Profile Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.AddAPhoto, contentDescription = "Add Photo", tint = Color(0xFF6C5CE7), modifier = Modifier.size(40.dp))
                }
            }
            
            TextButton(onClick = { galleryLauncher.launch("image/*") }) {
                Text("Change Profile Photo", color = Color(0xFF6C5CE7))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Name Field
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6C5CE7),
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedLabelColor = Color(0xFF6C5CE7),
                    unfocusedLabelColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Registration Number (Read Only)
            OutlinedTextField(
                value = user?.registrationNumber ?: "",
                onValueChange = {},
                label = { Text("Registration Number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = Color.Gray) },
                enabled = false,
                readOnly = true,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black.copy(alpha = 0.7f),
                    disabledBorderColor = Color.Gray.copy(alpha = 0.5f),
                    disabledLabelColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Save Button
            Button(
                onClick = {
                    if (fullName.isBlank()) {
                        Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    isUpdating = true
                    coroutineScope.launch {
                        try {
                            val api = BorrowBuddyApi.create()
                            val namePart = fullName.toRequestBody("text/plain".toMediaTypeOrNull())
                            
                            var photoPart: MultipartBody.Part? = null
                            imageUri?.let { uri ->
                                val inputStream = context.contentResolver.openInputStream(uri)
                                val file = File(context.cacheDir, "profile_upload.jpg")
                                val outputStream = FileOutputStream(file)
                                inputStream?.copyTo(outputStream)
                                
                                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                                photoPart = MultipartBody.Part.createFormData("profile_photo", file.name, requestFile)
                            }
                            
                            val updatedUser = api.updateProfile(user!!.id.toString(), namePart, photoPart)
                            session.saveUser(updatedUser)
                            Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isUpdating = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isUpdating,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C5CE7))
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Change Password", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = oldPassword,
                onValueChange = { oldPassword = it },
                label = { Text("Old Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors()
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (oldPassword.isBlank() || newPassword.isBlank()) {
                        Toast.makeText(context, "Passwords cannot be empty", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    isChangingPwd = true
                    coroutineScope.launch {
                        try {
                            val api = BorrowBuddyApi.create()
                            val response = api.changePassword(user!!.id.toString(), ChangePwdRequest(oldPassword, newPassword))
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Password changed successfully!", Toast.LENGTH_SHORT).show()
                                oldPassword = ""
                                newPassword = ""
                            } else {
                                Toast.makeText(context, "Failed to change password", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isChangingPwd = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isChangingPwd,
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
            ) {
                if (isChangingPwd) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Text("Change Password", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
