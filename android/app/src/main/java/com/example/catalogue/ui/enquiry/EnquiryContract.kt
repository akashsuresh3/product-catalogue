package com.example.catalogue.ui.enquiry

import com.example.catalogue.ui.mvi.UiEffect
import com.example.catalogue.ui.mvi.UiIntent
import com.example.catalogue.ui.mvi.UiState

enum class EnquiryField { Name, Email, Phone, Message }

data class EnquiryState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val message: String = "",
    /** Populated on submit (client validation or server 400), never on first render. */
    val errors: Map<EnquiryField, String> = emptyMap(),
    val isSubmitting: Boolean = false,
    val submitted: Boolean = false,
    val submitError: Boolean = false,
) : UiState

sealed interface EnquiryIntent : UiIntent {
    data class FieldChanged(val field: EnquiryField, val value: String) : EnquiryIntent
    data object Submit : EnquiryIntent
    data object Done : EnquiryIntent
}

sealed interface EnquiryEffect : UiEffect {
    data object Close : EnquiryEffect
}
