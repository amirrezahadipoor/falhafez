package ir.siliksama.falhafez.presentation.stories

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.siliksama.falhafez.core.designsystem.FalText
import ir.siliksama.falhafez.core.designsystem.readingColor
import ir.siliksama.falhafez.core.theme.FalThemeSpec
import ir.siliksama.falhafez.core.util.Clipboard
import ir.siliksama.falhafez.domain.model.Poem
import ir.siliksama.falhafez.presentation.components.EmptyState
import ir.siliksama.falhafez.presentation.components.GhostButton
import ir.siliksama.falhafez.presentation.components.RitualBackground
import ir.siliksama.falhafez.presentation.components.ScreenHeader
import ir.siliksama.falhafez.presentation.components.ScrollableColumn

@Composable
fun StoriesScreen() {
    val viewModel: StoriesViewModel = hiltViewModel()
    val stories by viewModel.stories.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    val themeId by viewModel.themeId.collectAsStateWithLifecycle()
    val readIds by viewModel.readIds.collectAsStateWithLifecycle()
    val spec = FalThemeSpec.byId(themeId)

    BackHandler(enabled = selected != null) { viewModel.close() }

    selected?.let { story ->
        StoryDetail(
            story = story,
            spec = spec,
            isRead = story.id in readIds,
            onToggleRead = { viewModel.toggleRead(story) },
            onBack = viewModel::close
        )
        return
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(spec.backgroundTop, spec.backgroundBottom)))
    ) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(title = "جهان", titleColor = spec.onBackground)
            if (stories.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    EmptyState(
                        icon = Icons.Outlined.AutoStories,
                        title = "در حال آماده‌سازی مطالب…",
                        subtitle = "لحظه‌ای صبر کنید",
                        color = spec.onBackgroundMuted
                    )
                    Spacer(Modifier.height(16.dp))
                    GhostButton(text = "تلاش دوباره", onClick = viewModel::load, textColor = spec.accent)
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
                        StoryTile(story = story, spec = spec, isRead = story.id in readIds, onClick = { viewModel.open(story) })
                    }
                }
            }
        }
    }
}

@Composable
private fun StoryTile(story: Poem, spec: FalThemeSpec, isRead: Boolean, onClick: () -> Unit) {
    val prose = story.verses.firstOrNull { !it.isCouplet }?.first ?: story.opening
    val title = prose.substringBefore("\n\n").trim().ifBlank { "جهان" }
    val body = prose.substringAfter("\n\n", prose)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(spec.card, RoundedCornerShape(16.dp))
            .border(1.dp, spec.border.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = FalText.heading,
                color = spec.accentSoft,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (isRead) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "خوانده‌شده",
                    tint = spec.accent,
                    modifier = Modifier.height(16.dp)
                )
            }
        }
        Text(
            text = body,
            style = FalText.caption,
            color = spec.onBackground,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StoryDetail(
    story: Poem,
    spec: FalThemeSpec,
    isRead: Boolean,
    onToggleRead: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val proseFull = story.verses.filter { !it.isCouplet }.joinToString("\n\n") { it.first }
    val title = proseFull.substringBefore("\n\n").trim().ifBlank { "جهان" }
    val body = proseFull.substringAfter("\n\n", proseFull)
    val morals = story.verses.filter { it.isCouplet }

    RitualBackground(spec = spec, showParticles = false) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
            ScreenHeader(
                title = title,
                onBack = onBack,
                titleColor = spec.onBackground
            )

            ScrollableColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = body, style = FalText.tafsir, color = readingColor(spec.onBackground), textAlign = TextAlign.Justify)

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
                            Text("چکیدهٔ کلیدی", style = FalText.caption, color = spec.accentSoft)
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
                IconButton(onClick = onToggleRead) {
                    Icon(
                        imageVector = if (isRead) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = if (isRead) "حذف علامت خوانده‌شده" else "علامت خوانده‌شده",
                        tint = if (isRead) spec.accent else spec.onBackgroundMuted
                    )
                }
                IconButton(onClick = {
                    Clipboard.copy(context, "جهان", "$proseFull\n\n${morals.joinToString("\n") { it.fullText }}")
                }) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = "کپی مطلب", tint = spec.onBackgroundMuted)
                }
            }
            GhostButton(text = "بازگشت", onClick = onBack, textColor = spec.onBackgroundMuted, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
        }
    }
}
