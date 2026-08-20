package ir.siliksama.falhafez.presentation.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import ir.siliksama.falhafez.core.designsystem.FalPalette
import ir.siliksama.falhafez.core.designsystem.FalText
import ir.siliksama.falhafez.core.theme.FalThemeSpec
import ir.siliksama.falhafez.core.util.PersianText
import ir.siliksama.falhafez.core.util.findActivity
import ir.siliksama.falhafez.domain.model.SupportTier
import ir.siliksama.falhafez.presentation.components.ScreenHeader
import ir.siliksama.falhafez.presentation.share.SocialNetwork

private enum class SettingsTab(val faName: String) {
    THEME("قالب"), DISPLAY("نمایش"), SOUND("صدا"), SUPPORT("حمایت مالی"), CHANNEL("کانال من"), APPS("برنامه‌ها"), GENERAL("عمومی")
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

    var tab by rememberSaveable { mutableIntStateOf(0) }

    Box(Modifier.fillMaxSize().background(FalPalette.Navy)) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(title = "تنظیمات", onBack = onBack)

            // tab bar — scrollable, one tap to any section
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SettingsTab.entries.toList()) { t ->
                    val index = SettingsTab.entries.indexOf(t)
                    FilterChip(
                        selected = tab == index,
                        onClick = { tab = index },
                        label = { Text(t.faName, style = FalText.caption) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FalPalette.Gold,
                            selectedLabelColor = Color(0xFF14100A),
                            containerColor = FalPalette.NavySoft,
                            labelColor = FalPalette.CreamMuted
                        )
                    )
                }
            }
            Spacer(Modifier.height(10.dp))

            Box(Modifier.weight(1f)) {
                when (SettingsTab.entries[tab]) {
                    SettingsTab.THEME -> ThemeGrid(
                        selectedId = themeId,
                        unlocked = unlockedThemes,
                        onSelect = { spec ->
                            when {
                                !spec.locked -> viewModel.setTheme(spec.id)
                                spec.id.id in unlockedThemes -> viewModel.setTheme(spec.id)
                                activity != null -> viewModel.requestUnlockTheme(activity, spec.id)
                            }
                        }
                    )
                    SettingsTab.DISPLAY -> Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CardBox {
                            Text("اندازهٔ قلم", style = FalText.heading, color = FalPalette.GoldBright)
                            Spacer(Modifier.height(8.dp))
                            Text("نمونه: غزلِ حافظ با قلمِ خوانا", style = FalText.body, color = FalPalette.Cream)
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
                                style = FalText.caption, color = FalPalette.CreamMuted
                            )
                        }
                        CardBox {
                            Text("رنگِ قلم", style = FalText.heading, color = FalPalette.GoldBright)
                            Spacer(Modifier.height(4.dp))
                            Text("رنگِ ابیات و تفسیر — یا دنبالهٔ قالب", style = FalText.caption, color = FalPalette.CreamMuted)
                            Spacer(Modifier.height(10.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val keys = listOf("theme", "cream", "white", "gold", "emerald", "azure")
                                items(keys) { key ->
                                    FilterChip(
                                        selected = fontColor == key,
                                        onClick = { viewModel.setFontColor(key) },
                                        label = { Text(FalFontColors.label(key), style = FalText.caption) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = FalPalette.Gold,
                                            selectedLabelColor = Color(0xFF14100A),
                                            containerColor = FalPalette.NavyLight,
                                            labelColor = FalPalette.Cream
                                        )
                                    )
                                }
                            }
                        }
                    }
                    SettingsTab.SOUND -> Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CardBox { SettingSwitchRow("صداهای آیینی", "زنگِ گشودن دیوان و صدای دکمه‌ها", soundEnabled, viewModel::setSound) }
                        CardBox { SettingSwitchRow("لرزش (هاپتیک)", "بازخورد لمسی هنگام فال گرفتن", hapticsEnabled, viewModel::setHaptics) }
                        CardBox { SettingSwitchRow("یادآوری فال روزانه", "هر روز ساعت ۸ صبح، بدون اینترنت", notificationsEnabled, ::onNotificationsToggle) }
                    }
                    SettingsTab.SUPPORT -> SupportTab(
                        currentTier = supportTier,
                        purchasing = purchasing,
                        onPurchase = { tier -> activity?.let { viewModel.purchase(it, tier) } }
                    )
                    SettingsTab.CHANNEL -> ChannelTab(
                        network = channelNetwork,
                        handle = channelHandle,
                        name = channelName,
                        onSave = { n, h, nm -> viewModel.setChannel(n, h, nm) },
                        onSharePromo = { h, nm -> viewModel.shareChannelPromo(h, nm) }
                    )
                    SettingsTab.APPS -> AppsTab()
                    SettingsTab.GENERAL -> Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CardBox {
                            Text("پشتیبان‌گیری از تاریخچه و علاقه‌مندی‌ها", style = FalText.body, color = FalPalette.Cream)
                            Spacer(Modifier.height(4.dp))
                            Text("یک فایل JSON از فال‌هایت می‌سازد.", style = FalText.caption, color = FalPalette.CreamMuted)
                            Spacer(Modifier.height(8.dp))
                            TextButton(
                                onClick = viewModel::exportData,
                                modifier = Modifier.background(FalPalette.NavyLight, RoundedCornerShape(12.dp))
                            ) { Text("خروجی گرفتن", style = FalText.button, color = FalPalette.Gold) }
                        }
                        CardBox {
                            Text("فال حافظ | تعبیر هوشمند — نسخهٔ ${PersianText.digits(version)}", style = FalText.body, color = FalPalette.Cream)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "متن اشعار از گنجور (مالکیت عمومی). قلم‌ها: وزیرمتن و نستعلیق اردو (OFL). تصویرسازی‌ها: اختصاصی.",
                                style = FalText.caption, color = FalPalette.CreamMuted
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TextButton(
                                onClick = {
                                    val uri = Uri.parse("market://details?id=${context.packageName}")
                                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                                },
                                modifier = Modifier.weight(1f).background(FalPalette.NavySoft, RoundedCornerShape(14.dp))
                            ) { Text("امتیاز دادن", style = FalText.button, color = FalPalette.Gold) }
                            TextButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, "فال حافظ | تعبیر هوشمند\nhttps://cafebazaar.ir/app/ir.siliksama.falhafez")
                                    }
                                    context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری اپلیکیشن").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                },
                                modifier = Modifier.weight(1f).background(FalPalette.NavySoft, RoundedCornerShape(14.dp))
                            ) { Text("معرفی", style = FalText.button, color = FalPalette.Gold) }
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
    onPurchase: (SupportTier) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CardBox {
            Text("حمایتِ مالی = حذفِ همیشگیِ تبلیغات", style = FalText.heading, color = FalPalette.GoldBright)
            Spacer(Modifier.height(6.dp))
            Text(
                "با یک‌بار حمایت، تبلیغات برای همیشه حذف می‌شود و اپلیکیشن با انرژیِ شما زنده می‌ماند.",
                style = FalText.bodyMuted, color = FalPalette.CreamMuted
            )
        }

        if (currentTier != SupportTier.NONE) {
            CardBox {
                Text("وضعیتِ شما: ${currentTier.faName} ✓", style = FalText.heading, color = FalPalette.Gold)
                Spacer(Modifier.height(4.dp))
                Text("تبلیغات حذف شده است. سپاس از حمایت شما ♥", style = FalText.bodyMuted, color = FalPalette.CreamMuted)
            }
        }

        listOf(SupportTier.BASE, SupportTier.PLUS, SupportTier.GOLD).forEach { tier ->
            val active = currentTier == tier
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (active) FalPalette.NavyLight else FalPalette.NavySoft, RoundedCornerShape(18.dp))
                    .border(1.5.dp, if (active) FalPalette.Gold else FalPalette.GoldDeep.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(tier.faName, style = FalText.heading, color = FalPalette.GoldBright, modifier = Modifier.weight(1f))
                    Text("${formatPrice(tier.priceToman)} تومان", style = FalText.heading, color = FalPalette.Cream)
                }
                Text(tier.perks, style = FalText.bodyMuted, color = FalPalette.CreamMuted)
                TextButton(
                    onClick = { if (!active) onPurchase(tier) },
                    enabled = !active && !purchasing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (active) FalPalette.GoldDeep.copy(alpha = 0.35f) else FalPalette.Gold,
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
                        color = if (active) FalPalette.Cream else Color(0xFF14100A)
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
    onSave: (String, String, String) -> Unit,
    onSharePromo: (String, String) -> Unit
) {
    var selectedNetwork by remember { mutableStateOf(network) }
    var handleText by remember { mutableStateOf(handle) }
    var nameText by remember { mutableStateOf(name) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CardBox {
            Text("کانال یا صفحهٔ اجتماعیِ شما", style = FalText.heading, color = FalPalette.GoldBright)
            Spacer(Modifier.height(6.dp))
            Text(
                "از این پس، تصویرِ فالِ اشتراکی، کانالِ شما را هم معرفی می‌کند — تبلیغِ رایگان در هر فال.",
                style = FalText.bodyMuted, color = FalPalette.CreamMuted
            )
        }

        CardBox {
            Text("انتخاب شبکه", style = FalText.body, color = FalPalette.Cream)
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(SocialNetwork.entries.toList()) { networkEnum ->
                    val selected = selectedNetwork == networkEnum.key
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) FalPalette.NavyLight else Color.Transparent)
                            .border(1.dp, if (selected) FalPalette.Gold else FalPalette.GoldDeep.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
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
                        Text(networkEnum.label, style = FalText.caption, color = FalPalette.CreamMuted)
                    }
                }
            }
        }

        CardBox {
            OutlinedTextField(
                value = nameText,
                onValueChange = { nameText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("نام نمایشی (مثلاً: فال حافظِ ما)", style = FalText.caption) },
                textStyle = FalText.body,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FalPalette.Gold,
                    unfocusedBorderColor = FalPalette.GoldDeep.copy(alpha = 0.5f),
                    focusedTextColor = FalPalette.Cream,
                    unfocusedTextColor = FalPalette.Cream,
                    cursorColor = FalPalette.Gold,
                    focusedContainerColor = FalPalette.NavySoft,
                    unfocusedContainerColor = FalPalette.NavySoft
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
                    focusedBorderColor = FalPalette.Gold,
                    unfocusedBorderColor = FalPalette.GoldDeep.copy(alpha = 0.5f),
                    focusedTextColor = FalPalette.Cream,
                    unfocusedTextColor = FalPalette.Cream,
                    cursorColor = FalPalette.Gold,
                    focusedContainerColor = FalPalette.NavySoft,
                    unfocusedContainerColor = FalPalette.NavySoft
                )
            )
            Spacer(Modifier.height(12.dp))
            TextButton(
                onClick = { onSave(selectedNetwork, handleText.trim(), nameText.trim()) },
                modifier = Modifier.fillMaxWidth().background(FalPalette.Gold, RoundedCornerShape(14.dp))
            ) { Text("ذخیرهٔ کانال", style = FalText.button, color = Color(0xFF14100A)) }
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = { onSharePromo(handleText.trim(), nameText.trim()) },
                modifier = Modifier.fillMaxWidth().background(FalPalette.NavyLight, RoundedCornerShape(14.dp))
            ) { Text("ساخت و اشتراکِ عکسِ تبلیغاتیِ کانال", style = FalText.button, color = FalPalette.Gold) }
        }
    }
}

