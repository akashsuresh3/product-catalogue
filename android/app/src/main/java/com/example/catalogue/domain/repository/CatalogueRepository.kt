package com.example.catalogue.domain.repository

import com.example.catalogue.domain.model.CategoryProducts
import com.example.catalogue.domain.model.Enquiry
import com.example.catalogue.domain.model.Product

/**
 * The only door the UI layer uses to reach catalogue data.
 * Failures are returned as [Result.failure] carrying a CatalogueException.
 */
interface CatalogueRepository {
    suspend fun getFeatured(): Result<List<Product>>
    suspend fun getCategories(): Result<List<String>>
    suspend fun getCategory(name: String): Result<CategoryProducts>
    suspend fun search(query: String): Result<List<Product>>
    suspend fun getProduct(id: Int): Result<Product>
    suspend fun submitEnquiry(enquiry: Enquiry): Result<Unit>
}
