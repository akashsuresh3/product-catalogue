package com.example.catalogue.data.remote

import com.example.catalogue.data.remote.dto.CategoriesResponse
import com.example.catalogue.data.remote.dto.CategoryResponse
import com.example.catalogue.data.remote.dto.EnquiryRequest
import com.example.catalogue.data.remote.dto.HomeResponse
import com.example.catalogue.data.remote.dto.ProductDto
import com.example.catalogue.data.remote.dto.ProductsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CatalogueApi {
    @GET("home") suspend fun getHome(): HomeResponse
    @GET("categories") suspend fun getCategories(): CategoriesResponse

    // @Path is URL-encoded by default, which matters for names like "men's clothing".
    @GET("categories/{name}") suspend fun getCategory(@Path("name") name: String): CategoryResponse

    @GET("products") suspend fun searchProducts(@Query("q") query: String): ProductsResponse
    @GET("products/{id}") suspend fun getProduct(@Path("id") id: Int): ProductDto

    @POST("enquiries") suspend fun submitEnquiry(@Body body: EnquiryRequest)
}
