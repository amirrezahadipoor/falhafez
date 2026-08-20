package ir.siliksama.falhafez.data.updates

import ir.siliksama.falhafez.BuildConfig
import ir.siliksama.falhafez.domain.model.UpdateCheckResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * بررسی بروزرسانی از کافه‌بازار — کاملاً سبک (بدون SDK اضافه)، از REST عمومی بازار.
 * آفلاین یا خطا → به‌صورت امن [UpdateCheckResult.Failed]/[UpdateCheckResult.UpToDate] برمی‌گردد؛
 * هرگز کراش نمی‌کند و اپ بدون اینترنت هم بدون مشکل کار می‌کند.
 */
object UpdateChecker {

    private const val ENDPOINT = "https://api.cafebazaar.ir/rest-v1/process/AppDownloadInfoRequest"

    suspend fun check(): UpdateCheckResult = withContext(Dispatchers.IO) {
        runCatching {
            val conn = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10_000
                readTimeout = 10_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }
            val body = """{"properties":{"language":2,"clientVersionCode":1100301,"androidClientInfo":{"cpu":"x86,armeabi-v7a,armeabi","sdkVersion":23},"clientVersion":"11.3.1","isKidsEnabled":false},"singleRequest":{"appDownloadInfoRequest":{"downloadStatus":1,"packageName":"ir.siliksama.falhafez","referrers":[]}}}"""
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val text = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            conn.disconnect()

            val reply = JSONObject(text)
                .getJSONObject("singleReply")
                .getJSONObject("appDownloadInfoReply")
            val versionCode = reply.optInt("versionCode", BuildConfig.VERSION_CODE)
            val versionName = reply.optString("versionName", "")

            if (versionCode > BuildConfig.VERSION_CODE) {
                UpdateCheckResult.Available(versionCode, versionName)
            } else {
                UpdateCheckResult.UpToDate
            }
        }.getOrDefault(UpdateCheckResult.Failed)
    }
}
