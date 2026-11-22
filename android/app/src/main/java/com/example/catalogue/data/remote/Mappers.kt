package com.example.catalogue.data.remote

import com.example.catalogue.data.remote.dto.CategoryResponse
import com.example.catalogue.data.remote.dto.EnquiryRequest
import com.example.catalogue.data.remote.dto.ProductDto
import com.example.catalogue.domain.model.CategoryProducts
import com.example.catalogue.domain.model.Enquiry
import com.example.catalogue.domain.model.Product

fun ProductDto.toDomain() = Product(
    id = id,
    title = title,
    description = description,
    price = price,
    category = category,
    imageUrl = imageUrl,
    rating = rating.rate,
    ratingCount = rating.count,
)

fun CategoryResponse.toDomain() = CategoryProducts(
    topRated = topRated.map { it.toDomain() },
    others = others.map { it.toDomain() },
)

fun Enquiry.toRequest() = EnquiryRequest(
    productId = productId,
    name = name.trim(),
    email = email.trim(),
    phone = phone?.trim()?.takeIf { it.isNotEmpty() },
    message = message.trim(),
)
