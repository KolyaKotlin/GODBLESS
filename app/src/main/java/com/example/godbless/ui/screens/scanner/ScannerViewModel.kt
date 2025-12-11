package com.example.godbless.ui.screens.scanner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.godbless.data.remote.OpenFoodProduct
import com.example.godbless.data.repository.OpenFoodFactsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
class ScannerViewModel(
    private val openFoodFactsRepository: OpenFoodFactsRepository
) : ViewModel() {
    private val _scannedProduct = MutableStateFlow<OpenFoodProduct?>(null)
    val scannedProduct: StateFlow<OpenFoodProduct?> = _scannedProduct.asStateFlow()
    private val _searchResults = MutableStateFlow<List<OpenFoodProduct>>(emptyList())
    val searchResults: StateFlow<List<OpenFoodProduct>> = _searchResults.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    fun searchByBarcode(barcode: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = openFoodFactsRepository.getProductByBarcode(barcode)
            result.onSuccess { product ->
                _scannedProduct.value = product
                _isLoading.value = false
            }.onFailure { exception ->
                _scannedProduct.value = null
                _error.value = "Продукт не найден в базе OpenFoodFacts"
                _isLoading.value = false
            }
        }
    }
    fun searchProducts(query: String) {
        if (query.length < 2) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val result = openFoodFactsRepository.searchProducts(query)
            result.onSuccess { products ->
                _searchResults.value = products
                _isLoading.value = false
            }.onFailure {
                _searchResults.value = emptyList()
                _isLoading.value = false
            }
        }
    }
    fun clearSearch() {
        _searchResults.value = emptyList()
        _scannedProduct.value = null
        _error.value = null
    }
}
class ScannerViewModelFactory(
    private val repository: OpenFoodFactsRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScannerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScannerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
