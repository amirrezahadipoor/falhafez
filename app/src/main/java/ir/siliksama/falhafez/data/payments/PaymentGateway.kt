package ir.siliksama.falhafez.data.payments

import android.app.Activity
import android.content.Context
import ir.siliksama.falhafez.domain.model.SupportTier

/**
 * پرداختِ حمایت مالی از طریق پرداخت درون‌برنامه‌ای کافه‌بازار.
 * (به docs/SUPPORT_PAYMENTS.md مراجعه کنید.)
 */
interface PaymentGateway {

    /** true یعنی جریانِ پرداخت آغاز شد و در صورت موفقیت [onPurchased] صدا زده می‌شود. */
    fun purchase(activity: Activity, tier: SupportTier, onPurchased: () -> Unit): Boolean

    /**
     * بازیابیِ خریدهای پیشین از بازار.
     *
     * برای کاربری که اپ را حذف/نصبِ دوباره کرده یا گوشی عوض کرده **الزامی** است؛
     * بدونِ آن حمایتِ خریداری‌شده از بین می‌رفت. همچنین تنها راهِ درستِ تشخیص اینکه
     * کاربر واقعاً مشترک است (در برابر مقدارِ کهنهٔ ذخیره‌شده روی دستگاه).
     *
     * [onResult] بالاترین سطحِ خریداری‌شده را برمی‌گرداند؛ اگر خریدی نبود [SupportTier.NONE]
     * و اگر بررسی ممکن نشد (بازار نصب نیست/آفلاین) `null` — که یعنی «نمی‌دانیم، مقدارِ
     * فعلی را دست نزن».
     */
    fun restorePurchases(context: Context, onResult: (SupportTier?) -> Unit)
}
