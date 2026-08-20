package ir.falhafez.tabir.presentation.share

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The social networks / messengers the fal image can be shared to directly.
 * `packageName` is used for a targeted intent; if the app is missing we fall
 * back to the generic share sheet (see [ShareManager.shareFileToApp]).
 */
enum class SocialNetwork(
    val label: String,
    val packageName: String?,
    val brandColor: Color,
    val icon: ImageVector
) {
    TELEGRAM("تلگرام", "org.telegram.messenger", Color(0xFF229ED9), Icons.Outlined.Send),
    WHATSAPP("واتساپ", "com.whatsapp", Color(0xFF25D366), Icons.Outlined.Chat),
    RUBIKA("روبیکا", "app.rbmain.a", Color(0xFF6D28D9), Icons.Outlined.Sms),
    BALE("بله", "ir.nasim", Color(0xFF00B899), Icons.Outlined.Forum),
    INSTAGRAM("اینستاگرام", "com.instagram.android", Color(0xFFE1306C), Icons.Outlined.CameraAlt),
    EITAA("ایتا", "ir.eitaa.messenger", Color(0xFFF97316), Icons.Outlined.Message),
    SOROUSH("سروش", "ir.soroush.app", Color(0xFF2F80ED), Icons.Outlined.ChatBubble)
}
