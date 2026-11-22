package com.example.catalogue.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.catalogue.R
import com.example.catalogue.domain.model.Product
import com.example.catalogue.ui.common.AsyncContent
import com.example.catalogue.ui.common.CollectEffects
import com.example.catalogue.ui.common.RatingRow
import com.example.catalogue.ui.common.displayName
import com.example.catalogue.ui.common.formatPrice
import com.example.catalogue.ui.mvi.Async

@Composable
fun DetailScreen(
    onBack: () -> Unit,
    onEnquire: (Int) -> Unit,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            is DetailEffect.OpenEnquiry -> onEnquire(effect.productId)
        }
    }
    DetailView(state = state, onIntent = viewModel::onIntent, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailView(state: DetailState, onIntent: (DetailIntent) -> Unit, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
        bottomBar = {
            if (state.product is Async.Success) {
                Surface(tonalElevation = 3.dp) {
                    Button(
                        onClick = { onIntent(DetailIntent.EnquireClicked) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp)
                            .testTag("enquire_button"),
                    ) {
                        Text(stringResource(R.string.enquire))
                    }
                }
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            AsyncContent(state.product, onRetry = { onIntent(DetailIntent.Retry) }) { product ->
                DetailBody(product)
            }
        }
    }
}

@Composable
private fun DetailBody(product: Product) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("product_detail"),
    ) {
        AsyncImage(
            model = product.imageUrl,
            contentDescription = product.title,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth().height(280.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(product.title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            formatPrice(product.price),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(4.dp))
        RatingRow(product.rating, product.ratingCount)
        Spacer(Modifier.height(4.dp))
        Text(displayName(product.category), style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(16.dp))
        Text(product.description, style = MaterialTheme.typography.bodyMedium)
    }
}
