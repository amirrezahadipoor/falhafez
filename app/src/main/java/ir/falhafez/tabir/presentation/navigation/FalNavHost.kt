package ir.falhafez.tabir.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ir.falhafez.tabir.presentation.main.MainScreen
import ir.falhafez.tabir.presentation.onboarding.OnboardingScreen
import ir.falhafez.tabir.presentation.settings.SettingsScreen
import ir.falhafez.tabir.presentation.splash.SplashScreen

@Composable
fun FalNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Route.Splash.route) {

        composable(Route.Splash.route) {
            SplashScreen(
                onFinished = { seenOnboarding ->
                    navController.navigate(
                        if (seenOnboarding) Route.Main.route else Route.Onboarding.route
                    ) {
                        popUpTo(Route.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.Onboarding.route) {
            OnboardingScreen(
                onDone = {
                    navController.navigate(Route.Main.route) {
                        popUpTo(Route.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.Main.route) {
            MainScreen(
                onOpenSettings = { navController.navigate(Route.Settings.route) }
            )
        }

        composable(Route.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
