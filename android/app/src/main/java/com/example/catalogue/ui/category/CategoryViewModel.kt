package com.example.catalogue.ui.category

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
class CategoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CatalogueRepository,
) : MviViewModel<CategoryState, CategoryIntent, CategoryEffect>(
    CategoryState(category = checkNotNull(savedStateHandle.get<String>(NavArgs.CATEGORY_NAME))),
) {

    init { load() }

    override fun onIntent(intent: CategoryIntent) {
        when (intent) {
            CategoryIntent.Retry -> load()
            is CategoryIntent.ProductClicked -> sendEffect(CategoryEffect.OpenProduct(intent.id))
        }
    }

    private fun load() {
        setState { copy(content = Async.Loading) }
        viewModelScope.launch {
            repository.getCategory(currentState.category).fold(
                onSuccess = { setState { copy(content = Async.Success(it)) } },
                onFailure = { setState { copy(content = Async.Failure(it.toErrorKind())) } },
            )
        }
    }
}
