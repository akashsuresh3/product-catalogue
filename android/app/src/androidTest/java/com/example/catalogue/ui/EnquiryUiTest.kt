package com.example.catalogue.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.catalogue.ui.enquiry.EnquiryField
import com.example.catalogue.ui.enquiry.EnquiryIntent
import com.example.catalogue.ui.enquiry.EnquiryState
import com.example.catalogue.ui.enquiry.EnquiryView
import com.example.catalogue.ui.theme.CatalogueTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class EnquiryUiTest {
    @get:Rule val rule = createComposeRule()

    private fun setForm(state: EnquiryState, onIntent: (EnquiryIntent) -> Unit = {}) =
        rule.setContent { CatalogueTheme { EnquiryView(state, onIntent, onBack = {}) } }

    @Test fun typingReportsFieldChanges() {
        var last: EnquiryIntent? = null
        setForm(EnquiryState(), { last = it })

        rule.onNodeWithTag("field_email").performTextInput("a@b.co")

        assertEquals(EnquiryIntent.FieldChanged(EnquiryField.Email, "a@b.co"), last)
    }

    @Test fun submitButtonSendsSubmitIntent() {
        var last: EnquiryIntent? = null
        setForm(EnquiryState(), { last = it })

        rule.onNodeWithTag("submit_button").performScrollTo().performClick()

        assertEquals(EnquiryIntent.Submit, last)
    }

    @Test fun fieldErrorsAreDisplayed() {
        setForm(EnquiryState(errors = mapOf(EnquiryField.Email to "Enter a valid email address")))

        rule.onNodeWithText("Enter a valid email address").assertIsDisplayed()
    }

    @Test fun thankYouIsShownAfterSuccessfulSubmit() {
        var last: EnquiryIntent? = null
        setForm(EnquiryState(submitted = true), { last = it })

        rule.onNodeWithTag("thank_you").assertIsDisplayed()
        rule.onNodeWithTag("done_button").performClick()

        assertEquals(EnquiryIntent.Done, last)
    }
}
