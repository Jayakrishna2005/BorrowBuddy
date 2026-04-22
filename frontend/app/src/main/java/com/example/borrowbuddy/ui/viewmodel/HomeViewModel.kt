package com.example.borrowbuddy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.borrowbuddy.model.Item
import com.example.borrowbuddy.network.BorrowBuddyApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeViewModel : ViewModel() {

    private val api: BorrowBuddyApi by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/") // Android Emulator localhost
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BorrowBuddyApi::class.java)
    }

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()

    init {
        loadItems()
    }

    fun loadItems() {
        viewModelScope.launch {
            try {
                _items.value = api.getAvailableItems()
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }
}