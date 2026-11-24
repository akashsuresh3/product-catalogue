package com.example.catalogue.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.catalogue.R
import com.example.catalogue.ui.common.AsyncContent
import com.example.catalogue.ui.common.CollectEffects
import com.example.catalogue.ui.common.productItems

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOpenProduct: (Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            is SearchEffect.OpenProduct -> onOpenProduct(effect.id)
        }
    }
    SearchView(state = state, onIntent = viewModel::onIntent, onBack = onBack)
}

@Composable
fun SearchView(state: SearchState, onIntent: (SearchIntent) -> Unit, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.statusBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }
                OutlinedTextField(
                    value = state.query,
                    onValueChange = { onIntent(SearchIntent.QueryChanged(it)) },
                    placeholder = { Text(stringResource(R.string.search_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onIntent(SearchIntent.SearchSubmitted) }),
                    trailingIcon = {
                        IconButton(
                            onClick = { onIntent(SearchIntent.SearchSubmitted) },
                            modifier = Modifier.testTag("search_submit"),
                        ) {
                            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search))
                        }
                    },
                    modifier = Modifier.weight(1f).testTag("search_field"),
                )
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            val results = state.results
            if (results == null) {
                CenteredMessage(stringResource(R.string.search_prompt))
            } else {
                AsyncContent(results, onRetry = { onIntent(SearchIntent.SearchSubmitted) }) { products ->
                    if (products.isEmpty()) {
                        CenteredMessage(
                            stringResource(R.string.search_no_results, state.submittedQuery.orEmpty()),
                            tag = "no_results",
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            productItems("results", products) { onIntent(SearchIntent.ProductClicked(it)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CenteredMessage(text: String, tag: String = "search_prompt") {
    Box(Modifier.fillMaxSize().padding(24.dp).testTag(tag), contentAlignment = Alignment.Center) {
        Text(text, textAlign = TextAlign.Center)
    }
}
