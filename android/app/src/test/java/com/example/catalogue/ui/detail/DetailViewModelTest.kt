package com.example.catalogue.ui.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.catalogue.domain.model.CatalogueException
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

class DetailViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()
    private val repo = FakeCatalogueRepository()
    private val handle = SavedStateHandle(mapOf(NavArgs.PRODUCT_ID to 5))

    @Test fun `loads the product`() = runTest {
        repo.productResult = Result.success(product(5))

        val vm = DetailViewModel(handle, repo)

        assertEquals(Async.Success(product(5)), vm.state.value.product)
    }

    @Test fun `missing product shows NotFound`() = runTest {
        repo.productResult = Result.failure(CatalogueException.NotFound())

        val vm = DetailViewModel(handle, repo)

        assertEquals(Async.Failure(ErrorKind.NotFound), vm.state.value.product)
    }

    @Test fun `enquire click opens the enquiry for this product`() = runTest {
        val vm = DetailViewModel(handle, repo)
        vm.effects.test {
            vm.onIntent(DetailIntent.EnquireClicked)
            assertEquals(DetailEffect.OpenEnquiry(5), awaitItem())
        }
    }
}
