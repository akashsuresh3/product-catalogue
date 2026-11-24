package com.example.catalogue.ui.common

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.example.catalogue.domain.model.Product

fun LazyGridScope.sectionHeader(title: String) {
    item(span = { GridItemSpan(maxLineSpan) }) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
    }
}

/** [section] namespaces the keys so the same product can appear in two sections without clashing. */
fun LazyGridScope.productItems(section: String, products: List<Product>, onClick: (Int) -> Unit) {
    items(products, key = { "$section-${it.id}" }) { product ->
        ProductTile(product = product, onClick = { onClick(product.id) })
    }
}
