package ir.siliksama.falhafez.core.util

object PersianText {
    private val FA_DIGITS = mapOf(
        '0' to '۰', '1' to '۱', '2' to '۲', '3' to '۳', '4' to '۴',
        '5' to '۵', '6' to '۶', '7' to '۷', '8' to '۸', '9' to '۹'
    )
    private val EN_DIGITS = FA_DIGITS.entries.associate { (k, v) -> v to k }

    fun digits(input: String): String =
        input.map { FA_DIGITS[it] ?: it }.joinToString("")

    fun number(value: Int): String = digits(value.toString())

    fun number(value: Long): String = digits(value.toString())

    fun fromDigits(input: String): String =
        input.map { EN_DIGITS[it] ?: it }.joinToString("")
}
