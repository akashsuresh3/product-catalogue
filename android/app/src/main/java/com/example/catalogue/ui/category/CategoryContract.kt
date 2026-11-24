package com.example.catalogue.ui.category

import com.example.catalogue.domain.model.CategoryProducts
import com.example.catalogue.ui.mvi.Async
import com.example.catalogue.ui.mvi.UiEffect
import com.example.catalogue.ui.mvi.UiIntent
import com.example.catalogue.ui.mvi.UiState

data class CategoryState(
    val category: String,
    val content: Async<CategoryProducts> = Async.Loading,
) : UiState

sealed interface CategoryIntent : UiIntent {
    data object Retry : CategoryIntent
    data class ProductClicked(val id: Int) : CategoryIntent
}

sealed interface CategoryEffect : UiEffect {
    data class OpenProduct(val id: Int) : CategoryEffect
}
