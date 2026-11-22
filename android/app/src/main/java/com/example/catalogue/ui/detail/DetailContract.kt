package com.example.catalogue.ui.detail

import com.example.catalogue.domain.model.Product
import com.example.catalogue.ui.mvi.Async
import com.example.catalogue.ui.mvi.UiEffect
import com.example.catalogue.ui.mvi.UiIntent
import com.example.catalogue.ui.mvi.UiState

data class DetailState(val product: Async<Product> = Async.Loading) : UiState

sealed interface DetailIntent : UiIntent {
    data object Retry : DetailIntent
    data object EnquireClicked : DetailIntent
}

sealed interface DetailEffect : UiEffect {
    data class OpenEnquiry(val productId: Int) : DetailEffect
}
