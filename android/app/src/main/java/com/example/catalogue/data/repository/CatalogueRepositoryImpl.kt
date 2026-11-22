package com.example.catalogue.data.repository

import com.example.catalogue.data.remote.CatalogueApi
import com.example.catalogue.data.remote.dto.ValidationErrorResponse
import com.example.catalogue.data.remote.toDomain
import com.example.catalogue.data.remote.toRequest
import com.example.catalogue.domain.model.CatalogueException
import com.example.catalogue.domain.model.CategoryProducts
import com.example.catalogue.domain.model.Enquiry
import com.example.catalogue.domain.model.Product
import com.example.catalogue.domain.repository.CatalogueRepository
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class CatalogueRepositoryImpl @Inject constructor(
    private val api: CatalogueApi,
    private val json: Json,
) : CatalogueRepository {

    override suspend fun getFeatured(): Result<List<Product>> =
        safeCall { api.getHome().featured.map { it.toDomain() } }

    override suspend fun getCategories(): Result<List<String>> =
        safeCall { api.getCategories().categories }

    override suspend fun getCategory(name: String): Result<CategoryProducts> =
        safeCall { api.getCategory(name).toDomain() }

    override suspend fun search(query: String): Result<List<Product>> =
        safeCall { api.searchProducts(query.trim()).products.map { it.toDomain() } }

    override suspend fun getProduct(id: Int): Result<Product> =
        safeCall { api.getProduct(id).toDomain() }

    override suspend fun submitEnquiry(enquiry: Enquiry): Result<Unit> =
        safeCall { api.submitEnquiry(enquiry.toRequest()) }

    private suspend fun <T> safeCall(block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e // never swallow coroutine cancellation
        } catch (e: IOException) {
            Result.failure(CatalogueException.NetworkUnavailable(e))
        } catch (e: HttpException) {
            Result.failure(e.toDomain())
        } catch (e: Exception) {
            // Includes SerializationException (unexpected payload shape).
            Result.failure(CatalogueException.Unknown(e))
        }

    private fun HttpException.toDomain(): CatalogueException = when (code()) {
        404 -> CatalogueException.NotFound()
        400 -> {
            val body = response()?.errorBody()?.string().orEmpty()
            val errors = runCatching { json.decodeFromString<ValidationErrorResponse>(body) }
                .getOrNull()?.errors.orEmpty()
            CatalogueException.Validation(errors)
        }
        else -> CatalogueException.Server(code())
    }
}
