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
                val existingItem = shoppingRepository.getShoppingItemByName(name)
                if (existingItem != null) {
                    val newQuantity = mergeQuantities(existingItem.quantity, quantity)
                    val updatedItem = existingItem.copy(quantity = newQuantity)
                    shoppingRepository.updateShoppingItem(updatedItem)
                } else {
                    val item = ShoppingItem(
                        name = name,
                        quantity = quantity
                    )
                    shoppingRepository.insertShoppingItem(item)
                }
            }
        }
    }
    private fun mergeQuantities(existingQuantity: String?, newQuantity: String?): String {
        val existingNum = parseQuantityNumber(existingQuantity)
        val newNum = parseQuantityNumber(newQuantity)
        val totalNum = existingNum + newNum
        val unit = extractUnit(existingQuantity ?: newQuantity)
        return if (unit.isNotBlank()) {
            "$totalNum $unit"
        } else {
            totalNum.toString()
        }
    }
    private fun parseQuantityNumber(quantity: String?): Int {
        if (quantity.isNullOrBlank()) return 1
        val numberMatch = Regex("^\\d+").find(quantity)
        return numberMatch?.value?.toIntOrNull() ?: 1
    }
    private fun extractUnit(quantity: String?): String {
        if (quantity.isNullOrBlank()) return ""
        return quantity.replace(Regex("^\\d+\\s*"), "").trim()
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
