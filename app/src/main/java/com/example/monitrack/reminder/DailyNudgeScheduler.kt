package com.example.monitrack.reminder

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.monitrack.data.entity.ActivityConfig
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

const val KEY_NUDGE_CONFIG_ID = "nudge_config_id"

/** Schedules the once-a-day "you haven't met your target" nudge per activity. */
object DailyNudgeScheduler {
    private const val WORK_PREFIX = "nudge-"

    fun scheduleAll(context: Context, configs: List<ActivityConfig>) {
        configs.forEach { schedule(context, it) }
    }

    /** Enqueue (or cancel) the next nudge check for one activity based on its config. */
    fun schedule(context: Context, config: ActivityConfig) {
        if (!config.dailyNudgeEnabled) {
            cancel(context, config.id)
            return
        }
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.now(zone)
        var next = LocalDateTime.of(LocalDate.now(zone), config.nudgeTime)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val delayMs = next.atZone(zone).toInstant().toEpochMilli() -
            now.atZone(zone).toInstant().toEpochMilli()

        val request = OneTimeWorkRequestBuilder<DailyNudgeWorker>()
            .setInitialDelay(delayMs.coerceAtLeast(0), TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(KEY_NUDGE_CONFIG_ID to config.id))
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(WORK_PREFIX + config.id, ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(context: Context, configId: Int) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_PREFIX + configId)
    }
}
