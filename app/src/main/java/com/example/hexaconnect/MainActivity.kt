package com.example.hexaconnect

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.hexaconnect.data.ThemeMode
import com.example.hexaconnect.ui.HexaConnectNavHost
import com.example.hexaconnect.ui.screens.AppViewModel
import com.example.hexaconnect.ui.theme.HexaConnectTheme
import com.example.hexaconnect.ui.theme.Midnight
import com.example.hexaconnect.ui.theme.palette

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as HexaConnectApp
        setContent {
            val appViewModel: AppViewModel = viewModel(
                factory = viewModelFactory { initializer { AppViewModel(app.repository) } },
            )
            val settings by appViewModel.settings.collectAsStateWithLifecycle()
            val loaded = settings
            if (loaded == null) {
                Box(Modifier.fillMaxSize().background(Midnight))
                return@setContent
            }
            val dark = when (loaded.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            LaunchedEffect(dark) {
                val style = if (dark) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }
            HexaConnectTheme(darkTheme = dark, boardPalette = loaded.boardTheme.palette()) {
                HexaConnectNavHost(app = app, appViewModel = appViewModel, settings = loaded)
            }
        }
    }
}
