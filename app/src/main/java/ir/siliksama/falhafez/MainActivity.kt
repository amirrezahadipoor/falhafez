package ir.siliksama.falhafez

import android.content.Intent
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
import ir.siliksama.falhafez.core.designsystem.FalHafezTheme
import ir.siliksama.falhafez.core.designsystem.LocalReadingColor
import ir.siliksama.falhafez.presentation.main.MainViewModel
import ir.siliksama.falhafez.presentation.navigation.FalNavHost
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

    /**
     * وقتی اپ زنده است و کاربر دوباره روی آیکونِ لانچر می‌زند، اندروید به‌جای
     * ساختِ اکتیویتیِ تازه این متد را صدا می‌زند.
     *
     * نبودِ این متد (به‌همراهِ launchMode="singleTask") باعثِ «صفحهٔ سیاه» می‌شد:
     * سیستم تسک را به ریشه برمی‌گرداند، اما `setContent` دوباره اجرا نمی‌شد و
     * NavHost در مقصدِ قبلی (مثلاً settings) گیر می‌کرد — در حالی که سیستم
     * انتظار داشت صفحهٔ اصلی دیده شود.
     *
     * فقط باید intent را به‌روز کنیم؛ با singleTop پشتهٔ Compose دست‌نخورده و
     * سالم می‌ماند و کاربر همان‌جایی که بود ادامه می‌دهد.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
