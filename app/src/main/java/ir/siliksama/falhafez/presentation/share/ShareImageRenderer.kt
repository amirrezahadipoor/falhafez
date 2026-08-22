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
 * verse (centered), and a Vazirmatn interpretation. 1080×1620 (2:3).
 *
 * Layout strategy: compact header, then the verse + FULL interpretation block is
 * fitted (measure-first) to fill the space between header and footer; the
 * interpretation is never truncated except as an absolute last resort.
 */
object ShareImageRenderer {

    private const val W = 1080
    private const val H = 1620
    private const val PAD = 120f

    fun render(
        context: Context,
        poem: Poem,
        category: FalCategory,
        spec: FalThemeSpec,
        channel: ChannelInfo? = null
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

        // ── سربرگِ فشرده — کمترین ارتفاع، تا فضای بیشتری به شعر و تعبیر برسد ──
        var y = 96f
        y = drawText(
            canvas, "فال حافظ", 56f, nastaliq, softGold,
            textWidth, PAD, y, Layout.Alignment.ALIGN_CENTER, 1.05f
        )
        y += 2f
        drawShamsa(canvas, W / 2f, y + 16f, 15f, gold, softGold, outline = true)
        y += 40f
        val meta = "${poem.collection.poet.faName} — ${poem.collection.faName}"
        y = drawText(
            canvas, meta, 26f, vazir, muted,
            textWidth, PAD, y, Layout.Alignment.ALIGN_CENTER, 1.0f
        )
        val contentTop = y + 18f

        // ── تعبیر کامل (+ زاویهٔ موضوعی) ──
        //
        // عمداً از تفسیرِ **اصیل** استفاده می‌شود، نه از متنِ شخصی‌شدهٔ صفحهٔ فال:
        // آن متن پرسشِ کاربر را عیناً داخل خودش دارد («برای آنچه پرسیدی — …») و
        // گذاشتنش روی تصویری که در واتساپ و اینستاگرام پخش می‌شود، یعنی افشای
        // نیّتِ خصوصیِ کاربر. آنچه به اشتراک گذاشته می‌شود شعر است و معنایش،
        // به‌علاوهٔ زاویهٔ موضوعیِ دسته — که چیزی دربارهٔ خودِ کاربر لو نمی‌دهد.
        val tafsirText = poem.tafsir + if (category != FalCategory.NONE) {
            val angle = CategoryAngles.text(category)
            if (angle != null) "\n\n$angle" else ""
        } else ""

        // ── پاورقی: کارتِ حامی یا برند (هر دو تا پایین‌ترین خطِ قاب) ──
        val ch = channel
        val withChannel = ch != null && ch.isSet
        val footerBlock = if (withChannel) 360f else 160f
        val footerTop = H - footerBlock

        // ── بودجهٔ فضای میانی (بین سربرگ و پاورقی) ──
        val middle = (footerTop - contentTop).coerceAtLeast(200f)
        val gapTop = 18f
        val gapAfterVerse = 24f
        val dividerH = 48f
        val labelH = 46f
        val gapAfterLabel = 12f
        val gapBottom = 16f

        fun buildVerseText(maxBeits: Int): String {
            val chosen = poem.verses.take(maxBeits)
            val lines = mutableListOf<String>()
            chosen.forEach { b ->
                lines += b.first
                b.second?.let { lines += it }
            }
            if (poem.verses.size > chosen.size) lines += "…"
            return lines.joinToString("\n")
        }

        // ── برازش (measure-first): تعبیرِ کامل + بیت‌های بیشتر، برای پُرکردنِ فضا ──
        var selVerseText = buildVerseText(2)
        var selVerseSize = 32f
        var selTafsirText = tafsirText
        var selTafsirSize = 18f
        var selVerseH = measureText(selVerseText, selVerseSize, nastaliq, textWidth, 1.5f)
        var selTafsirH = measureText(selTafsirText, selTafsirSize, vazir, textWidth, 1.5f)
        var bestScore = -1f

        fun consider(maxBeits: Int, verseSize: Float, ts: Float) {
            val verseText = buildVerseText(maxBeits)
            val verseH = measureText(verseText, verseSize, nastaliq, textWidth, 1.5f)
            val fixed = gapTop + verseH + gapAfterVerse + dividerH + labelH + gapAfterLabel + gapBottom
            val tafsirBudget = middle - fixed
            if (tafsirBudget < 60f) return
            val tafsirH = measureText(tafsirText, ts, vazir, textWidth, 1.5f)
            if (tafsirH > tafsirBudget) return
            // پُرکردنِ فضا: بلوکِ بلندتر بهتر؛ در تساوی، تعبیر بزرگ‌تر و بیتِ بیشتر.
            val blockH = fixed + tafsirH
            val score = blockH * 1000f + ts * 100f + maxBeits * 10f + verseSize
            if (score > bestScore) {
                bestScore = score
                selVerseText = verseText
                selVerseSize = verseSize
                selVerseH = verseH
                selTafsirText = tafsirText
                selTafsirSize = ts
                selTafsirH = tafsirH
            }
        }

        // لایهٔ ۱: تعبیر با فونتِ خوانا (≥۱۸)
        for (maxBeits in intArrayOf(5, 4, 3, 2)) {
            for (verseSize in floatArrayOf(48f, 44f, 40f, 36f, 32f)) {
                for (ts in floatArrayOf(28f, 26f, 24f, 22f, 20f, 18f)) {
                    consider(maxBeits, verseSize, ts)
                }
            }
        }
        // لایهٔ ۲: تعبیرِ خیلی بلند — فونتِ ریزتر ولی همچنان کامل
        if (bestScore < 0f) {
            for (maxBeits in intArrayOf(4, 3, 2)) {
                for (verseSize in floatArrayOf(38f, 34f, 30f)) {
                    for (ts in floatArrayOf(16f, 15f, 14f)) {
                        consider(maxBeits, verseSize, ts)
                    }
                }
            }
        }
        // لایهٔ ۳ (آخرین چارهٔ تضمینی): کوتاه‌کردن روی مرزِ واژه — عملاً رخ نمی‌دهد.
        if (bestScore < 0f) {
            selVerseText = buildVerseText(2)
            selVerseSize = 30f
            selVerseH = measureText(selVerseText, selVerseSize, nastaliq, textWidth, 1.5f)
            selTafsirSize = 14f
            val fixed = gapTop + selVerseH + gapAfterVerse + dividerH + labelH + gapAfterLabel + gapBottom
            val budget = middle - fixed
            selTafsirText = truncateToFit(tafsirText, budget, selTafsirSize, vazir, textWidth, 1.5f)
            selTafsirH = measureText(selTafsirText, selTafsirSize, vazir, textWidth, 1.5f)
        }

        // ── رسمِ بدنه — عمودی وسط‌چین میانِ سربرگ و پاورقی ──
        val blockH = gapTop + selVerseH + gapAfterVerse + dividerH + labelH + gapAfterLabel + selTafsirH
        val startY = contentTop + ((middle - blockH) / 2f).coerceAtLeast(0f)

        var cy = startY + gapTop
        cy = drawText(
            canvas, selVerseText, selVerseSize, nastaliq, ink,
            textWidth, PAD, cy, Layout.Alignment.ALIGN_CENTER, 1.5f
        )
        cy += gapAfterVerse

        drawDividerOrnament(canvas, cy + 14f, gold, softGold)
        cy += dividerH

        drawFlankedLabel(canvas, "تعبیر", 32f, vazirBold, gold, cy, gold, textWidth)
        cy += labelH + gapAfterLabel

        // تعبیرِ کامل — ارتفاعش داخل بودجه تضمین شده است؛ هرگز به پاورقی نمی‌رسد.
        drawText(
            canvas, selTafsirText, selTafsirSize, vazir, ink,
            textWidth, PAD, cy, Layout.Alignment.ALIGN_NORMAL, 1.5f
        )

        // ── پاورقی ──
        if (withChannel) {
            // کارتِ حامی/کانال — تا پایین‌ترین خطِ قاب کشیده می‌شود.
            val footerY = H - 356f
            drawDividerOrnament(canvas, footerY - 30f, spec.accent.copy(alpha = 0.6f).toArgb(), softGold)
            val network = SocialNetwork.byKey(ch!!.network)
            drawChannelFooter(canvas, context, ch, network, footerY, spec)
        } else {
            // تبلیغِ اپلیکیشن — دقیقاً روی پایین‌ترین خطِ قاب (تهِ فریم).
            val brand = "فال حافظ | تعبیر هوشمند"
            val brandH = measureText(brand, 28f, vazir, textWidth, 1.0f)
            val brandY = H - 66f - brandH
            drawDividerOrnament(canvas, brandY - 30f, spec.accent.copy(alpha = 0.6f).toArgb(), softGold)
            drawText(
                canvas, brand, 28f, vazir, muted,
                textWidth, PAD, brandY, Layout.Alignment.ALIGN_CENTER, 1.0f
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

    /**
     * متن را آن‌قدر کوتاه می‌کند که ارتفاعش در [maxHeight] جا شود؛
     * برش روی مرزِ واژه انجام می‌شود و «…» در پایان می‌آید.
     */
    private fun truncateToFit(
        text: String,
        maxHeight: Float,
        size: Float,
        typeface: Typeface,
        width: Int,
        lineSpacing: Float
    ): String {
        if (maxHeight <= 0f) return "…"
        if (measureText(text, size, typeface, width, lineSpacing) <= maxHeight) return text
        var lo = 0
        var hi = text.length
        var best = ""
        while (lo <= hi) {
            val mid = (lo + hi) / 2
            var candidate = text.substring(0, mid)
            if (candidate.isNotEmpty()) {
                val space = candidate.lastIndexOf(' ')
                if (space > mid * 3 / 4) candidate = candidate.substring(0, space).trimEnd()
            }
            val probe = if (candidate.isEmpty()) "…" else "$candidate…"
            if (measureText(probe, size, typeface, width, lineSpacing) <= maxHeight) {
                best = probe
                lo = mid + 1
            } else {
                hi = mid - 1
            }
        }
        return if (best.isNotEmpty()) best else "…"
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

        val corners = listOf(
            50f to 50f, (W - 50f) to 50f, 50f to (H - 50f), (W - 50f) to (H - 50f)
        )
        for ((cx, cy) in corners) {
            drawShamsa(canvas, cx, cy, 30f, gold, softGold, outline = false)
        }

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
     * قاب طلایی + آیکونِ شبکه + نام + @شناسه + دعوت به دنبال‌کردن.
     */
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

        val cardH = 330f
        val left = 70f
        val right = W - 70f
        val textWidth = (right - left - 40f).toInt()

        val vazir = ResourcesCompat.getFont(context, R.font.vazirmatn_regular) ?: Typeface.DEFAULT
        val vazirBold = ResourcesCompat.getFont(context, R.font.vazirmatn_bold) ?: Typeface.DEFAULT_BOLD

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

        // آیکون شبکه (بالای کارت)
        val size = 96f
        val iconLeft = (W - size) / 2f
        val iconTop = y + 30f
        canvas.drawBitmap(
            icon, null,
            Rect(iconLeft.toInt(), iconTop.toInt(), (iconLeft + size).toInt(), (iconTop + size).toInt()),
            Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }
        )
        var yy = iconTop + size + 16f

        val name = if (channel.name.isNotBlank()) channel.name else network.label
        yy = drawText(
            canvas, name, 40f, vazirBold, spec.accentSoft.toArgb(),
            textWidth, centerX, yy, Layout.Alignment.ALIGN_CENTER, 1.0f
        )

        yy = drawText(
            canvas, "@${channel.handle.trim().trimStart('@')}", 30f, vazir,
            spec.onBackground.toArgb(),
            textWidth, centerX, yy + 8f, Layout.Alignment.ALIGN_CENTER, 1.0f
        )

        drawText(
            canvas, "برای فالِ روزانه، ما را دنبال کنید", 26f, vazir,
            spec.onBackgroundMuted.toArgb(),
            textWidth, centerX, yy + 8f, Layout.Alignment.ALIGN_CENTER, 1.0f
        )
    }
}
