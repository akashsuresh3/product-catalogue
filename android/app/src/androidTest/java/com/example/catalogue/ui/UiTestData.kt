package com.example.catalogue.ui

import com.example.catalogue.domain.model.Product

fun testProduct(id: Int) = Product(
    id = id,
    title = "Gold ring $id",
    description = "Description $id",
    price = 12.5,
    category = "jewelery",
    imageUrl = "",
    rating = 4.5,
    ratingCount = 30,
)
