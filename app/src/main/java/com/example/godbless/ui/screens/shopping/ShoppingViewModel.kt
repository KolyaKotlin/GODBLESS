package com.example.godbless.ui.screens.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.godbless.data.repository.ShoppingRepository
import com.example.godbless.domain.model.ShoppingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShoppingViewModel(
    private val shoppingRepository: ShoppingRepository
) : ViewModel() {

    private val _shoppingItems = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val shoppingItems: StateFlow<List<ShoppingItem>> = _shoppingItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadShoppingItems()
    }

    private fun loadShoppingItems() {
        viewModelScope.launch {
            _isLoading.value = true
            shoppingRepository.getAllShoppingItems().collect { items ->
                _shoppingItems.value = items
                _isLoading.value = false
            }
        }
    }

    fun addShoppingItem(name: String, quantity: String? = null) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                val item = ShoppingItem(
                    name = name,
                    quantity = quantity
                )
                shoppingRepository.insertShoppingItem(item)
            }
        }
    }

    fun togglePurchased(item: ShoppingItem) {
        viewModelScope.launch {
            val updated = item.copy(isPurchased = !item.isPurchased)
            shoppingRepository.updateShoppingItem(updated)
        }
    }

    fun deleteItem(item: ShoppingItem) {
        viewModelScope.launch {
            shoppingRepository.deleteShoppingItem(item)
        }
    }

    fun deletePurchasedItems() {
        viewModelScope.launch {
            shoppingRepository.deletePurchasedItems()
        }
    }
}
