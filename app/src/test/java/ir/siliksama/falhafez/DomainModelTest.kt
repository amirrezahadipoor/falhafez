package ir.siliksama.falhafez

import ir.siliksama.falhafez.domain.model.Collection
import ir.siliksama.falhafez.domain.model.PersianOrdinal
import ir.siliksama.falhafez.domain.model.Poet
import ir.siliksama.falhafez.domain.model.Verse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** آزمون واحدِ مدل‌های خالصِ دامنه — قفلِ رگرسیون برای رفتارهای ناخواسته. */
class DomainModelTest {

    @Test
    fun poetFallback() {
        assertEquals(Poet.HAFEZ, Poet.fromKey("hafez"))
        assertEquals(Poet.RUMI, Poet.fromKey("rumi"))
        assertEquals(Poet.HAFEZ, Poet.fromKey("unknown-poet"))
    }

    @Test
    fun collectionFallback() {
        assertEquals(Collection.HAFEZ_GHAZAL, Collection.fromKey("ghazal"))
        assertNull(Collection.fromKey("nope"))
    }

    @Test
    fun verseCoupletDetection() {
        assertTrue(Verse(0, "مصرع یک", "مصرع دو", null).isCouplet)
        assertFalse(Verse(0, "نثر تنها", null, null).isCouplet)
        assertFalse(Verse(0, "نثر تنها", "", null).isCouplet)
        assertEquals("مصرع یک؛ مصرع دو", Verse(0, "مصرع یک", "مصرع دو", null).fullText)
        assertEquals("نثر تنها", Verse(0, "نثر تنها", null, null).fullText)
    }

    @Test
    fun ordinalWordForFirstTwenty() {
        assertEquals("اول", PersianOrdinal.number(1))
        assertEquals("بیستم", PersianOrdinal.number(20))
    }

    @Test
    fun ordinalPersianDigitsBeyondTwenty() {
        // پیش از این، ۲۱ به‌صورت «21» (لاتین) برمی‌گشت — باگِ ظاهریِ رفع‌شده.
        assertEquals("۲۱", PersianOrdinal.number(21))
        assertEquals("۴۹۵", PersianOrdinal.number(495))
    }
}
