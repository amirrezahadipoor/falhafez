package com.amirrezahadipoor.falhafez.presentation.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.amirrezahadipoor.falhafez.core.designsystem.FalPalette
import com.amirrezahadipoor.falhafez.core.designsystem.FalText
import com.amirrezahadipoor.falhafez.core.theme.FalThemeSpec
import com.amirrezahadipoor.falhafez.presentation.components.OrnamentalDivider
import com.amirrezahadipoor.falhafez.presentation.components.RitualBackground
import androidx.compose.material3.Text

@Composable
fun SplashScreen(onFinished: (seenOnboarding: Boolean) -> Unit) {
    val viewModel: SplashViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.ready) {
        if (state.ready) onFinished(state.seenOnboarding)
    }

    RitualBackground(spec = FalThemeSpec.tazhib(), showParticles = true) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "فال حافظ", style = FalText.display, color = FalPalette.GoldBright)
            Spacer(Modifier.height(12.dp))
            OrnamentalDivider(color = FalPalette.Gold, modifier = Modifier.fillMaxWidth(0.5f))
            Spacer(Modifier.height(28.dp))
            Text(
                text = "در حال آماده‌سازی دیوان…",
                style = FalText.bodyMuted,
                color = FalPalette.CreamMuted
            )
        }
    }
}
