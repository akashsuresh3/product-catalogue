package com.example.catalogue.ui.common

import java.util.Locale

// Assumption: all prices are USD, shown as returned by the backend (no currency/locale handling).
fun formatPrice(price: Double): String = "\$" + String.format(Locale.US, "%.2f", price)

/** "men's clothing" -> "Men's Clothing" */
fun displayName(category: String): String =
    category.split(" ").joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
