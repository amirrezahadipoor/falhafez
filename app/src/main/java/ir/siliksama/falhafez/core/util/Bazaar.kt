package ir.siliksama.falhafez.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri

/** باز کردن صفحهٔ یک اپ در کافه‌بازار (اول با app intent، سپس وب). */
fun openAppInBazaar(context: Context, packageName: String) {
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("bazaar://details?id=$packageName"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }.getOrElse {
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://cafebazaar.ir/app/?id=$packageName"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
