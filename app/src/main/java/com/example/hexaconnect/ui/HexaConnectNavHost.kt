package com.example.hexaconnect.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hexaconnect.HexaConnectApp
import com.example.hexaconnect.data.Settings
import com.example.hexaconnect.game.Player
import com.example.hexaconnect.ui.screens.AboutScreen
import com.example.hexaconnect.ui.screens.AppViewModel
import com.example.hexaconnect.ui.screens.GameScreen
import com.example.hexaconnect.ui.screens.GameViewModel
import com.example.hexaconnect.ui.screens.HomeScreen
import com.example.hexaconnect.ui.screens.HowToPlayScreen
import com.example.hexaconnect.ui.screens.MatchResult
import com.example.hexaconnect.ui.screens.ResultScreen
import com.example.hexaconnect.ui.screens.SettingsScreen
import com.example.hexaconnect.ui.screens.SplashScreen

private object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val GAME = "game"
    const val RESULT = "result/{winner}/{moves}/{path}/{duration}"
    const val HOW_TO_PLAY = "how_to_play"
    const val SETTINGS = "settings"
    const val ABOUT = "about"

    fun result(r: MatchResult) = "result/${r.winner.name}/${r.moves}/${r.pathLength}/${r.durationMs}"
}

@Composable
fun HexaConnectNavHost(
    app: HexaConnectApp,
    appViewModel: AppViewModel,
    settings: Settings,
    navController: NavHostController = rememberNavController(),
) {
    val stats by appViewModel.statistics.collectAsStateWithLifecycle()
    val sounds by app.sounds.collectAsStateWithLifecycle()

    fun NavHostController.back() {
        if (previousBackStackEntry != null) popBackStack()
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = { fadeIn(tween(220)) },
        exitTransition = { fadeOut(tween(180)) },
        popEnterTransition = { fadeIn(tween(220)) },
        popExitTransition = { fadeOut(tween(180)) },
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(onFinished = {
                navController.navigate(Routes.HOME) { popUpTo(Routes.SPLASH) { inclusive = true } }
            })
        }
        composable(Routes.HOME) {
            HomeScreen(
                stats = stats,
                onPlay = { navController.navigate(Routes.GAME) { launchSingleTop = true } },
                onHowToPlay = { navController.navigate(Routes.HOW_TO_PLAY) { launchSingleTop = true } },
                onSettings = { navController.navigate(Routes.SETTINGS) { launchSingleTop = true } },
                onAbout = { navController.navigate(Routes.ABOUT) { launchSingleTop = true } },
            )
        }
        composable(Routes.GAME) {
            val gameViewModel: GameViewModel = viewModel(
                factory = viewModelFactory { initializer { GameViewModel(app.repository) } },
            )
            GameScreen(
                viewModel = gameViewModel,
                settings = settings,
                sounds = sounds,
                onFinished = { result ->
                    navController.navigate(Routes.result(result)) {
                        popUpTo(Routes.GAME) { inclusive = true }
                    }
                },
                onHome = { navController.popBackStack(Routes.HOME, inclusive = false) },
            )
        }
        composable(
            Routes.RESULT,
            arguments = listOf(
                navArgument("winner") { type = NavType.StringType },
                navArgument("moves") { type = NavType.IntType },
                navArgument("path") { type = NavType.IntType },
                navArgument("duration") { type = NavType.LongType },
            ),
        ) { entry ->
            val args = entry.arguments
            val result = MatchResult(
                winner = Player.entries.firstOrNull { it.name == args?.getString("winner") } ?: Player.PLAYER_ONE,
                moves = args?.getInt("moves") ?: 0,
                pathLength = args?.getInt("path") ?: 0,
                durationMs = args?.getLong("duration") ?: 0L,
            )
            ResultScreen(
                result = result,
                onPlayAgain = {
                    navController.navigate(Routes.GAME) { popUpTo(Routes.HOME) }
                },
                onHome = { navController.popBackStack(Routes.HOME, inclusive = false) },
            )
        }
        composable(Routes.HOW_TO_PLAY) { HowToPlayScreen(onBack = { navController.back() }) }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                settings = settings,
                stats = stats,
                viewModel = appViewModel,
                onBack = { navController.back() },
            )
        }
        composable(Routes.ABOUT) { AboutScreen(onBack = { navController.back() }) }
    }
}
