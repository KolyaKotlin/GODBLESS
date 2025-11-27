package com.example.godbless.data.repository

import com.example.godbless.data.local.ProductDao
import com.example.godbless.data.remote.OpenFoodFactsApi
import com.example.godbless.data.remote.OpenFoodProduct
import com.example.godbless.domain.model.Product
import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val productDao: ProductDao,
    private val openFoodFactsApi: OpenFoodFactsApi
) {

    fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()

    suspend fun getProductById(id: Long): Product? = productDao.getProductById(id)

    suspend fun insertProduct(product: Product): Long = productDao.insertProduct(product)

    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)

    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)

    suspend fun deleteProductById(id: Long) = productDao.deleteProductById(id)

    suspend fun getProductByBarcode(barcode: String): Result<OpenFoodProduct?> {
        return try {
            val response = openFoodFactsApi.getProductByBarcode(barcode)
            if (response.isSuccessful && response.body()?.status == 1) {
                Result.success(response.body()?.product)
            } else {
                Result.failure(Exception("Product not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchProducts(searchTerm: String): Result<List<OpenFoodProduct>> {
        return try {
            val response = openFoodFactsApi.searchProducts(searchTerm)
            if (response.isSuccessful) {
                Result.success(response.body()?.products ?: emptyList())
            } else {
                Result.failure(Exception("Search failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
