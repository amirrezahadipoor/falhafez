package ir.siliksama.falhafez.presentation.privacy

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.siliksama.falhafez.core.designsystem.FalText
import ir.siliksama.falhafez.core.designsystem.readingColor
import ir.siliksama.falhafez.core.theme.FalThemeSpec
import ir.siliksama.falhafez.presentation.components.RitualBackground
import ir.siliksama.falhafez.presentation.components.ScreenHeader
import ir.siliksama.falhafez.presentation.components.ScrollableColumn

/** صفحهٔ کاملِ سیاستِ حفظ حریم خصوصی — داخلِ اپ، بدون نیاز به مرورگر. */
@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    val viewModel: PrivacyViewModel = hiltViewModel()
    val themeId by viewModel.themeId.collectAsStateWithLifecycle()
    val spec = FalThemeSpec.byId(themeId)

    BackHandler { onBack() }

    RitualBackground(spec = spec, showParticles = false) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(title = "حریم خصوصی", onBack = onBack, titleColor = spec.onBackground)

            ScrollableColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(horizontal = 22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                scrollbarColor = spec.accent
            ) {
                Text(
                    "سیاست حفظ حریم خصوصی",
                    style = FalText.heading,
                    color = spec.accentSoft,
                    textAlign = TextAlign.Center
                )
                Text(
                    "فال حافظ | تعبیر هوشمند — آخرین به‌روزرسانی: مرداد ۱۴۰۵",
                    style = FalText.caption,
                    color = spec.onBackgroundMuted,
                    textAlign = TextAlign.Center
                )

                PrivacySection(
                    spec = spec,
                    title = "خلاصه",
                    body = listOf(
                        "اپلیکیشن «فال حافظ | تعبیر هوشمند» کاملاً آفلاین طراحی شده است. ما حساب کاربری نمی‌سازیم؛ نام، ایمیل، شمارهٔ تلفن یا رمز عبور نمی‌گیریم؛ هیچ دادهٔ شخصی را جمع‌آوری، ذخیره یا ارسال نمی‌کنیم و سروری نداریم.",
                        "از هیچ ابزار تحلیل‌گر (Analytics)، ردیاب یا گزارشگر خطای شخص ثالث استفاده نمی‌کنیم. تمامِ محتوا (بیش از ۸٬۰۰۰ شعر، تفسیرها و معنیِ ابیات) داخلِ خودِ اپ بسته‌بندی شده و بدون اینترنت هم کار می‌کند."
                    )
                )

                PrivacySection(
                    spec = spec,
                    title = "داده‌هایی که روی دستگاهِ شما می‌ماند",
                    body = listOf(
                        "• تاریخچهٔ فال‌ها: شعری که آمده + نیّت/سؤالِ شما + زمانِ فال؛",
                        "• علاقه‌مندی‌ها و نشانِ «خوانده‌شده»؛",
                        "• تنظیماتِ شخصی: قالب، اندازه و رنگِ قلم، صدا، لرزش، یادآورِ روزانه؛",
                        "• وضعیتِ اشتراک (حمایت مالی): فقط این‌که کدام سطح را خریده‌اید — بدون هیچ جزئیات پرداخت؛",
                        "• نام/شناسهٔ کانالِ شما — فقط اگر خودتان برای نمایش روی فالِ اشتراکی واردش کنید.",
                        "همهٔ این‌ها فقط در پایگاه‌دادهٔ داخلی و تنظیماتِ محلیِ دستگاه نگهداری می‌شوند و هرگز از دستگاه خارج نمی‌شوند، مگر این‌که خودتان خروجی بگیرید یا فال را به اشتراک بگذارید."
                    )
                )

                PrivacySection(
                    spec = spec,
                    title = "داده‌هایی که هرگز جمع نمی‌کنیم",
                    body = listOf(
                        "هویت (نام، ایمیل، شماره)، مخاطبین، موقعیتِ جغرافیایی، عکس/رسانه، پیام‌ها، اطلاعاتِ دستگاه فراتر از حدِ لازم برای تبلیغات، و هر نوع دادهٔ رفتاریِ قابلِ ردیابی."
                    )
                )

                PrivacySection(
                    spec = spec,
                    title = "ارتباط‌های شبکه — دقیقاً چه زمانی و با چه مقصدی",
                    body = listOf(
                        "اپ به‌صورت پیش‌فرض آفلاین است. اتصالِ شبکه فقط در این موارد برقرار می‌شود:",
                        "• نمایش تبلیغات ← شبکهٔ ایرانیِ تپسل (فقط شناسهٔ تبلیغاتی و وضعیتِ شبکه)؛",
                        "• بررسی نسخهٔ جدید ← api.cafebazaar.ir (فقط نامِ بستهٔ اپ، بدون دادهٔ شخصی)؛",
                        "• پرداختِ حمایت مالی ← اپِ کافه‌بازار (در محیطِ امنِ بازار)؛",
                        "• اشتراک‌گذاری فال/کانال/معرفی ← تلگرام، واتساپ، روبیکا، بله، ایتا، سروش، اینستاگرام — فقط وقتی خودتان بزنید.",
                        "هیچ یک از این‌ها شاملِ نیّت، تاریخچه یا دادهٔ شخصیِ شما نیست."
                    )
                )

                PrivacySection(
                    spec = spec,
                    title = "پشتیبان‌گیری و خروجی",
                    body = listOf(
                        "پشتیبان‌گیریِ خودکارِ اندروید خاموش است؛ یعنی داده‌های شما به فضای ابریِ گوگل منتقل نمی‌شوند و فقط روی دستگاه می‌مانند.",
                        "در تنظیمات، گزینهٔ «خروجی گرفتن» یک فایل JSON از تاریخچه و علاقه‌مندی‌ها می‌سازد و به جایی می‌فرستد که خودتان انتخاب کنید — کاملاً به اختیارِ شما."
                    )
                )

                PrivacySection(
                    spec = spec,
                    title = "تبلیغات (تپسل)",
                    body = listOf(
                        "برای رایگان ماندن اپ، از شبکهٔ ایرانیِ تپسل استفاده می‌کنیم.",
                        "تپسل ممکن است شناسهٔ تبلیغاتی (Advertising ID) و وضعیتِ شبکه را برای نمایش و اندازه‌گیریِ تبلیغات پردازش کند؛ این شناسه هویتِ شما را در بر ندارد و از تنظیمات گوشی قابل بازنشانی است.",
                        "بدون اینترنت، هیچ تبلیغی نمایش داده نمی‌شود و تبلیغات هرگز وسطِ آیینِ فال (گشودن دیوان و رونمایی) نمی‌آید.",
                        "با خریدِ هر سطحِ حمایت مالی، تبلیغات برای همیشه حذف می‌شود."
                    )
                )

                PrivacySection(
                    spec = spec,
                    title = "پرداخت (کافه‌بازار / Poolakey)",
                    body = listOf(
                        "خریدِ حمایت مالی از طریق سامانهٔ پرداختِ کافه‌بازار انجام می‌شود؛ شمارهٔ کارت و اطلاعات بانکی در محیطِ امنِ بازار وارد می‌شود و ما هیچ‌کدام را نمی‌بینیم. پس از خرید فقط «سطحِ اشتراک» به‌صورت محلی روی دستگاه ثبت می‌شود."
                    )
                )

                PrivacySection(
                    spec = spec,
                    title = "مجوزهای اپلیکیشن",
                    body = listOf(
                        "• اینترنت (INTERNET): فقط برای تبلیغات، بررسی نسخهٔ جدید و پرداخت؛",
                        "• شناسهٔ تبلیغاتی (AD_ID): نمایش/اندازه‌گیری تبلیغات توسط تپسل — بدون هویت؛",
                        "• وضعیتِ شبکه (ACCESS_NETWORK_STATE): تشخیص آفلاین بودن تا تبلیغی نمایش داده نشود؛",
                        "• اعلان‌ها (POST_NOTIFICATIONS): فقط اگر یادآورِ روزانهٔ فال را خودتان فعال کنید.",
                        "اعلانِ «یادآوری فال روزانه» محلی است (بدون سرور) و هر روز ساعت ۸ صبح، بدون اینترنت هم می‌آید. ویجتِ «بیتِ امروز» هم فقط پایگاه‌دادهٔ خودِ دستگاه را می‌خواند."
                    )
                )

                PrivacySection(
                    spec = spec,
                    title = "ماندگاریِ داده و حقوقِ شما",
                    body = listOf(
                        "داده‌ها تا زمانی که اپ نصب باشد یا «پاک‌کردن داده» را بزنید روی دستگاه می‌مانند؛ ما هیچ نسخه‌ای نداریم.",
                        "برای حذفِ کامل، «پاک‌کردن دادهٔ اپ» یا حذفِ اپ از گوشی کافی است. در هر لحظه می‌توانید از تاریخچه و علاقه‌مندی‌هایتان خروجی بگیرید."
                    )
                )

                PrivacySection(
                    spec = spec,
                    title = "کودکان",
                    body = listOf(
                        "این اپ محتوای عمومیِ ادبی دارد و دادهٔ شخصی جمع نمی‌کند؛ اما برای استفادهٔ کودکان، همراهیِ والدین پیشنهاد می‌شود."
                    )
                )

                PrivacySection(
                    spec = spec,
                    title = "تغییراتِ این سیاست",
                    body = listOf(
                        "در صورت تغییر، نسخهٔ جدید همین‌جا به‌روزرسانی و تاریخِ آن ثبت می‌شود."
                    )
                )

                PrivacySection(
                    spec = spec,
                    title = "تماس",
                    body = listOf(
                        "ایمیل: siliksama@gmail.com",
                        "تلگرام / واتساپ: ۰۹۲۱۲۳۱۱۲۵۱ (هر دو روی همین شماره)"
                    )
                )

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun PrivacySection(spec: FalThemeSpec, title: String, body: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = FalText.heading, color = spec.accentSoft)
        body.forEach { paragraph ->
            Text(
                paragraph,
                style = FalText.bodyMuted,
                color = readingColor(spec.onBackground),
                textAlign = TextAlign.Justify
            )
        }
    }
}
