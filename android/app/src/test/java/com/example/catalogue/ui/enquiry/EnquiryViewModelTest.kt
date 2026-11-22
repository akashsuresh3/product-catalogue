package com.example.catalogue.ui.enquiry

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.catalogue.domain.model.CatalogueException
import com.example.catalogue.domain.model.Enquiry
import com.example.catalogue.testutil.FakeCatalogueRepository
import com.example.catalogue.testutil.MainDispatcherRule
import com.example.catalogue.ui.navigation.NavArgs
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class EnquiryViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()
    private val repo = FakeCatalogueRepository()
    private val handle = SavedStateHandle(mapOf(NavArgs.ENQUIRY_PRODUCT_ID to 9))

    private fun newVm() = EnquiryViewModel(handle, repo)

    private fun EnquiryViewModel.fillValid(phone: String = "") {
        onIntent(EnquiryIntent.FieldChanged(EnquiryField.Name, "Asha Rao"))
        onIntent(EnquiryIntent.FieldChanged(EnquiryField.Email, "asha@example.com"))
        onIntent(EnquiryIntent.FieldChanged(EnquiryField.Phone, phone))
        onIntent(EnquiryIntent.FieldChanged(EnquiryField.Message, "Do you have this in stock?"))
    }

    @Test fun `no errors are shown before the first submit`() = runTest {
        assertTrue(newVm().state.value.errors.isEmpty())
    }

    @Test fun `submitting an empty form shows errors and does not call the repository`() = runTest {
        val vm = newVm()

        vm.onIntent(EnquiryIntent.Submit)

        assertEquals(
            setOf(EnquiryField.Name, EnquiryField.Email, EnquiryField.Message),
            vm.state.value.errors.keys,
        )
        assertTrue(repo.enquiries.isEmpty())
        assertFalse(vm.state.value.isSubmitting)
    }

    @Test fun `invalid email and phone are rejected`() = runTest {
        val vm = newVm()
        vm.fillValid(phone = "123")
        vm.onIntent(EnquiryIntent.FieldChanged(EnquiryField.Email, "not-an-email"))

        vm.onIntent(EnquiryIntent.Submit)

        assertEquals(setOf(EnquiryField.Email, EnquiryField.Phone), vm.state.value.errors.keys)
        assertTrue(repo.enquiries.isEmpty())
    }

    @Test fun `editing a field clears only that field's error`() = runTest {
        val vm = newVm()
        vm.onIntent(EnquiryIntent.Submit)

        vm.onIntent(EnquiryIntent.FieldChanged(EnquiryField.Name, "A"))

        assertEquals(setOf(EnquiryField.Email, EnquiryField.Message), vm.state.value.errors.keys)
    }

    @Test fun `valid submit sends the enquiry for the route's product and shows thank you`() = runTest {
        val vm = newVm()
        vm.fillValid()

        vm.onIntent(EnquiryIntent.Submit)

        assertEquals(
            listOf(Enquiry(9, "Asha Rao", "asha@example.com", null, "Do you have this in stock?")),
            repo.enquiries,
        )
        assertTrue(vm.state.value.submitted)
        assertFalse(vm.state.value.isSubmitting)
    }

    @Test fun `server validation errors are mapped onto fields`() = runTest {
        repo.enquiryResult = Result.failure(CatalogueException.Validation(mapOf("email" to "Rejected by server")))
        val vm = newVm()
        vm.fillValid()

        vm.onIntent(EnquiryIntent.Submit)

        assertEquals("Rejected by server", vm.state.value.errors[EnquiryField.Email])
        assertFalse(vm.state.value.submitted)
        assertFalse(vm.state.value.isSubmitting)
    }

    @Test fun `server validation error with no known field falls back to a generic error`() = runTest {
        repo.enquiryResult = Result.failure(CatalogueException.Validation(emptyMap()))
        val vm = newVm()
        vm.fillValid()

        vm.onIntent(EnquiryIntent.Submit)

        assertTrue(vm.state.value.submitError)
        assertTrue(vm.state.value.errors.isEmpty())
    }

    @Test fun `network failure keeps the form data and shows a retryable error`() = runTest {
        repo.enquiryResult = Result.failure(CatalogueException.NetworkUnavailable())
        val vm = newVm()
        vm.fillValid()

        vm.onIntent(EnquiryIntent.Submit)

        assertTrue(vm.state.value.submitError)
        assertFalse(vm.state.value.submitted)
        assertEquals("asha@example.com", vm.state.value.email)

        // Retry succeeds
        repo.enquiryResult = Result.success(Unit)
        vm.onIntent(EnquiryIntent.Submit)
        assertTrue(vm.state.value.submitted)
        assertFalse(vm.state.value.submitError)
    }

    @Test fun `done closes the screen`() = runTest {
        val vm = newVm()
        vm.effects.test {
            vm.onIntent(EnquiryIntent.Done)
            assertEquals(EnquiryEffect.Close, awaitItem())
        }
    }
}
