package ir.siliksama.falhafez.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import ir.siliksama.falhafez.core.designsystem.FalText
import ir.siliksama.falhafez.core.designsystem.RotatingStar
import ir.siliksama.falhafez.core.theme.FalThemeSpec
import ir.siliksama.falhafez.presentation.components.GoldButton
import ir.siliksama.falhafez.presentation.components.OrnamentalDivider
import ir.siliksama.falhafez.presentation.components.RitualBackground

private data class OnboardPage(
    val title: String,
    val body: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val pages = listOf(
    OnboardPage(
        title = "سنتِ کهنِ فال",
        body = "قرن‌هاست ایرانیان در لحظه‌های تصمیم، دل به دیوانِ حافظ می‌سپارند؛ نیّتی در دل می‌کنند، دیوان را می‌گشایند و پاسخِ دلشان را در شعری می‌جویند.",
        icon = Icons.Outlined.MenuBook
    ),
    OnboardPage(
        title = "یک آیینِ واقعی",
        body = "نیّت کن، نفس بکش، و دیوان را بگشا. غزلِ تو بی‌ت‌به‌بیت آشکار می‌شود و سپس تعبیرش را می‌خوانی — آرام، مانند یک دانای مهربان.",
        icon = Icons.Outlined.AutoStories
    ),
    OnboardPage(
        title = "همیشه همراهِ تو",
        body = "کلِ دیوانِ حافظ، سعدی، مولانا و خیام — آفلاین و بدون نیاز به اینترنت. فالِ امروز، تاریخچه، علاقه‌مندی‌ها و جستجو در دستانِ توست.",
        icon = Icons.Outlined.WifiOff
    )
)

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val viewModel: OnboardingViewModel = hiltViewModel()
    val spec = FalThemeSpec.tazhib()

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    var finished by remember { mutableStateOf(false) }

    RitualBackground(spec = spec) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                RotatingStar(tint = spec.accent.copy(alpha = 0.5f), size = 130.dp)
                Text(text = "فال حافظ", style = FalText.display, color = spec.accentSoft)
            }
            Spacer(Modifier.height(8.dp))
            OrnamentalDivider(color = spec.accent, modifier = Modifier.fillMaxWidth(0.5f))
            Spacer(Modifier.height(20.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { pageIndex ->
                val page = pages[pageIndex]
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = null,
                        tint = spec.accent,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(22.dp))
                    Text(page.title, style = FalText.heading, color = spec.onBackground)
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = page.body,
                        style = FalText.bodyMuted,
                        color = spec.onBackgroundMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(pages.size) { i ->
                    Box(
                        modifier = Modifier
                            .size(if (i == pagerState.currentPage) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (i == pagerState.currentPage) spec.accent else spec.onBackgroundMuted.copy(alpha = 0.4f)
                            )
                    )
                }
            }
            Spacer(Modifier.height(20.dp))

            if (pagerState.currentPage == pages.lastIndex) {
                GoldButton(text = "آغاز", onClick = { if (!finished) { finished = true; viewModel.finish(); onDone() } }, glow = true)
            } else {
                GoldButton(
                    text = "بعدی",
                    onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                    glow = true
                )
            }

            TextButton(onClick = { if (!finished) { finished = true; viewModel.finish(); onDone() } }) {
                Text("رد شدن", style = FalText.caption, color = spec.onBackgroundMuted)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