/* ------------------------------------------------------------------ */
/*  برنامه‌های دیگر سازنده                                              */
/* ------------------------------------------------------------------ */
@Composable
private fun AppsTab() {
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

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CardBox {
            Text("سایر برنامه‌های سازنده", style = FalText.heading, color = FalPalette.GoldBright)
            Spacer(Modifier.height(4.dp))
            Text("اگر این اپ را دوست دارید، این‌ها را هم ببینید:", style = FalText.bodyMuted, color = FalPalette.CreamMuted)
        }

        // بنر ۱: کنکوریفای
        PromoBanner(
            title = "کنکوریفای",
            subtitle = "برنامه‌ریز کنکور — برای کنکوری‌های جدی",
            cta = "دریافت از بازار",
            onClick = { openBazaar("ir.konkoorify.app") }
        )

        // بنر ۲: فاکتور حسابداری
        PromoBanner(
            title = "فاکتور حسابداری",
            subtitle = "صدور فاکتور سریع و حرفه‌ای برای کسب‌وکار",
            cta = "دریافت از بازار",
            onClick = { openBazaar("com.siliksama.factor_hesabdari") }
        )
    }
}

@Composable
private fun PromoBanner(title: String, subtitle: String, cta: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(FalPalette.NavySoft, RoundedCornerShape(18.dp))
            .border(1.dp, FalPalette.GoldDeep.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, style = FalText.heading, color = FalPalette.GoldBright)
        Text(subtitle, style = FalText.bodyMuted, color = FalPalette.CreamMuted)
        TextButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().background(FalPalette.Gold, RoundedCornerShape(14.dp))
        ) { Text(cta, style = FalText.button, color = Color(0xFF14100A)) }
    }
}

