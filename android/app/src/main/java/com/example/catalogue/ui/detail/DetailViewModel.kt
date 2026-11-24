package com.example.catalogue.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.catalogue.domain.repository.CatalogueRepository
import com.example.catalogue.ui.mvi.Async
import com.example.catalogue.ui.mvi.MviViewModel
import com.example.catalogue.ui.mvi.toErrorKind
import com.example.catalogue.ui.navigation.NavArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CatalogueRepository,
) : MviViewModel<DetailState, DetailIntent, DetailEffect>(DetailState()) {

    private val productId: Int = checkNotNull(savedStateHandle.get<Int>(NavArgs.PRODUCT_ID))

    init { load() }

    override fun onIntent(intent: DetailIntent) {
        when (intent) {
            DetailIntent.Retry -> load()
            DetailIntent.EnquireClicked -> sendEffect(DetailEffect.OpenEnquiry(productId))
        }
    }

    private fun load() {
        setState { copy(product = Async.Loading) }
        viewModelScope.launch {
            repository.getProduct(productId).fold(
                onSuccess = { setState { copy(product = Async.Success(it)) } },
                onFailure = { setState { copy(product = Async.Failure(it.toErrorKind())) } },
            )
        }
    }
}
