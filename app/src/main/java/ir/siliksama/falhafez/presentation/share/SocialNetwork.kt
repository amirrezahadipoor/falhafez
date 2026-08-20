package ir.siliksama.falhafez.presentation.share

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import ir.siliksama.falhafez.R

/**
 * شبکه‌های اجتماعی/پیام‌رسان‌هایی که می‌توان فال را به آن‌ها فرستاد یا کانالِ
 * کاربر را در آن‌ها معرفی کرد. آیکون‌ها اختصاصی تولید شده‌اند.
 */
enum class SocialNetwork(
    val label: String,
    val key: String,
    val packageName: String?,
    val brandColor: Color,
    @DrawableRes val iconRes: Int
) {
    TELEGRAM("تلگرام", "telegram", "org.telegram.messenger", Color(0xFF229ED9), R.drawable.ic_social_telegram),
    WHATSAPP("واتساپ", "whatsapp", "com.whatsapp", Color(0xFF25D366), R.drawable.ic_social_whatsapp),
    RUBIKA("روبیکا", "rubika", "app.rbmain.a", Color(0xFF7C3AED), R.drawable.ic_social_rubika),
    BALE("بله", "bale", "ir.nasim", Color(0xFF00B899), R.drawable.ic_social_bale),
    INSTAGRAM("اینستاگرام", "instagram", "com.instagram.android", Color(0xFFE1306C), R.drawable.ic_social_instagram),
    EITAA("ایتا", "eitaa", "ir.eitaa.messenger", Color(0xFFF97316), R.drawable.ic_social_eitaa),
    SOROUSH("سروش", "soroush", "ir.soroush.app", Color(0xFF2F80ED), R.drawable.ic_social_soroush);

    companion object {
        fun byKey(key: String): SocialNetwork = entries.firstOrNull { it.key == key } ?: TELEGRAM
    }

    /** لینک صفحه/کانال برای handle واردشدهٔ کاربر. */
    fun channelUrl(handle: String): String? {
        val h = handle.trim().trimStart('@')
        if (h.isBlank()) return null
        return when (this) {
            TELEGRAM -> "https://t.me/$h"
            RUBIKA -> "https://rubika.ir/$h"
            BALE -> "https://ble.ir/$h"
            INSTAGRAM -> "https://instagram.com/$h"
            EITAA -> "https://eitaa.com/$h"
            SOROUSH -> "https://splus.ir/$h"
            WHATSAPP -> "https://wa.me/$h"
        }
    }
}
