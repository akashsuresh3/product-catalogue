package com.example.catalogue.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand tokens live here only. The entrepreneur's creative work lands in this file,
// not scattered through screens.
private val Brand = Color(0xFF1F5FBF)
private val BrandDark = Color(0xFF9DC1FF)

private val LightColors = lightColorScheme(primary = Brand)
private val DarkColors = darkColorScheme(primary = BrandDark)

@Composable
fun CatalogueTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
