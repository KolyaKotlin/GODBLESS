package com.example.godbless.data.remote
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
interface OpenFoodFactsApi {
    @GET("api/v0/product/{barcode}.json")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): Response<OpenFoodFactsResponse>
    @GET("cgi/search.pl")
    suspend fun searchProducts(
        @Query("search_terms") searchTerms: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("json") json: Boolean = true
    ): Response<OpenFoodFactsSearchResponse>
}
