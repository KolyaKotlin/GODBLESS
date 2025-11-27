package com.example.godbless.data.repository

import com.example.godbless.data.remote.OpenFoodFactsApi
import com.example.godbless.data.remote.OpenFoodProduct

class OpenFoodFactsRepository(
    private val api: OpenFoodFactsApi
) {
    suspend fun getProductByBarcode(barcode: String): Result<OpenFoodProduct?> {
        return try {
            val response = api.getProductByBarcode(barcode)
            if (response.isSuccessful && response.body()?.status == 1) {
                Result.success(response.body()?.product)
            } else {
                Result.failure(Exception("Product not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchProducts(query: String): Result<List<OpenFoodProduct>> {
        return try {
            val response = api.searchProducts(query)
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
