package ir.falhafez.tabir.presentation.main

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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ir.falhafez.tabir.core.designsystem.FalPalette
import ir.falhafez.tabir.core.designsystem.FalText
import ir.falhafez.tabir.presentation.ads.BannerAdView
import ir.falhafez.tabir.presentation.favorites.FavoritesScreen
import ir.falhafez.tabir.presentation.history.HistoryScreen
import ir.falhafez.tabir.presentation.home.HomeScreen
import ir.falhafez.tabir.presentation.library.LibraryScreen
import ir.falhafez.tabir.presentation.stories.StoriesScreen

enum class MainTab(val faName: String, val icon: ImageVector) {
    FAL("فال", Icons.Outlined.AutoAwesome),
    STORIES("داستان‌ها", Icons.Outlined.AutoStories),
    HISTORY("تاریخچه", Icons.Outlined.History),
    LIBRARY("دیوان", Icons.Outlined.MenuBook),
    FAVORITES("علاقه‌مندی‌ها", Icons.Outlined.FavoriteBorder)
}

@Composable
fun MainScreen(onOpenSettings: () -> Unit) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    var requestedPoemId by remember { mutableStateOf<Long?>(null) }
    val selected = MainTab.entries[selectedIndex]

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
