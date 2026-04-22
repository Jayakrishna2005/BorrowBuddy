package com.example.borrowbuddy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.borrowbuddy.model.User
import com.example.borrowbuddy.network.BorrowBuddyApi
import com.example.borrowbuddy.network.LoginRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val api: BorrowBuddyApi by lazy {
        Retrofit.Builder()
            // We use 10.0.2.2 to point to localhost from Android Emulator
            .baseUrl("http://10.0.2.2:8080/") 
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BorrowBuddyApi::class.java)
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(name: String, regNumber: String, email: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val user = api.login(LoginRequest(name, regNumber, email))
                _authState.value = AuthState.Success(user)
            } catch (e: Exception) {
                e.printStackTrace()
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }
}
