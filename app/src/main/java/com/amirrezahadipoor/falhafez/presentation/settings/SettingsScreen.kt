package com.amirrezahadipoor.falhafez.presentation.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amirrezahadipoor.falhafez.core.designsystem.FalPalette
import com.amirrezahadipoor.falhafez.core.designsystem.FalText
import com.amirrezahadipoor.falhafez.core.theme.FalThemeSpec
import com.amirrezahadipoor.falhafez.core.util.PersianText
import com.amirrezahadipoor.falhafez.core.util.findActivity
import com.amirrezahadipoor.falhafez.presentation.components.ScreenHeader

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val themeId by viewModel.themeId.collectAsStateWithLifecycle()
    val fontSizeScale by viewModel.fontSizeScale.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val unlockedThemes by viewModel.unlockedThemes.collectAsStateWithLifecycle()
    val version = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "1.0.0"
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.setNotifications(true)
    }

    fun onNotificationsToggle(enabled: Boolean) {
        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        viewModel.setNotifications(enabled)
    }

    Box(Modifier.fillMaxSize().background(FalPalette.Navy)) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(title = "تنظیمات", onBack = onBack)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { SectionTitle("قالبِ فال") }
                items(FalThemeSpec.All.size) { index ->
                    val spec = FalThemeSpec.All[index]
                    val unlocked = !spec.locked || spec.id.id in unlockedThemes
                    ThemeRow(
                        spec = spec,
                        selected = spec.id == themeId,
                        unlocked = unlocked,
                        onClick = {
                            when {
                                !spec.locked -> viewModel.setTheme(spec.id)
                                unlocked -> viewModel.setTheme(spec.id)
                                activity != null -> viewModel.requestUnlockTheme(activity, spec.id)
                            }
                        }
                    )
                }

                item { SectionTitle("اندازهٔ قلم") }
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(FalPalette.NavySoft, RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            "نمونه: غزلِ حافظ با قلمِ خوانا",
                            style = FalText.body,
                            color = FalPalette.Cream
                        )
                        Spacer(Modifier.height(12.dp))
                        Slider(
                            value = fontSizeScale,
                            onValueChange = viewModel::setFontSizeScale,
                            valueRange = 0.85f..1.4f,
                            colors = SliderDefaults.colors(
                                thumbColor = FalPalette.Gold,
                                activeTrackColor = FalPalette.Gold,
                                inactiveTrackColor = FalPalette.NavyLight
                            )
                        )
                        Text(
                            "مقیاس: ${PersianText.digits(String.format("%.2f", fontSizeScale))}",
                            style = FalText.caption,
                            color = FalPalette.CreamMuted
                        )
                    }
                }

                item { SectionTitle("یادآوری روزانه") }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(FalPalette.NavySoft, RoundedCornerShape(18.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("یادآوری فال", style = FalText.body, color = FalPalette.Cream)
                            Text(
                                "هر روز ساعت ۸ صبح، بدون نیاز به اینترنت",
                                style = FalText.caption,
                                color = FalPalette.CreamMuted
                            )
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = ::onNotificationsToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = FalPalette.Navy,
                                checkedTrackColor = FalPalette.Gold,
                                uncheckedThumbColor = FalPalette.CreamMuted,
                                uncheckedTrackColor = FalPalette.NavyLight
                            )
                        )
                    }
                }

                item { SectionTitle("داده‌ها") }
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(FalPalette.NavySoft, RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Text("پشتیبان‌گیری از تاریخچه و علاقه‌مندی‌ها", style = FalText.body, color = FalPalette.Cream)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "یک فایل JSON از فال‌هایت می‌سازد و برایت می‌فرستد.",
                            style = FalText.caption,
                            color = FalPalette.CreamMuted
                        )
                        Spacer(Modifier.height(10.dp))
                        TextButton(
                            onClick = viewModel::exportData,
                            modifier = Modifier.background(FalPalette.NavyLight, RoundedCornerShape(12.dp))
                        ) {
                            Text("خروجی گرفتن", style = FalText.button, color = FalPalette.Gold)
                        }
                    }
                }

                item { SectionTitle("درباره") }
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(FalPalette.NavySoft, RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Text("فال حافظ — نسخهٔ ${PersianText.digits(version)}", style = FalText.body, color = FalPalette.Cream)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "متن اشعار از گنجور (متن‌های کلاسیک در مالکیت عمومی). " +
                                "قلم‌ها: وزیرمتن و نستعلیق اردو (مجوز OFL). " +
                                "تصویرسازی‌ها: آثار تولیدشدهٔ اختصاصی.",
                            style = FalText.caption,
                            color = FalPalette.CreamMuted
                        )
                    }
                }

                item { SectionTitle("همراهی") }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(
                            onClick = {
                                val uri = Uri.parse("market://details?id=${context.packageName}")
                                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .background(FalPalette.NavySoft, RoundedCornerShape(16.dp))
                        ) {
                            Text("امتیاز دادن", style = FalText.button, color = FalPalette.Gold)
                        }
                        TextButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "فال حافظ — دیوان و فال حافظ\nhttps://github.com/amirrezahadipoor/falhafez")
                                }
                                context.startActivity(
                                    Intent.createChooser(intent, "اشتراک‌گذاری اپلیکیشن")
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .background(FalPalette.NavySoft, RoundedCornerShape(16.dp))
                        ) {
                            Text("معرفی به دوستان", style = FalText.button, color = FalPalette.Gold)
                        }
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = FalText.heading,
        color = FalPalette.GoldBright,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun ThemeRow(spec: FalThemeSpec, selected: Boolean, unlocked: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) FalPalette.NavyLight else FalPalette.NavySoft)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) spec.accent else FalPalette.GoldDeep.copy(alpha = 0.4f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
        if (spec.locked && !unlocked) {
            Text(text = "قفل — با ویدئو باز می‌شود", style = FalText.caption, color = FalPalette.Gold)
        } else if (selected) {
            Icon(Icons.Filled.Check, contentDescription = "انتخاب‌شده", tint = spec.accent)
        }
    }
}
