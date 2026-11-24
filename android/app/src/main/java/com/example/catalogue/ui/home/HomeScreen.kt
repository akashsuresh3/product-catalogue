package com.example.catalogue.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.catalogue.R
import com.example.catalogue.ui.common.AsyncContent
import com.example.catalogue.ui.common.CollectEffects
import com.example.catalogue.ui.common.displayName
import com.example.catalogue.ui.common.productItems
import com.example.catalogue.ui.common.sectionHeader

/** Stateful: wires the ViewModel and translates effects into navigation callbacks. */
@Composable
fun HomeScreen(
    onOpenProduct: (Int) -> Unit,
    onOpenCategory: (String) -> Unit,
    onOpenSearch: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            is HomeEffect.OpenProduct -> onOpenProduct(effect.id)
            is HomeEffect.OpenCategory -> onOpenCategory(effect.name)
            HomeEffect.OpenSearch -> onOpenSearch()
        }
    }
    HomeView(state = state, onIntent = viewModel::onIntent)
}

/** Stateless: renders [state], reports user actions through [onIntent]. Easy to test and preview. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(state: HomeState, onIntent: (HomeIntent) -> Unit) {
    // stringResource is @Composable, so resolve it here, outside the (non-composable) LazyGridScope lambda.
    val categoriesTitle = stringResource(R.string.home_categories)
    val featuredTitle = stringResource(R.string.home_featured)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(
                        onClick = { onIntent(HomeIntent.SearchClicked) },
                        modifier = Modifier.testTag("search_button"),
                    ) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search))
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            AsyncContent(state.content, onRetry = { onIntent(HomeIntent.Retry) }) { content ->
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    sectionHeader(categoriesTitle)
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(content.categories) { name ->
                                AssistChip(
                                    onClick = { onIntent(HomeIntent.CategoryClicked(name)) },
                                    label = { Text(displayName(name)) },
                                    modifier = Modifier.testTag("category_$name"),
                                )
                            }
                        }
                    }
                    sectionHeader(featuredTitle)
                    productItems("featured", content.featured) { onIntent(HomeIntent.ProductClicked(it)) }
                }
            }
        }
    }
}
