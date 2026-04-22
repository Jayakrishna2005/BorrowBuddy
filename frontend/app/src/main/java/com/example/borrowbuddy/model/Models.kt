package com.example.borrowbuddy.model

import java.util.UUID

data class User(
    val id: UUID,
    val fullName: String,
    val trustScore: Int
)

data class Item(
    val id: UUID?,
    val title: String,
    val description: String,
    val condition: String,
    val isAvailable: Boolean = true
)