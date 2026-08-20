package ir.siliksama.falhafez.domain.usecase

import ir.siliksama.falhafez.domain.model.DrawEntry
import ir.siliksama.falhafez.domain.model.FalCategory
import ir.siliksama.falhafez.domain.model.Poet
import ir.siliksama.falhafez.domain.repository.DrawRepository
import ir.siliksama.falhafez.domain.repository.PoemRepository
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
