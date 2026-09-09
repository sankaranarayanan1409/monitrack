package com.example.monitrack.reminder

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.monitrack.data.util.Metrics
import com.example.monitrack.repository
import java.time.LocalDate
import java.time.ZoneId

/** Fires the daily nudge if the day's total is below target, then reschedules for tomorrow. */
class DailyNudgeWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        val type = inputData.getString(KEY_NUDGE_TYPE) ?: return Result.failure()

        val repository = applicationContext.repository
        val config = repository.getConfig(type) ?: return Result.success()

        if (config.dailyNudgeEnabled) {
            val today = LocalDate.now(ZoneId.systemDefault())
            val total = Metrics.dailyTotalMs(
                repository.completedSessionsNow(type),
                today,
                ZoneId.systemDefault(),
            )
            if (total < config.targetMs) {
                ReminderNotifier.showDailyNudge(applicationContext, config, total)
            }
            // Reschedule for the next day's nudge time.
            DailyNudgeScheduler.schedule(applicationContext, config)
        }
        return Result.success()
    }
}
