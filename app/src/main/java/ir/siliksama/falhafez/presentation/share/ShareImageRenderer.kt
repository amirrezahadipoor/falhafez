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
        var y = 130f

        // ── سربرگ ──
        y = drawText(
            canvas, "فال حافظ", 60f, nastaliq, spec.accentSoft.toArgb(),
            textWidth, PAD, y, Layout.Alignment.ALIGN_CENTER, 1.2f
        )
        y += 4f
        drawCenterOrnament(canvas, y, spec.accent.toArgb())
        y += 36f
        val meta = "${poem.collection.poet.faName} — ${poem.collection.faName}"
        y = drawText(
            canvas, meta, 28f, vazir, spec.onBackgroundMuted.toArgb(),
            textWidth, PAD, y, Layout.Alignment.ALIGN_CENTER, 1.0f
        )
        val contentTop = y + 26f

        // ── تعبیر کامل (+ زاویهٔ موضوعی) ──
        val tafsirText = poem.tafsir + if (category != FalCategory.NONE) {
            val angle = CategoryAngles.text(category)
            if (angle != null) "\n\n$angle" else ""
        } else ""

        // ── پاورقی: کارتِ حامی یا برند ──
        val ch = channel
        val withChannel = ch != null && ch.isSet
        val footerBlock = if (withChannel) 360f else 130f
        val footerTop = H - footerBlock

        // ── بودجهٔ فضای میانی ──
        val middle = (footerTop - contentTop).coerceAtLeast(200f)
        val gapTop = 16f
        val gapAfterVerse = 22f
        val dividerH = 46f
        val labelH = 46f
        val gapAfterLabel = 12f
        val gapBottom = 20f

        // بهترین ترکیب: بزرگ‌ترین فونتِ تعبیر که جا شود (و بعد، بیت‌های بیشتر)
        var selMaxBeits = 2
        var selVerseSize = 32f
        var selTafsirSize = 20f
        var bestTafsir = -1f
        val tafsirSizes = floatArrayOf(30f, 27f, 24f, 22f, 20f)
        for (maxBeits in intArrayOf(3, 2)) {
            for (verseSize in floatArrayOf(42f, 36f, 32f)) {
                val chosen = poem.verses.take(maxBeits)
                val lines = mutableListOf<String>()
                chosen.forEach { b ->
                    lines += b.first
                    b.second?.let { lines += it }
                }
                if (poem.verses.size > chosen.size) lines += "…"
                val verseText = lines.joinToString("\n")
                val verseH = measureText(verseText, verseSize, nastaliq, textWidth, 1.5f)
                val fixed = gapTop + verseH + gapAfterVerse + dividerH + labelH + gapAfterLabel + gapBottom
                val tafsirBudget = middle - fixed
                if (tafsirBudget < 90f) continue
                for (ts in tafsirSizes) {
                    val tafsirH = measureText(tafsirText, ts, vazir, textWidth, 1.5f)
                    if (tafsirH <= tafsirBudget) {
                        if (ts > bestTafsir || (ts == bestTafsir && maxBeits > selMaxBeits)) {
                            bestTafsir = ts
                            selMaxBeits = maxBeits
                            selVerseSize = verseSize
                            selTafsirSize = ts
                        }
                    }
                }
            }
        }

        // ── رسمِ بیت‌ها ──
        val chosenBeits = poem.verses.take(selMaxBeits)
        val verseLines = mutableListOf<String>()
        chosenBeits.forEach { b ->
            verseLines += b.first
            b.second?.let { verseLines += it }
        }
        if (poem.verses.size > chosenBeits.size) verseLines += "…"
        val verseText = verseLines.joinToString("\n")

        var cy = contentTop + gapTop
        cy = drawText(
            canvas, verseText, selVerseSize, nastaliq, spec.onBackground.toArgb(),
            textWidth, PAD, cy, Layout.Alignment.ALIGN_NORMAL, 1.5f
        )
        cy += gapAfterVerse

        drawDividerOrnament(canvas, cy, spec.accent.toArgb())
        cy += dividerH

        cy = drawText(
            canvas, "تعبیر", 34f, vazirBold, spec.accent.toArgb(),
            textWidth, PAD, cy, Layout.Alignment.ALIGN_NORMAL, 1.0f
        )
        cy += gapAfterLabel

        // تعبیرِ کامل — همیشه بالای پاورقی
        drawText(
            canvas, tafsirText, selTafsirSize, vazir, spec.onBackground.toArgb(),
            textWidth, PAD, cy, Layout.Alignment.ALIGN_NORMAL, 1.5f
        )

        // ── پاورقی ──
        val footerY = footerTop
        drawDividerOrnament(canvas, footerY - 30f, spec.accent.copy(alpha = 0.6f).toArgb())

        if (withChannel) {
            val network = SocialNetwork.byKey(ch!!.network)
            drawChannelFooter(canvas, context, ch, network, footerY, spec, gold = supporterBadge)
        } else {
            drawText(
                canvas, "فال حافظ | تعبیر هوشمند", 28f, vazir, spec.onBackgroundMuted.toArgb(),
                textWidth, PAD, footerY, Layout.Alignment.ALIGN_CENTER, 1.0f
            )
        }

        return bitmap
    }

    /**
     * کارتِ حامی — برجسته در پایینِ فالِ اشتراکی:
     * قاب طلایی + آیکونِ شبکه + «با حمایتِ مالیِ» + نام + @شناسه + دعوت به دنبال‌کردن.
     */
    private fun drawChannelFooter(
        canvas: Canvas,
        context: Context,
        channel: ChannelInfo,
        network: SocialNetwork,
        y: Float,
        spec: FalThemeSpec,
        gold: Boolean
    ) {
        val icon = runCatching {
            BitmapFactory.decodeResource(context.resources, network.iconRes)
        }.getOrNull() ?: return

        val cardH = 330f
        val left = 70f
        val right = W - 70f
        val textWidth = (right - left - 40f).toInt()

        val vazir = ResourcesCompat.getFont(context, R.font.vazirmatn_regular) ?: Typeface.DEFAULT
        val vazirBold = ResourcesCompat.getFont(context, R.font.vazirmatn_bold) ?: Typeface.DEFAULT_BOLD

        // پس‌زمینهٔ کارت + قاب طلایی
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.card.copy(alpha = 0.85f).toArgb()
        }
        canvas.drawRoundRect(left, y, right, y + cardH, 28f, 28f, bgPaint)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.accent.copy(alpha = 0.9f).toArgb()
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(left, y, right, y + cardH, 28f, 28f, borderPaint)

        val centerX = (W - textWidth) / 2f

        // برچسب بالا
        var yy = y + 22f
        val badge = if (gold) "پشتیبانِ همیشگی ♥" else "با حمایتِ مالیِ"
        yy = drawText(
            canvas, badge, 28f, vazirBold, spec.accent.toArgb(),
            textWidth, centerX, yy, Layout.Alignment.ALIGN_CENTER, 1.0f
        )

        // آیکون شبکه (وسط)
        val size = 92f
        val iconLeft = (W - size) / 2f
        val iconTop = yy + 10f
        canvas.drawBitmap(
            icon, null,
            Rect(iconLeft.toInt(), iconTop.toInt(), (iconLeft + size).toInt(), (iconTop + size).toInt()),
            Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }
        )
        yy = iconTop + size + 12f

        // نام
        val name = if (channel.name.isNotBlank()) channel.name else network.label
        yy = drawText(
            canvas, name, 40f, vazirBold, spec.accentSoft.toArgb(),
            textWidth, centerX, yy, Layout.Alignment.ALIGN_CENTER, 1.0f
        )

        // شناسه
        yy = drawText(
            canvas, "@${channel.handle.trim().trimStart('@')}", 30f, vazir,
            spec.onBackground.toArgb(),
            textWidth, centerX, yy + 8f, Layout.Alignment.ALIGN_CENTER, 1.0f
        )

        // دعوت
        drawText(
            canvas, "برای فالِ روزانه، ما را دنبال کنید", 26f, vazir,
            spec.onBackgroundMuted.toArgb(),
            textWidth, centerX, yy + 8f, Layout.Alignment.ALIGN_CENTER, 1.0f
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
