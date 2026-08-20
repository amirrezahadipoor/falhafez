package com.amirrezahadipoor.falhafez.presentation.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.amirrezahadipoor.falhafez.MainActivity
import com.amirrezahadipoor.falhafez.R
import com.amirrezahadipoor.falhafez.data.local.FalDatabase
import com.amirrezahadipoor.falhafez.domain.model.Collection
import com.amirrezahadipoor.falhafez.domain.model.Poet

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
        val request = OneTimeWorkRequestBuilder<DailyFalWidgetWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "daily_fal_widget_update",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    companion object {
        suspend fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, DailyFalWidgetProvider::class.java))
            if (ids.isEmpty()) return

            val db = Room.databaseBuilder(context, FalDatabase::class.java, "falhafez.db")
                .addMigrations(FalDatabase.MIGRATION_1_2)
                .build()
            try {
                val poemDao = db.poemDao()
                val count = poemDao.countForPoet("hafez")
                if (count <= 0) return
                val day = System.currentTimeMillis() / 86_400_000L
                val poemId = poemDao.getPoemIdAtForPoet("hafez", (day % count.toLong()).toInt()) ?: return
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
            } finally {
                db.close()
            }
        }
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
