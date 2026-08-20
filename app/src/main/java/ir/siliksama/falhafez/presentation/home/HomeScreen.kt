package ir.siliksama.falhafez.presentation.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import ir.siliksama.falhafez.core.designsystem.FalPalette
import ir.siliksama.falhafez.core.designsystem.FalText
import ir.siliksama.falhafez.core.theme.FalThemeSpec
import ir.siliksama.falhafez.core.util.findActivity
import ir.siliksama.falhafez.presentation.components.RitualBackground
import ir.siliksama.falhafez.presentation.components.SupportPanel

@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    onOpenPoem: (Long) -> Unit = {}
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val themeId by viewModel.themeId.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val supportTier by viewModel.supportTier.collectAsStateWithLifecycle()
    val channel by viewModel.channel.collectAsStateWithLifecycle()
    val adsRemoved by viewModel.adsRemoved.collectAsStateWithLifecycle()
    val purchasing by viewModel.purchasing.collectAsStateWithLifecycle()
    val spec = FalThemeSpec.byId(themeId)

    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    RitualBackground(spec = spec) {
        if (state.supportOpen) {
            AlertDialog(
                onDismissRequest = viewModel::closeSupport,
                containerColor = FalPalette.NavySoft,
                titleContentColor = FalPalette.GoldBright,
                textContentColor = FalPalette.Cream,
                title = { Text("حمایت مالی", style = FalText.heading) },
                text = {
                    SupportPanel(
                        currentTier = supportTier,
                        purchasing = purchasing,
                        onPurchase = { tier -> activity?.let { viewModel.purchase(it, tier) } }
                    )
                },
                confirmButton = {
                    TextButton(onClick = viewModel::closeSupport) {
                        Text("بستن", style = FalText.button, color = FalPalette.CreamMuted)
                    }
                }
            )
        }

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
                    onSourceSelect = viewModel::onSourceSelect,
                    onDailyFal = viewModel::openDailyFal,
                    channel = channel,
                    adsRemoved = adsRemoved,
                    onOpenSupport = viewModel::openSupport,
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
                    onSourceSelect = viewModel::onSourceSelect,
                    onDailyFal = viewModel::openDailyFal,
                    channel = channel,
                    adsRemoved = adsRemoved,
                    onOpenSupport = viewModel::openSupport,
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
                        adsRemoved = adsRemoved,
                        supportTier = supportTier,
                        onOpenPoem = { onOpenPoem(entry.poem.id) },
                        onDismiss = if (activity != null) { { viewModel.dismissAndMaybeAd(activity) } } else viewModel::dismissOnly
                    )
                } ?: NiyyatContent(
                    spec = spec, state = state,
                    onQuestionChange = viewModel::onQuestionChange,
                    onCategorySelect = viewModel::onCategorySelect,
                    onDraw = viewModel::draw,
                    onRewardedDraw = null,
                    onSourceSelect = viewModel::onSourceSelect,
                    onDailyFal = viewModel::openDailyFal,
                    channel = channel,
                    adsRemoved = adsRemoved,
                    onOpenSupport = viewModel::openSupport,
                    onOpenSettings = onOpenSettings
                )
            }
        }
    }
}
