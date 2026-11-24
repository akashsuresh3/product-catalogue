package com.example.catalogue.domain.model

data class Product(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val category: String,
    val imageUrl: String,
    val rating: Double,
    val ratingCount: Int,
)

data class CategoryProducts(
    val topRated: List<Product>,
    val others: List<Product>,
)

/** Customer enquiry. [phone] is optional. */
data class Enquiry(
    val productId: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val message: String,
)
