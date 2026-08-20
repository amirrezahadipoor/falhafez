package ir.falhafez.tabir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.falhafez.tabir.core.designsystem.FalHafezTheme
import ir.falhafez.tabir.core.designsystem.LocalReadingColor
import ir.falhafez.tabir.presentation.main.MainViewModel
import ir.falhafez.tabir.presentation.navigation.FalNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        FalHafezApp.skipOnboardingForScreenshot =
            intent?.getBooleanExtra("fal_screenshot", false) == true
        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val fontScale by mainViewModel.fontSizeScale.collectAsStateWithLifecycle()
            val fontColor by mainViewModel.fontColor.collectAsStateWithLifecycle()
            val baseDensity = LocalDensity.current

            // Persian-first: force RTL everywhere + apply the user's reading-font scale.
            CompositionLocalProvider(
                LocalLayoutDirection provides LayoutDirection.Rtl,
                LocalDensity provides Density(baseDensity.density, baseDensity.fontScale * fontScale),
                LocalReadingColor provides fontColor
            ) {
                FalHafezTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        FalNavHost()
                    }
                }
            }
        }
    }
}
