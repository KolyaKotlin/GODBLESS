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
                // Проверяем, существует ли уже такой товар в списке (непокупленный)
                val existingItem = shoppingRepository.getShoppingItemByName(name)

                if (existingItem != null) {
                    // Товар уже существует - увеличиваем количество
                    val newQuantity = mergeQuantities(existingItem.quantity, quantity)
                    val updatedItem = existingItem.copy(quantity = newQuantity)
                    shoppingRepository.updateShoppingItem(updatedItem)
                } else {
                    // Товар не существует - создаем новый
                    val item = ShoppingItem(
                        name = name,
                        quantity = quantity
                    )
                    shoppingRepository.insertShoppingItem(item)
                }
            }
        }
    }

    // Вспомогательная функция для объединения количества
    private fun mergeQuantities(existingQuantity: String?, newQuantity: String?): String {
        val existingNum = parseQuantityNumber(existingQuantity)
        val newNum = parseQuantityNumber(newQuantity)
        val totalNum = existingNum + newNum

        // Извлекаем единицу измерения из существующего или нового количества
        val unit = extractUnit(existingQuantity ?: newQuantity)

        return if (unit.isNotBlank()) {
            "$totalNum $unit"
        } else {
            totalNum.toString()
        }
    }

    // Извлечь числовое значение из строки количества
    private fun parseQuantityNumber(quantity: String?): Int {
        if (quantity.isNullOrBlank()) return 1

        // Пытаемся найти число в начале строки
        val numberMatch = Regex("^\\d+").find(quantity)
        return numberMatch?.value?.toIntOrNull() ?: 1
    }

    // Извлечь единицу измерения из строки количества
    private fun extractUnit(quantity: String?): String {
        if (quantity.isNullOrBlank()) return ""

        // Удаляем число и пробелы в начале, оставляя только единицу измерения
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
