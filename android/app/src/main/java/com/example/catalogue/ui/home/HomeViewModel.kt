package com.example.catalogue.ui.home

import androidx.lifecycle.viewModelScope
import com.example.catalogue.domain.repository.CatalogueRepository
import com.example.catalogue.ui.mvi.Async
import com.example.catalogue.ui.mvi.MviViewModel
import com.example.catalogue.ui.mvi.toErrorKind
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CatalogueRepository,
) : MviViewModel<HomeState, HomeIntent, HomeEffect>(HomeState()) {

    init { load() }

    override fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.Retry -> load()
            is HomeIntent.ProductClicked -> sendEffect(HomeEffect.OpenProduct(intent.id))
            is HomeIntent.CategoryClicked -> sendEffect(HomeEffect.OpenCategory(intent.name))
            HomeIntent.SearchClicked -> sendEffect(HomeEffect.OpenSearch)
        }
    }

    private fun load() {
        setState { copy(content = Async.Loading) }
        viewModelScope.launch {
            val (featured, categories) = coroutineScope {
                val f = async { repository.getFeatured() }
                val c = async { repository.getCategories() }
                f.await() to c.await()
            }
            val error = featured.exceptionOrNull() ?: categories.exceptionOrNull()
            setState {
                copy(
                    content = if (error != null) {
                        Async.Failure(error.toErrorKind())
                    } else {
                        Async.Success(HomeContent(featured.getOrThrow(), categories.getOrThrow()))
                    },
                )
            }
        }
    }
}
