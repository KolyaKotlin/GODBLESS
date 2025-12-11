package com.example.godbless.ui.screens.addproduct
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.godbless.NeprosrochApp
import com.example.godbless.R
import com.example.godbless.data.remote.OpenFoodProduct
import com.example.godbless.data.repository.ProductRepository
import com.example.godbless.domain.model.Product
import com.example.godbless.domain.model.ProductCategory
import com.example.godbless.domain.model.StorageLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
class AddEditProductViewModel(
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val productId: Long = savedStateHandle.get<String>("productId")?.toLongOrNull() ?: -1L
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()
    private val _brand = MutableStateFlow("")
    val brand: StateFlow<String> = _brand.asStateFlow()
    private val _category = MutableStateFlow(ProductCategory.OTHER)
    val category: StateFlow<ProductCategory> = _category.asStateFlow()
    private val _storageLocation = MutableStateFlow(StorageLocation.FRIDGE)
    val storageLocation: StateFlow<StorageLocation> = _storageLocation.asStateFlow()
    private val _expiryDate = MutableStateFlow(Date())
    val expiryDate: StateFlow<Date> = _expiryDate.asStateFlow()
    private val _barcode = MutableStateFlow("")
    val barcode: StateFlow<String> = _barcode.asStateFlow()
    private val _imageUrl = MutableStateFlow("")
    val imageUrl: StateFlow<String> = _imageUrl.asStateFlow()
    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()
    private val _searchResults = MutableStateFlow<List<OpenFoodProduct>>(emptyList())
    val searchResults: StateFlow<List<OpenFoodProduct>> = _searchResults.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    init {
        if (productId != -1L) {
            loadProduct()
        }
    }
    private fun loadProduct() {
        viewModelScope.launch {
            _isLoading.value = true
            val product = productRepository.getProductById(productId)
            product?.let {
                _name.value = it.name
                _brand.value = it.brand ?: ""
                _category.value = it.category
                _storageLocation.value = it.storageLocation
                _expiryDate.value = it.expiryDate
                _barcode.value = it.barcode ?: ""
                _imageUrl.value = it.imageUrl ?: ""
                _notes.value = it.notes ?: ""
            }
            _isLoading.value = false
        }
    }
    fun updateName(value: String) {
        _name.value = value
    }
    fun updateBrand(value: String) {
        _brand.value = value
    }
    fun updateCategory(value: ProductCategory) {
        _category.value = value
    }
    fun updateStorageLocation(value: StorageLocation) {
        _storageLocation.value = value
    }
    fun updateExpiryDate(value: Date) {
        _expiryDate.value = value
    }
    fun updateBarcode(value: String) {
        _barcode.value = value
    }
    fun updateImageUrl(value: String) {
        _imageUrl.value = value
    }
    fun updateNotes(value: String) {
        _notes.value = value
    }
    fun searchByBarcode(barcode: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = productRepository.getProductByBarcode(barcode)
            result.onSuccess { product ->
                product?.let {
                    _name.value = it.productName ?: ""
                    _brand.value = it.brands ?: ""
                    _imageUrl.value = it.imageUrl ?: ""
                    _barcode.value = barcode
                }
            }.onFailure { exception ->
                _error.value = exception.message ?: NeprosrochApp.instance.getString(R.string.error_product_not_found)
            }
            _isLoading.value = false
        }
    }
    fun searchProducts(searchTerm: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = productRepository.searchProducts(searchTerm)
            result.onSuccess { products ->
                _searchResults.value = products
            }.onFailure { exception ->
                _error.value = exception.message ?: NeprosrochApp.instance.getString(R.string.error_search_failed)
            }
            _isLoading.value = false
        }
    }
    fun selectProduct(product: OpenFoodProduct) {
        _name.value = product.productName ?: ""
        _brand.value = product.brands ?: ""
        _imageUrl.value = product.imageUrl ?: ""
        _barcode.value = product.barcode ?: ""
        _searchResults.value = emptyList()
    }
    fun saveProduct(onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (_name.value.isBlank()) {
                _error.value = NeprosrochApp.instance.getString(R.string.error_name_empty)
                return@launch
            }
            _isLoading.value = true
            val product = Product(
                id = if (productId == -1L) 0 else productId,
                name = _name.value,
                brand = _brand.value.ifBlank { null },
                category = _category.value,
                storageLocation = _storageLocation.value,
                expiryDate = _expiryDate.value,
                barcode = _barcode.value.ifBlank { null },
                imageUrl = _imageUrl.value.ifBlank { null },
                notes = _notes.value.ifBlank { null },
                updatedAt = Date()
            )
            if (productId == -1L) {
                productRepository.insertProduct(product)
            } else {
                productRepository.updateProduct(product)
            }
            _isLoading.value = false
            onSuccess()
        }
    }
    fun clearError() {
        _error.value = null
    }
}
