package ir.siliksama.falhafez.presentation.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit
import ir.siliksama.falhafez.MainActivity
import ir.siliksama.falhafez.R
import ir.siliksama.falhafez.core.util.DayNumber
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import ir.siliksama.falhafez.data.local.PoemDao
import ir.siliksama.falhafez.domain.model.Collection
import ir.siliksama.falhafez.domain.model.Poet

/**
 * بیتِ امروز widget — shows today's deterministic fal verse on the home screen.
 * Fully offline: the worker reads the bundled Room corpus and refreshes daily.
 */
class DailyFalWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        scheduleUpdate(context)
    }

    override fun onEnabled(context: Context) {
        scheduleUpdate(context)
    }

    private fun scheduleUpdate(context: Context) {
        val wm = WorkManager.getInstance(context)
        // به‌روزرسانی فوری هنگام افزودن ویجت
        val once = OneTimeWorkRequestBuilder<DailyFalWidgetWorker>().build()
        wm.enqueueUniqueWork("daily_fal_widget_now", ExistingWorkPolicy.REPLACE, once)
        // به‌روزرسانی روزانه — تا «بیتِ امروز» واقعاً هر روز عوض شود
        val periodic = PeriodicWorkRequestBuilder<DailyFalWidgetWorker>(1, TimeUnit.DAYS).build()
        wm.enqueueUniquePeriodicWork(
            "daily_fal_widget_daily",
            ExistingPeriodicWorkPolicy.UPDATE,
            periodic
        )
    }

    companion object {
        suspend fun updateAll(context: Context) {
            // حفاظِ کراش: خطای دیتابیس/رندر نباید Worker را از کار بیندازد.
            runCatching {
                updateAllUnsafe(context)
            }
        }

        private suspend fun updateAllUnsafe(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, DailyFalWidgetProvider::class.java))
            if (ids.isEmpty()) return

            // ⚠️ اینجا قبلاً یک نمونهٔ **دومِ** Room ساخته می‌شد (خارج از Hilt) با
            // fallbackToDestructiveMigration. یعنی ویجت می‌توانست در پس‌زمینه کلِ
            // پایگاه‌داده — تاریخچه و علاقه‌مندی‌های کاربر — را پاک کند، بدونِ آنکه
            // اپ اصلاً باز شده باشد. حالا همان نمونهٔ Singleton اپ استفاده می‌شود.
            val poemDao = EntryPointAccessors
                .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
                .poemDao()

            val count = poemDao.countForPoetCollection("hafez", "ghazal")
            if (count <= 0) return
            // همان «روزِ محلی» که فالِ روزِ داخل اپ از آن استفاده می‌کند — تا ویجت و اپ یکی باشند.
            val day = DayNumber.local()
            val poemId = poemDao.getPoemIdAtForPoetCollection("hafez", "ghazal", (day % count.toLong()).toInt()) ?: return
            val withVerses = poemDao.getPoemWithVerses(poemId) ?: return
            val opening = withVerses.verses.sortedBy { it.position }
                .take(3)
                .joinToString("\n") { v -> v.first + if (v.second != null) " — " + v.second else "" }

            val intent = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            val poetFa = Poet.fromKey(withVerses.poem.poet).faName
            val collFa = Collection.fromKey(withVerses.poem.collection)?.faName ?: ""
            for (id in ids) {
                val views = RemoteViews(context.packageName, R.layout.widget_fal).apply {
                    setTextViewText(R.id.widget_title, "فالِ امروز")
                    setTextViewText(R.id.widget_verse, opening)
                    setTextViewText(R.id.widget_meta, "$poetFa — $collFa")
                    setOnClickPendingIntent(R.id.widget_root, intent)
                }
                manager.updateAppWidget(id, views)
            }
        }
    }

    /** دسترسیِ ویجت به گرافِ Hilt (Provider خودش تزریق‌پذیر نیست). */
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun poemDao(): PoemDao
    }
}

class DailyFalWidgetWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        DailyFalWidgetProvider.updateAll(applicationContext)
        return Result.success()
    }
}
