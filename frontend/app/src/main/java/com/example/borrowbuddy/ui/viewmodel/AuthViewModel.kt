package com.example.borrowbuddy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.borrowbuddy.model.User
import com.example.borrowbuddy.network.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class ActionSuccess(val message: String) : AuthState()
    data class NeedsVerification(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val api: BorrowBuddyApi get() = BorrowBuddyApi.create()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun login(email: String, password: String, regNumber: String = "") {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = api.login(LoginRequest(email, password, regNumber))
                if (response.isSuccessful && response.body() != null) {
                    _authState.value = AuthState.Success(response.body()!!)
                } else {
                    if (response.code() == 401) {
                        val errorBody = response.errorBody()?.string() ?: ""
                        if (errorBody.contains("needs_verification")) {
                            _authState.value = AuthState.NeedsVerification("Email not verified. OTP sent.")
                            return@launch
                        }
                    }
                    _authState.value = AuthState.Error("Login failed. Check credentials.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _authState.value = AuthState.Error("Network error. Is the backend running?")
            }
        }
    }

    fun register(name: String, regNumber: String, email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = api.register(RegisterRequest(name, regNumber, email, password))
                if (response.isSuccessful) {
                    _authState.value = AuthState.NeedsVerification("OTP sent to email.")
                } else {
                    _authState.value = AuthState.Error("Registration failed. Email or Reg No might exist.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _authState.value = AuthState.Error("Network error.")
            }
        }
    }

    fun verifyOtp(email: String, otp: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = api.verifyOtp(VerifyOtpRequest(email, otp))
                if (response.isSuccessful && response.body() != null) {
                    _authState.value = AuthState.Success(response.body()!!)
                } else {
                    _authState.value = AuthState.Error("Invalid OTP.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _authState.value = AuthState.Error("Network error.")
            }
        }
    }

    fun forgotPassword(email: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = api.forgotPassword(ForgotPwdRequest(email))
                if (response.isSuccessful) {
                    _authState.value = AuthState.ActionSuccess("OTP sent to reset password.")
                } else {
                    _authState.value = AuthState.Error("User with this email not found.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _authState.value = AuthState.Error("Network error.")
            }
        }
    }

    fun resetPassword(email: String, otp: String, newPassword: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = api.resetPassword(ResetPwdRequest(email, otp, newPassword))
                if (response.isSuccessful) {
                    _authState.value = AuthState.ActionSuccess("Password reset successfully. Please login.")
                } else {
                    _authState.value = AuthState.Error("Invalid OTP or error.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _authState.value = AuthState.Error("Network error.")
            }
        }
    }

    fun changePassword(userId: String, oldPwd: String, newPwd: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = api.changePassword(userId, ChangePwdRequest(oldPwd, newPwd))
                if (response.isSuccessful) {
                    _authState.value = AuthState.ActionSuccess("Password changed successfully!")
                } else {
                    _authState.value = AuthState.Error("Incorrect old password or error.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _authState.value = AuthState.Error("Network error.")
            }
        }
    }
}
