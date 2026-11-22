package com.example.catalogue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.catalogue.ui.navigation.CatalogueNavHost
import com.example.catalogue.ui.theme.CatalogueTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // targetSdk 35 enforces edge-to-edge; opt in explicitly so every API level behaves the same
        setContent {
            CatalogueTheme { CatalogueNavHost() }
        }
    }
}
