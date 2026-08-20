package ir.siliksama.falhafez.core.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import ir.siliksama.falhafez.R

/**
 * Mystical sound cues (synthesized, bundled offline): a golden pluck for taps,
 * a pentatonic bell arpeggio for the Divan opening, a warm chord for the reveal,
 * and a soft breathing drone for the niyyat moment. All gated behind [enabled].
 */
object Sounds {
    @Volatile var enabled: Boolean = true
    @Volatile var hapticsEnabled: Boolean = true

    private var pool: SoundPool? = null
    private var tapId = 0
    private var drawId = 0
    private var revealId = 0
    private var ambientId = 0
    private var loaded = false

    fun init(context: Context) {
        if (pool != null) return
        val p = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
        p.setOnLoadCompleteListener { _, _, status -> if (status == 0) loaded = true }
        tapId = p.load(context, R.raw.btn_tap, 1)
        drawId = p.load(context, R.raw.raw_fal_draw, 1)
        revealId = p.load(context, R.raw.raw_reveal, 1)
        ambientId = p.load(context, R.raw.raw_ambient, 1)
        pool = p
    }

    fun tap() = play(tapId, 0.55f)
    fun draw() = play(drawId, 0.7f)
    fun reveal() = play(revealId, 0.6f)
    fun ambient() = play(ambientId, 0.45f)

    private fun play(id: Int, volume: Float) {
        if (!enabled || !loaded) return
        runCatching { pool?.play(id, volume, volume, 1, 0, 1f) }
    }

    fun release() {
        runCatching { pool?.release() }
        pool = null
        loaded = false
    }
}
