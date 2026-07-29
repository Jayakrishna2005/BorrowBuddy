package com.example.borow_buddy_frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.borow_buddy_frontend.ui.theme.Borow_Buddy_FrontendTheme
import com.example.borrowbuddy.ui.navigation.BorrowBuddyApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Borow_Buddy_FrontendTheme {
                BorrowBuddyApp()
            }
        }
    }
}