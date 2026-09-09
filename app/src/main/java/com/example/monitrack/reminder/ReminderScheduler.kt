package com.example.monitrack.reminder

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.monitrack.data.MonitrackRepository
import com.example.monitrack.data.entity.Session
import java.util.concurrent.TimeUnit

const val KEY_SESSION_ID = "session_id"
const val KEY_ATTEMPT = "attempt"

/**
 * Schedules the "did you forget to stop?" reminder. The first reminder fires once elapsed
 * time reaches 1.5× the activity's average; each "still going" dismissal reschedules with an
 * exponentially growing delay until the session is stopped.
 */
object ReminderScheduler {
    private const val WORK_PREFIX = "reminder-"
    private const val OVERRUN_FACTOR = 1.5
    private const val BASE_BACKOFF_MS = 5 * 60_000L
    private const val MAX_BACKOFF_MS = 2 * 60 * 60_000L

    /** Schedule the first reminder. No-op when the activity has overrun alerts disabled. */
    suspend fun schedule(context: Context, repository: MonitrackRepository, session: Session) {
        val config = repository.getConfig(session.activityId) ?: return
        if (!config.overrunAlertEnabled) return
        val threshold = (config.targetMs * OVERRUN_FACTOR).toLong()
        val elapsed = System.currentTimeMillis() - session.startTime
        enqueue(context, session.id, (threshold - elapsed).coerceAtLeast(0), attempt = 0)
    }

    /** Reschedule after a "still going" dismissal, doubling the delay each attempt. */
    fun scheduleBackoff(context: Context, sessionId: Long, attempt: Int) {
        val delay = (BASE_BACKOFF_MS shl (attempt - 1)).coerceAtMost(MAX_BACKOFF_MS)
        enqueue(context, sessionId, delay, attempt)
    }

    fun cancel(context: Context, sessionId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_PREFIX + sessionId)
    }

    private fun enqueue(context: Context, sessionId: Long, delayMs: Long, attempt: Int) {
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(KEY_SESSION_ID to sessionId, KEY_ATTEMPT to attempt))
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(WORK_PREFIX + sessionId, ExistingWorkPolicy.REPLACE, request)
    }
}
