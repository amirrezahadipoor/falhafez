package ir.siliksama.falhafez.data.payments

import android.app.Activity
import android.widget.Toast
import androidx.activity.ComponentActivity
import ir.cafebazaar.poolakey.Connection
import ir.cafebazaar.poolakey.ConnectionState
import ir.cafebazaar.poolakey.Payment
import ir.cafebazaar.poolakey.config.PaymentConfiguration
import ir.cafebazaar.poolakey.config.SecurityCheck
import ir.cafebazaar.poolakey.request.PurchaseRequest
import ir.siliksama.falhafez.BuildConfig
import ir.siliksama.falhafez.core.util.openAppInBazaar
import ir.siliksama.falhafez.domain.model.SupportTier
import javax.inject.Inject
import javax.inject.Singleton

/**
 * پرداخت حمایت مالی از طریق Poolakey (SDK رسمی پرداخت درون‌برنامه‌ای کافه‌بازار).
 * - DEBUG: خرید برای تست فوراً ثبت می‌شود.
 * - RELEASE: اگر کلید RSA در [BazaarKeys] باشد، جریان خرید Poolakey اجرا می‌شود؛
 *   وگرنه صفحهٔ بازار اپ باز می‌شود (بدون خطا).
 */
@Singleton
class BazaarPaymentGateway @Inject constructor() : PaymentGateway {

    private var payment: Payment? = null
    private var connection: Connection? = null

    override fun purchase(activity: Activity, tier: SupportTier, onPurchased: () -> Unit): Boolean {
        if (BuildConfig.DEBUG) {
            onPurchased()
            Toast.makeText(activity, "حمایتِ آزمایشی ثبت شد (debug)", Toast.LENGTH_SHORT).show()
            return true
        }

        val rsa = BazaarKeys.RSA_PUBLIC_KEY
        if (rsa.isBlank()) {
            Toast.makeText(activity, "کلید عمومی بازار تنظیم نشده است", Toast.LENGTH_LONG).show()
            openAppInBazaar(activity, BazaarKeys.PACKAGE_NAME)
            return false
        }

        val registry = (activity as? ComponentActivity)?.activityResultRegistry
        if (registry == null) {
            Toast.makeText(activity, "فعالیت برای پرداخت نامعتبر است", Toast.LENGTH_SHORT).show()
            return false
        }

        return runCatching {
            val p = payment ?: Payment(
                context = activity,
                config = PaymentConfiguration(localSecurityCheck = SecurityCheck.Enable(rsa))
            ).also { payment = it }

            val conn = connection
            if (conn != null && conn.getState() == ConnectionState.Connected) {
                purchaseNow(p, registry, activity, tier, onPurchased)
            } else {
                connection = p.connect {
                    connectionSucceed { purchaseNow(p, registry, activity, tier, onPurchased) }
                    connectionFailed {
                        Toast.makeText(activity, "اتصال به بازار برقرار نشد", Toast.LENGTH_SHORT).show()
                    }
                    disconnected { /* no-op */ }
                }
            }
            true
        }.getOrElse {
            Toast.makeText(activity, "بازار نصب نیست یا خطا رخ داد", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun purchaseNow(
        p: Payment,
        registry: androidx.activity.result.ActivityResultRegistry,
        activity: Activity,
        tier: SupportTier,
        onPurchased: () -> Unit
    ) {
        p.purchaseProduct(
            registry = registry,
            request = PurchaseRequest(
                productId = tier.sku,
                payload = "fal_support_${tier.key}",
                dynamicPriceToken = null
            )
        ) {
            purchaseFlowBegan { /* no-op */ }
            failedToBeginFlow {
                Toast.makeText(activity, "شروع پرداخت ممکن نشد", Toast.LENGTH_SHORT).show()
            }
            purchaseSucceed { onPurchased() }
            purchaseCanceled { /* no-op */ }
            purchaseFailed {
                Toast.makeText(activity, "پرداخت ناموفق بود", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
