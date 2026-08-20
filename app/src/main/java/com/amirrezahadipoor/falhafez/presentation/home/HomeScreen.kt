package com.amirrezahadipoor.falhafez.presentation.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amirrezahadipoor.falhafez.core.theme.FalThemeSpec
import com.amirrezahadipoor.falhafez.core.util.findActivity
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

    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    RitualBackground(spec = spec) {
        state.dailyFal?.let { dailyPoem ->
            DailyFalContent(
                spec = spec,
                poem = dailyPoem,
                isFavorite = isFavorite,
                onToggleFavorite = viewModel::onToggleFavorite,
                onBack = viewModel::closeDailyFal
            )
            return@RitualBackground
        }

        AnimatedContent(
            targetState = state.stage,
            transitionSpec = {
                (fadeIn(tween(450)) + slideInVertically(tween(450)) { it / 10 })
                    .togetherWith(fadeOut(tween(260)))
            },
            label = "draw-stage"
        ) { stage ->
            when (stage) {
                DrawStage.NIYYAT -> NiyyatContent(
                    spec = spec,
                    state = state,
                    onQuestionChange = viewModel::onQuestionChange,
                    onCategorySelect = viewModel::onCategorySelect,
                    onDraw = viewModel::draw,
                    onRewardedDraw = if (activity != null) { { viewModel.requestExtraDraw(activity) } } else null,
                    onDailyFal = viewModel::openDailyFal,
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
                    onRewardedDraw = null,
                    onDailyFal = viewModel::openDailyFal,
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
                        onRewarded = if (activity != null) { { viewModel.requestSkipCooldown(activity) } } else null,
                        onOpenPoem = { onOpenPoem(entry.poem.id) },
                        onDismiss = if (activity != null) { { viewModel.dismissAndMaybeAd(activity) } } else viewModel::dismissOnly
                    )
                } ?: NiyyatContent(
                    spec = spec, state = state,
                    onQuestionChange = viewModel::onQuestionChange,
                    onCategorySelect = viewModel::onCategorySelect,
                    onDraw = viewModel::draw,
                    onRewardedDraw = null,
                    onDailyFal = viewModel::openDailyFal,
                    onOpenSettings = onOpenSettings
                )
            }
        }
    }
}
