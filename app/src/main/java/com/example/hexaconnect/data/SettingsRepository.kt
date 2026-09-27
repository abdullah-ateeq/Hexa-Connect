package com.example.hexaconnect.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.hexaconnect.game.Player
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class BoardTheme { CLASSIC, MIDNIGHT }

data class Settings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val boardTheme: BoardTheme = BoardTheme.MIDNIGHT,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val showLastMove: Boolean = true,
    val showObjectiveBorders: Boolean = true,
)

data class Statistics(
    val gamesPlayed: Int = 0,
    val playerOneWins: Int = 0,
    val playerTwoWins: Int = 0,
    /** Fewest cells in any winning path, or null if no game has been won yet. */
    val shortestPath: Int? = null,
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hexa_connect")

class SettingsRepository(context: Context) {

    private val store = context.applicationContext.dataStore

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val BOARD_THEME = stringPreferencesKey("board_theme")
        val SOUND = booleanPreferencesKey("sound")
        val HAPTICS = booleanPreferencesKey("haptics")
        val LAST_MOVE = booleanPreferencesKey("show_last_move")
        val BORDERS = booleanPreferencesKey("show_objective_borders")
        val GAMES = intPreferencesKey("games_played")
        val P1_WINS = intPreferencesKey("player_one_wins")
        val P2_WINS = intPreferencesKey("player_two_wins")
        val SHORTEST = intPreferencesKey("shortest_path")
    }

    val settings: Flow<Settings> = store.data.map { p ->
        Settings(
            themeMode = p[Keys.THEME_MODE].toEnum(ThemeMode.SYSTEM),
            boardTheme = p[Keys.BOARD_THEME].toEnum(BoardTheme.MIDNIGHT),
            soundEnabled = p[Keys.SOUND] ?: true,
            hapticsEnabled = p[Keys.HAPTICS] ?: true,
            showLastMove = p[Keys.LAST_MOVE] ?: true,
            showObjectiveBorders = p[Keys.BORDERS] ?: true,
        )
    }

    val statistics: Flow<Statistics> = store.data.map { p ->
        Statistics(
            gamesPlayed = p[Keys.GAMES] ?: 0,
            playerOneWins = p[Keys.P1_WINS] ?: 0,
            playerTwoWins = p[Keys.P2_WINS] ?: 0,
            shortestPath = p[Keys.SHORTEST],
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) = store.edit { it[Keys.THEME_MODE] = mode.name }
    suspend fun setBoardTheme(theme: BoardTheme) = store.edit { it[Keys.BOARD_THEME] = theme.name }
    suspend fun setSoundEnabled(enabled: Boolean) = store.edit { it[Keys.SOUND] = enabled }
    suspend fun setHapticsEnabled(enabled: Boolean) = store.edit { it[Keys.HAPTICS] = enabled }
    suspend fun setShowLastMove(enabled: Boolean) = store.edit { it[Keys.LAST_MOVE] = enabled }
    suspend fun setShowObjectiveBorders(enabled: Boolean) = store.edit { it[Keys.BORDERS] = enabled }

    suspend fun recordWin(winner: Player, pathLength: Int) {
        store.edit { p ->
            p[Keys.GAMES] = (p[Keys.GAMES] ?: 0) + 1
            val key = if (winner == Player.PLAYER_ONE) Keys.P1_WINS else Keys.P2_WINS
            p[key] = (p[key] ?: 0) + 1
            val best = p[Keys.SHORTEST]
            if (best == null || pathLength < best) p[Keys.SHORTEST] = pathLength
        }
    }

    suspend fun resetStatistics() {
        store.edit { p ->
            p.remove(Keys.GAMES)
            p.remove(Keys.P1_WINS)
            p.remove(Keys.P2_WINS)
            p.remove(Keys.SHORTEST)
        }
    }

    private inline fun <reified T : Enum<T>> String?.toEnum(default: T): T =
        this?.let { name -> enumValues<T>().firstOrNull { it.name == name } } ?: default
}
