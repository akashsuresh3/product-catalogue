package com.example.catalogue.testutil

import com.example.catalogue.domain.model.CategoryProducts
import com.example.catalogue.domain.model.Enquiry
import com.example.catalogue.domain.model.Product
import com.example.catalogue.domain.repository.CatalogueRepository

fun product(id: Int = 1, category: String = "jewelery", rating: Double = 4.0) = Product(
    id = id,
    title = "Product $id",
    description = "Description $id",
    price = 9.99,
    category = category,
    imageUrl = "https://img.example/$id.png",
    rating = rating,
    ratingCount = 10,
)

/** Configurable fake. Set the Result you want before constructing the ViewModel. */
class FakeCatalogueRepository : CatalogueRepository {
    var featured: Result<List<Product>> = Result.success(emptyList())
    var categories: Result<List<String>> = Result.success(emptyList())
    var category: Result<CategoryProducts> = Result.success(CategoryProducts(emptyList(), emptyList()))
    var searchResult: Result<List<Product>> = Result.success(emptyList())
    var productResult: Result<Product> = Result.success(product())
    var enquiryResult: Result<Unit> = Result.success(Unit)

    val categoryRequests = mutableListOf<String>()
    val searchQueries = mutableListOf<String>()
    val enquiries = mutableListOf<Enquiry>()

    override suspend fun getFeatured() = featured
    override suspend fun getCategories() = categories
    override suspend fun getCategory(name: String): Result<CategoryProducts> {
        categoryRequests += name
        return category
    }
    override suspend fun search(query: String): Result<List<Product>> {
        searchQueries += query
        return searchResult
    }
    override suspend fun getProduct(id: Int) = productResult
    override suspend fun submitEnquiry(enquiry: Enquiry): Result<Unit> {
        enquiries += enquiry
        return enquiryResult
    }
}
