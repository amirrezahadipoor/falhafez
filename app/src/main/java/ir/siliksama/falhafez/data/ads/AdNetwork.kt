package ir.siliksama.falhafez.data.ads

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/** بررسیِ اتصال به اینترنت — مدرن (NetworkCapabilities) و بدون API منسوخ. */
fun isOnline(context: Context): Boolean {
    val cm = context.getSystemService(ConnectivityManager::class.java) ?: return false
    val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
