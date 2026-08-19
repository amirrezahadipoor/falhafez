package com.amirrezahadipoor.falhafez.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.amirrezahadipoor.falhafez.core.designsystem.FalPalette
import com.amirrezahadipoor.falhafez.core.designsystem.FalText
import com.amirrezahadipoor.falhafez.core.theme.FalThemeSpec
import com.amirrezahadipoor.falhafez.presentation.components.GoldButton
import com.amirrezahadipoor.falhafez.presentation.components.OrnamentalDivider
import com.amirrezahadipoor.falhafez.presentation.components.RitualBackground

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val viewModel: OnboardingViewModel = hiltViewModel()
    val spec = FalThemeSpec.tazhib()

    RitualBackground(spec = spec) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "فال حافظ", style = FalText.display, color = spec.accentSoft)
            Spacer(Modifier.height(12.dp))
            OrnamentalDivider(color = spec.accent, modifier = Modifier.fillMaxWidth(0.5f))
            Spacer(Modifier.height(36.dp))

            Text(
                text = "سنتِ کهنِ فال و دیوان",
                style = FalText.heading,
                color = spec.onBackground
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "قرن‌هاست ایرانیان در لحظه‌های تصمیم، دل به دیوان حافظ می‌سپارند؛ نیّتی در دل می‌کنند، دیوان را می‌گشایند و پاسخِ دلشان را در شعری می‌جویند.",
                style = FalText.body,
                color = spec.onBackgroundMuted,
                textAlign = TextAlign.Center,
                lineHeight = FalText.body.lineHeight
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "این اپلیکیشن، همان آیین را با احترام، آفلاین و برای همیشه همراه شما می‌آورد.",
                style = FalText.bodyMuted,
                color = spec.onBackgroundMuted,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))
            GoldButton(text = "آغاز", onClick = { viewModel.finish(); onDone() })
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { viewModel.finish(); onDone() }) {
                Text("رد شدن", style = FalText.caption, color = spec.onBackgroundMuted)
            }
        }
    }
}
