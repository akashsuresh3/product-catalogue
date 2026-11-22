package com.example.catalogue.ui.enquiry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.catalogue.domain.model.CatalogueException
import com.example.catalogue.domain.model.Enquiry
import com.example.catalogue.domain.model.EnquiryValidator
import com.example.catalogue.domain.repository.CatalogueRepository
import com.example.catalogue.ui.mvi.MviViewModel
import com.example.catalogue.ui.navigation.NavArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnquiryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CatalogueRepository,
) : MviViewModel<EnquiryState, EnquiryIntent, EnquiryEffect>(EnquiryState()) {

    private val productId: Int = checkNotNull(savedStateHandle.get<Int>(NavArgs.ENQUIRY_PRODUCT_ID))

    override fun onIntent(intent: EnquiryIntent) {
        when (intent) {
            is EnquiryIntent.FieldChanged -> onFieldChanged(intent.field, intent.value)
            EnquiryIntent.Submit -> submit()
            EnquiryIntent.Done -> sendEffect(EnquiryEffect.Close)
        }
    }

    private fun onFieldChanged(field: EnquiryField, value: String) = setState {
        // Editing a field clears its own error; the others stay until the next submit.
        val cleared = errors - field
        when (field) {
            EnquiryField.Name -> copy(name = value, errors = cleared)
            EnquiryField.Email -> copy(email = value, errors = cleared)
            EnquiryField.Phone -> copy(phone = value, errors = cleared)
            EnquiryField.Message -> copy(message = value, errors = cleared)
        }
    }

    private fun submit() {
        val s = currentState
        if (s.isSubmitting) return // guard against double taps

        val errors = buildMap {
            EnquiryValidator.validateName(s.name)?.let { put(EnquiryField.Name, it) }
            EnquiryValidator.validateEmail(s.email)?.let { put(EnquiryField.Email, it) }
            EnquiryValidator.validatePhone(s.phone)?.let { put(EnquiryField.Phone, it) }
            EnquiryValidator.validateMessage(s.message)?.let { put(EnquiryField.Message, it) }
        }
        if (errors.isNotEmpty()) {
            setState { copy(errors = errors, submitError = false) }
            return
        }

        setState { copy(errors = emptyMap(), isSubmitting = true, submitError = false) }
        viewModelScope.launch {
            val enquiry = Enquiry(
                productId = productId,
                name = s.name,
                email = s.email,
                phone = s.phone.ifBlank { null },
                message = s.message,
            )
            repository.submitEnquiry(enquiry).fold(
                onSuccess = { setState { copy(isSubmitting = false, submitted = true) } },
                onFailure = { e -> onSubmitFailed(e) },
            )
        }
    }

    private fun onSubmitFailed(e: Throwable) {
        val serverErrors = (e as? CatalogueException.Validation)?.fieldErrors.orEmpty().toFieldErrors()
        // If the server rejected the request but we can't attribute it to a field, show the generic error.
        if (serverErrors.isEmpty()) {
            setState { copy(isSubmitting = false, submitError = true) }
        } else {
            setState { copy(isSubmitting = false, errors = serverErrors) }
        }
    }

    private fun Map<String, String>.toFieldErrors(): Map<EnquiryField, String> =
        mapNotNull { (key, message) ->
            EnquiryField.entries.firstOrNull { it.name.equals(key, ignoreCase = true) }?.let { it to message }
        }.toMap()
}
