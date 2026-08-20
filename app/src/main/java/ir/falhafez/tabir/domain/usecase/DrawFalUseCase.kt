package ir.falhafez.tabir.domain.usecase

import ir.falhafez.tabir.domain.model.DrawEntry
import ir.falhafez.tabir.domain.model.FalCategory
import ir.falhafez.tabir.domain.model.Poet
import ir.falhafez.tabir.domain.repository.DrawRepository
import ir.falhafez.tabir.domain.repository.PoemRepository
import javax.inject.Inject

/**
 * Draws a random fal: picks a weighted-random poem (excluding recently drawn ones to
 * reduce immediate repeats) and records it in history. [source] = null draws from ALL
 * collections (حافظ + سعدی + مولانا + خیام); otherwise only that poet's divan.
 */
class DrawFalUseCase @Inject constructor(
    private val poemRepository: PoemRepository,
    private val drawRepository: DrawRepository
) {
    suspend operator fun invoke(
        question: String?,
        category: FalCategory,
        source: Poet? = Poet.HAFEZ
    ): DrawEntry? {
        val recentIds = drawRepository.recentPoemIds(limit = 30)
        val poem = poemRepository.getRandomPoem(excludeIds = recentIds, poet = source) ?: return null
        val drawId = drawRepository.record(poem.id, question, category)
        return DrawEntry(
            id = drawId,
            poem = poem,
            question = question,
            category = category,
            drawnAt = System.currentTimeMillis()
        )
    }
}
