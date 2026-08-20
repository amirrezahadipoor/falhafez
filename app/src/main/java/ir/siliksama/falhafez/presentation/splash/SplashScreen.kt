package ir.siliksama.falhafez.presentation.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.siliksama.falhafez.core.designsystem.FalPalette
import ir.siliksama.falhafez.core.designsystem.FalText
import ir.siliksama.falhafez.core.designsystem.RotatingStar
import ir.siliksama.falhafez.core.theme.FalThemeSpec
import ir.siliksama.falhafez.presentation.components.OrnamentalDivider
import ir.siliksama.falhafez.presentation.components.RitualBackground
import androidx.compose.material3.Text
import ir.siliksama.falhafez.FalHafezApp

@Composable
fun SplashScreen(onFinished: (seenOnboarding: Boolean) -> Unit) {
    val viewModel: SplashViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.ready) {
        if (state.ready) onFinished(state.seenOnboarding || FalHafezApp.skipOnboardingForScreenshot)
    }

    RitualBackground(spec = FalThemeSpec.tazhib(), showParticles = true) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                RotatingStar(tint = FalPalette.Gold.copy(alpha = 0.55f), size = 150.dp)
                Text(text = "فال حافظ", style = FalText.display, color = FalPalette.GoldBright)
            }
            Spacer(Modifier.height(12.dp))
            OrnamentalDivider(color = FalPalette.Gold, modifier = Modifier.fillMaxWidth(0.5f))
            Spacer(Modifier.height(28.dp))
            Text(
                text = "تعبیر هوشمند",
                style = FalText.caption,
                color = FalPalette.CreamMuted
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "دیوان در حال گشوده شدن است…",
                style = FalText.bodyMuted,
                color = FalPalette.CreamMuted
            )
        }
    }
}
