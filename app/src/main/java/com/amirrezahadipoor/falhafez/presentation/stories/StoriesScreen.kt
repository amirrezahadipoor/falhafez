package com.amirrezahadipoor.falhafez.presentation.stories

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amirrezahadipoor.falhafez.core.designsystem.FalPalette
import com.amirrezahadipoor.falhafez.core.designsystem.FalText
import com.amirrezahadipoor.falhafez.core.designsystem.readingColor
import com.amirrezahadipoor.falhafez.core.theme.FalThemeSpec
import com.amirrezahadipoor.falhafez.core.util.Clipboard
import com.amirrezahadipoor.falhafez.core.util.PersianText
import com.amirrezahadipoor.falhafez.domain.model.Poem
import com.amirrezahadipoor.falhafez.presentation.components.EmptyState
import com.amirrezahadipoor.falhafez.presentation.components.GhostButton
import com.amirrezahadipoor.falhafez.presentation.components.RitualBackground
import com.amirrezahadipoor.falhafez.presentation.components.ScreenHeader

@Composable
fun StoriesScreen() {
    val viewModel: StoriesViewModel = hiltViewModel()
    val stories by viewModel.stories.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    val themeId by viewModel.themeId.collectAsStateWithLifecycle()
    val spec = FalThemeSpec.byId(themeId)

    selected?.let { story ->
        StoryDetail(story = story, spec = spec, onBack = viewModel::close)
        return
    }

    Box(Modifier.fillMaxSize().background(FalPalette.Navy)) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(title = "داستان‌های آموزنده")
            if (stories.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Outlined.AutoStories,
                        title = "هنوز داستانی بارگذاری نشده",
                        subtitle = "لحظه‌ای صبر کنید…"
                    )
                }
            } else {
                // 2-column grid → ~2× more stories per screen, far less scrolling
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(stories) { story ->
                        StoryTile(story = story, onClick = { viewModel.open(story) })
                    }
                }
            }
        }
    }
}

@Composable
private fun StoryTile(story: Poem, onClick: () -> Unit) {
    val prose = story.verses.firstOrNull { !it.isCouplet }?.first ?: story.opening
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(FalPalette.NavySoft, RoundedCornerShape(16.dp))
            .border(1.dp, FalPalette.GoldDeep.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "داستانِ ${PersianText.number(story.number)}",
            style = FalText.heading,
            color = FalPalette.GoldBright
        )
        Text(
            text = prose,
            style = FalText.caption,
            color = FalPalette.Cream,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StoryDetail(story: Poem, spec: FalThemeSpec, onBack: () -> Unit) {
    val context = LocalContext.current
    val prose = story.verses.filter { !it.isCouplet }.joinToString("\n\n") { it.first }
    val morals = story.verses.filter { it.isCouplet }

    RitualBackground(spec = spec, showParticles = false) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
            ScreenHeader(
                title = "داستانِ ${PersianText.number(story.number)}",
                onBack = onBack,
                titleColor = spec.onBackground
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = prose, style = FalText.tafsir, color = readingColor(spec.onBackground), textAlign = TextAlign.Justify)

                if (morals.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(spec.card.copy(alpha = 0.9f), RoundedCornerShape(18.dp))
                            .border(1.dp, spec.accent.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("درسِ این حکایت", style = FalText.caption, color = spec.accentSoft)
                            Spacer(Modifier.height(8.dp))
                            morals.forEach { m ->
                                Text(m.first, style = FalText.verse, color = readingColor(spec.onBackground))
                                if (m.isCouplet) {
                                    Spacer(Modifier.height(3.dp))
                                    Text(m.second!!, style = FalText.verse, color = readingColor(spec.onBackground))
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(story.tafsir, style = FalText.bodyMuted, color = readingColor(spec.onBackgroundMuted), textAlign = TextAlign.Center)
                Spacer(Modifier.height(10.dp))
            }

            // fixed bottom actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    Clipboard.copy(context, "داستان", "$prose\n\n${morals.joinToString("\n") { it.fullText }}")
                }) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = "کپی داستان", tint = spec.onBackgroundMuted)
                }
            }
            GhostButton(text = "بازگشت", onClick = onBack, textColor = spec.onBackgroundMuted, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
        }
    }
}
