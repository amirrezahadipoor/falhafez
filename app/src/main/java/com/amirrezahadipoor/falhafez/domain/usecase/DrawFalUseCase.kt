package com.amirrezahadipoor.falhafez.domain.usecase

import com.amirrezahadipoor.falhafez.domain.model.DrawEntry
import com.amirrezahadipoor.falhafez.domain.model.FalCategory
import com.amirrezahadipoor.falhafez.domain.repository.DrawRepository
import com.amirrezahadipoor.falhafez.domain.repository.PoemRepository
import javax.inject.Inject

/**
 * Draws a random fal: picks a weighted-random poem (excluding recently drawn ones
 * to reduce immediate repeats) and records it in history.
 */
class DrawFalUseCase @Inject constructor(
    private val poemRepository: PoemRepository,
    private val drawRepository: DrawRepository
) {
    suspend operator fun invoke(
        question: String?,
        category: FalCategory
    ): DrawEntry? {
        val recentIds = drawRepository.recentPoemIds(limit = 30)
        val poem = poemRepository.getRandomPoem(excludeIds = recentIds) ?: return null
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
