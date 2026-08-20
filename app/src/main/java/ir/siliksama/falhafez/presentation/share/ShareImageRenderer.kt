package ir.siliksama.falhafez.presentation.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.res.ResourcesCompat
import ir.siliksama.falhafez.R
import ir.siliksama.falhafez.core.theme.FalThemeSpec
import ir.siliksama.falhafez.domain.model.ChannelInfo
import ir.siliksama.falhafez.domain.model.FalCategory
import ir.siliksama.falhafez.domain.model.Poem
import ir.siliksama.falhafez.presentation.home.CategoryAngles

/**
 * Composes the shareable fal image entirely with android.graphics (Canvas) —
 * ornamental gold frame, Nastaliq verse, Vazirmatn interpretation. 1080×1350 (4:5).
 */
object ShareImageRenderer {

    private const val W = 1080
    private const val H = 1350
    private const val PAD = 120f

    fun render(
        context: Context,
        poem: Poem,
        category: FalCategory,
        spec: FalThemeSpec,
        channel: ChannelInfo? = null,
        supporterBadge: Boolean = false
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, H.toFloat(),
                spec.backgroundTop.toArgb(), spec.backgroundBottom.toArgb(),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, W.toFloat(), H.toFloat(), bg)

        val nastaliq = ResourcesCompat.getFont(context, R.font.noto_nastaliq_urdu) ?: Typeface.DEFAULT_BOLD
        val vazir = ResourcesCompat.getFont(context, R.font.vazirmatn_regular) ?: Typeface.DEFAULT
        val vazirBold = ResourcesCompat.getFont(context, R.font.vazirmatn_bold) ?: Typeface.DEFAULT_BOLD

        drawOrnamentFrame(canvas, spec.accent.toArgb())

        val textWidth = (W - 2 * PAD).toInt()
        var y = 150f

        y = drawText(
            canvas, "فال حافظ", 64f, nastaliq, spec.accentSoft.toArgb(),
            textWidth, PAD, y, Layout.Alignment.ALIGN_CENTER, 1.2f
        )
        y += 6f
        drawCenterOrnament(canvas, y, spec.accent.toArgb())
        y += 44f

        val meta = "${poem.collection.poet.faName} — ${poem.collection.faName}"
        y = drawText(
            canvas, meta, 30f, vazir, spec.onBackgroundMuted.toArgb(),
            textWidth, PAD, y, Layout.Alignment.ALIGN_CENTER, 1.0f
        )
        y += 40f

        // --- متن کاملِ تعبیر (+ زاویهٔ موضوعی) را اندازه بگیریم ---
        val tafsirText = poem.tafsir + if (category != FalCategory.NONE) {
            val angle = CategoryAngles.text(category)
            if (angle != null) "\n\n$angle" else ""
        } else ""

        val footerReserve = if (channel != null && channel.isSet) 205f else 135f
        val supporterReserve = if (supporterBadge) 72f else 0f

        var tafsirSize = 30f
        var tafsirH = measureText(tafsirText, tafsirSize, vazir, textWidth, 1.5f)
        val maxTafsirH = H * 0.42f
        if (tafsirH > maxTafsirH) {
            tafsirSize = 25f
            tafsirH = measureText(tafsirText, tafsirSize, vazir, textWidth, 1.5f)
        }

        // --- چند بیت از شعر، به‌اندازه‌ای که در فضای باقی‌مانده جا شود ---
        val dividerSpace = 56f
        val tafsirLabelH = 58f
        val headerBottom = y + 34f
        val availableForBeits =
            H - headerBottom - footerReserve - supporterReserve - dividerSpace - tafsirLabelH - tafsirH - 80f

        val verseSize = 42f
        val beitHeight = 2f * verseSize * 1.5f + 16f
        val maxBeits = (availableForBeits / beitHeight).toInt().coerceIn(2, 5)

        val chosen = poem.verses.take(maxBeits)
        val lines = mutableListOf<String>()
        chosen.forEach { b ->
            lines += b.first
            b.second?.let { lines += it }
        }
        if (poem.verses.size > chosen.size) lines += "…"
        val verseText = lines.joinToString("\n")
        val verseH = measureText(verseText, verseSize, nastaliq, textWidth, 1.5f)

        y = headerBottom + ((availableForBeits - verseH) / 2f).coerceAtLeast(0f)
        y = drawText(
            canvas, verseText, verseSize, nastaliq, spec.onBackground.toArgb(),
            textWidth, PAD, y, Layout.Alignment.ALIGN_NORMAL, 1.5f
        )
        y += 30f

        drawDividerOrnament(canvas, y, spec.accent.toArgb())
        y += 40f

        y = drawText(
            canvas, "تعبیر", 40f, vazirBold, spec.accent.toArgb(),
            textWidth, PAD, y, Layout.Alignment.ALIGN_NORMAL, 1.0f
        )
        y += 14f

        drawText(
            canvas, tafsirText, tafsirSize, vazir, spec.onBackground.toArgb(),
            textWidth, PAD, y, Layout.Alignment.ALIGN_NORMAL, 1.5f
        )

        val footerY = H - 190f
        drawDividerOrnament(canvas, footerY - 30f, spec.accent.copy(alpha = 0.6f).toArgb())

