package ir.siliksama.falhafez.data.payments

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import ir.cafebazaar.poolakey.Connection
import ir.cafebazaar.poolakey.ConnectionState
import ir.cafebazaar.poolakey.Payment
import ir.cafebazaar.poolakey.config.PaymentConfiguration
import ir.cafebazaar.poolakey.config.SecurityCheck
import ir.cafebazaar.poolakey.entity.PurchaseInfo
import ir.cafebazaar.poolakey.entity.PurchaseState
import ir.cafebazaar.poolakey.request.PurchaseRequest
import ir.siliksama.falhafez.domain.model.SupportTier
import ir.siliksama.falhafez.core.util.openAppInBazaar
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FalHafezPay"

/**
 * پرداخت از طریق Poolakey (SDK رسمی کافه‌بازار).
 *
 * تغییرات مهم نسبت به نسخهٔ قبل:
 *  1. **میان‌بُرِ DEBUG حذف شد.** قبلاً در بیلدِ debug خرید بدونِ پرداخت ثبت می‌شد و
 *     چون هیچ مسیرِ پاک‌کردنی وجود نداشت، دستگاه برای همیشه «مشترک» می‌ماند و
 *     تبلیغاتش خاموش می‌شد — یکی از دو علتِ اصلیِ «تبلیغ نمایش داده نمی‌شود».
 *  2. **[restorePurchases] اضافه شد** (getPurchasedProducts) — بازیابیِ خرید پس از
 *     نصبِ دوباره، و منبعِ حقیقتِ واقعی برای وضعیتِ اشتراک.
 */
@Singleton
class BazaarPaymentGateway @Inject constructor() : PaymentGateway {

    private var payment: Payment? = null
    private var connection: Connection? = null

    private fun paymentOf(context: Context): Payment? {
        val rsa = BazaarKeys.RSA_PUBLIC_KEY
        if (rsa.isBlank()) return null
        return payment ?: runCatching {
            Payment(
                context = context.applicationContext,
                config = PaymentConfiguration(localSecurityCheck = SecurityCheck.Enable(rsa))
            )
        }.getOrNull()?.also { payment = it }
    }

    /** اتصال را برقرار می‌کند (یا از اتصالِ موجود استفاده می‌کند) و سپس [block] را اجرا می‌کند. */
    private fun withConnection(
        context: Context,
        payment: Payment,
        onFailed: () -> Unit,
        block: () -> Unit
    ) {
        val conn = connection
        if (conn != null && runCatching { conn.getState() }.getOrNull() == ConnectionState.Connected) {
            block()
            return
        }
        connection = runCatching {
            payment.connect {
                connectionSucceed { block() }
                connectionFailed {
                    Log.w(TAG, "bazaar connection failed", it)
                    onFailed()
                }
                disconnected { /* no-op */ }
            }
        }.getOrElse {
            Log.w(TAG, "connect() threw", it)
            onFailed()
            null
        }
    }

    override fun purchase(activity: Activity, tier: SupportTier, onPurchased: () -> Unit): Boolean {
        val p = paymentOf(activity)
        if (p == null) {
            Toast.makeText(activity, "پرداخت درون‌برنامه‌ای در دسترس نیست", Toast.LENGTH_LONG).show()
            openAppInBazaar(activity, BazaarKeys.PACKAGE_NAME)
            return false
        }

        val registry = (activity as? ComponentActivity)?.activityResultRegistry
        if (registry == null) {
            Toast.makeText(activity, "فعالیت برای پرداخت نامعتبر است", Toast.LENGTH_SHORT).show()
            return false
        }

        withConnection(
            context = activity,
            payment = p,
            onFailed = {
                Toast.makeText(activity, "اتصال به بازار برقرار نشد", Toast.LENGTH_SHORT).show()
            }
        ) {
            purchaseNow(p, registry, activity, tier, onPurchased)
        }
        return true
    }

    private fun purchaseNow(
        p: Payment,
        registry: androidx.activity.result.ActivityResultRegistry,
        activity: Activity,
        tier: SupportTier,
        onPurchased: () -> Unit
    ) {
        runCatching {
            p.purchaseProduct(
                registry = registry,
                request = PurchaseRequest(
                    productId = tier.sku,
                    payload = "fal_support_${tier.key}",
                    dynamicPriceToken = null
                )
            ) {
                purchaseFlowBegan { Log.d(TAG, "purchase flow began: ${tier.sku}") }
                failedToBeginFlow {
                    Log.w(TAG, "failedToBeginFlow", it)
                    Toast.makeText(activity, "شروع پرداخت ممکن نشد", Toast.LENGTH_SHORT).show()
                }
                purchaseSucceed {
                    Log.d(TAG, "purchase succeed: ${it.productId}")
                    onPurchased()
                }
                purchaseCanceled { Log.d(TAG, "purchase canceled") }
                purchaseFailed {
                    Log.w(TAG, "purchase failed", it)
                    Toast.makeText(activity, "پرداخت ناموفق بود", Toast.LENGTH_SHORT).show()
                }
            }
        }.onFailure {
            Log.w(TAG, "purchaseProduct threw", it)
            Toast.makeText(activity, "خطا در پرداخت", Toast.LENGTH_SHORT).show()
        }
    }

    override fun restorePurchases(context: Context, onResult: (SupportTier?) -> Unit) {
        val p = paymentOf(context)
        if (p == null) {
            Log.d(TAG, "restore skipped: payment unavailable")
            onResult(null)
            return
        }

        withConnection(context = context, payment = p, onFailed = { onResult(null) }) {
            runCatching {
                p.getPurchasedProducts {
                    querySucceed { purchases: List<PurchaseInfo> ->
                        val tier = highestTierOf(purchases)
                        Log.d(TAG, "restore: ${purchases.size} purchase(s) → tier=$tier")
                        onResult(tier)
                    }
                    queryFailed {
                        Log.w(TAG, "restore query failed", it)
                        onResult(null)
                    }
                }
            }.onFailure {
                Log.w(TAG, "getPurchasedProducts threw", it)
                onResult(null)
            }
        }
    }

    /** بالاترین سطحِ خریداری‌شده و تأییدشده. */
    private fun highestTierOf(purchases: List<PurchaseInfo>): SupportTier {
        val owned = purchases
            .filter { it.purchaseState == PurchaseState.PURCHASED }
            .mapNotNull { info ->
                SupportTier.entries.firstOrNull { it.sku.isNotBlank() && it.sku == info.productId }
            }
        // ترتیبِ enum: NONE < BASE < PLUS < GOLD
        return owned.maxByOrNull { it.ordinal } ?: SupportTier.NONE
    }
}
