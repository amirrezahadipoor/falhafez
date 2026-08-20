package com.amirrezahadipoor.falhafez.domain.usecase

import com.amirrezahadipoor.falhafez.domain.model.Poem
import com.amirrezahadipoor.falhafez.domain.repository.PoemRepository
import java.util.Calendar
import javax.inject.Inject

/**
 * فالِ روز — a deterministic fal: every user draws the same poem on the same
 * day (seed = day number since epoch), so it becomes a shared, return-driving
 * ritual ("فال امروزت چی اومد؟"). Fully offline and repeatable within a day.
 */
class DailyFalUseCase @Inject constructor(
    private val poemRepository: PoemRepository
) {
    suspend fun today(): Poem? {
        val count = poemRepository.count()
        if (count <= 0) return null
        val day = startOfToday() / 86_400_000L
        val index = (day % count.toLong()).toInt()
        return poemRepository.getPoemAt(index)
    }

    /** Same deterministic pick, used by the home-screen widget. */
    suspend fun poemForDay(dayNumber: Long): Poem? {
        val count = poemRepository.count()
        if (count <= 0) return null
        return poemRepository.getPoemAt((dayNumber % count.toLong()).toInt())
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
