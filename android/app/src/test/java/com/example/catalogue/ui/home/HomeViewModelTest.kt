package com.example.catalogue.ui.home

import app.cash.turbine.test
import com.example.catalogue.domain.model.CatalogueException
import com.example.catalogue.testutil.FakeCatalogueRepository
import com.example.catalogue.testutil.MainDispatcherRule
import com.example.catalogue.testutil.product
import com.example.catalogue.ui.mvi.Async
import com.example.catalogue.ui.mvi.ErrorKind
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()
    private val repo = FakeCatalogueRepository()

    @Test fun `loads featured products and categories`() = runTest {
        repo.featured = Result.success(listOf(product(1), product(2)))
        repo.categories = Result.success(listOf("jewelery", "electronics"))

        val vm = HomeViewModel(repo)

        assertEquals(
            Async.Success(HomeContent(listOf(product(1), product(2)), listOf("jewelery", "electronics"))),
            vm.state.value.content,
        )
    }

    @Test fun `network failure becomes a Network error state`() = runTest {
        repo.featured = Result.failure(CatalogueException.NetworkUnavailable())

        val vm = HomeViewModel(repo)

        assertEquals(Async.Failure(ErrorKind.Network), vm.state.value.content)
    }

    @Test fun `failure in either call fails the whole screen`() = runTest {
        repo.categories = Result.failure(CatalogueException.Server(500))

        val vm = HomeViewModel(repo)

        assertEquals(Async.Failure(ErrorKind.Server), vm.state.value.content)
    }

    @Test fun `retry recovers after a failure`() = runTest {
        repo.featured = Result.failure(CatalogueException.NetworkUnavailable())
        val vm = HomeViewModel(repo)

        repo.featured = Result.success(listOf(product(1)))
        vm.onIntent(HomeIntent.Retry)

        assertEquals(Async.Success(HomeContent(listOf(product(1)), emptyList())), vm.state.value.content)
    }

    @Test fun `clicks are emitted as one-off effects`() = runTest {
        val vm = HomeViewModel(repo)
        vm.effects.test {
            vm.onIntent(HomeIntent.ProductClicked(7))
            assertEquals(HomeEffect.OpenProduct(7), awaitItem())
            vm.onIntent(HomeIntent.CategoryClicked("men's clothing"))
            assertEquals(HomeEffect.OpenCategory("men's clothing"), awaitItem())
            vm.onIntent(HomeIntent.SearchClicked)
            assertEquals(HomeEffect.OpenSearch, awaitItem())
        }
    }
}