/* ------------------------------------------------------------------ */
/*  helpers                                                             */
/* ------------------------------------------------------------------ */
@Composable
private fun ThemeGrid(
    selectedId: ir.siliksama.falhafez.core.theme.FalThemeId,
    unlocked: Set<String>,
    onSelect: (FalThemeSpec) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        gridItems(FalThemeSpec.All) { spec ->
            val isUnlocked = !spec.locked || spec.id.id in unlocked
            val selected = spec.id == selectedId
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (selected) FalPalette.NavyLight else FalPalette.NavySoft)
                    .border(
                        width = if (selected) 1.5.dp else 1.dp,
                        color = if (selected) spec.accent else FalPalette.GoldDeep.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelect(spec) }
                    .padding(8.dp)
            ) {
                val art = spec.artworkRes
                if (art != null) {
                    Image(
                        painter = painterResource(art),
                        contentDescription = spec.id.faName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(84.dp).clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(spec.id.faName, style = FalText.body, color = FalPalette.Cream, modifier = Modifier.weight(1f))
                    when {
                        spec.locked && !isUnlocked -> Text("قفل", style = FalText.caption, color = FalPalette.Gold)
                        selected -> Icon(Icons.Filled.Check, contentDescription = "انتخاب‌شده", tint = spec.accent, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CardBox(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().background(FalPalette.NavySoft, RoundedCornerShape(16.dp)).padding(16.dp),
        content = content
    )
}

@Composable
private fun SettingSwitchRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = FalText.body, color = FalPalette.Cream)
            Text(subtitle, style = FalText.caption, color = FalPalette.CreamMuted)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = FalPalette.Navy,
                checkedTrackColor = FalPalette.Gold,
                uncheckedThumbColor = FalPalette.CreamMuted,
                uncheckedTrackColor = FalPalette.NavyLight
            )
        )
    }
}
