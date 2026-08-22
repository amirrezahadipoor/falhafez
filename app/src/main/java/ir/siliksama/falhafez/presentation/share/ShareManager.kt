package ir.siliksama.falhafez.presentation.share

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import ir.siliksama.falhafez.core.theme.FalThemeSpec
import ir.siliksama.falhafez.core.util.ChannelStore
import ir.siliksama.falhafez.core.util.SupportStore
import ir.siliksama.falhafez.domain.model.FalCategory
import ir.siliksama.falhafez.domain.model.Poem
import java.io.File

object ShareManager {

    /** Renders the fal image (off the main thread) into a cache file. */
    fun renderFile(context: Context, poem: Poem, category: FalCategory, spec: FalThemeSpec): File {
        val tier = SupportStore.tier
        // نام و کانال فقط برای حمایتِ ویژه/همیشگی روی فالِ اشتراکی نقش می‌بندد.
        val channel = if (tier.showsChannel) ChannelStore.info else null
        val bitmap = ShareImageRenderer.render(
            context.applicationContext, poem, category, spec,
            channel = channel
        )
        val dir = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(dir, "fal_${System.currentTimeMillis()}.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return file
    }

    /**
     * Shares [file] directly to [network]'s app. If the target app is not installed
     * the generic chooser is shown instead, so the user can always share somewhere.
     */
    fun shareFileToApp(context: Context, file: File, network: SocialNetwork): Boolean {
        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        return try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                network.packageName?.let { setPackage(it) }
            }
            context.startActivity(
                Intent.createChooser(intent, "اشتراک‌گذاری با ${network.label}")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            true
        } catch (e: Exception) {
            shareFileGeneric(context, file)
        }
    }

    /** Generic share sheet listing every app that can receive an image. */
    fun shareFileGeneric(context: Context, file: File): Boolean {
        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        return try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(
                Intent.createChooser(intent, "اشتراک‌گذاری فال")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    /** Saves the rendered fal into the device gallery (best effort, no crash). */
    fun saveToGallery(context: Context, file: File): Boolean {
        return try {
            val resolver = context.contentResolver
            val name = file.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, name)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/FalHafez"
                    )
                }
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                uri?.let { u ->
                    resolver.openOutputStream(u)?.use { out ->
                        file.inputStream().use { it.copyTo(out) }
                    }
                    true
                } ?: false
            } else {
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "FalHafez"
                )
                if (!dir.exists()) dir.mkdirs()
                val target = File(dir, name)
                file.inputStream().use { input ->
                    target.outputStream().use { input.copyTo(it) }
                }
                MediaScannerConnection.scanFile(
                    context, arrayOf(target.absolutePath), arrayOf("image/png"), null
                )
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}
