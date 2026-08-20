package ir.siliksama.falhafez.presentation.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import ir.siliksama.falhafez.R
import ir.siliksama.falhafez.domain.model.ChannelInfo

/**
 * ساخت عکسِ تبلیغاتی کانال کاربر — آیکون شبکه + نام + شناسه + دعوت به فالِ روزانه.
 * 1080×1350، قابل اشتراک در همهٔ شبکه‌ها.
 */
object ChannelPromoRenderer {

    private const val W = 1080
    private const val H = 1350

    fun render(context: Context, info: ChannelInfo): Bitmap {
        val bitmap = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val network = SocialNetwork.byKey(info.network)

        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, H.toFloat(),
                android.graphics.Color.parseColor("#0B1120"),
                android.graphics.Color.parseColor("#101A33"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, W.toFloat(), H.toFloat(), bg)

        val gold = android.graphics.Color.parseColor("#C9A24B")
        val goldSoft = android.graphics.Color.parseColor("#E7C878")
        val cream = android.graphics.Color.parseColor("#F3E9D2")
        val muted = android.graphics.Color.parseColor("#B9A98C")

        // ornamental frame
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = gold; style = Paint.Style.STROKE; strokeWidth = 3f
        }
        canvas.drawRoundRect(40f, 40f, W - 40f, H - 40f, 20f, 20f, stroke)
        stroke.strokeWidth = 1.5f
        canvas.drawRoundRect(58f, 58f, W - 58f, H - 58f, 14f, 14f, stroke)

        val nastaliq = ResourcesCompat.getFont(context, R.font.noto_nastaliq_urdu) ?: Typeface.DEFAULT_BOLD
        val vazir = ResourcesCompat.getFont(context, R.font.vazirmatn_regular) ?: Typeface.DEFAULT
        val vazirBold = ResourcesCompat.getFont(context, R.font.vazirmatn_bold) ?: Typeface.DEFAULT_BOLD

        val textWidth = W - 240
        var y = 150f

        y = drawText(canvas, "فالِ حافظ", 66f, nastaliq, goldSoft, textWidth, 120f, y, Layout.Alignment.ALIGN_CENTER, 1.2f)
        y += 30f
        drawText(canvas, "هر روز، یک فالِ تازه", 30f, vazir, muted, textWidth, 120f, y, Layout.Alignment.ALIGN_CENTER, 1.0f)
        y += 90f

        // channel icon centered
        val icon = runCatching { BitmapFactory.decodeResource(context.resources, network.iconRes) }.getOrNull()
        if (icon != null) {
            val size = 320f
            val cx = (W - size) / 2f
            val dst = Rect(cx.toInt(), y.toInt(), (cx + size).toInt(), (y + size).toInt())
            canvas.drawBitmap(icon, null, dst, Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true })
            y += size + 40f
        }

        val label = if (info.name.isNotBlank()) info.name else network.label
        y = drawText(canvas, label, 52f, vazirBold, cream, textWidth, 120f, y, Layout.Alignment.ALIGN_CENTER, 1.0f)
        y += 20f
        y = drawText(canvas, "@${info.handle.trim().trimStart('@')}", 40f, vazir, goldSoft, textWidth, 120f, y, Layout.Alignment.ALIGN_CENTER, 1.0f)
        y += 60f

        drawText(canvas, "برای فالِ روزانه، ما را دنبال کنید", 32f, vazir, cream, textWidth, 120f, y, Layout.Alignment.ALIGN_CENTER, 1.0f)
        y += 70f

        // footer
        val footerY = H - 150f
        canvas.drawRect(240f, footerY - 30f, W - 240f, footerY - 28f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = gold })
        drawText(canvas, "فال حافظ | تعبیر هوشمند", 28f, vazir, muted, textWidth, 120f, footerY, Layout.Alignment.ALIGN_CENTER, 1.0f)

        return bitmap
    }

    private fun Int.copy(alpha: Float): Int =
        android.graphics.Color.argb(
            (alpha * 255).toInt().coerceIn(0, 255),
            android.graphics.Color.red(this), android.graphics.Color.green(this), android.graphics.Color.blue(this)
        )

    private fun drawText(
        canvas: Canvas, text: String, size: Float, typeface: Typeface, color: Int,
        width: Int, x: Float, startY: Float, align: Layout.Alignment, lineSpacing: Float
    ): Float {
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface; textSize = size; this.color = color
        }
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, width)
            .setAlignment(align)
            .setTextDirection(TextDirectionHeuristics.RTL)
            .setLineSpacing(0f, lineSpacing)
            .setIncludePad(false)
            .build()
        canvas.save(); canvas.translate(x, startY); layout.draw(canvas); canvas.restore()
        return startY + layout.height
    }
}
