package com.example.catalogue.ui.category

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.catalogue.domain.model.CatalogueException
import com.example.catalogue.domain.model.CategoryProducts
import com.example.catalogue.testutil.FakeCatalogueRepository
import com.example.catalogue.testutil.MainDispatcherRule
import com.example.catalogue.testutil.product
import com.example.catalogue.ui.mvi.Async
import com.example.catalogue.ui.mvi.ErrorKind
import com.example.catalogue.ui.navigation.NavArgs
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CategoryViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()
    private val repo = FakeCatalogueRepository()
    private val handle = SavedStateHandle(mapOf(NavArgs.CATEGORY_NAME to "jewelery"))

    @Test fun `loads top rated and other products for the route's category`() = runTest {
        val data = CategoryProducts(topRated = listOf(product(1)), others = listOf(product(2), product(3)))
        repo.category = Result.success(data)

        val vm = CategoryViewModel(handle, repo)

        assertEquals(listOf("jewelery"), repo.categoryRequests)
        assertEquals("jewelery", vm.state.value.category)
        assertEquals(Async.Success(data), vm.state.value.content)
    }

    @Test fun `not found surfaces as NotFound`() = runTest {
        repo.category = Result.failure(CatalogueException.NotFound())

        val vm = CategoryViewModel(handle, repo)

        assertEquals(Async.Failure(ErrorKind.NotFound), vm.state.value.content)
    }

    @Test fun `product click emits OpenProduct`() = runTest {
        val vm = CategoryViewModel(handle, repo)
        vm.effects.test {
            vm.onIntent(CategoryIntent.ProductClicked(3))
            assertEquals(CategoryEffect.OpenProduct(3), awaitItem())
        }
    }
}
