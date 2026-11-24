package com.example.catalogue.ui.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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

@Composable
fun CategoryScreen(
    onBack: () -> Unit,
    onOpenProduct: (Int) -> Unit,
    viewModel: CategoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            is CategoryEffect.OpenProduct -> onOpenProduct(effect.id)
        }
    }
    CategoryView(state = state, onIntent = viewModel::onIntent, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryView(state: CategoryState, onIntent: (CategoryIntent) -> Unit, onBack: () -> Unit) {
    val title = displayName(state.category)
    // Resolved outside the (non-composable) LazyGridScope lambda.
    val topRatedTitle = stringResource(R.string.category_top_rated)
    val moreTitle = stringResource(R.string.category_more, title)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            AsyncContent(state.content, onRetry = { onIntent(CategoryIntent.Retry) }) { content ->
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (content.topRated.isNotEmpty()) {
                        sectionHeader(topRatedTitle)
                        productItems("top", content.topRated) { onIntent(CategoryIntent.ProductClicked(it)) }
                    }
                    if (content.others.isNotEmpty()) {
                        sectionHeader(moreTitle)
                        productItems("more", content.others) { onIntent(CategoryIntent.ProductClicked(it)) }
                    }
                }
            }
        }
    }
}
