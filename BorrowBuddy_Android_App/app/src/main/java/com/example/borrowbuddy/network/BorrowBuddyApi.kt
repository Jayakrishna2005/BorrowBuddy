package com.example.borrowbuddy.network

import com.example.borrowbuddy.model.Item
import com.example.borrowbuddy.model.User
import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.PATCH
import retrofit2.http.Multipart
import retrofit2.http.Part
import okhttp3.MultipartBody
import okhttp3.RequestBody

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

interface BorrowBuddyApi {
    @GET("api/v1/items/")
    suspend fun getItems(): List<Item>

    @GET("api/v1/items/{itemId}/")
    suspend fun getItem(@Path("itemId") itemId: String): Item

    @Multipart
    @POST("api/v1/items/")
    suspend fun createItem(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("condition") condition: RequestBody,
        @Part("is_available") isAvailable: RequestBody,
        @Part("category") categoryId: RequestBody,
        @Part("max_borrow_days") maxBorrowDays: RequestBody,
        @Part("owner") ownerId: RequestBody,
        @Part image: MultipartBody.Part?
    ): Item

    @GET("api/v1/categories/")
    suspend fun getCategories(): List<CategoryDTO>

    @POST("api/v1/auth/login/")
    suspend fun login(@Body request: LoginRequest): retrofit2.Response<User>

    @POST("api/v1/auth/register/")
    suspend fun register(@Body request: RegisterRequest): retrofit2.Response<AuthResponse>

    @POST("api/v1/auth/verify-otp/")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): retrofit2.Response<User>

    @POST("api/v1/auth/forgot-password/")
    suspend fun forgotPassword(@Body request: ForgotPwdRequest): retrofit2.Response<AuthResponse>

    @POST("api/v1/auth/reset-password/")
    suspend fun resetPassword(@Body request: ResetPwdRequest): retrofit2.Response<AuthResponse>

    @POST("api/v1/auth/change-password/{userId}/")
    suspend fun changePassword(@Path("userId") userId: String, @Body request: ChangePwdRequest): retrofit2.Response<AuthResponse>

    @POST("api/v1/requests/")
    suspend fun createBooking(@Body booking: BookingRequest): BookingResponse

    @GET("api/v1/users/{userId}/bookings/")
    suspend fun getUserBookings(
        @Path("userId") userId: String,
        @retrofit2.http.Query("user_id") userQueryId: String? = null
    ): UserBookingsResponse

    @GET("api/v1/auth/profile/{userId}/")
    suspend fun getProfile(@Path("userId") userId: String): User

    @Multipart
    @PATCH("api/v1/auth/profile/{userId}/")
    suspend fun updateProfile(
        @Path("userId") userId: String,
        @Part("fullName") fullName: okhttp3.RequestBody?,
        @Part profilePhoto: okhttp3.MultipartBody.Part?
    ): User

    @PATCH("api/v1/bookings/{bookingId}/")
    suspend fun updateBookingStatus(@Path("bookingId") bookingId: String, @Body status: StatusUpdate): BookingDTO

    @GET("api/v1/bookings/{bookingId}/")
    suspend fun getBooking(@Path("bookingId") bookingId: String): BookingDTO

    @GET("api/v1/bookings/{bookingId}/messages/")
    suspend fun getMessages(
        @Path("bookingId") bookingId: String,
        @retrofit2.http.Query("user_id") userId: String? = null
    ): List<MessageDTO>

    @POST("api/v1/bookings/{bookingId}/messages/")
    suspend fun sendMessage(@Path("bookingId") bookingId: String, @Body message: MessageRequest): MessageDTO

    @POST("api/v1/reviews/")
    suspend fun createReview(@Body review: ReviewRequest): ReviewDTO

    @GET("api/v1/users/{userId}/reviews/")
    suspend fun getUserReviews(@Path("userId") userId: String): List<ReviewDTO>

    @GET("api/v1/leaderboard/")
    suspend fun getLeaderboard(): List<User>

    @retrofit2.http.DELETE("api/v1/items/{itemId}/")
    suspend fun deleteItem(@Path("itemId") itemId: String): retrofit2.Response<Unit>

    companion object {
        private var instance: BorrowBuddyApi? = null
        private var currentBaseUrl: String? = null

        fun create(): BorrowBuddyApi {
            val context = com.example.borrowbuddy.BorrowBuddyApplication.getContext()
            val sessionManager = com.example.borrowbuddy.util.SessionManager(context)
            val baseUrl = sessionManager.getBaseUrl()
            
            if (instance == null || baseUrl != currentBaseUrl) {
                currentBaseUrl = baseUrl
                val client = OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build()

                instance = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(BorrowBuddyApi::class.java)
            }
            return instance!!
        }
    }
}

data class StatusUpdate(val status: String)

data class CategoryDTO(
    val id: Int,
    val name: String
)

data class UserBookingsResponse(
    val sent: List<BookingDTO>?,
    val received: List<BookingDTO>?
)

data class BookingDTO(
    val id: String?,
    val item: String?,
    val borrower: String?,
    val status: String?,
    @SerializedName("request_date")
    val requestDate: String?,
    @SerializedName("due_date")
    val dueDate: String? = null,
    @SerializedName("penalty_amount")
    val penaltyAmount: Int? = 0,
    @SerializedName("has_review")
    val hasReview: Boolean? = false,
    @SerializedName("item_name")
    val itemName: String? = "",
    @SerializedName("item_image")
    val itemImage: String? = null,
    @SerializedName("owner_name")
    val ownerName: String? = "",
    @SerializedName("item_owner_id")
    val itemOwnerId: String? = "",
    @SerializedName("borrower_name")
    val borrowerName: String? = "",
    @SerializedName("unread_count")
    val unreadCount: Int? = 0
)

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
    val email: String,
    val password: String,
    val regNumber: String? = null // For fallback
)

data class RegisterRequest(
    val name: String,
    val regNumber: String,
    val email: String,
    val password: String
)

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

data class ForgotPwdRequest(
    val email: String
)

data class ResetPwdRequest(
    val email: String,
    val otp: String,
    @SerializedName("newPassword") val newPassword: String
)

data class ChangePwdRequest(
    @SerializedName("oldPassword") val oldPassword: String,
    @SerializedName("newPassword") val newPassword: String
)

data class AuthResponse(
    val message: String?,
    val error: String?,
    @SerializedName("needs_verification") val needsVerification: Boolean? = false
)

data class MessageDTO(
    val id: String?,
    val booking: String,
    val sender: String,
    @SerializedName("message_text")
    val messageText: String,
    val timestamp: String,
    val status: String? = "SENT",
    @SerializedName("is_seen")
    val isSeen: Boolean = false
)

data class MessageRequest(
    val sender: String, // UUID
    val content: String
)

data class ReviewDTO(
    val id: Int,
    val item: String,
    @SerializedName("item_title")
    val itemTitle: String? = null,
    val reviewer: String,
    @SerializedName("reviewer_name")
    val reviewerName: String? = null,
    val rating: Int,
    val comment: String,
    val booking: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null
)

data class ReviewRequest(
    val item: String, // UUID
    val reviewer: String, // UUID
    val rating: Int,
    val comment: String,
    val booking: String
)