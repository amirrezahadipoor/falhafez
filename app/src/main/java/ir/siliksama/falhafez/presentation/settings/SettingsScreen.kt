package ir.siliksama.falhafez.presentation.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.siliksama.falhafez.core.designsystem.FalFontColors
import ir.siliksama.falhafez.core.designsystem.FalText
import ir.siliksama.falhafez.core.theme.FalThemeSpec
import ir.siliksama.falhafez.core.util.PersianText
import ir.siliksama.falhafez.core.util.findActivity
import ir.siliksama.falhafez.core.util.openAppInBazaar
import ir.siliksama.falhafez.domain.model.SupportTier
import ir.siliksama.falhafez.domain.model.UpdateCheckResult
import ir.siliksama.falhafez.presentation.components.ScreenHeader
import ir.siliksama.falhafez.presentation.components.ScrollableColumn
import ir.siliksama.falhafez.presentation.share.SocialNetwork

private enum class SettingsTab(val faName: String) {
    THEME("قالب"), DISPLAY("نمایش"), SOUND("صدا"), SUPPORT("حمایت"), CHANNEL("کانال"), APPS("سایر"), GENERAL("عمومی")
}

private fun formatPrice(toman: Int): String =
    PersianText.digits(toman.toString().reversed().chunked(3).joinToString(",").reversed())

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val themeId by viewModel.themeId.collectAsStateWithLifecycle()
    val fontSizeScale by viewModel.fontSizeScale.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val soundEnabled by viewModel.soundEnabled.collectAsStateWithLifecycle()
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsStateWithLifecycle()
    val fontColor by viewModel.fontColor.collectAsStateWithLifecycle()
    val supportTier by viewModel.supportTier.collectAsStateWithLifecycle()
    val purchasing by viewModel.purchasing.collectAsStateWithLifecycle()
    val channelNetwork by viewModel.channelNetwork.collectAsStateWithLifecycle()
    val channelHandle by viewModel.channelHandle.collectAsStateWithLifecycle()
    val channelName by viewModel.channelName.collectAsStateWithLifecycle()
    val updateResult by viewModel.updateResult.collectAsStateWithLifecycle()
    val showSupport by viewModel.showSupport.collectAsStateWithLifecycle()
    val spec = FalThemeSpec.byId(themeId)

    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
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

    var tab by rememberSaveable { mutableIntStateOf(0) }

    // اگر روی قالبِ «ویژهٔ حامیان» کلیک شود، به تبِ حمایت مالی می‌پریم.
    LaunchedEffect(showSupport) {
        if (showSupport) {
            tab = SettingsTab.entries.indexOf(SettingsTab.SUPPORT)
            viewModel.closeSupport()
        }
    }

    LaunchedEffect(updateResult) {
        when (val r = updateResult) {
            is UpdateCheckResult.UpToDate -> {
                Toast.makeText(context, "نسخهٔ شما به‌روز است ✓", Toast.LENGTH_SHORT).show()
                viewModel.clearUpdateResult()
            }
            is UpdateCheckResult.Failed -> {
                Toast.makeText(context, "بررسی نشد — اینترنت را چک کنید", Toast.LENGTH_SHORT).show()
                viewModel.clearUpdateResult()
            }
            else -> Unit
        }
    }

    val currentUpdate = updateResult as? UpdateCheckResult.Available
    if (currentUpdate != null) {
        AlertDialog(
            onDismissRequest = viewModel::clearUpdateResult,
            containerColor = spec.card,
            titleContentColor = spec.accentSoft,
            textContentColor = spec.onBackground,
            title = { Text("نسخهٔ جدید موجود است", style = FalText.heading) },
            text = {
                Text(
                    if (currentUpdate.versionName.isNotBlank()) "نسخهٔ ${currentUpdate.versionName} آماده است؛ بروزرسانی کنید."
                    else "بروزرسانی جدیدی در کافه‌بازار موجود است.",
                    style = FalText.bodyMuted
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    openAppInBazaar(context, "ir.siliksama.falhafez")
                    viewModel.clearUpdateResult()
                }) { Text("بروزرسانی", style = FalText.button, color = spec.accent) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::clearUpdateResult) {
                    Text("بعداً", style = FalText.button, color = spec.onBackgroundMuted)
                }
            }
        )
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(spec.backgroundTop, spec.backgroundBottom)))
    ) {
        Column(Modifier.fillMaxSize().navigationBarsPadding()) {
            ScreenHeader(title = "تنظیمات", onBack = onBack, titleColor = spec.onBackground)

            // تب‌های جمع‌وجور — یک ردیف فشرده با عرض برابر، بدون اسکرول
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(spec.card, RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SettingsTab.entries.forEach { t ->
                    val index = SettingsTab.entries.indexOf(t)
                    val selected = tab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(11.dp))
                            .background(if (selected) spec.accent else androidx.compose.ui.graphics.Color.Transparent)
                            .clickable { tab = index }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            t.faName,
                            style = FalText.caption,
                            color = if (selected) Color(0xFF14100A) else spec.onBackgroundMuted,
                            maxLines = 1
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))

            Box(Modifier.weight(1f)) {
                when (SettingsTab.entries[tab]) {
                    SettingsTab.THEME -> ThemeGrid(
                        selectedId = themeId,
                        isSubscriber = supportTier.adsRemoved,
                        appSpec = spec,
                        onSelect = { picked ->
                            if (picked.subscriberOnly && !supportTier.adsRemoved) {
                                viewModel.openSupport()
                            } else {
                                viewModel.setTheme(picked.id)
                            }
                        }
                    )
                    SettingsTab.DISPLAY -> ScrollableColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CardBox(spec = spec) {
                            Text("اندازهٔ قلم", style = FalText.heading, color = spec.accentSoft)
                            Spacer(Modifier.height(8.dp))
                            Text("نمونه: غزلِ حافظ با قلمِ خوانا", style = FalText.body, color = spec.onBackground)
                            Spacer(Modifier.height(12.dp))
                            Slider(
                                value = fontSizeScale,
                                onValueChange = viewModel::setFontSizeScale,
                                valueRange = 0.85f..1.4f,
                                colors = SliderDefaults.colors(
                                    thumbColor = spec.accent,
                                    activeTrackColor = spec.accent,
                                    inactiveTrackColor = spec.border
                                )
                            )
                            Text(
                                "مقیاس: ${PersianText.digits(String.format(Locale.US, "%.2f", fontSizeScale))}",
                                style = FalText.caption, color = spec.onBackgroundMuted
                            )
                        }
                        CardBox(spec = spec) {
                            Text("رنگِ قلم", style = FalText.heading, color = spec.accentSoft)
                            Spacer(Modifier.height(4.dp))
                            Text("رنگِ ابیات و تفسیر — یا دنبالهٔ قالب", style = FalText.caption, color = spec.onBackgroundMuted)
                            Spacer(Modifier.height(10.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val keys = listOf("theme", "cream", "white", "gold", "emerald", "azure")
                                items(keys) { key ->
                                    FilterChip(
                                        selected = fontColor == key,
                                        onClick = { viewModel.setFontColor(key) },
                                        label = { Text(FalFontColors.label(key), style = FalText.caption) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = spec.accent,
                                            selectedLabelColor = Color(0xFF14100A),
                                            containerColor = spec.card,
                                            labelColor = spec.onBackground
                                        )
                                    )
                                }
                            }
                        }
                    }
                    SettingsTab.SOUND -> ScrollableColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CardBox(spec = spec) { SettingSwitchRow(spec = spec, title = "صداهای آیینی", subtitle = "زنگِ گشودن دیوان و صدای دکمه‌ها", checked = soundEnabled, onCheckedChange = viewModel::setSound) }
                        CardBox(spec = spec) { SettingSwitchRow(spec = spec, title = "لرزش (هاپتیک)", subtitle = "بازخورد لمسی هنگام فال گرفتن", checked = hapticsEnabled, onCheckedChange = viewModel::setHaptics) }
                        CardBox(spec = spec) { SettingSwitchRow(spec = spec, title = "یادآوری فال روزانه", subtitle = "هر روز ساعت ۸ صبح، بدون اینترنت", checked = notificationsEnabled, onCheckedChange = ::onNotificationsToggle) }
                    }
                    SettingsTab.SUPPORT -> SupportTab(
                        currentTier = supportTier,
                        purchasing = purchasing,
                        onPurchase = { tier -> activity?.let { viewModel.purchase(it, tier) } },
                        spec = spec
                    )
                    SettingsTab.CHANNEL -> ChannelTab(
                        network = channelNetwork,
                        handle = channelHandle,
                        name = channelName,
                        spec = spec,
                        onSave = { n, h, nm -> viewModel.setChannel(n, h, nm) },
                        onSharePromo = { h, nm -> viewModel.shareChannelPromo(h, nm) }
                    )
                    SettingsTab.APPS -> AppsTab(spec = spec)
                    SettingsTab.GENERAL -> ScrollableColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CardBox(spec = spec) {
                            Text("پشتیبان‌گیری از تاریخچه و علاقه‌مندی‌ها", style = FalText.body, color = spec.onBackground)
                            Spacer(Modifier.height(4.dp))
                            Text("یک فایل JSON از فال‌هایت می‌سازد.", style = FalText.caption, color = spec.onBackgroundMuted)
                            Spacer(Modifier.height(8.dp))
                            TextButton(
                                onClick = viewModel::exportData,
                                modifier = Modifier.background(spec.card, RoundedCornerShape(12.dp))
                            ) { Text("خروجی گرفتن", style = FalText.button, color = spec.accent) }
                        }
                        CardBox(spec = spec) {
                            Text("ارتباط با ما", style = FalText.heading, color = spec.accentSoft)
                            Spacer(Modifier.height(4.dp))
                            Text("ایمیل: siliksama@gmail.com", style = FalText.caption, color = spec.onBackgroundMuted)
                            Text("تلگرام و واتس‌اپ: ۰۹۲۱ ۲۳۱ ۱۲۵۱", style = FalText.caption, color = spec.onBackgroundMuted)
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ContactButton(
                                    label = "ایمیل", spec = spec, modifier = Modifier.weight(1f)
                                ) {
                                    runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:siliksama@gmail.com"))
                                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        )
                                    }
                                }
                                ContactButton(
                                    label = "تلگرام", spec = spec, modifier = Modifier.weight(1f)
                                ) {
                                    runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/+989212311251"))
                                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        )
                                    }
                                }
                                ContactButton(
                                    label = "واتس‌اپ", spec = spec, modifier = Modifier.weight(1f)
                                ) {
                                    runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/989212311251"))
                                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        )
                                    }
                                }
                            }
                        }
                        CardBox(spec = spec) {
                            Text("حریم خصوصی", style = FalText.heading, color = spec.accentSoft)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "این اپ دادهٔ شخصی جمع نمی‌کند؛ متن کاملِ سیاستِ حفظ حریم خصوصی را بخوانید.",
                                style = FalText.caption, color = spec.onBackgroundMuted
                            )
                            Spacer(Modifier.height(8.dp))
                            TextButton(
                                onClick = {
                                    runCatching {
                                        context.startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("https://amirrezahadipoor.github.io/falhafez/privacy.html")
                                            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        )
                                    }
                                },
                                modifier = Modifier.background(spec.card, RoundedCornerShape(12.dp))
                            ) { Text("مشاهدهٔ سیاستِ حریم خصوصی", style = FalText.button, color = spec.accent) }
                        }
                        CardBox(spec = spec) {
                            Text("فال حافظ | تعبیر هوشمند — نسخهٔ ${PersianText.digits(version)}", style = FalText.body, color = spec.onBackground)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "متن اشعار از گنجور (مالکیت عمومی). قلم‌ها: وزیرمتن و نستعلیق اردو (OFL). تصویرسازی‌ها: اختصاصی.",
                                style = FalText.caption, color = spec.onBackgroundMuted
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TextButton(
                                onClick = {
                                    val uri = Uri.parse("market://details?id=${context.packageName}")
                                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                                },
                                modifier = Modifier.weight(1f).background(spec.card, RoundedCornerShape(14.dp))
                            ) { Text("امتیاز دادن", style = FalText.button, color = spec.accent) }
                            TextButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, "فال حافظ | تعبیر هوشمند\nhttps://cafebazaar.ir/app/ir.siliksama.falhafez")
                                    }
                                    context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری اپلیکیشن").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                },
                                modifier = Modifier.weight(1f).background(spec.card, RoundedCornerShape(14.dp))
                            ) { Text("معرفی", style = FalText.button, color = spec.accent) }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

