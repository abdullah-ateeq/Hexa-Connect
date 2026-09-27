package com.example.hexaconnect

import android.app.Application
import com.example.hexaconnect.audio.SoundEffects
import com.example.hexaconnect.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HexaConnectApp : Application() {

    val repository: SettingsRepository by lazy { SettingsRepository(this) }

    private val _sounds = MutableStateFlow<SoundEffects?>(null)

    /** Becomes non-null once the synthesized effects have been prepared off the main thread. */
    val sounds: StateFlow<SoundEffects?> = _sounds

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            _sounds.value = runCatching { SoundEffects(this@HexaConnectApp) }.getOrNull()
        }
    }
}
