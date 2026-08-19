package com.amirrezahadipoor.falhafez.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.amirrezahadipoor.falhafez.core.designsystem.FalPalette
import com.amirrezahadipoor.falhafez.presentation.components.EmptyState
import com.amirrezahadipoor.falhafez.presentation.components.ScreenHeader

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    Box(Modifier.fillMaxSize().background(FalPalette.Navy)) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(title = "تنظیمات", onBack = onBack)
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Outlined.Settings,
                    title = "تنظیمات در حال آماده‌سازی است",
                    subtitle = "به‌زودی: قالب فال، اندازه قلم، یادآوری روزانه و درباره ما."
                )
            }
        }
    }
}