/* ------------------------------------------------------------------ */
/*  حمایت مالی                                                          */
/* ------------------------------------------------------------------ */
@Composable
private fun SupportTab(
    currentTier: SupportTier,
    purchasing: Boolean,
    onPurchase: (SupportTier) -> Unit,
    spec: FalThemeSpec
) {
    ScrollableColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CardBox(spec = spec) {
            Text("حمایتِ مالی = حذف تبلیغات + فالِ نامحدود", style = FalText.heading, color = spec.accentSoft)
            Spacer(Modifier.height(6.dp))
            Text(
                "با یک‌بار حمایت، تبلیغات برای همیشه حذف می‌شود و فال برایتان نامحدود می‌ماند.",
                style = FalText.bodyMuted, color = spec.onBackgroundMuted
            )
        }

        if (currentTier != SupportTier.NONE) {
            CardBox(spec = spec) {
                Text("وضعیتِ شما: ${currentTier.faName} ✓", style = FalText.heading, color = spec.accent)
                Spacer(Modifier.height(4.dp))
                Text("تبلیغات حذف شده است. سپاس از حمایت شما ♥", style = FalText.bodyMuted, color = spec.onBackgroundMuted)
            }
        }

        listOf(SupportTier.BASE, SupportTier.PLUS, SupportTier.GOLD).forEach { tier ->
            val active = currentTier == tier
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (active) spec.card else spec.card, RoundedCornerShape(18.dp))
                    .border(1.5.dp, if (active) spec.accent else spec.border.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(tier.faName, style = FalText.heading, color = spec.accentSoft, modifier = Modifier.weight(1f))
                    Text("${formatPrice(tier.priceToman)} تومان", style = FalText.heading, color = spec.onBackground)
                }
                Text(tier.perks, style = FalText.bodyMuted, color = spec.onBackgroundMuted)
                TextButton(
                    onClick = { if (!active) onPurchase(tier) },
                    enabled = !active && !purchasing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (active) spec.accent.copy(alpha = 0.30f) else spec.accent,
                            RoundedCornerShape(14.dp)
                        )
                ) {
                    Text(
                        text = when {
                            active -> "فعال ✓"
                            purchasing -> "در حال پرداخت…"
                            else -> "حمایت و حذفِ تبلیغات"
                        },
                        style = FalText.button,
                        color = if (active) spec.onBackground else Color(0xFF14100A)
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

/* ------------------------------------------------------------------ */
/*  کانال من                                                            */
/* ------------------------------------------------------------------ */
@Composable
private fun ChannelTab(
    network: String,
    handle: String,
    name: String,
    spec: FalThemeSpec,
    onSave: (String, String, String) -> Unit,
    onSharePromo: (String, String) -> Unit
) {
    var selectedNetwork by remember { mutableStateOf(network) }
    var handleText by remember { mutableStateOf(handle) }
    var nameText by remember { mutableStateOf(name) }

    ScrollableColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CardBox(spec = spec) {
            Text("کانال یا صفحهٔ اجتماعیِ شما", style = FalText.heading, color = spec.accentSoft)
            Spacer(Modifier.height(6.dp))
            Text(
                "با «حمایتِ ویژه» یا «حمایتِ همیشگی»، نام و کانالِ شما روی فالِ اشتراکی نقش می‌بندد — تبلیغِ شما در هر فال.",
                style = FalText.bodyMuted, color = spec.onBackgroundMuted
            )
        }

        CardBox(spec = spec) {
            Text("انتخاب شبکه", style = FalText.body, color = spec.onBackground)
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(SocialNetwork.entries.toList()) { networkEnum ->
                    val selected = selectedNetwork == networkEnum.key
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) spec.card else Color.Transparent)
                            .border(1.dp, if (selected) spec.accent else spec.border.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                            .clickable { selectedNetwork = networkEnum.key }
                            .padding(8.dp)
                    ) {
                        Image(
                            painter = painterResource(networkEnum.iconRes),
                            contentDescription = networkEnum.label,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(networkEnum.label, style = FalText.caption, color = spec.onBackgroundMuted)
                    }
                }
            }
        }

        CardBox(spec = spec) {
            OutlinedTextField(
                value = nameText,
                onValueChange = { nameText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("نام نمایشی (مثلاً: فال حافظِ ما)", style = FalText.caption) },
                textStyle = FalText.body,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = spec.accent,
                    unfocusedBorderColor = spec.border.copy(alpha = 0.6f),
                    focusedTextColor = spec.onBackground,
                    unfocusedTextColor = spec.onBackground,
                    cursorColor = spec.accent,
                    focusedContainerColor = spec.card,
                    unfocusedContainerColor = spec.card
                )
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = handleText,
                onValueChange = { handleText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("شناسه (بدون @ — مثلاً mychannel)", style = FalText.caption) },
                textStyle = FalText.body,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = spec.accent,
                    unfocusedBorderColor = spec.border.copy(alpha = 0.6f),
                    focusedTextColor = spec.onBackground,
                    unfocusedTextColor = spec.onBackground,
                    cursorColor = spec.accent,
                    focusedContainerColor = spec.card,
                    unfocusedContainerColor = spec.card
                )
            )
            Spacer(Modifier.height(12.dp))
            TextButton(
                onClick = { onSave(selectedNetwork, handleText.trim(), nameText.trim()) },
                modifier = Modifier.fillMaxWidth().background(spec.accent, RoundedCornerShape(14.dp))
            ) { Text("ذخیرهٔ کانال", style = FalText.button, color = Color(0xFF14100A)) }
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = { onSharePromo(handleText.trim(), nameText.trim()) },
                modifier = Modifier.fillMaxWidth().background(spec.card, RoundedCornerShape(14.dp))
            ) { Text("ساخت و اشتراکِ عکسِ تبلیغاتیِ کانال", style = FalText.button, color = spec.accent) }
        }
    }
}

