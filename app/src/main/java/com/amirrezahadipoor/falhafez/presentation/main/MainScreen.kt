package com.amirrezahadipoor.falhafez.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
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
import com.amirrezahadipoor.falhafez.core.designsystem.FalPalette
import com.amirrezahadipoor.falhafez.core.designsystem.FalText
import com.amirrezahadipoor.falhafez.presentation.favorites.FavoritesScreen
import com.amirrezahadipoor.falhafez.presentation.history.HistoryScreen
import com.amirrezahadipoor.falhafez.presentation.home.HomeScreen
import com.amirrezahadipoor.falhafez.presentation.library.LibraryScreen

enum class MainTab(val faName: String, val icon: ImageVector) {
    FAL("فال", Icons.Outlined.AutoAwesome),
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
        bottomBar = {
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
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (selected) {
                MainTab.FAL -> HomeScreen(
                    onOpenSettings = onOpenSettings,
                    onOpenPoem = { id ->
                        requestedPoemId = id
                        selectedIndex = 2
                    }
                )
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