        if (channel != null && channel.isSet) {
            val network = SocialNetwork.byKey(channel.network)
            drawChannelFooter(canvas, context, channel, network, footerY, spec)
        } else {
            drawText(
                canvas, "فال حافظ | تعبیر هوشمند", 30f, vazir, spec.onBackgroundMuted.toArgb(),
                textWidth, PAD, footerY, Layout.Alignment.ALIGN_CENTER, 1.0f
            )
        }

        if (supporterBadge) {
            drawText(
                canvas, "پشتیبانِ همیشگی ♥", 26f, vazirBold, spec.accent.toArgb(),
                textWidth, PAD, H - 96f, Layout.Alignment.ALIGN_CENTER, 1.0f
            )
        }

        return bitmap
    }

    /** کانالِ کاربر — آیکون + نام + شناسه، تا فالِ اشتراکی تبلیغِ کانال باشد. */
    private fun drawChannelFooter(
        canvas: Canvas,
        context: Context,
        channel: ChannelInfo,
        network: SocialNetwork,
        y: Float,
        spec: FalThemeSpec
    ) {
        val icon = runCatching {
            BitmapFactory.decodeResource(context.resources, network.iconRes)
        }.getOrNull() ?: return

        val size = 84f
        val cx = W / 2f - size / 2f - 130f
        val dst = Rect(cx.toInt(), y.toInt(), (cx + size).toInt(), (y + size).toInt())
        canvas.drawBitmap(icon, null, dst, Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true })

        val vazir = ResourcesCompat.getFont(context, R.font.vazirmatn_regular) ?: Typeface.DEFAULT
        val vazirBold = ResourcesCompat.getFont(context, R.font.vazirmatn_bold) ?: Typeface.DEFAULT_BOLD
        val textWidth = (W - 2 * PAD).toInt()
        val label = if (channel.name.isNotBlank()) channel.name else network.label
        drawText(
            canvas, label, 32f, vazirBold, spec.onBackground.toArgb(),
            (W / 2 - 20).toInt(), W / 2f + 20f, y, Layout.Alignment.ALIGN_NORMAL, 1.0f
        )
        drawText(
            canvas, "@${channel.handle.trim().trimStart('@')}", 26f, vazir,
            spec.onBackgroundMuted.toArgb(),
            (W / 2 - 20).toInt(), W / 2f + 20f, y + 52f, Layout.Alignment.ALIGN_NORMAL, 1.0f
        )
        drawText(
            canvas, "برای فالِ روزانه، ما را دنبال کنید", 24f, vazir, spec.onBackgroundMuted.toArgb(),
            textWidth, PAD, y + 96f, Layout.Alignment.ALIGN_CENTER, 1.0f
        )
    }

    /** ارتفاع متن رندر‌شده را بدون کشیدن اندازه می‌گیرد. */
    private fun measureText(
        text: String, size: Float, typeface: Typeface, width: Int, lineSpacing: Float
    ): Float {
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            textSize = size
        }
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setTextDirection(TextDirectionHeuristics.RTL)
            .setLineSpacing(0f, lineSpacing)
            .setIncludePad(false)
            .build()
        return layout.height.toFloat()
    }

    private fun drawText(
        canvas: Canvas, text: String, size: Float, typeface: Typeface, color: Int,
        width: Int, x: Float, startY: Float, align: Layout.Alignment, lineSpacing: Float
    ): Float {
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            textSize = size
            this.color = color
        }
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, width)
            .setAlignment(align)
            .setTextDirection(TextDirectionHeuristics.RTL)
            .setLineSpacing(0f, lineSpacing)
            .setIncludePad(false)
            .build()
        canvas.save()
        canvas.translate(x, startY)
        layout.draw(canvas)
        canvas.restore()
        return startY + layout.height
    }

    private fun drawOrnamentFrame(canvas: Canvas, gold: Int) {
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = gold
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        val in1 = 40f
        val in2 = 58f
        canvas.drawRoundRect(in1, in1, W - in1, H - in1, 20f, 20f, stroke)
        stroke.strokeWidth = 1.5f
        canvas.drawRoundRect(in2, in2, W - in2, H - in2, 14f, 14f, stroke)

        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = gold; style = Paint.Style.FILL }
        val r = 30f
        val corners = listOf(
            in1 to in1, (W - in1) to in1, in1 to (H - in1), (W - in1) to (H - in1)
        )
        for ((cx, cy) in corners) {
            val path = Path().apply {
                moveTo(cx, cy - r); lineTo(cx + r, cy); lineTo(cx, cy + r); lineTo(cx - r, cy); close()
            }
            canvas.drawPath(path, fill)
            canvas.drawCircle(cx, cy, 7f, fill)
        }
    }

    private fun drawCenterOrnament(canvas: Canvas, y: Float, gold: Int) {
        val cx = W / 2f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = gold; style = Paint.Style.FILL }
        val line = Paint(paint).apply { strokeWidth = 1.5f; style = Paint.Style.STROKE }
        canvas.drawLine(PAD * 1.4f, y, cx - 34f, y, line)
        canvas.drawLine(cx + 34f, y, W - PAD * 1.4f, y, line)
        val r = 16f
        val path = Path().apply {
            moveTo(cx, y - r); lineTo(cx + r, y); lineTo(cx, y + r); lineTo(cx - r, y); close()
        }
        canvas.drawPath(path, paint)
    }

    private fun drawDividerOrnament(canvas: Canvas, y: Float, gold: Int) {
        drawCenterOrnament(canvas, y, gold)
    }
}
