package ir.siliksama.falhafez.presentation.library

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.siliksama.falhafez.core.designsystem.FalText
import ir.siliksama.falhafez.core.theme.FalThemeSpec
import ir.siliksama.falhafez.domain.model.Collection
import ir.siliksama.falhafez.domain.model.FalCategory
import ir.siliksama.falhafez.domain.model.Poet
import ir.siliksama.falhafez.presentation.components.EmptyState
import ir.siliksama.falhafez.presentation.components.PoemCard
import ir.siliksama.falhafez.presentation.components.PoemDetail
import ir.siliksama.falhafez.presentation.components.RitualBackground
import ir.siliksama.falhafez.presentation.components.ScreenHeader
import ir.siliksama.falhafez.presentation.ads.NativeAdCard

@Composable
fun LibraryScreen(
    requestedPoemId: Long? = null,
    onRequestConsumed: () -> Unit = {}
) {
    val viewModel: LibraryViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val detail by viewModel.detail.collectAsStateWithLifecycle()
    val themeId by viewModel.themeId.collectAsStateWithLifecycle()
    val results by viewModel.searchResults.collectAsStateWithLifecycle()
    val isFavorite by viewModel.favorite.isSelectedFavorite.collectAsStateWithLifecycle()
    val readIds by viewModel.readIds.collectAsStateWithLifecycle()
    val spec = FalThemeSpec.byId(themeId)

    LaunchedEffect(requestedPoemId) {
        if (requestedPoemId != null) {
            viewModel.openPoemById(requestedPoemId)
            onRequestConsumed()
        }
    }

    // دکمهٔ بازگشتِ سیستم: جزئیات ← فهرست ← مجموعه ← شاعر
    BackHandler(
        enabled = detail != null || state.collection != null || state.poet != null
    ) {
        viewModel.back()
    }

    detail?.let { poem ->
        RitualBackground(spec = spec, showParticles = false) {
            PoemDetail(
                poem = poem,
                category = FalCategory.NONE,
                spec = spec,
                isFavorite = isFavorite,
                isRead = poem.id in readIds,
                onToggleFavorite = viewModel.favorite::toggleSelected,
                onToggleRead = { viewModel.toggleRead(poem) },
                onBack = viewModel::back
            )
        }
        return
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(spec.backgroundTop, spec.backgroundBottom)))
    ) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(
                title = headerTitle(state),
                onBack = if (state.poet != null) viewModel::back else null,
                titleColor = spec.onBackground
            )
            SearchField(
                query = state.query,
                onQueryChange = viewModel::onQueryChange,
                spec = spec
            )
            val currentPoet = state.poet
            val currentCollection = state.collection
            when {
                state.query.isNotBlank() -> SearchResults(results = results, readIds = readIds, onOpen = viewModel::openPoem, spec = spec)
                currentCollection != null -> PoemsList(
                    poems = state.poems,
                    loading = state.loading,
                    readIds = readIds,
                    onOpen = viewModel::openPoem,
                    spec = spec
                )
                currentPoet != null -> CollectionsList(
                    collections = Collection.byPoet(currentPoet).filter { it != Collection.STORIES },
                    onOpen = viewModel::openCollection,
                    spec = spec
                )
                else -> PoetsList(onOpen = viewModel::openPoet, spec = spec)
            }
        }
    }
}

private fun headerTitle(state: LibraryUiState): String = when {
    state.collection != null -> state.collection.faName
    state.poet != null -> state.poet.faName
    else -> "دیوان شاعران"
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, spec: FalThemeSpec) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        placeholder = { Text("جستجو در اشعار…", style = FalText.bodyMuted, color = spec.onBackgroundMuted) },
        textStyle = FalText.body,
        singleLine = true,
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "جستجو", tint = spec.onBackgroundMuted) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Outlined.Clear, contentDescription = "پاک کردن", tint = spec.onBackgroundMuted)
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
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
}

