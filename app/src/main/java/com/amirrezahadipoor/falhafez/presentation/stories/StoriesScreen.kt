package com.amirrezahadipoor.falhafez.presentation.stories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.amirrezahadipoor.falhafez.core.theme.FalThemeSpec
import com.amirrezahadipoor.falhafez.core.util.Clipboard
import com.amirrezahadipoor.falhafez.core.util.PersianText
import com.amirrezahadipoor.falhafez.domain.model.Poem
import com.amirrezahadipoor.falhafez.presentation.components.EmptyState
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(stories.size) { index ->
                        StoryCard(story = stories[index], onClick = { viewModel.open(stories[index]) })
                    }
                }
            }
        }
    }
}

@Composable
private fun StoryCard(story: Poem, onClick: () -> Unit) {
    val prose = story.verses.firstOrNull { !it.isCouplet }?.first ?: story.opening
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(FalPalette.NavySoft, RoundedCornerShape(18.dp))
            .border(1.dp, FalPalette.GoldDeep.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(
            text = "داستانِ ${PersianText.number(story.number)}",
            style = FalText.heading,
            color = FalPalette.GoldBright
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = prose,
            style = FalText.bodyMuted,
            color = FalPalette.Cream,
            maxLines = 3,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScreenHeader(
                title = "داستانِ ${PersianText.number(story.number)}",
                onBack = onBack,
                titleColor = spec.onBackground
            )

            Spacer(Modifier.height(6.dp))
            Text(
                text = prose,
                style = FalText.tafsir,
                color = spec.onBackground,
                textAlign = TextAlign.Justify
            )

            if (morals.isNotEmpty()) {
                Spacer(Modifier.height(22.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(spec.card.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                        .border(1.dp, spec.accent.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                        .padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("درسِ این حکایت", style = FalText.caption, color = spec.accentSoft)
                        Spacer(Modifier.height(10.dp))
                        morals.forEach { m ->
                            Text(m.first, style = FalText.verse, color = spec.onBackground)
                            if (m.isCouplet) {
                                Spacer(Modifier.height(4.dp))
                                Text(m.second!!, style = FalText.verse, color = spec.onBackground)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Text(
                text = story.tafsir,
                style = FalText.bodyMuted,
                color = spec.onBackgroundMuted,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(18.dp))
            IconButton(onClick = {
                Clipboard.copy(context, "داستان", "$prose\n\n${morals.joinToString("\n") { it.fullText }}")
            }) {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = "کپی داستان",
                    tint = spec.onBackgroundMuted
                )
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
