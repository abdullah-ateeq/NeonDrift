package com.example.neondrift

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.neondrift.data.ThemeMode
import com.example.neondrift.ui.navigation.Screen
import com.example.neondrift.ui.screens.about.AboutScreen
import com.example.neondrift.ui.screens.game.GameScreen
import com.example.neondrift.ui.screens.game.GameViewModel
import com.example.neondrift.ui.screens.home.HomeScreen
import com.example.neondrift.ui.screens.result.ResultScreen
import com.example.neondrift.ui.screens.settings.SettingsScreen
import com.example.neondrift.ui.screens.settings.SettingsViewModel
import com.example.neondrift.ui.screens.splash.SplashScreen
import com.example.neondrift.ui.theme.NeonDriftTheme

@Composable
fun NeonDriftApp() {
    val navController = rememberNavController()

    // Shared or Screen ViewModels
    val gameViewModel: GameViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    val settings by settingsViewModel.settings.collectAsState()
    val systemDark = isSystemInDarkTheme()
    val isDark = when (settings.themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    NeonDriftTheme(darkTheme = isDark) {
        Surface(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() },
                popEnterTransition = { fadeIn() },
                popExitTransition = { fadeOut() }
            ) {
                // 1. Splash Screen
                composable(Screen.Splash.route) {
                    SplashScreen(
                        onSplashFinished = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    )
                }

                // 2. Home Screen
                composable(Screen.Home.route) {
                    HomeScreen(
                        statsState = settingsViewModel.stats,
                        onPlayClick = {
                            navController.navigate(Screen.Game.route)
                        },
                        onSettingsClick = {
                            navController.navigate(Screen.Settings.route)
                        },
                        onAboutClick = {
                            navController.navigate(Screen.About.route)
                        }
                    )
                }

                // 3. Game Screen
                composable(Screen.Game.route) {
                    GameScreen(
                        viewModel = gameViewModel,
                        onNavigateToResult = { score, distance, streak, isNewHigh ->
                            navController.navigate(
                                Screen.Result.createRoute(score, distance, streak, isNewHigh)
                            ) {
                                popUpTo(Screen.Game.route) { inclusive = true }
                            }
                        },
                        onNavigateHome = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    )
                }

                // 4. Result Screen
                composable(
                    route = Screen.Result.route,
                    arguments = listOf(
                        navArgument("score") { type = NavType.IntType },
                        navArgument("distance") { type = NavType.FloatType },
                        navArgument("streak") { type = NavType.IntType },
                        navArgument("isNewHigh") { type = NavType.BoolType }
                    )
                ) { backStackEntry ->
                    val score = backStackEntry.arguments?.getInt("score") ?: 0
                    val distance = backStackEntry.arguments?.getFloat("distance") ?: 0f
                    val streak = backStackEntry.arguments?.getInt("streak") ?: 0
                    val isNewHigh = backStackEntry.arguments?.getBoolean("isNewHigh") ?: false

                    ResultScreen(
                        score = score,
                        distance = distance,
                        streak = streak,
                        isNewHighScore = isNewHigh,
                        statsState = settingsViewModel.stats,
                        onPlayAgain = {
                            navController.navigate(Screen.Game.route) {
                                popUpTo(Screen.Home.route)
                            }
                        },
                        onHome = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    )
                }

                // 5. Settings Screen
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // 6. About Screen
                composable(Screen.About.route) {
                    AboutScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
