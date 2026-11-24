package com.example.catalogue.ui.search

import com.example.catalogue.domain.model.CatalogueException
import com.example.catalogue.testutil.FakeCatalogueRepository
import com.example.catalogue.testutil.MainDispatcherRule
import com.example.catalogue.testutil.product
import com.example.catalogue.ui.mvi.Async
import com.example.catalogue.ui.mvi.ErrorKind
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SearchViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()
    private val repo = FakeCatalogueRepository()

    @Test fun `typing alone does not search`() = runTest {
        val vm = SearchViewModel(repo)

        vm.onIntent(SearchIntent.QueryChanged("ring"))

        assertEquals("ring", vm.state.value.query)
        assertNull(vm.state.value.results)
        assertTrue(repo.searchQueries.isEmpty())
    }

    @Test fun `blank query is ignored`() = runTest {
        val vm = SearchViewModel(repo)

        vm.onIntent(SearchIntent.QueryChanged("   "))
        vm.onIntent(SearchIntent.SearchSubmitted)

        assertTrue(repo.searchQueries.isEmpty())
        assertNull(vm.state.value.results)
    }

    @Test fun `submitting searches with the trimmed query and shows results`() = runTest {
        repo.searchResult = Result.success(listOf(product(1), product(2)))
        val vm = SearchViewModel(repo)

        vm.onIntent(SearchIntent.QueryChanged("  ring "))
        vm.onIntent(SearchIntent.SearchSubmitted)

        assertEquals(listOf("ring"), repo.searchQueries)
        assertEquals("ring", vm.state.value.submittedQuery)
        assertEquals(Async.Success(listOf(product(1), product(2))), vm.state.value.results)
    }

    @Test fun `editing the query after a search keeps the current results`() = runTest {
        repo.searchResult = Result.success(listOf(product(1)))
        val vm = SearchViewModel(repo)
        vm.onIntent(SearchIntent.QueryChanged("ring"))
        vm.onIntent(SearchIntent.SearchSubmitted)

        vm.onIntent(SearchIntent.QueryChanged("rings and"))

        assertEquals("ring", vm.state.value.submittedQuery)
        assertEquals(Async.Success(listOf(product(1))), vm.state.value.results)
    }

    @Test fun `empty result list is a success with no items`() = runTest {
        repo.searchResult = Result.success(emptyList())
        val vm = SearchViewModel(repo)
        vm.onIntent(SearchIntent.QueryChanged("zzz"))
        vm.onIntent(SearchIntent.SearchSubmitted)

        assertEquals(Async.Success(emptyList<Nothing>()), vm.state.value.results)
    }

    @Test fun `failure is reported without losing the query`() = runTest {
        repo.searchResult = Result.failure(CatalogueException.NetworkUnavailable())
        val vm = SearchViewModel(repo)
        vm.onIntent(SearchIntent.QueryChanged("ring"))
        vm.onIntent(SearchIntent.SearchSubmitted)

        assertEquals(Async.Failure(ErrorKind.Network), vm.state.value.results)
        assertEquals("ring", vm.state.value.query)
    }
}