/* ------------------------------------------------------------------ */
/*  برنامه‌های دیگر سازنده                                              */
/* ------------------------------------------------------------------ */
@Composable
private fun AppsTab(spec: FalThemeSpec) {
    val context = LocalContext.current

    fun openBazaar(packageName: String) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("bazaar://details?id=$packageName")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }.getOrElse {
            runCatching {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://cafebazaar.ir/app/?id=$packageName")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }
    }

    ScrollableColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CardBox(spec = spec) {
            Text("سایر برنامه‌های سازنده", style = FalText.heading, color = spec.accentSoft)
            Spacer(Modifier.height(4.dp))
            Text("اگر این اپ را دوست دارید، این‌ها را هم ببینید:", style = FalText.bodyMuted, color = spec.onBackgroundMuted)
        }

        // بنر ۱: کنکوریفای
        PromoBanner(
            title = "کنکوریفای",
            subtitle = "برنامه‌ریز کنکور — برای کنکوری‌های جدی",
            cta = "دریافت از بازار",
            spec = spec,
            onClick = { openBazaar("ir.konkoorify.app") }
        )

        // بنر ۲: فاکتور حسابداری
        PromoBanner(
            title = "فاکتور حسابداری",
            subtitle = "صدور فاکتور سریع و حرفه‌ای برای کسب‌وکار",
            cta = "دریافت از بازار",
            spec = spec,
            onClick = { openBazaar("com.siliksama.factor_hesabdari") }
        )
    }
}

