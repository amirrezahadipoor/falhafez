package ir.siliksama.falhafez.domain.usecase

import ir.siliksama.falhafez.domain.model.Poem
import ir.siliksama.falhafez.domain.model.Poet
import ir.siliksama.falhafez.domain.repository.PoemRepository
import java.util.Calendar
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
        val day = startOfToday() / 86_400_000L
        val index = (day % count.toLong()).toInt()
        return poemRepository.getPoemAt(Poet.HAFEZ, index)
    }

    /** Same deterministic pick (Hafez), used by the home-screen widget. */
    suspend fun poemForDay(dayNumber: Long): Poem? {
        val count = poemRepository.countForPoet(Poet.HAFEZ)
        if (count <= 0) return null
        return poemRepository.getPoemAt(Poet.HAFEZ, (dayNumber % count.toLong()).toInt())
    }

    private fun startOfToday(): Long {
        val c = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return c.timeInMillis
    }

    companion object {
        fun todayDayNumber(): Long = System.currentTimeMillis() / 86_400_000L
    }
}
