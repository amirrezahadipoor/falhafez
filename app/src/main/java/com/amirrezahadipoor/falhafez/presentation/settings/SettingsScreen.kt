package com.amirrezahadipoor.falhafez.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amirrezahadipoor.falhafez.core.designsystem.FalPalette
import com.amirrezahadipoor.falhafez.core.designsystem.FalText
import com.amirrezahadipoor.falhafez.core.theme.FalThemeSpec
import com.amirrezahadipoor.falhafez.presentation.components.ScreenHeader

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val themeId by viewModel.themeId.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize().background(FalPalette.Navy)) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(title = "تنظیمات", onBack = onBack)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 20.dp, vertical = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "قالبِ فال",
                        style = FalText.heading,
                        color = FalPalette.Cream,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                items(FalThemeSpec.All.size) { index ->
                    val spec = FalThemeSpec.All[index]
                    ThemeRow(
                        spec = spec,
                        selected = spec.id == themeId,
                        onClick = { viewModel.setTheme(spec.id) }
                    )
                }
                item {
                    Text(
                        text = "دیگر تنظیمات (اندازه قلم، یادآوری، درباره) در فاز بعدی کامل می‌شود.",
                        style = FalText.caption,
                        color = FalPalette.CreamMuted,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeRow(
    spec: FalThemeSpec,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (selected) FalPalette.NavyLight else FalPalette.NavySoft
            )
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) spec.accent else FalPalette.GoldDeep.copy(alpha = 0.4f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // mini color preview
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(spec.backgroundTop, spec.accent, spec.particle, spec.onBackground)
                .forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text = spec.id.faName,
            style = FalText.body,
            color = FalPalette.Cream,
            modifier = Modifier.weight(1f)
        )
        if (spec.locked) {
            Text(text = "قفل", style = FalText.caption, color = FalPalette.CreamMuted)
        } else if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "انتخاب‌شده",
                tint = spec.accent
            )
        }
    }
}
