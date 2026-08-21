package ir.siliksama.falhafez

import ir.siliksama.falhafez.core.util.PersianText
import org.junit.Assert.assertEquals
import org.junit.Test

class PersianTextTest {

    @Test
    fun digitsConversion() {
        assertEquals("۱۲۳۴", PersianText.digits("1234"))
        assertEquals("۰", PersianText.digits("0"))
        assertEquals("۹۸۷۶۵۴۳۲۱۰", PersianText.digits("9876543210"))
    }

    @Test
    fun numberConversion() {
        assertEquals("۴۹۵", PersianText.number(495))
        assertEquals("۱۰۰", PersianText.number(100))
        assertEquals("۲", PersianText.number(2))
    }

    @Test
    fun mixedTextPreserved() {
        assertEquals("فالِ ۵", PersianText.digits("فالِ 5"))
    }
}
