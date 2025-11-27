package com.example.godbless.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.godbless.data.repository.ProductRepository
import com.example.godbless.domain.model.Product
import com.example.godbless.domain.model.ProductCategory
import com.example.godbless.domain.model.StorageLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class HomeViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            productRepository.getAllProducts().collect { productList ->
                _products.value = productList
                _isLoading.value = false
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            productRepository.deleteProduct(product)
        }
    }

    fun addProduct(
        name: String,
        brand: String?,
        category: ProductCategory,
        storageLocation: StorageLocation,
        expiryDate: Date,
        barcode: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            val product = Product(
                name = name,
                brand = brand,
                category = category,
                storageLocation = storageLocation,
                expiryDate = expiryDate,
                barcode = barcode,
                notes = notes
            )
            productRepository.insertProduct(product)
        }
    }
}
