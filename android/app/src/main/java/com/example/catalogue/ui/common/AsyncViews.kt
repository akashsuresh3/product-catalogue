package com.example.catalogue.ui.common

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.catalogue.R
import com.example.catalogue.ui.mvi.Async
import com.example.catalogue.ui.mvi.ErrorKind

@Composable
fun LoadingView(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().testTag("loading"), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorView(kind: ErrorKind, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp).testTag("error"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(kind.messageRes()), textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry, modifier = Modifier.testTag("retry_button")) {
            Text(stringResource(R.string.retry))
        }
    }
}

/** Renders loading / error / content for any [Async] value, so screens don't repeat this. */
@Composable
fun <T> AsyncContent(
    state: Async<T>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    when (state) {
        Async.Loading -> LoadingView(modifier)
        is Async.Failure -> ErrorView(state.error, onRetry, modifier)
        is Async.Success -> content(state.data)
    }
}

@StringRes
private fun ErrorKind.messageRes(): Int = when (this) {
    ErrorKind.Network -> R.string.error_network
    ErrorKind.NotFound -> R.string.error_not_found
    ErrorKind.Server -> R.string.error_server
    ErrorKind.Unknown -> R.string.error_unknown
}
