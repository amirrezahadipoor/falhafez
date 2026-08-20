package ir.siliksama.falhafez.data.payments

import android.app.Activity
import ir.siliksama.falhafez.domain.model.SupportTier

/**
 * پرداختِ حمایت مالی. پیاده‌سازی واقعی از طریق پرداخت درون‌برنامه‌ای کافه‌بازار است
 * (به docs/SUPPORT_PAYMENTS.md مراجعه کنید). در نسخهٔ debug برای تست، خرید فوراً ثبت می‌شود.
 */
interface PaymentGateway {
    /** Returns true when the payment went through and [onPurchased] has been invoked. */
    fun purchase(activity: Activity, tier: SupportTier, onPurchased: () -> Unit): Boolean
}
