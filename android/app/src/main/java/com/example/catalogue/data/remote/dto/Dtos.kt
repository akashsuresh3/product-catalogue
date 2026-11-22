package com.example.catalogue.data.remote.dto

import kotlinx.serialization.Serializable

// Wire contract with OUR backend (not the Fake Store API). The backend owns the mapping from
// whatever the product source returns, so the client never changes when the source does.

@Serializable
data class RatingDto(val rate: Double, val count: Int)

@Serializable
data class ProductDto(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val category: String,
    val imageUrl: String,
    val rating: RatingDto,
)

@Serializable data class HomeResponse(val featured: List<ProductDto>)
@Serializable data class CategoriesResponse(val categories: List<String>)
@Serializable data class CategoryResponse(val topRated: List<ProductDto>, val others: List<ProductDto>)
@Serializable data class ProductsResponse(val products: List<ProductDto>)

@Serializable
data class EnquiryRequest(
    val productId: Int,
    val name: String,
    val email: String,
    val phone: String? = null,
    val message: String,
)

/** 400 body: { "errors": { "email": "Enter a valid email address" } } */
@Serializable
data class ValidationErrorResponse(val errors: Map<String, String> = emptyMap())