@Composable
private fun PoetsList(onOpen: (Poet) -> Unit, spec: FalThemeSpec) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(Poet.entries.size) { index ->
            val poet = Poet.entries[index]
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(spec.card, RoundedCornerShape(18.dp))
                    .border(1.dp, spec.border.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                    .clickable { onOpen(poet) }
                    .padding(18.dp)
            ) {
                Text(poet.faName, style = FalText.heading, color = spec.accentSoft)
                Spacer(Modifier.height(4.dp))
                Text(
                    Collection.byPoet(poet).joinToString("، ") { it.faName },
                    style = FalText.caption,
                    color = spec.onBackgroundMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CollectionsList(collections: List<Collection>, onOpen: (Collection) -> Unit, spec: FalThemeSpec) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(collections.size) { index ->
            val collection = collections[index]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(spec.card, RoundedCornerShape(18.dp))
                    .border(1.dp, spec.border.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                    .clickable { onOpen(collection) }
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.MenuBook, contentDescription = null, tint = spec.accent, modifier = Modifier.size(22.dp))
                Spacer(Modifier.size(12.dp))
                Text(collection.faName, style = FalText.body, color = spec.onBackground)
            }
        }
    }
}

private val THEME_FA = mapOf(
    "love" to "عشق", "hope" to "امید", "patience" to "صبوری", "joy" to "شادی",
    "new-beginnings" to "آغاز تازه", "travel" to "سفر", "effort" to "کوشش",
    "wisdom" to "حکمت", "compassion" to "مهرورزی", "legacy" to "ماندگاری",
    "faith" to "ایمان", "decision" to "تصمیم", "general" to "عمومی"
)

@Composable
private fun PoemsList(poems: List<ir.siliksama.falhafez.domain.model.Poem>, loading: Boolean, readIds: Set<Long>, onOpen: (ir.siliksama.falhafez.domain.model.Poem) -> Unit, spec: FalThemeSpec) {
    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = spec.accent)
        }
        return
    }
    if (poems.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyState(
                icon = Icons.Outlined.MenuBook,
                title = "در این بخش شعری نیست",
                subtitle = "به‌زودی افزوده می‌شود.",
                color = spec.onBackgroundMuted
            )
        }
        return
    }

    val tags = remember(poems) { poems.map { it.themeTag }.distinct() }
    var selectedTag by remember(poems) { mutableStateOf<String?>(null) }
    val filtered = if (selectedTag == null) poems else poems.filter { it.themeTag == selectedTag }

    // 2-column grid — twice the poems per screen, single-row filter (no vertical space)
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (tags.size > 1) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedTag == null,
                            onClick = { selectedTag = null },
                            label = { Text("همه", style = FalText.caption) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = spec.accent,
                                selectedLabelColor = androidx.compose.ui.graphics.Color(0xFF14100A),
                                containerColor = spec.card,
                                labelColor = spec.onBackgroundMuted
                            )
                        )
                    }
                    items(tags) { tag ->
                        FilterChip(
                            selected = selectedTag == tag,
                            onClick = { selectedTag = if (selectedTag == tag) null else tag },
                            label = { Text(THEME_FA[tag] ?: tag, style = FalText.caption) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = spec.accent,
                                selectedLabelColor = androidx.compose.ui.graphics.Color(0xFF14100A),
                                containerColor = spec.card,
                                labelColor = spec.onBackgroundMuted
                            )
                        )
                    }
                }
            }
        }
        items(filtered.size) { index ->
            val poem = filtered[index]
            PoemCard(poem = poem, spec = spec, onClick = { onOpen(poem) }, isRead = poem.id in readIds)
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            NativeAdCard()
        }
    }
}

@Composable
private fun SearchResults(results: List<ir.siliksama.falhafez.domain.model.Poem>, readIds: Set<Long>, onOpen: (ir.siliksama.falhafez.domain.model.Poem) -> Unit, spec: FalThemeSpec) {
    if (results.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyState(
                icon = Icons.Outlined.Search,
                title = "نتیجه‌ای یافت نشد",
                subtitle = "واژهٔ دیگری را امتحان کنید.",
                color = spec.onBackgroundMuted
            )
        }
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(results) { poem ->
            PoemCard(poem = poem, spec = spec, onClick = { onOpen(poem) }, isRead = poem.id in readIds)
        }
    }
}
