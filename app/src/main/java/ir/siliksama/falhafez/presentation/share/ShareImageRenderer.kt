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
 * a tazhib-style double gold frame with corner shamseh medallions, Nastaliq
 * verse (centered), and a Vazirmatn interpretation. 1080×1350 (4:5).
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

        val gold = spec.accent.toArgb()
        val softGold = spec.accentSoft.toArgb()
        val ink = spec.onBackground.toArgb()
        val muted = spec.onBackgroundMuted.toArgb()

        drawOrnamentFrame(canvas, gold, softGold)

        val textWidth = (W - 2 * PAD).toInt()
        var y = 150f

        // ── سربرگ ──
        y = drawText(
            canvas, "فال حافظ", 64f, nastaliq, softGold,
            textWidth, PAD, y, Layout.Alignment.ALIGN_CENTER, 1.15f
        )
        y += 6f
        drawShamsa(canvas, W / 2f, y + 18f, 17f, gold, softGold, outline = true)
        y += 52f
        val meta = "${poem.collection.poet.faName} — ${poem.collection.faName}"
        y = drawText(
            canvas, meta, 28f, vazir, muted,
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
        val gapTop = 20f
        val gapAfterVerse = 26f
        val dividerH = 52f
        val labelH = 50f
        val gapAfterLabel = 14f
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

        // ── رسمِ بیت‌ها (وسط‌چین) ──
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
            canvas, verseText, selVerseSize, nastaliq, ink,
            textWidth, PAD, cy, Layout.Alignment.ALIGN_CENTER, 1.5f
        )
        cy += gapAfterVerse

        drawDividerOrnament(canvas, cy + 14f, gold, softGold)
        cy += dividerH

        drawFlankedLabel(canvas, "تعبیر", 34f, vazirBold, gold, cy, gold, textWidth)
        cy += labelH + gapAfterLabel

        // تعبیرِ کامل — همیشه بالای پاورقی
        drawText(
            canvas, tafsirText, selTafsirSize, vazir, ink,
            textWidth, PAD, cy, Layout.Alignment.ALIGN_NORMAL, 1.5f
        )

        // ── پاورقی ──
        val footerY = footerTop
        drawDividerOrnament(canvas, footerY - 30f, spec.accent.copy(alpha = 0.6f).toArgb(), softGold)

        if (withChannel) {
            val network = SocialNetwork.byKey(ch!!.network)
            drawChannelFooter(canvas, context, ch, network, footerY, spec, gold = supporterBadge)
        } else {
            drawText(
                canvas, "فال حافظ | تعبیر هوشمند", 28f, vazir, muted,
                textWidth, PAD, footerY, Layout.Alignment.ALIGN_CENTER, 1.0f
            )
        }

        return bitmap
    }

    /** برچسبِ وسط‌چین با خط و نقطهٔ تزیینی در دو طرف. */
    private fun drawFlankedLabel(
        canvas: Canvas, text: String, size: Float, typeface: Typeface,
        color: Int, y: Float, accent: Int, width: Int
    ) {
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            textSize = size
        }
        val w = paint.measureText(text)
        val cx = W / 2f
        val lineY = y + size / 2f
        val gap = 20f
        val len = 90f
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = accent
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        canvas.drawLine(cx - w / 2f - gap - len, lineY, cx - w / 2f - gap, lineY, line)
        canvas.drawLine(cx + w / 2f + gap, lineY, cx + w / 2f + gap + len, lineY, line)
        val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = accent; style = Paint.Style.FILL }
        canvas.drawCircle(cx - w / 2f - gap - len - 9f, lineY, 3.5f, dot)
        canvas.drawCircle(cx + w / 2f + gap + len + 9f, lineY, 3.5f, dot)
        drawText(canvas, text, size, typeface, color, width, PAD, y, Layout.Alignment.ALIGN_CENTER, 1.0f)
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

    /** قابِ دوخطیِ طلایی + شمسهٔ گوشه‌ها (لچک) + نقطه‌های گوشه. */
    private fun drawOrnamentFrame(canvas: Canvas, gold: Int, softGold: Int) {
        val outer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = gold
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(32f, 32f, W - 32f, H - 32f, 26f, 26f, outer)

        val inner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = gold
            alpha = 130
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
        }
        canvas.drawRoundRect(50f, 50f, W - 50f, H - 50f, 18f, 18f, inner)

        // شمسهٔ گوشه‌ها
        val corners = listOf(
            50f to 50f, (W - 50f) to 50f, 50f to (H - 50f), (W - 50f) to (H - 50f)
        )
        for ((cx, cy) in corners) {
            drawShamsa(canvas, cx, cy, 30f, gold, softGold, outline = false)
        }

        // نقطه‌های طلایی روی گوشهٔ قابِ بیرونی
        val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = gold; style = Paint.Style.FILL }
        for ((cx, cy) in corners) {
            canvas.drawCircle(cx, cy, 6f, dot)
        }
    }

    /** شمسه (ستارهٔ هشت‌پر) — دو مربعِ چرخیدهٔ روی هم + حلقه و نقطهٔ مرکزی. */
    private fun drawShamsa(
        canvas: Canvas, cx: Float, cy: Float, r: Float,
        gold: Int, softGold: Int, outline: Boolean
    ) {
        val fillSoft = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = softGold; style = Paint.Style.FILL }
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = gold; style = Paint.Style.FILL }
        val squares = listOf(0.0 to fillSoft, Math.PI / 4 to fill)
        for ((rot, paint) in squares) {
            val p = Path()
            for (i in 0..3) {
                val a = rot + i * Math.PI / 2
                val x = cx + (r * Math.cos(a)).toFloat()
                val y = cy + (r * Math.sin(a)).toFloat()
                if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
            }
            p.close()
            canvas.drawPath(p, paint)
        }
        if (outline) {
            val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = gold
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
            }
            canvas.drawCircle(cx, cy, r * 0.62f, ring)
        }
        canvas.drawCircle(cx, cy, r * 0.30f, fill)
    }

    /** جداکنندهٔ تزیینی: خط + شمسهٔ کوچک + خط. */
    private fun drawDividerOrnament(canvas: Canvas, y: Float, gold: Int, softGold: Int) {
        val cx = W / 2f
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = gold
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        canvas.drawLine(PAD * 1.4f, y, cx - 34f, y, line)
        canvas.drawLine(cx + 34f, y, W - PAD * 1.4f, y, line)
        drawShamsa(canvas, cx, y, 15f, gold, softGold, outline = false)
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
}
