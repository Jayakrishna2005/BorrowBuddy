package com.example.borrowbuddy.model

import java.util.UUID

data class User(
    val id: UUID,
    val fullName: String,
    val trustScore: Int,
    val itemsLent: Int = 0,
    val itemsBorrowed: Int = 0,
    val registrationNumber: String? = null,
    val email: String? = null
)

data class Item(
    val id: UUID?,
    val title: String,
    val description: String,
    val condition: String,
    val isAvailable: Boolean = true
)