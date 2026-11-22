package com.example.catalogue.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.catalogue.ui.home.HomeContent
import com.example.catalogue.ui.home.HomeIntent
import com.example.catalogue.ui.home.HomeState
import com.example.catalogue.ui.home.HomeView
import com.example.catalogue.ui.mvi.Async
import com.example.catalogue.ui.mvi.ErrorKind
import com.example.catalogue.ui.theme.CatalogueTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProductFlowUiTest {
    @get:Rule val rule = createComposeRule()

    private fun setHome(state: HomeState, onIntent: (HomeIntent) -> Unit = {}) =
        rule.setContent { CatalogueTheme { HomeView(state, onIntent) } }

    @Test fun featuredProductIsShownAndClickable() {
        var last: HomeIntent? = null
        setHome(HomeState(Async.Success(HomeContent(listOf(testProduct(1)), listOf("jewelery")))), { last = it })

        rule.onNodeWithText("Gold ring 1").assertIsDisplayed()
        rule.onNodeWithTag("product_tile_1").performClick()

        assertEquals(HomeIntent.ProductClicked(1), last)
    }

    @Test fun categoryChipIsClickable() {
        var last: HomeIntent? = null
        setHome(HomeState(Async.Success(HomeContent(emptyList(), listOf("jewelery")))), { last = it })

        rule.onNodeWithTag("category_jewelery").performClick()

        assertEquals(HomeIntent.CategoryClicked("jewelery"), last)
    }

    @Test fun errorStateOffersRetry() {
        var last: HomeIntent? = null
        setHome(HomeState(Async.Failure(ErrorKind.Network)), { last = it })

        rule.onNodeWithTag("retry_button").performClick()

        assertEquals(HomeIntent.Retry, last)
    }
}
