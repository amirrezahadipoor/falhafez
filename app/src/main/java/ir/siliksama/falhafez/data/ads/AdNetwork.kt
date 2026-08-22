package ir.siliksama.falhafez.data.ads

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * بررسیِ اتصالِ واقعی به اینترنت.
 *
 * ⚠️ نکتهٔ ظریف: `NET_CAPABILITY_INTERNET` فقط یعنی «این شبکه *ادعا* می‌کند
 * می‌تواند به اینترنت وصل شود» — نه اینکه واقعاً وصل است. وای‌فایِ هتل پیش از
 * لاگین، سیم‌کارتِ بدونِ بسته، یا اینترنتِ قطع‌شده همگی این پرچم را دارند.
 *
 * `NET_CAPABILITY_VALIDATED` همان چیزی است که سیستم پس از یک بررسیِ واقعی ست
 * می‌کند. بدونِ آن، اپ فکر می‌کرد آنلاین است، درخواستِ تبلیغ می‌فرستاد، شکست
 * می‌خورد، و کاربر نه تبلیغ می‌دید و نه از حالتِ «آفلاین = نامحدود» بهره می‌برد.
 *
 * برای اقتصادِ اپ این تفاوت مهم است: «آفلاین» یعنی فالِ نامحدود بدونِ تبلیغ، پس
 * تشخیصِ اشتباه مستقیماً به کاربر ضرر می‌زد.
 */
fun isOnline(context: Context): Boolean {
    val cm = context.getSystemService(ConnectivityManager::class.java) ?: return false
    val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
