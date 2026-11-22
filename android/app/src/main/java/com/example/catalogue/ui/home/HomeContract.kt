package com.example.catalogue.ui.home

import com.example.catalogue.domain.model.Product
import com.example.catalogue.ui.mvi.Async
import com.example.catalogue.ui.mvi.UiEffect
import com.example.catalogue.ui.mvi.UiIntent
import com.example.catalogue.ui.mvi.UiState

data class HomeContent(val featured: List<Product>, val categories: List<String>)

data class HomeState(val content: Async<HomeContent> = Async.Loading) : UiState

sealed interface HomeIntent : UiIntent {
    data object Retry : HomeIntent
    data class ProductClicked(val id: Int) : HomeIntent
    data class CategoryClicked(val name: String) : HomeIntent
    data object SearchClicked : HomeIntent
}

sealed interface HomeEffect : UiEffect {
    data class OpenProduct(val id: Int) : HomeEffect
    data class OpenCategory(val name: String) : HomeEffect
    data object OpenSearch : HomeEffect
}
