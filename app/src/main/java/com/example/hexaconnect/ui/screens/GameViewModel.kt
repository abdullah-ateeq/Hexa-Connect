package com.example.hexaconnect.ui.screens

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hexaconnect.data.SettingsRepository
import com.example.hexaconnect.game.HexCell
import com.example.hexaconnect.game.HexaConnectGame
import com.example.hexaconnect.game.HexaConnectState
import com.example.hexaconnect.game.Player
import com.example.hexaconnect.ui.board.PLACE_ANIMATION_MS
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class MatchResult(
    val winner: Player,
    val moves: Int,
    val pathLength: Int,
    val durationMs: Long,
)

sealed interface GameEvent {
    data object Placed : GameEvent
    data object TurnChanged : GameEvent
    data object PathComplete : GameEvent
    data class Finished(val result: MatchResult) : GameEvent
}

class GameViewModel(private val repository: SettingsRepository) : ViewModel() {

    private val game = HexaConnectGame()
    private val _state = MutableStateFlow(game.state)
    val state: StateFlow<HexaConnectState> = _state.asStateFlow()

    private val _events = Channel<GameEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var startedAt = SystemClock.elapsedRealtime()
    private var moveJob: Job? = null

    fun onCellTapped(cell: HexCell) {
        if (!game.placePiece(cell)) return
        // Lock input for the length of the fill animation.
        game.setAnimating(true)
        publish()
        _events.trySend(GameEvent.Placed)

        val placed = game.state
        moveJob = viewModelScope.launch {
            delay(PLACE_ANIMATION_MS.toLong())
            val winner = placed.winner
            if (winner == null) {
                game.setAnimating(false)
                publish()
                _events.trySend(GameEvent.TurnChanged)
                return@launch
            }
            // The board stays locked while the winning path is revealed.
            val result = MatchResult(
                winner = winner,
                moves = placed.moveHistory.size,
                pathLength = placed.winningPath.size,
                durationMs = SystemClock.elapsedRealtime() - startedAt,
            )
            _events.trySend(GameEvent.PathComplete)
            repository.recordWin(winner, result.pathLength)
            delay(WIN_HOLD_MS)
            _events.trySend(GameEvent.Finished(result))
        }
    }

    fun undo() {
        if (game.undo()) publish()
    }

    fun restart() {
        moveJob?.cancel()
        game.restart()
        startedAt = SystemClock.elapsedRealtime()
        publish()
    }

    private fun publish() {
        _state.value = game.state
    }

    private companion object {
        const val WIN_HOLD_MS = 1_700L
    }
}
