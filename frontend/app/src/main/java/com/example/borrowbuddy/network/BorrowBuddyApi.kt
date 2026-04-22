package com.example.borrowbuddy.network

import com.example.borrowbuddy.model.Item
import com.example.borrowbuddy.model.User
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body

interface BorrowBuddyApi {
    @GET("api/v1/items")
    suspend fun getAvailableItems(): List<Item>

    @POST("api/v1/items")
    suspend fun createItem(@Body item: Item): Item

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): User

    @POST("api/v1/requests")
    suspend fun createBooking(@Body booking: BookingRequest): BookingResponse
}

data class BookingRequest(
    val item: String, // UUID
    val borrower: String, // UUID
    val status: String = "PENDING"
)

data class BookingResponse(
    val id: String,
    val status: String
)

data class LoginRequest(
    val name: String,
    val regNumber: String,
    val email: String
)