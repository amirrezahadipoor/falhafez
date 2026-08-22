package ir.siliksama.falhafez.presentation.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ir.siliksama.falhafez.core.designsystem.FalText
import ir.siliksama.falhafez.core.theme.FalThemeSpec
import ir.siliksama.falhafez.core.util.openAppInBazaar
import ir.siliksama.falhafez.presentation.ads.BannerAdView
import ir.siliksama.falhafez.presentation.favorites.FavoritesScreen
import ir.siliksama.falhafez.presentation.history.HistoryScreen
import ir.siliksama.falhafez.presentation.home.HomeScreen
import ir.siliksama.falhafez.presentation.library.LibraryScreen
import ir.siliksama.falhafez.presentation.stories.StoriesScreen

enum class MainTab(val faName: String, val icon: ImageVector) {
    FAL("فال", Icons.Outlined.AutoAwesome),
    STORIES("جهان", Icons.Outlined.Public),
    HISTORY("تاریخچه", Icons.Outlined.History),
    LIBRARY("دیوان", Icons.Outlined.MenuBook),
    FAVORITES("علاقه‌مندی‌ها", Icons.Outlined.FavoriteBorder)
}

@Composable
fun MainScreen(onOpenSettings: () -> Unit) {
    val mainViewModel: MainViewModel = hiltViewModel()
    // وضعیتِ اشتراک را واکنشی می‌خوانیم تا بلافاصله پس از خرید/بازیابی،
    // بنر بدونِ نیاز به راه‌اندازیِ دوبارهٔ اپ ناپدید شود.
    val adsRemoved by mainViewModel.adsRemoved.collectAsStateWithLifecycle()
    val pendingUpdate by mainViewModel.pendingUpdate.collectAsStateWithLifecycle()
    val themeId by mainViewModel.themeId.collectAsStateWithLifecycle()
    val spec = FalThemeSpec.byId(themeId)
    val context = LocalContext.current

    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    var requestedPoemId by remember { mutableStateOf<Long?>(null) }
    val selected = MainTab.entries[selectedIndex]

    // بازگشتِ سیستم از هر تب → بازگشت به تبِ فال (و اگر در فال بود، بستنِ اپ).
    // این هندلر قبل از محتوای تب‌ها ثبت می‌شود؛ هندلرهای داخلیِ هر تب (جزئیات، مراحل فال و…)
    // چون بعداً compose می‌شوند، در صورت فعال بودن بر این اولویت دارند.
    BackHandler(enabled = selectedIndex != 0) {
        selectedIndex = 0
    }

    pendingUpdate?.let { update ->
        AlertDialog(
            onDismissRequest = mainViewModel::dismissUpdate,
            containerColor = spec.card,
            titleContentColor = spec.accentSoft,
            textContentColor = spec.onBackground,
            title = { Text("نسخهٔ جدید موجود است", style = FalText.heading) },
            text = {
                Text(
                    if (update.versionName.isNotBlank()) "نسخهٔ ${update.versionName} آماده است؛ بروزرسانی کنید."
                    else "بروزرسانی جدیدی در کافه‌بازار موجود است.",
                    style = FalText.bodyMuted
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    openAppInBazaar(context, "ir.siliksama.falhafez")
                    mainViewModel.dismissUpdate()
                }) { Text("بروزرسانی", style = FalText.button, color = spec.accent) }
            },
            dismissButton = {
                TextButton(onClick = mainViewModel::dismissUpdate) {
                    Text("بعداً", style = FalText.button, color = spec.onBackgroundMuted)
                }
            }
        )
    }

    Scaffold(
        containerColor = spec.backgroundBottom,
        // Each screen manages its own status-bar inset (ScreenHeader/statusBarsPadding),
        // so the Scaffold must not add a second top inset.
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        bottomBar = {
            Column {
                // بنر فقط در «تاریخچه» و «دیوان» — صفحاتِ آرامِ مرور.
                // آیینِ فال (نیّت، گشودن، رونمایی، تفسیر) کاملاً بدونِ تبلیغ می‌مانَد.
                // برای حمایت‌کننده و در حالتِ آفلاین هم چیزی نمایش داده نمی‌شود
                // (خودِ BannerAdView هم این دو شرط را بررسی می‌کند).
                if ((selected == MainTab.HISTORY || selected == MainTab.LIBRARY) && !adsRemoved) {
                    BannerAdView()
                }
                NavigationBar(containerColor = spec.backgroundTop) {
                    MainTab.entries.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = index == selectedIndex,
                            onClick = { selectedIndex = index },
                            icon = { Icon(tab.icon, contentDescription = tab.faName) },
                            label = { Text(tab.faName, style = FalText.caption) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = spec.accentSoft,
                                selectedTextColor = spec.accent,
                                unselectedIconColor = spec.onBackgroundMuted,
                                unselectedTextColor = spec.onBackgroundMuted,
                                indicatorColor = spec.accent.copy(alpha = 0.18f)
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (selected) {
                MainTab.FAL -> HomeScreen(
                    onOpenSettings = onOpenSettings,
                    onOpenPoem = { id ->
                        requestedPoemId = id
                        selectedIndex = 3
                    }
                )
                MainTab.STORIES -> StoriesScreen()
                MainTab.HISTORY -> HistoryScreen()
                MainTab.LIBRARY -> LibraryScreen(
                    requestedPoemId = requestedPoemId,
                    onRequestConsumed = { requestedPoemId = null }
                )
                MainTab.FAVORITES -> FavoritesScreen()
            }
        }
    }
}
