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

    private val api: BorrowBuddyApi get() = BorrowBuddyApi.create()

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadItems() {
        viewModelScope.launch {
            // Only show loader if we have NO items. 
            // If we have items, load in the background for an 'instant' feel.
            if (_items.value.isEmpty()) {
                _isLoading.value = true
            }
            try {
                val newItems = api.getItems()
                if (newItems != _items.value) {
                    _items.value = newItems
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}