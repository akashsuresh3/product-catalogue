package com.example.catalogue.ui.search

import com.example.catalogue.domain.model.Product
import com.example.catalogue.ui.mvi.Async
import com.example.catalogue.ui.mvi.UiEffect
import com.example.catalogue.ui.mvi.UiIntent
import com.example.catalogue.ui.mvi.UiState

/**
 * [query] is what the user is typing; [submittedQuery] is what the results below belong to.
 * Results only change when the search button is pressed (per the acceptance criteria).
 * [results] is null until the first search.
 */
data class SearchState(
    val query: String = "",
    val submittedQuery: String? = null,
    val results: Async<List<Product>>? = null,
) : UiState

sealed interface SearchIntent : UiIntent {
    data class QueryChanged(val query: String) : SearchIntent
    data object SearchSubmitted : SearchIntent
    data class ProductClicked(val id: Int) : SearchIntent
}

sealed interface SearchEffect : UiEffect {
    data class OpenProduct(val id: Int) : SearchEffect
}