@Composable
private fun PromoBanner(title: String, subtitle: String, cta: String, spec: FalThemeSpec, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(spec.card, RoundedCornerShape(18.dp))
            .border(1.dp, spec.border.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, style = FalText.heading, color = spec.accentSoft)
        Text(subtitle, style = FalText.bodyMuted, color = spec.onBackgroundMuted)
        TextButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().background(spec.accent, RoundedCornerShape(14.dp))
        ) { Text(cta, style = FalText.button, color = Color(0xFF14100A)) }
    }
}

/* ------------------------------------------------------------------ */
/*  helpers                                                             */
/* ------------------------------------------------------------------ */
@Composable
private fun ThemeGrid(
    selectedId: ir.siliksama.falhafez.core.theme.FalThemeId,
    isSubscriber: Boolean,
    appSpec: FalThemeSpec,
    onSelect: (FalThemeSpec) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        gridItems(FalThemeSpec.All) { theme ->
            val selected = theme.id == selectedId
            val lockedForSubscriber = theme.subscriberOnly && !isSubscriber
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (selected) appSpec.accent.copy(alpha = 0.14f) else appSpec.card)
                    .border(
                        width = if (selected) 1.5.dp else 1.dp,
                        color = if (selected) theme.accent else appSpec.border.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelect(theme) }
                    .padding(8.dp)
            ) {
                val art = theme.artworkRes
                if (art != null) {
                    Image(
                        painter = painterResource(art),
                        contentDescription = theme.id.faName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(84.dp).clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(theme.id.faName, style = FalText.body, color = appSpec.onBackground, modifier = Modifier.weight(1f))
                    when {
                        lockedForSubscriber -> Text("ویژهٔ حامیان ♥", style = FalText.caption, color = appSpec.accent)
                        selected -> Icon(Icons.Filled.Check, contentDescription = "انتخاب‌شده", tint = theme.accent, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CardBox(spec: FalThemeSpec, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().background(spec.card, RoundedCornerShape(16.dp)).padding(16.dp),
        content = content
    )
}

@Composable
private fun ContactButton(
    label: String,
    spec: FalThemeSpec,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.background(spec.card, RoundedCornerShape(12.dp))
    ) {
        Text(label, style = FalText.button, color = spec.accent)
    }
}

@Composable
private fun SettingSwitchRow(spec: FalThemeSpec, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = FalText.body, color = spec.onBackground)
            Text(subtitle, style = FalText.caption, color = spec.onBackgroundMuted)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = spec.onBackground,
                checkedTrackColor = spec.accent,
                uncheckedThumbColor = spec.onBackgroundMuted,
                uncheckedTrackColor = spec.border
            )
        )
    }
}
