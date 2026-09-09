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
        val configId = inputData.getInt(KEY_NUDGE_CONFIG_ID, -1)
        if (configId < 0) return Result.failure()

        val repository = applicationContext.repository
        val config = repository.getConfigById(configId) ?: return Result.success()
        val activityId = config.activityId ?: return Result.success()

        if (config.dailyNudgeEnabled) {
            val today = LocalDate.now(ZoneId.systemDefault())
            val total = Metrics.dailyTotalMs(
                repository.completedSessionsNow(activityId),
                today,
                ZoneId.systemDefault(),
            )
            if (total < config.targetMs) {
                val activityName = repository.getActivity(activityId)?.name ?: config.type
                ReminderNotifier.showDailyNudge(applicationContext, activityName, config, total)
            }
            // Reschedule for the next day's nudge time.
            DailyNudgeScheduler.schedule(applicationContext, config)
        }
        return Result.success()
    }
}
