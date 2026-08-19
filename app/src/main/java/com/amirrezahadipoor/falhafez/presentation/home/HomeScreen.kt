package com.amirrezahadipoor.falhafez.presentation.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amirrezahadipoor.falhafez.core.theme.FalThemeSpec
import com.amirrezahadipoor.falhafez.presentation.components.RitualBackground

@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    onOpenPoem: (Long) -> Unit = {}
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val themeId by viewModel.themeId.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val spec = FalThemeSpec.byId(themeId)

    RitualBackground(spec = spec) {
        AnimatedContent(
            targetState = state.stage,
            transitionSpec = { fadeIn(tween(450)) togetherWith fadeOut(tween(260)) },
            label = "draw-stage"
        ) { stage ->
            when (stage) {
                DrawStage.NIYYAT -> NiyyatContent(
                    spec = spec,
                    state = state,
                    onQuestionChange = viewModel::onQuestionChange,
                    onCategorySelect = viewModel::onCategorySelect,
                    onDraw = viewModel::draw,
                    onOpenSettings = onOpenSettings
                )

                DrawStage.DRAWING -> DivanOpeningAnimation(
                    spec = spec,
                    onFinished = viewModel::onDrawingFinished
                )

                DrawStage.REVEAL -> state.lastDraw?.let { entry ->
                    RevealContent(
                        spec = spec,
                        poem = entry.poem,
                        onReadInterpretation = viewModel::onReadInterpretation
                    )
                } ?: NiyyatContent(
                    spec = spec, state = state,
                    onQuestionChange = viewModel::onQuestionChange,
                    onCategorySelect = viewModel::onCategorySelect,
                    onDraw = viewModel::draw,
                    onOpenSettings = onOpenSettings
                )

                DrawStage.INTERPRETATION -> state.lastDraw?.let { entry ->
                    InterpretationContent(
                        spec = spec,
                        poem = entry.poem,
                        category = entry.category,
                        isFavorite = isFavorite,
                        cooldownActive = state.cooldownActive,
                        remainingToday = state.remainingToday,
                        onToggleFavorite = viewModel::onToggleFavorite,
                        onDrawAgain = viewModel::draw,
                        onOpenPoem = { onOpenPoem(entry.poem.id) },
                        onDismiss = viewModel::onDismiss
                    )
                } ?: NiyyatContent(
                    spec = spec, state = state,
                    onQuestionChange = viewModel::onQuestionChange,
                    onCategorySelect = viewModel::onCategorySelect,
                    onDraw = viewModel::draw,
                    onOpenSettings = onOpenSettings
                )
            }
        }
    }
}
