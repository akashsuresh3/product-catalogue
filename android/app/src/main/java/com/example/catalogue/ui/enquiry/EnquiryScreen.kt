package com.example.catalogue.ui.enquiry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.catalogue.R
import com.example.catalogue.ui.common.CollectEffects

@Composable
fun EnquiryScreen(onClose: () -> Unit, viewModel: EnquiryViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            EnquiryEffect.Close -> onClose()
        }
    }
    EnquiryView(state = state, onIntent = viewModel::onIntent, onBack = onClose)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnquiryView(state: EnquiryState, onIntent: (EnquiryIntent) -> Unit, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.enquiry_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        if (state.submitted) {
            ThankYou(onDone = { onIntent(EnquiryIntent.Done) }, modifier = Modifier.padding(padding))
        } else {
            EnquiryForm(state, onIntent, Modifier.padding(padding))
        }
    }
}

@Composable
private fun EnquiryForm(state: EnquiryState, onIntent: (EnquiryIntent) -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FormField(
            label = R.string.enquiry_name, value = state.name, error = state.errors[EnquiryField.Name],
            onChange = { onIntent(EnquiryIntent.FieldChanged(EnquiryField.Name, it)) },
            keyboardType = KeyboardType.Text, tag = "field_name",
        )
        FormField(
            label = R.string.enquiry_email, value = state.email, error = state.errors[EnquiryField.Email],
            onChange = { onIntent(EnquiryIntent.FieldChanged(EnquiryField.Email, it)) },
            keyboardType = KeyboardType.Email, tag = "field_email",
        )
        FormField(
            label = R.string.enquiry_phone, value = state.phone, error = state.errors[EnquiryField.Phone],
            onChange = { onIntent(EnquiryIntent.FieldChanged(EnquiryField.Phone, it)) },
            keyboardType = KeyboardType.Phone, tag = "field_phone",
        )
        FormField(
            label = R.string.enquiry_message, value = state.message, error = state.errors[EnquiryField.Message],
            onChange = { onIntent(EnquiryIntent.FieldChanged(EnquiryField.Message, it)) },
            keyboardType = KeyboardType.Text, tag = "field_message",
            singleLine = false, imeAction = ImeAction.Default,
        )

        if (state.submitError) {
            Text(
                text = stringResource(R.string.enquiry_failed),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag("submit_error"),
            )
        }

        Button(
            onClick = { onIntent(EnquiryIntent.Submit) },
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth().testTag("submit_button"),
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.enquiry_submit))
            }
        }
    }
}

@Composable
private fun FormField(
    label: Int,
    value: String,
    error: String?,
    onChange: (String) -> Unit,
    keyboardType: KeyboardType,
    tag: String,
    singleLine: Boolean = true,
    imeAction: ImeAction = ImeAction.Next,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(stringResource(label)) },
        isError = error != null,
        supportingText = error?.let { message -> { Text(message) } },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        modifier = Modifier.fillMaxWidth().testTag(tag),
    )
}

@Composable
private fun ThankYou(onDone: () -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp).testTag("thank_you"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            stringResource(R.string.thank_you_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.thank_you_body), textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onDone, modifier = Modifier.testTag("done_button")) {
            Text(stringResource(R.string.done))
        }
    }
}
