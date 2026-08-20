package ir.siliksama.falhafez.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.AlertDialog
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
import ir.siliksama.falhafez.core.designsystem.FalPalette
import ir.siliksama.falhafez.core.designsystem.FalText
import ir.siliksama.falhafez.core.util.openAppInBazaar
import ir.siliksama.falhafez.presentation.ads.BannerAdView
import ir.siliksama.falhafez.presentation.favorites.FavoritesScreen
import ir.siliksama.falhafez.presentation.history.HistoryScreen
import ir.siliksama.falhafez.presentation.home.HomeScreen
import ir.siliksama.falhafez.presentation.library.LibraryScreen
import ir.siliksama.falhafez.presentation.stories.StoriesScreen

enum class MainTab(val faName: String, val icon: ImageVector) {
    FAL("فال", Icons.Outlined.AutoAwesome),
    STORIES("داستان‌ها", Icons.Outlined.AutoStories),
    HISTORY("تاریخچه", Icons.Outlined.History),
    LIBRARY("دیوان", Icons.Outlined.MenuBook),
    FAVORITES("علاقه‌مندی‌ها", Icons.Outlined.FavoriteBorder)
}

@Composable
fun MainScreen(onOpenSettings: () -> Unit) {
    val mainViewModel: MainViewModel = hiltViewModel()
    val pendingUpdate by mainViewModel.pendingUpdate.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    var requestedPoemId by remember { mutableStateOf<Long?>(null) }
    val selected = MainTab.entries[selectedIndex]

    pendingUpdate?.let { update ->
        AlertDialog(
            onDismissRequest = mainViewModel::dismissUpdate,
            containerColor = FalPalette.NavySoft,
            titleContentColor = FalPalette.GoldBright,
            textContentColor = FalPalette.Cream,
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
                }) { Text("بروزرسانی", style = FalText.button, color = FalPalette.Gold) }
            },
            dismissButton = {
                TextButton(onClick = mainViewModel::dismissUpdate) {
                    Text("بعداً", style = FalText.button, color = FalPalette.CreamMuted)
                }
            }
        )
    }

    Scaffold(
        containerColor = FalPalette.Navy,
        // Each screen manages its own status-bar inset (ScreenHeader/statusBarsPadding),
        // so the Scaffold must not add a second top inset.
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        bottomBar = {
            Column {
                // Persistent banner on History & Library only (Home has its own on the
                // niyyat screen; the draw ritual and reveal stay completely ad-free).
                if (selected == MainTab.HISTORY || selected == MainTab.LIBRARY) {
                    BannerAdView()
                }
                NavigationBar(containerColor = FalPalette.Navy) {
                    MainTab.entries.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = index == selectedIndex,
                            onClick = { selectedIndex = index },
                            icon = { Icon(tab.icon, contentDescription = tab.faName) },
                            label = { Text(tab.faName, style = FalText.caption) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = FalPalette.GoldBright,
                                selectedTextColor = FalPalette.Gold,
                                unselectedIconColor = FalPalette.CreamMuted,
                                unselectedTextColor = FalPalette.CreamMuted,
                                indicatorColor = FalPalette.NavyLight
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
