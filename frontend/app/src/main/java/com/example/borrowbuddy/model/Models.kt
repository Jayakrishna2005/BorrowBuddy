package com.example.borrowbuddy.model

import java.util.UUID
import com.google.gson.annotations.SerializedName

data class User(
    val id: UUID,
    val fullName: String,
    val trustScore: Float,
    val itemsLent: Int = 0,
    val itemsBorrowed: Int = 0,
    val registrationNumber: String? = null,
    val email: String? = null,
    @SerializedName("profile_photo")
    val profilePhoto: String? = null,
    val points: Int = 0,
    val level: Int = 1,
    val badge: String? = "Novice",
    @SerializedName("sellerSentiment")
    val sellerSentiment: Int? = 100
)

data class Item(
    val id: UUID?,
    val title: String,
    val description: String,
    val condition: String,
    @SerializedName("is_available")
    val isAvailable: Boolean = true,
    @SerializedName("max_borrow_days")
    val maxBorrowDays: Int = 7,
    val quantity: Int = 1,
    @SerializedName("category_name")
    val categoryName: String? = null,
    @SerializedName("owner_id")
    val ownerId: String? = null,
    @SerializedName("owner_name")
    val ownerName: String? = null,
    val image: String? = null,
    @SerializedName("average_rating")
    val averageRating: Float = 0f,
    @SerializedName("reviews_count")
    val reviewsCount: Int = 0,
    val reviews: List<Review>? = null,
    @SerializedName("owner_sentiment")
    val ownerSentiment: Int? = 100
)

data class Review(
    val id: Int? = null,
    val rating: Int,
    val comment: String,
    @SerializedName("reviewer_name")
    val reviewerName: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null
)