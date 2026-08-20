package ir.siliksama.falhafez.data.payments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import ir.siliksama.falhafez.BuildConfig
import ir.siliksama.falhafez.domain.model.SupportTier
import javax.inject.Inject

/**
 * پرداخت حمایت مالی.
 * - DEBUG: برای تست، خرید فوراً تأیید می‌شود.
 * - RELEASE: تا وقتی SDK پرداخت کافه‌بازار وصل شود، صفحهٔ بازار اپ باز می‌شود
 *   و راهنمای فعال‌سازی در docs/SUPPORT_PAYMENTS.md آمده است.
 */
class BazaarPaymentGateway @Inject constructor() : PaymentGateway {

    override fun purchase(activity: Activity, tier: SupportTier, onPurchased: () -> Unit): Boolean {
        if (BuildConfig.DEBUG) {
            onPurchased()
            Toast.makeText(activity, "حمایتِ آزمایشی ثبت شد (debug)", Toast.LENGTH_SHORT).show()
            return true
        }

        // TODO(انتشار): اتصال پرداخت درون‌برنامه‌ای کافه‌بازار — docs/SUPPORT_PAYMENTS.md
        runCatching {
            activity.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=ir.siliksama.falhafez"))
            )
        }.getOrElse {
            runCatching {
                activity.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://cafebazaar.ir/app/ir.siliksama.falhafez"))
                )
            }
        }
        Toast.makeText(
            activity,
            "پرداخت درون‌برنامه‌ای به‌زودی فعال می‌شود — از طریق پشتیبانی تماس بگیرید",
            Toast.LENGTH_LONG
        ).show()
        return false
    }
}
