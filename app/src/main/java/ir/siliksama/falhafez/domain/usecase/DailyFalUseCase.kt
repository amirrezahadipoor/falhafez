package ir.siliksama.falhafez.domain.usecase

import ir.siliksama.falhafez.core.util.DayNumber
import ir.siliksama.falhafez.domain.model.Poem
import ir.siliksama.falhafez.domain.model.Poet
import ir.siliksama.falhafez.domain.repository.PoemRepository
import javax.inject.Inject

/**
 * فالِ روز — a deterministic fal from the **Divan of Hafez only**: every user draws
 * the same ghazal on the same day (seed = day number), so it becomes a shared,
 * return-driving ritual ("فال امروزت چی اومد؟"). Fully offline and repeatable.
 */
class DailyFalUseCase @Inject constructor(
    private val poemRepository: PoemRepository
) {
    suspend fun today(): Poem? {
        val count = poemRepository.countForPoet(Poet.HAFEZ)
        if (count <= 0) return null
        val day = DayNumber.local()
        val index = (day % count.toLong()).toInt()
        return poemRepository.getPoemAt(Poet.HAFEZ, index)
    }

    /** Same deterministic pick (Hafez), used by the home-screen widget. */
    suspend fun poemForDay(dayNumber: Long): Poem? {
        val count = poemRepository.countForPoet(Poet.HAFEZ)
        if (count <= 0) return null
        return poemRepository.getPoemAt(Poet.HAFEZ, (dayNumber % count.toLong()).toInt())
    }

    companion object {
        /** شمارهٔ روزِ محلی — مبنای مشترکِ فالِ روز در اپ و ویجت. */
        fun todayDayNumber(): Long = DayNumber.local()
    }
}
