package ir.siliksama.falhafez.presentation.navigation

sealed class Route(val route: String) {
    data object Splash : Route("splash")
    data object Onboarding : Route("onboarding")
    data object Main : Route("main")
    data object Settings : Route("settings")
    data object Privacy : Route("privacy")
}
