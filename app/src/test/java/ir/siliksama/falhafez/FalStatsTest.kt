package ir.siliksama.falhafez

import ir.siliksama.falhafez.core.util.FalStats
import org.junit.Assert.assertEquals
import org.junit.Test

/** آزمون واحدِ منطقِ زنجیرهٔ روزانهٔ کارنامه (بدون اندروید). */
class FalStatsTest {

    @Test
    fun emptyHistory() {
        assertEquals(0, FalStats.currentStreak(emptyList(), today = 100))
        assertEquals(0, FalStats.bestStreak(emptyList()))
    }

    @Test
    fun streakEndingToday() {
        // امروز + دو روز قبلِ آن → زنجیرهٔ ۳
        assertEquals(3, FalStats.currentStreak(listOf(100, 99, 98), today = 100))
    }

    @Test
    fun streakEndingYesterdayStillCounts() {
        // آخرین فال دیروز بوده → زنجیرهٔ ۲
        assertEquals(2, FalStats.currentStreak(listOf(99, 98), today = 100))
    }

    @Test
    fun gapBreaksStreak() {
        // امروز و پریروز (دیروز جا افتاده) → فقط ۱
        assertEquals(1, FalStats.currentStreak(listOf(100, 98), today = 100))
    }

    @Test
    fun oldDrawsGiveZeroCurrent() {
        assertEquals(0, FalStats.currentStreak(listOf(97, 96), today = 100))
    }

    @Test
    fun bestStreakAcrossHistory() {
        assertEquals(3, FalStats.bestStreak(listOf(1, 2, 3, 5, 6)))
        assertEquals(1, FalStats.bestStreak(listOf(1, 3, 5)))
        assertEquals(4, FalStats.bestStreak(listOf(4, 3, 2, 1)))
    }

    @Test
    fun unsortedInputHandled() {
        assertEquals(3, FalStats.currentStreak(listOf(98, 100, 99), today = 100))
    }
}
