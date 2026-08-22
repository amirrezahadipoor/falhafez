package ir.siliksama.falhafez.data.payments

/**
 * کلیدهای پرداخت درون‌برنامه‌ای کافه‌بازار.
 *
 * RSA_PUBLIC_KEY را از پنل توسعه‌دهندگان کافه‌بازار بگیرید:
 *   پنل → برنامهٔ شما → «پرداخت درون‌برنامه‌ای» → «کلید عمومی (RSA)»
 * تا وقتی خالی باشد، دکمهٔ خرید صفحهٔ بازار را باز می‌کند (بدون خطا).
 */
object BazaarKeys {
    const val PACKAGE_NAME = "ir.siliksama.falhafez"
    const val RSA_PUBLIC_KEY = "MIHNMA0GCSqGSIb3DQEBAQUAA4G7ADCBtwKBrwClfrm71TFwAJKBSejSzOG00paeF8NlWzH2jkJwzNZ4fKoVB2kExuQKlspndvbGx8CD//ZduyEX0gwhNp8l8U3jBHnPJ8Bs/vI3nlVZeQcS3sj3nqbMB49Pw2g+0tr3NqwHe/Rx2z/Dg1FfcNLojZ/6MVFd6tDei9yeKfdm9iAEJR4vWc0Vq/zTbYtvSsY2ZKqfqD8EVUFNo7oY1HgknhIb8IpEVKHozrFqOMy9Dh8CAwEAAQ=="
}
