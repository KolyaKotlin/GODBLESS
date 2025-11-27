package com.example.godbless.data.remote

import com.google.gson.annotations.SerializedName

data class OpenFoodFactsResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("product")
    val product: OpenFoodProduct?
)

data class OpenFoodProduct(
    @SerializedName("product_name")
    val productName: String?,
    @SerializedName("brands")
    val brands: String?,
    @SerializedName("categories")
    val categories: String?,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("code")
    val barcode: String?
)

data class OpenFoodFactsSearchResponse(
    @SerializedName("count")
    val count: Int,
    @SerializedName("page")
    val page: Int,
    @SerializedName("page_size")
    val pageSize: Int,
    @SerializedName("products")
    val products: List<OpenFoodProduct>
)
