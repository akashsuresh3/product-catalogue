package com.example.catalogue.ui.search

import androidx.lifecycle.viewModelScope
import com.example.catalogue.domain.repository.CatalogueRepository
import com.example.catalogue.ui.mvi.Async
import com.example.catalogue.ui.mvi.MviViewModel
import com.example.catalogue.ui.mvi.toErrorKind
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: CatalogueRepository,
) : MviViewModel<SearchState, SearchIntent, SearchEffect>(SearchState()) {

    private var searchJob: Job? = null

    override fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.QueryChanged -> setState { copy(query = intent.query) }
            SearchIntent.SearchSubmitted -> search()
            is SearchIntent.ProductClicked -> sendEffect(SearchEffect.OpenProduct(intent.id))
        }
    }

    private fun search() {
        val query = currentState.query.trim()
        if (query.isEmpty()) return // assumption: a blank search does nothing

        searchJob?.cancel() // a newer search supersedes an in-flight one
        setState { copy(submittedQuery = query, results = Async.Loading) }
        searchJob = viewModelScope.launch {
            repository.search(query).fold(
                onSuccess = { setState { copy(results = Async.Success(it)) } },
                onFailure = { setState { copy(results = Async.Failure(it.toErrorKind())) } },
            )
        }
    }
}
