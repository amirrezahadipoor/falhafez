package ir.siliksama.falhafez.domain.model

/** نتیجهٔ بررسی بروزرسانی از کافه‌بازار. */
sealed class UpdateCheckResult {
    data class Available(val versionCode: Int, val versionName: String) : UpdateCheckResult()
    object UpToDate : UpdateCheckResult()
    object Failed : UpdateCheckResult()
}
