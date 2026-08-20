package ir.siliksama.falhafez.domain.model

/** کانال/صفحهٔ اجتماعیِ کاربر که روی فال‌های اشتراکی تبلیغ می‌شود. */
data class ChannelInfo(
    val network: String,   // key از SocialNetwork
    val handle: String,
    val name: String
) {
    val isSet: Boolean get() = handle.isNotBlank()
}
