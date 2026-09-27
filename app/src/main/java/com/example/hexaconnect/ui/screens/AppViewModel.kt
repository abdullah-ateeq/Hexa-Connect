package com.example.hexaconnect.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hexaconnect.data.BoardTheme
import com.example.hexaconnect.data.Settings
import com.example.hexaconnect.data.SettingsRepository
import com.example.hexaconnect.data.Statistics
import com.example.hexaconnect.data.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Activity-scoped settings and statistics shared by every screen. */
class AppViewModel(private val repository: SettingsRepository) : ViewModel() {

    /** Null until the first read from DataStore completes, so the UI never flashes the wrong theme. */
    val settings: StateFlow<Settings?> =
        repository.settings.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val statistics: StateFlow<Statistics> =
        repository.statistics.stateIn(viewModelScope, SharingStarted.Eagerly, Statistics())

    fun setThemeMode(mode: ThemeMode) = launch { repository.setThemeMode(mode) }
    fun setBoardTheme(theme: BoardTheme) = launch { repository.setBoardTheme(theme) }
    fun setSoundEnabled(enabled: Boolean) = launch { repository.setSoundEnabled(enabled) }
    fun setHapticsEnabled(enabled: Boolean) = launch { repository.setHapticsEnabled(enabled) }
    fun setShowLastMove(enabled: Boolean) = launch { repository.setShowLastMove(enabled) }
    fun setShowObjectiveBorders(enabled: Boolean) = launch { repository.setShowObjectiveBorders(enabled) }
    fun resetStatistics() = launch { repository.resetStatistics() }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
