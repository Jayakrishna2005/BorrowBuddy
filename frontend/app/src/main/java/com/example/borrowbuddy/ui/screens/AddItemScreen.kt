package com.example.borrowbuddy.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
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
import com.example.borrowbuddy.network.BorrowBuddyApi
import com.example.borrowbuddy.network.CategoryDTO
import com.example.borrowbuddy.util.SessionManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AddItemScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val session = remember { SessionManager(context) }
    var currentBaseUrl by remember { mutableStateOf(session.getBaseUrl()) }
    val user = session.getUser()
    val api = remember(currentBaseUrl) { BorrowBuddyApi.create() }

    var currentStep by remember { mutableIntStateOf(0) }
    var itemName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var selectedCategory by remember { mutableStateOf<CategoryDTO?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isPosting by remember { mutableStateOf(false) }

    val categories = listOf(
        CategoryDTO(2, "Stationery"),
        CategoryDTO(1, "Electronics"),
        CategoryDTO(3, "Books"),
        CategoryDTO(6, "Sports")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Custom App Bar / Progress
        AddItemHeader(currentStep, onBack = {
            if (currentStep > 0) currentStep-- else navController.popBackStack()
        })

        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally(animationSpec = tween(300)) { it } + fadeIn() togetherWith
                                slideOutHorizontally(animationSpec = tween(300)) { -it } + fadeOut()
                    } else {
                        slideInHorizontally(animationSpec = tween(300)) { -it } + fadeIn() togetherWith
                                slideOutHorizontally(animationSpec = tween(300)) { it } + fadeOut()
                    }.using(SizeTransform(clip = false))
                }, label = ""
            ) { step ->
                when (step) {
                    0 -> CategoryStep(categories) {
                        selectedCategory = it
                        currentStep = 1
                    }
                    1 -> DetailsStep(
                        itemName = itemName,
                        onItemNameChange = { itemName = it },
                        quantity = quantity,
                        onQuantityChange = { quantity = it },
                        imageUri = imageUri,
                        onImageSelected = { imageUri = it },
                        isPosting = isPosting,
                        onPost = {
                            if (itemName.isBlank() || quantity.isBlank()) {
                                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                            } else {
                                isPosting = true
                                coroutineScope.launch {
                                    try {
                                        val titlePart = itemName.toRequestBody("text/plain".toMediaTypeOrNull())
                                        val descPart = "".toRequestBody("text/plain".toMediaTypeOrNull())
                                        val condPart = "Good".toRequestBody("text/plain".toMediaTypeOrNull())
                                        val availPart = "true".toRequestBody("text/plain".toMediaTypeOrNull())
                                        val categoryPart = selectedCategory!!.id.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                                        val maxBorrowDaysPart = "7".toRequestBody("text/plain".toMediaTypeOrNull())
                                        val ownerPart = user!!.id.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                                        val quantityPart = quantity.toRequestBody("text/plain".toMediaTypeOrNull())
                                        
                                        var imagePart: MultipartBody.Part? = null
                                        imageUri?.let { uri ->
                                            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                                val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                                                val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
                                                FileOutputStream(file).use { outputStream ->
                                                    originalBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 75, outputStream)
                                                }
                                                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                                                imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
                                            }
                                        }

                                        api.createItem(titlePart, descPart, condPart, availPart, categoryPart, maxBorrowDaysPart, ownerPart, quantityPart, imagePart)
                                        Toast.makeText(context, "Item Live! 🚀", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                    } finally {
                                        isPosting = false
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AddItemHeader(currentStep: Int, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (currentStep == 0) "What are you offering?" else "Item Details",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Progress Dots
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(2) { index ->
                Box(
                    modifier = Modifier
                        .height(6.dp)
                        .weight(1f)
                        .clip(CircleShape)
                        .background(if (index <= currentStep) Color(0xFF6C5CE7) else Color(0xFFEEEEF5))
                )
            }
        }
    }
}

@Composable
fun CategoryStep(categories: List<CategoryDTO>, onSelected: (CategoryDTO) -> Unit) {
    Column(modifier = Modifier.padding(24.dp).background(Color.White)) {
        Text("Select Category", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(20.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(categories) { category ->
                CategoryCard(category) { onSelected(category) }
            }
        }
    }
}

@Composable
fun CategoryCard(category: CategoryDTO, onClick: () -> Unit) {
    val icon = when (category.name) {
        "Electronics" -> Icons.Default.Devices
        "Stationery" -> Icons.Default.Edit
        "Books" -> Icons.AutoMirrored.Filled.MenuBook
        "Tools" -> Icons.Default.Build
        "Fashion" -> Icons.Default.Checkroom
        "Sports" -> Icons.Default.SportsBasketball
        else -> Icons.Default.GridView
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEF5))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF6C5CE7).copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFF6C5CE7))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(category.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
        }
    }
}

@Composable
fun DetailsStep(
    itemName: String,
    onItemNameChange: (String) -> Unit,
    quantity: String,
    onQuantityChange: (String) -> Unit,
    imageUri: Uri?,
    onImageSelected: (Uri) -> Unit,
    isPosting: Boolean,
    onPost: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onImageSelected(it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            val file = File(context.cacheDir, "camera_capture_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
            }
            onImageSelected(Uri.fromFile(file))
        }
    }

    var showImageSourceSheet by remember { mutableStateOf(false) }

    if (showImageSourceSheet) {
        AlertDialog(
            onDismissRequest = { showImageSourceSheet = false },
            title = { Text("Select Image Source") },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { 
                            galleryLauncher.launch("image/*")
                            showImageSourceSheet = false
                        }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color(0xFF6C5CE7))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Gallery")
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { 
                            cameraLauncher.launch(null)
                            showImageSourceSheet = false
                        }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFF6C5CE7))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Camera")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImageSourceSheet = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
            .verticalScroll(scrollState)
    ) {
        // Image Slot
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFF9FAFB))
                .clickable { showImageSourceSheet = true }
                .border(2.dp, Color(0xFFEEEEF5), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color(0xFF6C5CE7), modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Add clear photos", color = Color.Gray, fontSize = 14.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Give it a title", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = itemName,
            onValueChange = onItemNameChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g. Scientific Calculator") },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFEEEEF5),
                focusedBorderColor = Color(0xFF6C5CE7),
                unfocusedContainerColor = Color(0xFFF9FAFB),
                focusedContainerColor = Color(0xFFF9FAFB)
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Quantity", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = quantity,
            onValueChange = onQuantityChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g. 1") },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFEEEEF5),
                focusedBorderColor = Color(0xFF6C5CE7),
                unfocusedContainerColor = Color(0xFFF9FAFB),
                focusedContainerColor = Color(0xFFF9FAFB)
            )
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onPost,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(),
            enabled = !isPosting
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(listOf(Color(0xFF6C5CE7), Color(0xFFA855F7))),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isPosting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Post Item", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}
