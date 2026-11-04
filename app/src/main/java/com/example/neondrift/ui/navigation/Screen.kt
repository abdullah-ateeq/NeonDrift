package com.example.neondrift.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Game : Screen("game")
    object Result : Screen("result/{score}/{distance}/{streak}/{isNewHigh}") {
        fun createRoute(score: Int, distance: Float, streak: Int, isNewHigh: Boolean): String {
            return "result/$score/$distance/$streak/$isNewHigh"
        }
    }
    object Settings : Screen("settings")
    object About : Screen("about")
}
