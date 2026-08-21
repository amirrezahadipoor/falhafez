package ir.siliksama.falhafez

import ir.siliksama.falhafez.core.util.SearchSanitizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * قفلِ رگرسیون برای باگِ «malformed MATCH expression» در جستجو:
 * عملگرهای FTS4 نباید به کوئریِ MATCH برسند.
 */
class SearchSanitizerTest {

    @Test
    fun plainPersianQueryGetsPrefixes() {
        assertEquals("شعر*", SearchSanitizer.sanitize("شعر"))
        assertEquals("حافظ* سعدی*", SearchSanitizer.sanitize("حافظ سعدی"))
    }

    @Test
    fun parenthesesAndOperatorsAreStripped() {
        assertEquals("شعر*", SearchSanitizer.sanitize("(شعر*"))
        assertEquals("شعر*", SearchSanitizer.sanitize("شعر* )"))
        assertEquals("حافظ* سعدی*", SearchSanitizer.sanitize("حافظ - سعدی"))
        assertEquals("شعر*", SearchSanitizer.sanitize("\"شعر\""))
        assertEquals("شعر*", SearchSanitizer.sanitize(":شعر:"))
    }

    @Test
    fun englishOperatorWordsAreDropped() {
        assertNull(SearchSanitizer.sanitize("AND"))
        assertNull(SearchSanitizer.sanitize("OR"))
        assertNull(SearchSanitizer.sanitize("NOT"))
        assertEquals("شعر*", SearchSanitizer.sanitize("شعر AND"))
        assertEquals("شعر*", SearchSanitizer.sanitize("AND شعر"))
    }

    @Test
    fun blankOrSymbolOnlyReturnsNull() {
        assertNull(SearchSanitizer.sanitize(""))
        assertNull(SearchSanitizer.sanitize("   "))
        assertNull(SearchSanitizer.sanitize("( ) - * :"))
        assertNull(SearchSanitizer.sanitize("hello world"))
    }

    @Test
    fun digitsAreSearchable() {
        assertEquals("۲*", SearchSanitizer.sanitize("۲"))
        assertEquals("شعر* ۲*", SearchSanitizer.sanitize("شعر ۲"))
    }

    @Test
    fun zwnjIsPreservedInsideTokens() {
        assertEquals("می\u200Cرود*", SearchSanitizer.sanitize("می\u200Cرود"))
    }
}
